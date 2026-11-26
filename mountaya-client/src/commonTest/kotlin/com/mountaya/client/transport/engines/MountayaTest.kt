package com.mountaya.client

import com.mountaya.client.StubServer.Ending
import com.mountaya.client.StubServer.Step
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MountayaTest {
    private val inert = Scripted.Exchange(kotlinx.coroutines.test.TestScope(), emptyList())

    /** What every request names the SDK that made it by, a map's own included. */
    private val identity = "mountaya-kotlin/$CLIENT_VERSION"

    @Test
    fun aClientRefusesACredentialALanguageOrABudgetNoRequestCouldCarry() {
        assertFailsWith<ConfigurationException> {
            Mountaya(
                Credential.Publishable("pk_test_abc", SessionToken.Function { null }),
                language = " ",
                exchange = inert,
            )
        }
        assertFailsWith<ConfigurationException> {
            Mountaya(Credential.Publishable("pk_test_abc"), timeout = Duration.ZERO, exchange = inert)
        }
    }

    @Test
    fun aMapsRequestToTheTileApiCarriesTheKeyTheSessionTokenAndTheLanguage() {
        val client =
            Mountaya(
                Credential.Publishable("pk_test_a", SessionToken.Value("sess_a")),
                language = "fr",
                exchange = inert,
            )

        assertEquals(
            mapOf(
                "X-API-Key" to "pk_test_a",
                "X-Session-Token" to "sess_a",
                "Accept-Language" to "fr",
                "X-Mountaya-Client" to identity,
            ),
            client.mapRequestHeaders(Url("https://tiles.mountaya.com/v1/zones/12/2093/1425")),
        )
        assertEquals("fr", client.language)
    }

    @Test
    fun aMapsRequestToAnyOtherHostIsNoRequestOfTheClientsAndIsSentNothing() {
        val client = Mountaya(Credential.Publishable("pk_test_a"), language = "fr", exchange = inert)

        assertNull(client.mapRequestHeaders(Url("https://fonts.example.com/a.pbf")))
        assertNull(client.mapRequestHeaders(Url("https://tiles.mountaya.com.evil.net/v1")))
    }

    @Test
    fun theTileApisOriginIsTheClientsOwnABaseUrlWithAPathIncluded() {
        val client =
            Mountaya(
                Credential.Publishable("pk_test_a"),
                baseUrls = BaseUrls(tiles = Url("https://proxy.example.com/tiles")),
                exchange = inert,
            )

        assertEquals(
            mapOf("X-API-Key" to "pk_test_a", "Accept-Language" to "*", "X-Mountaya-Client" to identity),
            client.mapRequestHeaders(Url("https://proxy.example.com/tiles/v1/zones")),
        )
        assertNull(client.mapRequestHeaders(Url("https://tiles.mountaya.com/v1/zones")))
        assertNull(client.language)
    }

    @Test
    fun aShareTokenReplacesTheKeyAndItsVisitorsCookiesRideWhereTheirSourceAnswers() {
        val asked = mutableListOf<Url>()
        val client =
            Mountaya(
                Credential.Share("share_a") { url ->
                    asked += url
                    "session=visitor"
                },
                exchange = inert,
            )
        val tile = Url("https://tiles.mountaya.com/v1/itineraries/it/1/2/3")

        assertEquals(
            mapOf(
                "X-Share-Token" to "share_a",
                "Accept-Language" to "*",
                "X-Mountaya-Client" to identity,
                "Cookie" to "session=visitor",
            ),
            client.mapRequestHeaders(tile),
        )
        assertEquals(listOf(tile), asked)
        assertEquals(
            mapOf("X-Share-Token" to "share_a", "Accept-Language" to "*", "X-Mountaya-Client" to identity),
            Mountaya(Credential.Share("share_a") { null }, exchange = inert).mapRequestHeaders(tile),
        )
    }

    /**
     * A map's requests are held to what a header carries as a call's are, so a visitor's cookie no header can carry
     * is refused at the map's request, as `sdk-dart` and `sdk-swift` refuse it, and never handed to a renderer.
     */
    @Test
    fun aVisitorsCookieNoHeaderCarriesIsRefusedAtTheMapsRequestItsValueNeverWrittenOut() {
        val tile = Url("https://tiles.mountaya.com/v1/itineraries/it/1/2/3")
        for (cookie in listOf("session=café", "session=a\r\nX-Evil: 1", "session=\u0000")) {
            val client = Mountaya(Credential.Share("share_a") { cookie }, exchange = inert)

            val refused = assertFailsWith<ConfigurationException>(cookie) { client.mapRequestHeaders(tile) }
            assertFalse("session=" in refused.message.orEmpty(), refused.message)
        }
    }

    @Test
    fun eachMapRequestReadsTheSessionTokenAsItStandsThenAndNeverWaitsForOne() {
        val held = MutableStateFlow(SessionSnapshot(token = "sess_a", userId = "u1"))
        val source =
            object : SessionTokenSource {
                override val snapshot: StateFlow<SessionSnapshot> = held
            }
        val client = Mountaya(Credential.Publishable("pk_test_a", SessionToken.Source(source)), exchange = inert)
        val tile = Url("https://tiles.mountaya.com/v1/zones/1/2/3")

        assertEquals("sess_a", client.mapRequestHeaders(tile)?.get("X-Session-Token"))
        held.value = SessionSnapshot(token = "sess_b", userId = "u1")
        assertEquals("sess_b", client.mapRequestHeaders(tile)?.get("X-Session-Token"))
        held.value = SessionSnapshot(isMinting = true)
        assertEquals(
            mapOf("X-API-Key" to "pk_test_a", "Accept-Language" to "*", "X-Mountaya-Client" to identity),
            client.mapRequestHeaders(tile),
        )
    }

    @Test
    fun aSessionTokenAFunctionAnswersIsHeldToATokensRulesAtTheMapsRequest() {
        var token: String? = "sess_a"
        val client = Mountaya(Credential.Publishable("pk_test_a", SessionToken.Function { token }), exchange = inert)
        val tile = Url("https://tiles.mountaya.com/v1/zones/1/2/3")

        assertEquals("sess_a", client.mapRequestHeaders(tile)?.get("X-Session-Token"))
        token = "sk_live_oops"
        assertFailsWith<ConfigurationException> { client.mapRequestHeaders(tile) }
        token = null
        assertEquals(
            mapOf("X-API-Key" to "pk_test_a", "Accept-Language" to "*", "X-Mountaya-Client" to identity),
            client.mapRequestHeaders(tile),
        )
        assertNull(client.mapRequestHeaders(Url("https://example.com/v1/zones/1/2/3")))
    }

    @OptIn(InternalMountayaApi::class)
    @Test
    fun anOrganizationReadsAttemptTakesTheLargerOfTheClientsBudgetAndTheOperationsOwnWithItsMargin() {
        assertEquals(
            35.seconds,
            Mountaya(Credential.Publishable("pk_test_a"), exchange = inert).organizationLookupBudget,
        )
        assertEquals(
            50.seconds,
            Mountaya(Credential.Publishable("pk_test_a"), timeout = 50.seconds, exchange = inert)
                .organizationLookupBudget,
        )
    }

    @OptIn(InternalMountayaApi::class)
    @Test
    fun aMapFileIsReadAtTheAddressTheTileApiStatesUnderTheMapsCredential() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(Scripted.Reply.Answer(200, mapOf("Content-Type" to "image/webp"), byteArrayOf(1, 2, 3))),
                )
            val client = Scripted.client(this, exchange)
            val file =
                client.mapFile(
                    Url("https://tiles.mountaya.com/v1/elevation/14/8504/5833.webp"),
                    "getElevationTile",
                )
            val sent = exchange.sent.single().request

            assertEquals(listOf<Byte>(1, 2, 3), file.bytes.toList())
            assertEquals("https://tiles.mountaya.com/v1/elevation/14/8504/5833.webp", sent.url.toString())
            assertEquals("GET", sent.method)
            assertEquals(identity, sent.header("X-Mountaya-Client"))
            assertNotNull(sent.header("X-API-Key"), "a map's file carries the map's credential")
        }

    @OptIn(InternalMountayaApi::class)
    @Test
    fun aMapFileAtAnAddressTheTileApiDoesNotClaimIsRefusedBeforeAnyRequest() =
        runTest {
            val exchange = Scripted.Exchange(this, emptyList())
            val client = Scripted.client(this, exchange)

            assertFailsWith<ConfigurationException> {
                client.mapFile(Url("https://elevation.example.com/v1/elevation/tiles.json"), "getElevationTileJson")
            }
            assertTrue(exchange.sent.isEmpty())
        }

    @Test
    fun aCallByDeclarationSendsThePathTheQueryTheCallersHeadersAndTheBodyAndReadsTheEnvelope() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(
                            201,
                            mapOf("Location" to "/v1/things/t1", "ETag" to "\"d1\""),
                            """{"data":{"id":"t1"}}""".encodeToByteArray(),
                        ),
                    ),
                )
            val client = Scripted.client(this, exchange)
            val response =
                client.send(
                    OperationDeclaration("postThing", "POST", declaresIdempotencyKey = true),
                    "/v1/things",
                    JsonElement.serializer(),
                    JsonObject.serializer().nullable,
                    query = listOf("tag" to "a b&c"),
                    headers = mapOf("X-Trace" to "t-1"),
                    body = RequestBody.Json(JsonObject(mapOf("name" to JsonPrimitive("Hut")))),
                    idempotencyKey = "order-1",
                )
            val sent = exchange.sent.single().request

            assertEquals(201, response.status)
            assertEquals("/v1/things/t1", response.location)
            assertEquals("\"d1\"", response.etag)
            assertEquals(JsonObject(mapOf("id" to JsonPrimitive("t1"))), response.data)
            assertEquals("https://api.mountaya.com/v1/things?tag=a%20b%26c", sent.url.toString())
            assertEquals("t-1", sent.header("x-trace"))
            assertEquals("order-1", sent.header("Idempotency-Key"))
            assertEquals("application/json", sent.header("Content-Type"))
            assertEquals(identity, sent.header("X-Mountaya-Client"))
            assertEquals("""{"name":"Hut"}""", sent.body?.decodeToString())
        }

    @Test
    fun anAnswersHeadersAreEveryOneItSentNamedInLowercaseAndReadInAnyCase() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(
                            200,
                            mapOf("X-Request-Id" to "req-1", "Cache-Control" to "private", "X-Served-By" to "edge-3"),
                            """{"data":{"id":"t1"}}""".encodeToByteArray(),
                        ),
                    ),
                )
            val response =
                Scripted.client(this, exchange).send(
                    OperationDeclaration("getThing", "GET"),
                    "/v1/things/t1",
                    JsonElement.serializer(),
                    JsonObject.serializer().nullable,
                )

            assertEquals(
                mapOf("x-request-id" to "req-1", "cache-control" to "private", "x-served-by" to "edge-3"),
                response.headers,
            )
            assertEquals("edge-3", response.header("X-SERVED-BY"))
            assertEquals("req-1", response.requestId)
            assertNull(response.header("ETag"))
        }

    @Test
    fun aFormByDeclarationIsSentAsMultipartItsPartsInTheirOrder() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(Scripted.Reply.Answer(200, body = """{"data":null}""".encodeToByteArray())),
                )
            Scripted.client(this, exchange).send(
                OperationDeclaration("postThingPreview", "POST", idempotent = true),
                "/v1/things/preview",
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
                body =
                    RequestBody.Multipart(
                        listOf(
                            MultipartPart.Text("name", "Hut"),
                            MultipartPart.File(
                                "track",
                                "<gpx/>".encodeToByteArray(),
                                "tour.gpx",
                                "application/gpx+xml",
                            ),
                            MultipartPart.File("photo", byteArrayOf(1)),
                        ),
                    ),
            )
            val sent = exchange.sent.single().request
            val boundary = sent.header("Content-Type").orEmpty().substringAfter("multipart/form-data; boundary=")

            assertTrue(boundary.isNotEmpty(), "${sent.header("Content-Type")}")
            assertEquals(
                "--$boundary\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\nHut\r\n" +
                    "--$boundary\r\nContent-Disposition: form-data; name=\"track\"; filename=\"tour.gpx\"\r\n" +
                    "Content-Type: application/gpx+xml\r\n\r\n<gpx/>\r\n" +
                    "--$boundary\r\nContent-Disposition: form-data; name=\"photo\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n\u0001\r\n--$boundary--\r\n",
                sent.body?.decodeToString(),
            )
        }

    @Test
    fun aDataApiCallByDeclarationIsHeldToEveryRuleOfAPrintedOne() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(
                            200,
                            body = """{"data":{"weather":{"temperature":4}}}""".encodeToByteArray(),
                        ),
                    ),
                )
            val operation =
                DataOperation(
                    "weather",
                    "WeatherInput!",
                    FieldSelection {
                        field("temperature")
                        field("wind")
                    },
                    timeout = 20.seconds,
                )
            val client = Scripted.client(this, exchange, Scripted.session)

            val weather =
                client.query(
                    operation,
                    JsonObject(mapOf("point" to JsonPrimitive("6.86,45.83"))),
                    JsonElement.serializer(),
                    select = FieldSelection { field("temperature") },
                )
            val sent = exchange.sent.single().request
            val sentBody = assertNotNull(sent.body, "a Data API call sends its query as a body")
            val body = Json.parseToJsonElement(sentBody.decodeToString()).jsonObject

            assertEquals(JsonObject(mapOf("temperature" to JsonPrimitive(4))), weather)
            assertEquals("https://data.mountaya.com/graphql", sent.url.toString())
            assertEquals("POST", sent.method)
            assertEquals("sess_abc", sent.header("X-Session-Token"))
            assertEquals("""{"input":{"point":"6.86,45.83"}}""", body.getValue("variables").toString())
            assertNotNull(body["extensions"], "the persisted query's digest rides the first attempt")
            assertFailsWith<SelectionException> {
                client.query(
                    operation,
                    JsonObject(emptyMap()),
                    JsonElement.serializer(),
                    select = FieldSelection { field("rain") },
                )
            }
            assertFailsWith<ConfigurationException> {
                Scripted.client(this, exchange).query(operation, JsonObject(emptyMap()), JsonElement.serializer())
            }
            assertEquals(1, exchange.sent.size, "a refused call sends nothing")
        }

    @Test
    fun aListingByDeclarationIsWalkedOverTheCallersQueryItsCursorReadFromTheOpenMetadata() =
        runTest {
            fun page(
                rows: String,
                next: String,
            ) = Scripted.Reply.Answer(
                200,
                body =
                    """{"data":$rows,"extensions":{"metadata":{"pagination":{"next_cursor":$next}}}}"""
                        .encodeToByteArray(),
            )
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(page("[\"a\",\"b\"]", "\"c2\""), page("[]", "\"c3\""), page("[\"c\"]", "7")),
                )
            val client = Scripted.client(this, exchange)

            val rows =
                client
                    .walk(
                        OperationDeclaration("getThings", "GET", idempotent = true),
                        "/v1/things",
                        String.serializer(),
                        query = listOf("kind" to "hut", "cursor" to "c0", "limit" to "9"),
                        pageSize = 2,
                    ).toList()

            assertEquals(listOf("a", "b", "c"), rows, "a cursor that is no text ends the walk")
            assertEquals(
                listOf(
                    "https://api.mountaya.com/v1/things?kind=hut&cursor=c0&limit=2",
                    "https://api.mountaya.com/v1/things?kind=hut&cursor=c2&limit=2",
                    "https://api.mountaya.com/v1/things?kind=hut&cursor=c3&limit=2",
                ),
                exchange.sent.map { it.request.url.toString() },
            )
            assertFailsWith<ConfigurationException> {
                client.walk(OperationDeclaration("getThings", "GET"), "/v1/things", String.serializer(), maxPages = 0)
            }
        }

    @Test
    fun aCallByDeclarationNamingTheHeaderTheSdkIsNamedInIsRefusedBeforeAnyRequest() =
        runTest {
            val exchange = Scripted.Exchange(this, emptyList())
            val client = Scripted.client(this, exchange)

            val refusal =
                assertFailsWith<ConfigurationException> {
                    client.send(
                        OperationDeclaration("getThing", "GET"),
                        "/v1/things/t1",
                        JsonElement.serializer(),
                        JsonObject.serializer().nullable,
                        headers = mapOf("x-mountaya-client" to "mountaya-kotlin/9.9.9"),
                    )
                }

            assertTrue(refusal.message.orEmpty().startsWith("`x-mountaya-client` is a header the client writes itself"))
            assertEquals(0, exchange.sent.size)
        }

    @Test
    fun aStreamByDeclarationIsReadAsAFeedOfRecords() =
        runTest {
            val text = "event: thing\nid: 1-0\ndata: {}\n\nevent: reconnect\ndata: {}\n\n"
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Stream(
                            200,
                            Scripted.BodyScript(
                                listOf(Duration.ZERO to text.encodeToByteArray()),
                                Scripted.BodyScript.End.StaysOpen,
                            ),
                        ),
                        Scripted.Reply.Answer(404),
                    ),
                )
            val client = Scripted.client(this, exchange, Scripted.session)
            val records = mutableListOf<String?>()
            assertFailsWith<com.mountaya.client.NotFoundException> {
                client.events(OperationDeclaration("followThings", "GET"), "/v1/things/events").collect {
                    records +=
                        it.event.event
                }
            }

            assertEquals(listOf<String?>("thing"), records)
            assertEquals(
                "1-0",
                exchange.sent
                    .last()
                    .request
                    .header("Last-Event-ID"),
            )
        }

    @Test
    fun aClientWhoseCredentialCarriesAnEngineKeepsItMintingUntilItIsClosed() =
        runTest {
            var mints = 0
            val clock = Scripted.clock(this)
            val engine =
                SessionTokenEngine(backgroundScope, mint = {
                    mints += 1
                    RestSchema.Session(expires_at = clock.now() + 300.seconds, token = "sess_$mints", user_id = null)
                }, clock = clock)
            val client =
                Scripted.client(
                    this,
                    inert,
                    Credential.Publishable("pk_test_abc", SessionToken.Source(engine)),
                )
            runCurrent()
            assertEquals(1, mints)

            client.close()
            advanceTimeBy(3_600.seconds)
            assertEquals(1, mints)
            assertEquals("sess_1", engine.snapshot.value.token)
        }

    @Test
    fun theAccessorsShareTheOneTransport() =
        runTest {
            val page = """{"data":[],"extensions":{"metadata":{"pagination":{"limit":2,"next_cursor":null}}}}"""
            val exchange =
                Scripted.Exchange(
                    this,
                    emptyList(),
                    exhausted = Scripted.Reply.Answer(200, body = page.encodeToByteArray()),
                )
            val client = Scripted.client(this, exchange, Scripted.session)
            val listed = client.studio.collections.getCollectionItineraries("col") { limit = 2 }
            val itineraries = client.studio.itineraries.getItineraries { status = RestSchema.ItineraryStatus("draft") }

            val tiles = runCatching { client.tiles.getZonesSpecification() }

            assertEquals(emptyList(), listed.data)
            assertTrue(tiles.isFailure)
            assertEquals(emptyList(), itineraries.data)
            assertEquals(
                listOf(
                    "https://api.mountaya.com/v1/collections/col/itineraries?limit=2",
                    "https://api.mountaya.com/v1/itineraries?status=draft",
                ),
                exchange.sent.take(2).map { it.request.url.toString() },
            )
            // The tiles accessor reaches the same exchange, under the Tile API's origin; the page
            // answered is no specification, so its decode fails, which is not what this test reads.
            assertTrue(
                exchange.sent
                    .last()
                    .request.url
                    .toString()
                    .startsWith("https://tiles.mountaya.com/"),
            )
        }

    @Test
    fun aClientGivenNoExchangeSendsThroughThePlatformsOverTheProcessesOneClient() {
        val client = Mountaya(Credential.Publishable("pk_test_a"))
        val exchange = assertIs<SystemExchange>(client.transport.exchange)

        assertSame(SystemExchange().loader, exchange.loader)
        client.close()
    }

    @Test
    fun aClientReadsAndDecodesOnTheDispatcherItIsGivenAndOnThePlatformsDefaultOneWhereItIsGivenNone() {
        val given = StandardTestDispatcher()
        val client = Mountaya(Credential.Publishable("pk_test_a"), exchange = inert, dispatcher = given)
        val plain = Mountaya(Credential.Publishable("pk_test_a"), exchange = inert)

        assertSame(given, client.transport.dispatcher)
        assertSame(SystemDefaults.dispatcher, plain.transport.dispatcher)
        client.close()
        plain.close()
    }

    @Test
    fun aPrintedWriteLeavesOverThePlatformsStackAndAnswersWhatItsSuccessDeclares() =
        overThePlatform(
            Step.Answer(
                201,
                mapOf("Location" to "/v1/itineraries/it/share-link", "X-Request-Id" to "req_201", "ETag" to "\"e1\""),
                listOf("""{"data": {"share_token": "share_0a", "created_at": "2026-11-05T08:00:00Z"}}"""),
            ),
        ) { server, client ->
            val link = client.studio.itineraries.postItineraryShareLink("it") { idempotencyKey = "order-7f3a" }

            assertEquals(201, link.status)
            assertEquals("share_0a", link.data.share_token)
            assertEquals("/v1/itineraries/it/share-link", link.location)
            assertEquals("req_201", link.requestId)
            assertEquals("\"e1\"", link.etag)
            val sent = server.seen.first()
            assertEquals("POST", sent.method)
            assertEquals("/v1/itineraries/it/share-link", sent.target)
            assertEquals("pk_test_a", sent.header("X-API-Key"))
            assertEquals("sess_a", sent.header("X-Session-Token"))
            assertEquals("fr", sent.header("Accept-Language"))
            assertEquals("order-7f3a", sent.header("Idempotency-Key"))
            assertEquals("application/json", sent.header("Accept"))
            assertNull(sent.header("Cookie"))
        }

    @Test
    fun anExportAnswersItsBytesTheContentTypeTheServerChoseAndItsRequestId() =
        overThePlatform(
            Step.Answer(
                200,
                mapOf("Content-Type" to "application/gpx+xml", "X-Request-Id" to "req_gpx"),
                listOf("<gpx>", "</gpx>"),
            ),
        ) { server, client ->
            val file = client.studio.itineraries.getItineraryExport("it", RestSchema.ExportFormat.Gpx)

            assertEquals("<gpx></gpx>", file.bytes.decodeToString())
            assertEquals("application/gpx+xml", file.contentType)
            assertEquals(200, file.status)
            assertEquals("req_gpx", file.requestId)
            assertEquals("/v1/itineraries/it/export?format=gpx", server.seen.first().target)
            assertEquals("*/*", server.seen.first().header("Accept"))
        }

    @Test
    fun anEventStreamIsReadAsThePlatformDeliversItAndEndsOnItsDone() =
        overThePlatform(
            Step.Answer(
                200,
                mapOf("Content-Type" to "text/event-stream"),
                listOf(
                    "id: 1\nevent: message\ndata: {\"content\": \"Bonjour\", \"message_index\": 0}\n\n",
                    "id: 2\nevent: done\ndata: {\"reason\": \"completed\"}\n\n",
                ),
                Ending.StaysOpen,
            ),
        ) { server, client ->
            val frames =
                client.agent.runs
                    .getAgentRunEvents("ru")
                    .toList()

            assertEquals(listOf("1", "2"), frames.map { it.id })
            assertEquals("Bonjour", assertIs<RestSchema.AgentMessageEvent>(frames.first().event).content)
            assertEquals("text/event-stream", server.seen.first().header("Accept"))
            server.awaitStops(1)
        }

    @Test
    fun aDataApiCallSendsItsDigestAloneAndItsDocumentWhereTheServerAsks() =
        overThePlatform(
            Step.Answer(200, chunks = listOf("""{"errors": [{"message": "PersistedQueryNotFound"}]}""")),
            Step.Answer(200, chunks = listOf("""{"data": {"directions": {"routes": [{"distance": 1200}]}}}""")),
        ) { server, client ->
            val result =
                client.data.directions(
                    DataSchema.DirectionsInput(
                        activity = DataSchema.ActivitySlug.HikingAndTrail,
                        waypoints = listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)),
                    ),
                    select = FieldSelection { field("routes") { field("distance") } },
                )

            assertEquals(1200.0, result.routes?.first()?.distance)
            val sent = server.seen
            assertEquals(2, sent.size)
            val first = Json.parseToJsonElement(sent[0].body.decodeToString()).jsonObject
            val second = Json.parseToJsonElement(sent[1].body.decodeToString()).jsonObject
            assertNull(first["query"])
            assertNotNull(first.persistedQueryHash(), "the request names its persisted query")
            assertTrue(
                second
                    .getValue("query")
                    .jsonPrimitive.content
                    .startsWith("query Directions"),
            )
            assertNotNull(second.persistedQueryHash(), "the request names its persisted query")
            assertTrue(sent.all { it.method == "POST" && it.header("X-Session-Token") == "sess_a" })
        }

    @Test
    fun aPagedListingIsWalkedOverThePlatformsStackOnePageARequest() =
        overThePlatform(
            Step.Answer(
                200,
                chunks =
                    listOf(
                        """{"data": [], "extensions": {"metadata": {"pagination": {"limit": 2, "next_cursor": "c2"}}}}""",
                    ),
            ),
            Step.Answer(
                200,
                chunks =
                    listOf(
                        """{"data": [], "extensions": {"metadata": {"pagination": {"limit": 2, "next_cursor": null}}}}""",
                    ),
            ),
        ) { server, client ->
            val rows = PageWalk(pageSize = 2) { paged -> client.studio.itineraries.getItineraries { paged() } }.toList()

            assertEquals(0, rows.size)
            assertEquals(
                listOf("/v1/itineraries?limit=2", "/v1/itineraries?cursor=c2&limit=2"),
                server.seen.map { it.target },
            )
        }

    /**
     * Runs [block] against a client whose every surface is a local server answering [steps], over
     * the platform's exchange with a client of its own, on real time: an attempt's budget is a
     * real deadline here, which a test's virtual clock would spend at once while the network works.
     */
    private fun overThePlatform(
        vararg steps: Step,
        block: suspend (StubServer, Mountaya) -> Unit,
    ): TestResult =
        runTest {
            val server = StubServer.start(*steps)
            val loader = SystemExchange.Loader(SystemExchange.systemClient())
            val client =
                Mountaya(
                    Credential.Publishable("pk_test_a", SessionToken.Value("sess_a")),
                    language = "fr",
                    baseUrls = BaseUrls(data = server.origin, rest = server.origin, tiles = server.origin),
                    retry = RetryPolicy(maxRetries = 0),
                    exchange = SystemExchange(loader),
                )
            try {
                StubServer.realTime { block(server, client) }
            } finally {
                client.close()
                loader.client.close()
                loader.client.engine.close()
                server.close()
            }
        }

    /** The `extensions.persistedQuery.sha256Hash` a Data API request carries, or null. */
    private fun JsonObject.persistedQueryHash(): String? =
        (this["extensions"] as? JsonObject)
            ?.let { it["persistedQuery"] as? JsonObject }
            ?.let { it["sha256Hash"] as? JsonPrimitive }
            ?.content
}
