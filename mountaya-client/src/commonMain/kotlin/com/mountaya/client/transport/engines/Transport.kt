package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.time.Clock
import kotlin.time.Duration

/** The header an answer says it was replayed for a key in. */
private const val REPLAYED_HEADER = "Idempotent-Replayed"

/** The version of automatic persisted queries a Data API request names. */
private const val PERSISTED_QUERY_VERSION = 1

/**
 * Transport is the pipeline every operation is sent through: the credential's headers, the
 * attempt budget, the retries and their waits, the error ladder and the decoding of a success.
 *
 * It is the one implementation of [OperationSender], and it reaches the network through an
 * [HttpExchange] and nothing else, so a test that scripts the exchange, the wall clock and the
 * draws runs every rule here without a server, its waits on the test's virtual time.
 *
 * A buffered answer's body is read, and the answer then decoded, on [dispatcher], never on the
 * caller's: a call made on the main thread waits there and parses nothing there. Everything else, the
 * waits, the budgets and the hook an answer is reported to, stays the caller's.
 */
internal class Transport(
    val configuration: Configuration,
    val exchange: HttpExchange,
    val clock: Clock,
    val random: RandomSource,
    /** Where a buffered answer's body is read and the answer decoded. */
    val dispatcher: CoroutineDispatcher,
) : OperationSender {
    /** Configuration is what a client is built with and every call reads. */
    class Configuration(
        val credential: Credential,
        val language: String?,
        val baseUrls: BaseUrls,
        val retry: RetryPolicy,
        /** The client's attempt budget. */
        val timeout: Duration,
        val onResponse: ((ResponseReport) -> Unit)?,
    )

    /** Call is what every attempt of one call sends the same. */
    class Call(
        val facts: OperationFacts,
        val url: Url,
        /** What the call carries beside the credential's: `Accept`, `Content-Type` and the caller's own. */
        val headers: Map<String, String>,
        val body: ByteArray?,
        /** The key every attempt sends, or null where the call sends none. */
        val idempotencyKey: String?,
        /** The tag every attempt sends, or null where the call sends none. */
        val ifMatch: String?,
        val policy: RetryPolicy,
        /** Each attempt's time budget. */
        val budget: Duration,
    )

    /**
     * Answer is one attempt's answer, its body read whole, and its headers folded once as it arrives, so every header
     * read of it, its hook's report, its error and its response, is one lookup.
     */
    class Answer(
        val status: Int,
        headers: Map<String, String>,
        val body: ByteArray,
    ) {
        /** The answer's headers, folded once. */
        val headers: FoldedHeaders = FoldedHeaders(headers)

        val isSuccess: Boolean get() = status in 200..299
    }

    /** Outcome is how one attempt ended. */
    sealed interface Outcome {
        /** An answer that came back. */
        class Answered(
            val answer: Answer,
        ) : Outcome

        /**
         * A request whose answer never came: a dropped connection, a broken body, or a budget
         * spent, which carries no underlying failure.
         */
        class Unanswered(
            val cause: Throwable?,
        ) : Outcome
    }

    /** Settled is what a call's reader makes of an answer. */
    sealed interface Settled<out Success> {
        class Done<Success>(
            val value: Success,
        ) : Settled<Success>

        class Failed(
            val error: MountayaException,
        ) : Settled<Nothing>
    }

    /**
     * Whether the Data API takes a digest alone, for the transport's lifetime: one answer of "not
     * implemented" turns it off for every later call.
     */
    private val persistedQueries = PersistedQuerySupport()

    /**
     * Checks what a call sends and fixes what every attempt of it sends the same, or refuses it
     * with [ConfigurationException] before any request.
     *
     * The checks run in this order: the credential and the language, the call's own budget, the
     * caller's headers, a buffered request that asks for an event stream, the `If-Match`, then the
     * `Idempotency-Key`, which is drawn last, once nothing else can refuse the call.
     */
    fun prepare(
        facts: OperationFacts,
        path: String,
        query: List<Pair<String, String>>,
        callerHeaders: List<Pair<String, String>>,
        body: RequestBody,
        accept: String,
        idempotencyKey: String?,
        ifMatch: String?,
        options: RequestOptions,
    ): Call {
        CredentialHeaders.check(configuration.credential)
        CredentialHeaders.checkLanguage(configuration.language)
        options.timeout?.let { AttemptBudget.check(it, "The call's timeout") }
        val headers = linkedMapOf("Accept" to accept)
        for ((name, value) in callerHeaders) {
            if (!HeaderName.isToken(name)) {
                throw ConfigurationException(
                    "`$name` is no header name: a name is one or more letters, digits and `!#$%&'*+-.^_|~` or a " +
                        "backtick, which is what the platform's HTTP stack sends.",
                )
            }
            if (HeaderLookup.asciiLowercase(name) in clientHeaders) {
                throw ConfigurationException(
                    "`$name` is a header the client writes itself, from its credential, its language, the SDK " +
                        "that made it or the call's own arguments: leave it out of the call's headers.",
                )
            }
            if (HeaderName.isStackWritten(name)) {
                throw ConfigurationException(
                    "`$name` is a header the platform's HTTP stack writes itself as it sends a request: leave it " +
                        "out of the call's headers.",
                )
            }
            if (!HeaderValue.isCarried(value)) {
                throw ConfigurationException(
                    "The header `$name` holds a control character or a character outside printable ASCII, which " +
                        "the platform's HTTP stack does not send.",
                )
            }
            headers[if (HeaderLookup.asciiLowercase(name) == "accept") "Accept" else name] = value
        }
        StreamRefusal.check(if (accept == EVENT_STREAM_MEDIA_TYPE) emptyMap() else headers)

        val bytes =
            when (body) {
                is RequestBody.Empty -> {
                    null
                }

                is RequestBody.Json -> {
                    headers["Content-Type"] = "application/json"
                    body.text.encodeToByteArray()
                }

                is RequestBody.Multipart -> {
                    val form = FormBody.write(body.parts)
                    headers["Content-Type"] = form.contentType
                    form.bytes
                }
            }

        val origin =
            when (facts.surface) {
                Surface.Rest -> configuration.baseUrls.rest
                Surface.Tiles -> configuration.baseUrls.tiles
                Surface.Data -> configuration.baseUrls.data
            }
        val tag = ConditionalTag.tag(facts.isConditional, ifMatch)
        val key = IdempotencyKey.key(facts.declaresIdempotencyKey, idempotencyKey) { random.cryptographicUuid() }
        return Call(
            facts = facts,
            url = RequestAddress.url(origin, path, query),
            headers = headers,
            body = bytes,
            idempotencyKey = key,
            ifMatch = tag,
            policy = options.retry ?: configuration.retry,
            budget = AttemptBudget.budget(options.timeout, configuration.timeout, facts.timeout),
        )
    }

    /** Checks and fixes a REST or Tile call a printed method or a declaration wrote. */
    fun prepare(
        call: OperationCall,
        accept: String,
    ): Call =
        prepare(
            facts = call.facts,
            path = call.path,
            query = call.query,
            callerHeaders = call.headers,
            body = call.body,
            accept = accept,
            idempotencyKey = call.idempotencyKey,
            ifMatch = call.ifMatch,
            options = call.options,
        )

    /**
     * Sends a call's attempts until one settles it, and returns what [read] makes of the answer
     * that did, [read] run on the transport's dispatcher.
     *
     * An attempt that fails is repeated where the contract repeats it and the wait its answer names
     * fits the call's budget; otherwise the call throws the error its last attempt raised. Every
     * error carries the call's `Idempotency-Key`, and a cancelled caller throws its own
     * `CancellationException`, during a wait and a decode included.
     */
    suspend fun <Success> run(
        call: Call,
        read: (Answer) -> Settled<Success>,
    ): Success {
        var attempt = 0
        var counted = 0
        var waited = Duration.ZERO
        while (true) {
            currentCoroutineContext().ensureActive()
            val failure: MountayaException
            var status: Int? = null
            var code: ApiErrorCode? = null
            var retryAfter: Duration? = null
            when (val outcome = attempt(call)) {
                is Outcome.Answered -> {
                    val answer = outcome.answer
                    report(answer, call, attempt)
                    when (val settled = withContext(dispatcher) { read(answer) }) {
                        is Settled.Done -> {
                            return settled.value
                        }

                        is Settled.Failed -> {
                            failure = settled.error.carrying(call.idempotencyKey)
                            status = answer.status
                            code = (failure as? ApiException)?.code
                            retryAfter = answer.headers[RETRY_AFTER_HEADER]?.let { parseRetryAfter(it, clock.now()) }
                        }
                    }
                }

                is Outcome.Unanswered -> {
                    failure = ConnectionException(call.idempotencyKey, outcome.cause)
                }
            }
            val keyed = call.idempotencyKey != null
            if (!isRetryable(counted, status, code, call.facts.idempotent, keyed, call.policy)) throw failure
            val keyInUse = status == 409 && code == ApiErrorCode.IdempotencyKeyInUse
            val wait =
                backoffDelay(
                    attempt = counted,
                    draw = random.unitDraw(),
                    retryAfter = retryAfter,
                    policy = call.policy,
                    waited = waited,
                    minimum = if (keyInUse) keyInUseMinimum else Duration.ZERO,
                ) ?: throw failure
            waitBeforeRetry(wait)
            waited += wait
            attempt += 1
            if (!keyInUse) counted += 1
        }
    }

    /**
     * Sends one attempt within the call's budget, the wait for a first session token and the read of
     * the body included, the body read on the transport's dispatcher.
     */
    private suspend fun attempt(call: Call): Outcome {
        val raced =
            try {
                Deadline.run(call.budget) {
                    val request = request(call)
                    try {
                        val response = exchange.send(request)
                        val body =
                            try {
                                withContext(dispatcher) { response.body.readAll() }
                            } finally {
                                // A read cancelled before it began never closed the body: closing
                                // it here lets the connection go, and one read to its end is
                                // closed already.
                                response.body.close()
                            }
                        Outcome.Answered(Answer(response.status, response.headers, body))
                    } catch (cancelled: CancellationException) {
                        // The caller's own cancellation, a spent budget, or the platform reporting a
                        // request it cancelled, which the exchange throws as a cancellation: none is
                        // a request that never arrived, and none is repeated.
                        throw cancelled
                    } catch (failure: Exception) {
                        // The caller's cancellation and a spent budget leave here; anything else the
                        // exchange throws is a request that never arrived.
                        currentCoroutineContext().ensureActive()
                        Outcome.Unanswered(failure)
                    }
                }
            } catch (refusal: MountayaException) {
                throw refusal.carrying(call.idempotencyKey)
            }
        return when (raced) {
            is Deadline.Raced.Finished -> raced.value
            is Deadline.Raced.Expired -> Outcome.Unanswered(null)
        }
    }

    /**
     * Writes one attempt of a call: the credential's headers as its session token reads now, after
     * waiting for a first one where its source reports one on its way, the SDK that made it, and
     * [position] as the `Last-Event-ID` a stream's reopen resumes from.
     *
     * @throws ConfigurationException where a header of the attempt is one the platform's HTTP stack
     * does not send, a cookie a credential's function answered or a position a stream named among
     * them: refused before the attempt leaves, and never repeated, where the stack would refuse it as
     * the attempt is sent, which reads as a request that never arrived.
     */
    suspend fun request(
        call: Call,
        position: String? = null,
    ): ExchangeRequest {
        val projection =
            credentialHeaders(
                call.facts.credentials,
                isRead = CredentialHeaders.isRead(call.facts.method, call.facts.credentials),
            )
        val headers = LinkedHashMap(call.headers)
        headers.putAll(projection.headers)
        headers[CLIENT_HEADER] = ClientIdentity.value
        call.idempotencyKey?.let { headers[IDEMPOTENCY_KEY_HEADER] = it }
        call.ifMatch?.let { headers[IF_MATCH_HEADER] = it }
        projection.cookies?.invoke(call.url)?.let { headers[COOKIE_HEADER] = it }
        position?.let { headers[LAST_EVENT_ID_HEADER] = it }
        for ((name, value) in headers) {
            if (HeaderName.isToken(name) && HeaderValue.isCarried(value)) continue
            // The value is a credential's where the name is one of its headers, so it is never written out.
            throw ConfigurationException(
                "The header `$name` holds a control character or a character outside printable ASCII, which the " +
                    "platform's HTTP stack does not send, so the request was not sent.",
            )
        }
        return ExchangeRequest(call.facts.method, call.url, headers, call.body)
    }

    /** Projects the client's credential and language for an operation declaring [declaration], reading its session token for this request. */
    private suspend fun credentialHeaders(
        declaration: CredentialDeclaration,
        isRead: Boolean,
    ): CredentialHeaders.Projection {
        val credential = configuration.credential
        val token = (credential as? Credential.Publishable)?.sessionToken
        var session: ResolvedSession? = null
        if (token != null && !declaration.takesNoCredential) {
            var snapshot: SessionSnapshot? = null
            if (token is SessionToken.Source && !declaration.requiresSecretKey) {
                snapshot = ResolvedSession.awaitingFirstToken(token.source)
            }
            session = ResolvedSession.reading(token, snapshot)
        }
        return CredentialHeaders.project(credential, session, configuration.language, declaration, isRead)
    }

    /** Tells the client's hook of an attempt an answer came back to. An exception it throws is dropped. */
    fun report(
        answer: Answer,
        call: Call,
        attempt: Int,
    ) {
        val hook = configuration.onResponse ?: return
        val report =
            ResponseReport(
                operationId = call.facts.operationId,
                surface = call.facts.surface,
                status = answer.status,
                attempt = attempt,
                isReplayed = answer.headers[REPLAYED_HEADER]?.let(HeaderLookup::asciiLowercase) == "true",
                requestId = answer.headers[REQUEST_ID_HEADER],
                rateLimit = rateLimit(answer),
            )
        try {
            hook(report)
        } catch (dropped: Exception) {
            // Nothing the hook does changes what the call answers, a failure of its own included.
        }
    }

    /**
     * The error a failing answer raises, its next step sharpened by what the call's operation declares.
     * [envelope] is what its body carries, read here where the caller has not read it already.
     */
    fun failure(
        answer: Answer,
        call: Call,
        envelope: ErrorEnvelope = ErrorEnvelope.read(answer.body),
    ): ApiException =
        AnswerError.exception(
            answer.status,
            answer.headers,
            envelope,
            clock.now(),
            call.idempotencyKey,
            declaration = NextStep.Declaration(call.facts.credentials),
        )

    /**
     * The error a success whose body does not decode raises: the base `ApiException`, carrying the
     * status and what the headers said, with the decoder's failure riding along as its cause, rebuilt
     * with no byte of the body (`decoderFailure`), since the body may hold a secret. The contract names
     * no class for it yet (SPEC.md section 1), and this is the one place that raises it.
     */
    private fun undecodable(
        answer: Answer,
        call: Call,
        failure: IllegalArgumentException,
    ): ApiException =
        ApiException(
            answer.status,
            emptyList(),
            null,
            null,
            answer.headers[REQUEST_ID_HEADER],
            rateLimit(answer),
            null,
            call.idempotencyKey,
            decoderFailure(failure),
        )

    private fun rateLimit(answer: Answer): RateLimitState? = AnswerError.rateLimit(answer.headers)

    override suspend fun <Value, Metadata> send(
        call: OperationCall,
        data: KSerializer<Value>,
        metadata: KSerializer<Metadata>,
    ): Response<Value, Metadata> {
        val prepared = prepare(call, "application/json")
        return run(prepared) { answer ->
            if (!answer.isSuccess) return@run Settled.Failed(failure(answer, prepared))
            try {
                val (value, read) = Envelope.rest(answer.body, data, metadata)
                Settled.Done(
                    Response(
                        data = value,
                        metadata = read,
                        status = answer.status,
                        requestId = answer.headers[REQUEST_ID_HEADER],
                        location = answer.headers["Location"],
                        etag = answer.headers[ETAG_HEADER],
                        headers = answer.headers.byName,
                    ),
                )
            } catch (undecoded: IllegalArgumentException) {
                Settled.Failed(undecodable(answer, prepared, undecoded))
            }
        }
    }

    override suspend fun download(call: OperationCall): BinaryResponse {
        val prepared = prepare(call, "*/*")
        return run(prepared) { answer -> binary(answer, prepared) }
    }

    /**
     * Sends a call answered with a file at [url] as it is written, rather than at a path under its surface's origin: a
     * file whose address another answer of the surface states, which the client claims by its surface's origin before
     * handing it here. Every other rule of [download] holds: the credential [facts] declare, the budget, the retries and
     * the error ladder.
     */
    suspend fun downloadAt(
        url: Url,
        facts: OperationFacts,
    ): BinaryResponse {
        val prepared =
            prepare(
                facts = facts,
                path = "",
                query = emptyList(),
                callerHeaders = emptyList(),
                body = RequestBody.Empty,
                accept = "*/*",
                idempotencyKey = null,
                ifMatch = null,
                options = RequestOptions(),
            )
        val addressed =
            Call(
                facts = prepared.facts,
                url = url,
                headers = prepared.headers,
                body = prepared.body,
                idempotencyKey = prepared.idempotencyKey,
                ifMatch = prepared.ifMatch,
                policy = prepared.policy,
                budget = prepared.budget,
            )
        return run(addressed) { answer -> binary(answer, addressed) }
    }

    /** Settles an attempt of a call answered with a file: its bytes and their media type, or the error its status maps to. */
    private fun binary(
        answer: Answer,
        prepared: Call,
    ): Settled<BinaryResponse> =
        if (!answer.isSuccess) {
            Settled.Failed(failure(answer, prepared))
        } else {
            Settled.Done(
                BinaryResponse(
                    answer.body,
                    answer.headers["Content-Type"] ?: "application/octet-stream",
                    status = answer.status,
                    requestId = answer.headers[REQUEST_ID_HEADER],
                ),
            )
        }

    override fun <Event> stream(
        call: OperationCall,
        event: KSerializer<Event>,
    ): EventStream<Event> {
        // A printed stream carries the events of an agent run or of a collaboration feed. One whose
        // events neither reading knows is read as a feed, every record handed on, so a stream the
        // contract adds is never lost before its reading is written.
        val reading =
            StreamedEvents.of(event, call.facts.operationId)
                ?: StreamedEvents(EventStreamRules.collaborationFeed, { record ->
                    StreamedEvents.decoded(record, event)?.let { decoded ->
                        decoded to if (record.event == "reconnect") EventRole.reconnect else EventRole.ordinary
                    }
                })
        val prepared = prepare(call, EVENT_STREAM_MEDIA_TYPE)
        return EventStream({ member -> EventStreamReader(this, prepared, reading, member).frames() })
    }

    /** Returns the cold stream of a call by declaration, its records handed on as they are. */
    fun records(call: OperationCall): Flow<EventFrame<EventRecord>> =
        EventStreamReader(this, prepare(call, EVENT_STREAM_MEDIA_TYPE), StreamedEvents.records).frames()

    override suspend fun <Result : Any> query(
        call: DataCall,
        result: KSerializer<Result>,
    ): Result {
        val digest = persistedQueryDigest(call.document)
        var attempt = persistedQueries.firstAttempt(digest)
        while (true) {
            val prepared =
                prepare(
                    facts = call.facts,
                    path = call.facts.path,
                    query = emptyList(),
                    callerHeaders = emptyList(),
                    body = RequestBody.Json(dataBody(call, attempt)),
                    accept = "application/json",
                    idempotencyKey = null,
                    ifMatch = null,
                    options = call.options,
                )
            val sent = attempt
            when (val outcome = run(prepared) { answer -> readData(answer, prepared, call, sent, result) }) {
                is DataOutcome.Answered -> return outcome.result
                is DataOutcome.Resend -> attempt = outcome.attempt
            }
        }
    }

    /** DataOutcome is what a Data API answer makes of the call: its result, or the attempt the persisted-query protocol asks for next. */
    private sealed interface DataOutcome<out Result> {
        class Answered<Result>(
            val result: Result,
        ) : DataOutcome<Result>

        class Resend(
            val attempt: PersistedQueryAttempt,
        ) : DataOutcome<Nothing>
    }

    /**
     * The body of a Data API attempt, as the text sent: the document where it sends it, its input
     * under `variables`, and the digest where it sends one, in that order. The input is already the
     * client's `Json`'s text, and each string is written by it too, so the body is never built as a
     * tree of JSON first.
     */
    private fun dataBody(
        call: DataCall,
        attempt: PersistedQueryAttempt,
    ): String =
        buildString {
            append('{')
            if (attempt.sendsDocument) {
                append("\"query\":").append(wireJson.encodeToString(String.serializer(), call.document)).append(',')
            }
            append("\"variables\":{\"input\":").append(call.input).append('}')
            attempt.digest?.let { digest ->
                append(",\"extensions\":{\"persistedQuery\":{\"version\":").append(PERSISTED_QUERY_VERSION)
                append(",\"sha256Hash\":").append(wireJson.encodeToString(String.serializer(), digest)).append("}}")
            }
            append('}')
        }

    /**
     * Reads a Data API answer: a refusal of the digest it sent before the rule that a success
     * carrying errors is a failure, then the operation's own field, an answer without it being a
     * `DataException`.
     *
     * A failing status is read for that refusal too, before it makes the answer a failure: the Data
     * API with the extension turned off answers a digest-only attempt `GRAPHQL_VALIDATION_FAILED`
     * under 422, or 400 under `application/graphql-response+json`, where a digest it does not hold
     * yet is answered under 200.
     */
    private fun <Result : Any> readData(
        answer: Answer,
        call: Call,
        data: DataCall,
        attempt: PersistedQueryAttempt,
        result: KSerializer<Result>,
    ): Settled<DataOutcome<Result>> {
        if (!answer.isSuccess) {
            val envelope = ErrorEnvelope.read(answer.body)
            val next =
                resend(envelope.errors, attempt)
                    ?: return Settled.Failed(failure(answer, call, envelope))
            return Settled.Done(DataOutcome.Resend(next))
        }
        val read =
            try {
                Envelope.data(answer.body, data.operation.name, result)
            } catch (undecoded: IllegalArgumentException) {
                return Settled.Failed(undecodable(answer, call, undecoded))
            }
        if (read.envelope.hasErrors) {
            val next =
                resend(read.envelope.errors, attempt)
                    ?: return Settled.Failed(failure(answer, call, read.envelope))
            return Settled.Done(DataOutcome.Resend(next))
        }
        val field =
            read.result
                ?: return Settled.Failed(
                    AnswerError.exception(answer.status, answer.headers, read.envelope, clock.now(), null),
                )
        return Settled.Done(DataOutcome.Answered(field))
    }

    /**
     * Returns the attempt the persisted-query protocol asks for once [errors] answered [attempt], or
     * null where none of them refuses its digest: the document alone where the server declines the
     * protocol, which declines it for the transport's lifetime, and the document beside the digest
     * where the server asks to have it registered.
     */
    private fun resend(
        errors: List<ErrorEntry>,
        attempt: PersistedQueryAttempt,
    ): PersistedQueryAttempt? =
        when (persistedQueries.read(errors, attempt)) {
            PersistedQueryRefusal.Unsupported -> PersistedQueryAttempt(digest = null, sendsDocument = true)
            PersistedQueryRefusal.Register -> PersistedQueryAttempt(digest = attempt.digest, sendsDocument = true)
            null -> null
        }

    companion object {
        /** The headers the client writes itself, which no caller's header may name, lowercased. */
        val clientHeaders: Set<String> =
            setOf(
                API_KEY_HEADER,
                SESSION_TOKEN_HEADER,
                SHARE_TOKEN_HEADER,
                ACCEPT_LANGUAGE_HEADER,
                CLIENT_HEADER,
                IDEMPOTENCY_KEY_HEADER,
                IF_MATCH_HEADER,
                LAST_EVENT_ID_HEADER,
                COOKIE_HEADER,
            ).mapTo(mutableSetOf(), HeaderLookup::asciiLowercase)
    }
}

/** Returns this failure carrying the key its call sent, where it sent one. */
internal fun MountayaException.carrying(key: String?): MountayaException {
    if (key != null) idempotencyKey = key
    return this
}
