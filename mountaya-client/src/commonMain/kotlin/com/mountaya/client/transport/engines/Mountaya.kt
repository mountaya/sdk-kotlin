package com.mountaya.client

import com.mountaya.client.generated.Agent
import com.mountaya.client.generated.Data
import com.mountaya.client.generated.OperationTable
import com.mountaya.client.generated.Studio
import com.mountaya.client.generated.Tiles
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.Duration

/** The most a Tile API route a map reads for itself spends before it answers a 504 itself, as the contract states. */
private const val MAP_FILE_TIMEOUT_MS: Int = 30_000

/**
 * Mountaya is the client, and the root of every operation the contract publishes:
 * `studio.itineraries.getItinerary(itinerary_id)`, `tiles.getZonesSpecification()`,
 * `data.directions(input)`. The operations of the contract's `admin` group are the admin unit's, which
 * `mountaya-admin` declares on this client as `admin` once it is imported, `mountaya.admin.keys`, so an app that
 * draws a map, edits an itinerary or queries the Data API links none of them.
 *
 * [language] rides every request as `Accept-Language`, and no method takes one. Every request names
 * the SDK that made it too, as `X-Mountaya-Client: mountaya-kotlin/<version>`, the version the modules
 * are published at, and no method takes that either. [retry] and [timeout] are the client's retry
 * policy and attempt budget, which a call's `options` may override. [onResponse] is told of every
 * attempt an answer came back to; it does not suspend, and nothing it does, an exception it throws
 * included, changes what a call answers.
 *
 * The last four are seams: [exchange] is what every attempt leaves through, the platform's
 * `SystemExchange` where none is given; [clock] is the wall clock a `Retry-After` date and a
 * session token's expiry are read against, the platform's where none is given; [random] is what
 * every wait's jitter and every drawn `Idempotency-Key` come from, the platform's where none is
 * given; and [dispatcher] is where an answer's body is read and the answer decoded,
 * `Dispatchers.Default` where none is given, so a call made on the main thread parses nothing there.
 * Every wait and every budget runs on the caller's coroutine time, so a test on virtual time spends
 * none of its own; such a test passes its own test dispatcher as [dispatcher], so the read and the
 * decode run on that time too, and an attempt's budget is never spent while a thread of the
 * platform's reads.
 *
 * Where the credential carries a `SessionTokenEngine`, the client collects it in the engine's own
 * scope until it is closed, so a headless app that never renders still keeps its session fresh.
 * It throws `ConfigurationException` for a credential or a language no request could carry, and
 * for a timeout of zero or less.
 */
public class Mountaya(
    credential: Credential,
    language: String? = null,
    baseUrls: BaseUrls = BaseUrls.standard,
    retry: RetryPolicy = RetryPolicy.standard,
    timeout: Duration = AttemptBudget.clientDefault,
    onResponse: ((ResponseReport) -> Unit)? = null,
    exchange: HttpExchange = SystemExchange(),
    clock: Clock = SystemDefaults.clock,
    random: RandomSource = SystemDefaults.random,
    dispatcher: CoroutineDispatcher = SystemDefaults.dispatcher,
) : AutoCloseable {
    init {
        CredentialHeaders.check(credential)
        CredentialHeaders.checkLanguage(language)
        AttemptBudget.check(timeout, "The client's timeout")
    }

    /**
     * The language every request carries as `Accept-Language`, or null where the client asks for none
     * in particular. A map given no label language of its own labels in this one, so the labels on a
     * map and the words the API answers in are one language.
     */
    public val language: String? = language

    /** The credential a map's own requests are given, read for each request. */
    private val mapCredential: Credential = credential

    /**
     * The publishable key the client's credential holds, or null where it holds a share token or a secret key: the
     * key a map reads its organization by, sent in the body of `postOrganizationLookup` (`map.organization.read`). It
     * is `InternalMountayaApi`, no part of the published API, and no consumer reads it (SPEC.md section 7).
     */
    @InternalMountayaApi
    public val publishableKey: String? get() = (mapCredential as? Credential.Publishable)?.key

    /**
     * The origins the client sends each surface's requests to, as it was given them. A map reads the Tile
     * API's to name the address of a read it was refused, the layers manifest's, which the client's own
     * refusal carries no address of.
     */
    public val baseUrls: BaseUrls = baseUrls

    /** The Tile API's origin, which claims the requests a map makes for itself. */
    private val tileOrigin: Url = baseUrls.tiles

    /** The one pipeline every accessor and every call by declaration sends through. */
    internal val transport: Transport =
        Transport(
            Transport.Configuration(credential, language, baseUrls, retry, timeout, onResponse),
            exchange,
            clock,
            random,
            dispatcher,
        )

    /** Keeps the credential's session engine minting for as long as the client is open. */
    private val hold: Job? =
        (((credential as? Credential.Publishable)?.sessionToken as? SessionToken.Source)?.source as? SessionTokenEngine)
            ?.hold()

    /**
     * The seam every printed method hands its call over through, this client's transport: the admin unit's printed
     * accessor, which `mountaya-admin` declares as `admin`, sends through it from beside the core. It is
     * `InternalMountayaApi`, no part of the published API, and no consumer reads it (SPEC.md section 7).
     */
    @InternalMountayaApi
    public val sender: OperationSender get() = transport

    /** The REST API's `agent` operations. */
    public val agent: Agent = Agent(transport)

    /** The REST API's `studio` operations. */
    public val studio: Studio = Studio(transport)

    /** The Tile API's operations. */
    public val tiles: Tiles = Tiles(transport)

    /** The Data API's operations. */
    public val data: Data = Data(transport)

    /**
     * Calls an operation this build prints no method for, by what it declares, and returns its
     * envelope with `data` read with [data] and `extensions.metadata` with [metadata].
     *
     * The call is held to every rule a printed method is: its credential's refusals, its
     * `Idempotency-Key` and `If-Match`, its budget, its retries and the error ladder. [path] is
     * the path the request goes to under its surface's origin, already filled in, [query] the
     * query as names and values not yet encoded, and [headers] the caller's own, which may name
     * none the client writes itself. [body] is sent in the media type it names, a JSON value as
     * `application/json` and a form as `multipart/form-data`, with the `Content-Type` that says so.
     * A request whose `Accept` asks for `text/event-stream` is refused before any attempt: a stream
     * is opened with [events].
     */
    public suspend fun <Value, Metadata> send(
        operation: OperationDeclaration,
        path: String,
        data: KSerializer<Value>,
        metadata: KSerializer<Metadata>,
        query: List<Pair<String, String>> = emptyList(),
        headers: Map<String, String> = emptyMap(),
        body: RequestBody = RequestBody.Empty,
        idempotencyKey: String? = null,
        ifMatch: String? = null,
        options: RequestOptions = RequestOptions(),
    ): Response<Value, Metadata> =
        transport.send(
            OperationCall(
                facts = OperationFacts(operation, path),
                path = path,
                query = query,
                headers = headers.entries.sortedBy { it.key }.map { it.key to it.value },
                body = body,
                idempotencyKey = idempotencyKey,
                ifMatch = ifMatch,
                options = options,
            ),
            data,
            metadata,
        )

    /**
     * Calls a Data API operation this build prints no method for, by what [operation] declares,
     * with [input], and returns the operation's own field read with [result].
     *
     * The call is held to every rule a printed method of `data` is: the session token every Data
     * API request carries, the persisted query's digest, its budget, its retries and the error
     * ladder. [select] narrows the document sent, as a printed method's does, and a field outside
     * the operation's tree is refused with `SelectionException` before any request; an answer that
     * lacks the operation's field is a `DataException`. [input] is the operation's one variable,
     * `input`, written by the client's `Json`.
     */
    public suspend fun <Result : Any> query(
        operation: DataOperation,
        input: JsonElement,
        result: KSerializer<Result>,
        select: FieldSelection? = null,
        options: RequestOptions = RequestOptions(),
    ): Result =
        transport.query(
            DataCall(
                facts = OperationFacts(operation),
                operation = operation,
                document = operation.document(select),
                input = wireJson.encodeToString(JsonElement.serializer(), input),
                options = options,
            ),
            result,
        )

    /**
     * Walks a listing this build prints no method for, by what [operation] declares, one row at a
     * time, each read with [row], requesting a page only as its collector takes the rows before it
     * (`transport.pagination.walk`).
     *
     * A listing this build prints is walked through its own method, `walk` before its operation's
     * name, `studio.itineraries.walkGetItineraries()`. Every request of this one sends [query] with
     * its `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
     * from the `cursor` [query] names, so a walk resumes where an earlier one stopped, and every
     * other name and value as given, each request held to every rule [send] is. It reads the next
     * cursor from the envelope's `pagination.next_cursor`, follows every page for as long as one is
     * named, an empty page included, and stops where none is, at [maxPages] requests, which raises
     * nothing, or at a cursor it has already sent, which raises the base `ApiException`.
     *
     * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
     * outside 1 to `PageWalk.maxPageSize` or a [maxPages] under 1. A collector that is cancelled,
     * or that stops early, requests no further page.
     */
    public fun <Row> walk(
        operation: OperationDeclaration,
        path: String,
        row: KSerializer<Row>,
        query: List<Pair<String, String>> = emptyList(),
        headers: Map<String, String> = emptyMap(),
        pageSize: Int = PageWalk.defaultPageSize,
        maxPages: Int? = null,
        options: RequestOptions = RequestOptions(),
    ): Flow<Row> {
        val rows = ListSerializer(row)
        val metadata = JsonObject.serializer().nullable
        return PageWalk.pages(pageSize, maxPages, ::readNextCursor) { paged ->
            val page = DeclaredQuery(query).apply(paged)
            send(operation, path, rows, metadata, page.pairs, headers, options = options)
        }
    }

    /**
     * Opens an event stream this build prints no method for, by what its operation declares, and
     * returns its cold stream of records.
     *
     * The client knows no event of such a stream, so it reads it as a feed: no event ends it, it is
     * opened again after every close and every drop on a collaboration feed's schedule, resuming
     * from the last record's id, and it ends on a failure that would repeat itself, or when its
     * collector lets it go.
     */
    public fun events(
        operation: OperationDeclaration,
        path: String,
        query: List<Pair<String, String>> = emptyList(),
        options: RequestOptions = RequestOptions(),
    ): Flow<EventFrame<EventRecord>> =
        transport.records(
            OperationCall(
                facts = OperationFacts(operation, path, SuccessKind.EventStream),
                path = path,
                query = query,
                headers = emptyList(),
                body = RequestBody.Empty,
                options = options,
            ),
        )

    /**
     * Returns the headers a request a map renderer makes for itself carries, for a tile, a style, a
     * sprite or a glyph, where [url] is the Tile API's, and null where it is not: a request bound for
     * any other host a style names is sent no credential at all.
     *
     * The Tile API's is a request of the same origin as the client's Tile API base URL, its scheme and
     * host read without regard to case and its port with the scheme's default filled in, carrying no
     * user name and no password, whose path, its dot segments resolved, lies under the base URL's own
     * path at a segment boundary.
     *
     * The headers are the client's credential as a tile route declares one, every route a map fetches
     * for itself declaring the same: the publishable key with the session token as it reads now, or the
     * share token alone with the visitor's `Cookie` where its source answers one, the client's
     * language as `Accept-Language`, `*` where it names none, and the SDK that made the request as
     * `X-Mountaya-Client`, as a call carries both. A session token is read and never waited for, since
     * a renderer asks for a request's headers on a thread of its own and cannot suspend, so a tile asked
     * for while a first token is on its way goes out under the key alone.
     *
     * It throws `ConfigurationException` where the session token read now cannot be sent, where the
     * visitor's `Cookie` its source answers holds a character no header carries, which the message never
     * writes out, and for a secret key, which authenticates no route a map fetches.
     */
    public fun mapRequestHeaders(url: Url): Map<String, String>? {
        if (!isTileRequest(url, tileOrigin)) return null
        val session =
            (mapCredential as? Credential.Publishable)?.sessionToken?.let { ResolvedSession.reading(it, null) }
        val projection = CredentialHeaders.project(mapCredential, session, language, mapRoute, isRead = true)
        val headers = LinkedHashMap(projection.headers)
        headers[CLIENT_HEADER] = ClientIdentity.value
        projection.cookies?.invoke(url)?.let { cookie ->
            CredentialHeaders.checkCookie(cookie)
            headers[COOKIE_HEADER] = cookie
        }
        return headers
    }

    /**
     * The time budget one attempt of `postOrganizationLookup` takes on this client: the larger of the client's own and
     * the operation's `x-timeout-ms` plus the transport's margin, 35 seconds where the client names none
     * (`sdk/SPEC.md` section 2). A map draws its watermark once its organization's read has waited that long without an
     * answer, while the read's further attempts go on (`geo.organization.watermark`). It is `InternalMountayaApi`, no
     * part of the published API, and no consumer reads it (SPEC.md section 7).
     */
    @InternalMountayaApi
    public val organizationLookupBudget: Duration
        get() =
            AttemptBudget.budget(
                call = null,
                client = transport.configuration.timeout,
                operation = OperationTable.Rest.postOrganizationLookup.timeout,
            )

    /**
     * Downloads the file at [url], an address on the client's Tile API, under the credential a map's own requests
     * carry, as the route [operationId] reads one: the elevation source's TileJSON document at the address the layers
     * catalogue names, and each tile at the address the document's template fills (`map.readout.open`). It is held to
     * every rule a tile route is, its budget, its retries and the error ladder, and an address the Tile API does not
     * claim, as [mapRequestHeaders] claims one, is refused with `ConfigurationException` before any request, so the
     * credential never leaves for another host. It is `InternalMountayaApi`, no part of the published API, and no
     * consumer reads it (SPEC.md section 7).
     */
    @InternalMountayaApi
    public suspend fun mapFile(
        url: Url,
        operationId: String,
    ): BinaryResponse {
        if (!isTileRequest(url, tileOrigin)) {
            throw ConfigurationException(
                "`$url` is no address on the client's Tile API, the one origin a map reads its own files from.",
            )
        }
        return transport.downloadAt(
            url,
            OperationFacts(
                surface = Surface.Tiles,
                operationId = operationId,
                method = "GET",
                path = url.text,
                idempotent = true,
                credentials = mapRoute,
                timeoutMs = MAP_FILE_TIMEOUT_MS,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = false,
                pagination = null,
                success = SuccessKind.Binary,
                successResponses = listOf(SuccessResponse(200)),
                errorStatuses = emptyList(),
            ),
        )
    }

    /** Lets the credential's session engine go: the client no longer keeps it minting. */
    override fun close() {
        hold?.cancel()
    }

    private companion object {
        /**
         * What every route a map fetches for itself declares, the model's `templates`: it acts as
         * nobody, takes a share token on its own, and requires no session token.
         */
        val mapRoute = CredentialDeclaration(acceptsShareToken = true)
    }
}
