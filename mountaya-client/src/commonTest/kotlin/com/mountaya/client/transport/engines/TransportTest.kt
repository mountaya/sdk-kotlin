package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import com.mountaya.client.generated.DataOperations
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.OperationTable
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class TransportTest {
    private val unavailable =
        """{"errors":[{"message":"busy","extensions":{"code":"SERVICE_UNAVAILABLE"}}]}"""
            .encodeToByteArray()
    private val keyInUse =
        """{"errors":[{"message":"running","extensions":{"code":"IDEMPOTENCY_KEY_IN_USE"}}]}"""
            .encodeToByteArray()
    private val empty = """{"data":null}""".encodeToByteArray()

    /** Draws nothing and keys one fixed key. */
    private object FixedKey : RandomSource {
        override fun unitDraw(): Double = 0.0

        override fun cryptographicUuid(): Uuid = Uuid.parse("6f1c2a9e-3b4d-4e5f-8a6b-7c8d9e0f1a2b")
    }

    private fun quick(maxRetries: Int = 2): RetryPolicy = RetryPolicy(maxRetries, Duration.ZERO, Duration.ZERO)

    private fun write(
        method: String = "POST",
        key: Boolean = true,
        conditional: Boolean = false,
    ): OperationFacts =
        OperationFacts(
            OperationDeclaration("writeThing", method, declaresIdempotencyKey = key, isConditional = conditional),
            "/v1/things",
        )

    private fun TestScope.transport(
        exchange: HttpExchange,
        credential: Credential = Credential.Publishable("pk_test_abc"),
        language: String? = null,
        retry: RetryPolicy = RetryPolicy.standard,
        onResponse: ((ResponseReport) -> Unit)? = null,
        dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler),
    ): Transport =
        Transport(
            Transport.Configuration(credential, language, BaseUrls.standard, retry, 30.seconds, onResponse),
            exchange,
            Scripted.clock(this),
            FixedKey,
            dispatcher,
        )

    private suspend fun Transport.envelope(
        facts: OperationFacts,
        headers: List<Pair<String, String>> = emptyList(),
        key: String? = null,
        ifMatch: String? = null,
        options: RequestOptions = RequestOptions(),
    ) = send(
        OperationCall(facts, facts.path, emptyList(), headers, RequestBody.Empty, key, ifMatch, options),
        JsonElement.serializer(),
        JsonObject.serializer().nullable,
    )

    @Test
    fun aWriteSendsOneKeyOnEveryAttemptAndTheErrorItRaisesCarriesIt() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    emptyList(),
                    exhausted = Scripted.Reply.Answer(503, body = unavailable),
                )
            val error = assertFailsWith<ServerException> { transport(exchange, retry = quick()).envelope(write()) }

            assertEquals(
                List(3) {
                    "6f1c2a9e-3b4d-4e5f-8a6b-7c8d9e0f1a2b"
                },
                exchange.sent.map { it.request.header("Idempotency-Key") },
            )
            assertEquals("ServerError", error.name)
            assertEquals("6f1c2a9e-3b4d-4e5f-8a6b-7c8d9e0f1a2b", error.idempotencyKey)
        }

    @Test
    fun aCeilingSettlesAtOnceAsARateLimitFailureNamingNoWait() =
        runTest {
            val ceiling =
                """{"errors":[{"message":"Seats used up.","extensions":{"code":"LIMIT_REACHED"}}],""" +
                    """"extensions":{"metadata":{"limit":"seats","allowed":5,"used":5}}}"""
            val exchange =
                Scripted.Exchange(
                    this,
                    emptyList(),
                    exhausted = Scripted.Reply.Answer(429, body = ceiling.encodeToByteArray()),
                )
            val error =
                assertFailsWith<RateLimitException> {
                    transport(exchange, retry = quick()).envelope(write(), key = "seat-9c1d")
                }

            assertEquals(1, exchange.sent.size)
            assertEquals(ApiErrorCode("LIMIT_REACHED"), error.code)
            assertNull(error.retryAfter)
            assertEquals(
                """{"limit":"seats","allowed":5,"used":5}""",
                error.metadata.toString(),
            )
            assertEquals("seat-9c1d", error.idempotencyKey)
        }

    @Test
    fun aKeyInUseIsAskedAgainASecondApartAtLeastCountedAgainstNoCeiling() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(409, mapOf("Retry-After" to "0"), keyInUse),
                        Scripted.Reply.Answer(409, body = keyInUse),
                        Scripted.Reply.Answer(201, body = empty),
                    ),
                )
            val response = transport(exchange, retry = quick(maxRetries = 0)).envelope(write(), key = "order-7f3a")

            assertEquals(201, response.status)
            assertEquals(listOf(Duration.ZERO, 1.seconds, 2.seconds), exchange.sent.map { it.at })
            assertTrue(exchange.sent.all { it.request.header("Idempotency-Key") == "order-7f3a" })
        }

    private val slowDown =
        """{"errors":[{"message":"slow down","extensions":{"code":"RATE_LIMITED"}}]}""".encodeToByteArray()

    @Test
    fun aRetryAfterDateIsWaitedInWholeSecondsRoundedUpTheWaitItsErrorNames() =
        runTest {
            // The answer arrives 0.8 s in and names 00:00:02, 1.2 s later: the wait is 2 s, never 1.2 s.
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(
                            429,
                            mapOf("Retry-After" to "Thu, 01 Jan 1970 00:00:02 GMT"),
                            slowDown,
                            800.milliseconds,
                        ),
                        Scripted.Reply.Answer(201, body = empty),
                    ),
                )
            val response = transport(exchange, retry = quick(maxRetries = 1)).envelope(write(), key = "order-7f3a")

            assertEquals(201, response.status)
            assertEquals(listOf(Duration.ZERO, 2_800.milliseconds), exchange.sent.map { it.at })

            // Where no repeat is left, the error names that same wait: answered at 2.8 s, 00:00:04 is 1.2 s off.
            val settled =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(429, mapOf("Retry-After" to "Thu, 01 Jan 1970 00:00:04 GMT"), slowDown),
                    ),
                )
            val error =
                assertFailsWith<RateLimitException> {
                    transport(settled, retry = quick(maxRetries = 0)).envelope(write(), key = "order-7f3a")
                }
            assertEquals(2_800.milliseconds, settled.sent.single().at)
            assertEquals(2.seconds, error.retryAfter)
        }

    @Test
    fun aCancellationTheExchangeThrowsEndsTheCallAsItIsAndIsNeverRepeated() =
        runTest {
            var sent = 0
            val cancelling =
                object : HttpExchange {
                    override suspend fun send(request: ExchangeRequest): ExchangeResponse {
                        sent += 1
                        throw CancellationException("The platform cancelled the request.")
                    }
                }

            val transport = transport(cancelling, retry = quick())
            val failure = assertFailsWith<CancellationException> { transport.envelope(write()) }

            assertEquals(1, sent)
            assertEquals("The platform cancelled the request.", failure.message)
        }

    @Test
    fun theHookIsToldOfEveryAttemptAnAnswerCameBackToItsReplayAndItsRequestId() =
        runTest {
            val told = mutableListOf<ResponseReport>()
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Drop(),
                        Scripted.Reply.Answer(503, mapOf("X-Request-Id" to "a1"), unavailable),
                        Scripted.Reply.Answer(
                            200,
                            mapOf("Idempotent-Replayed" to "true", "x-request-id" to "b2"),
                            empty,
                        ),
                    ),
                )
            transport(exchange, retry = quick(), onResponse = {
                told += it
                throw IllegalStateException("dropped")
            }).envelope(write())

            assertEquals(listOf(1, 2), told.map { it.attempt })
            assertEquals(listOf(503, 200), told.map { it.status })
            assertEquals(listOf("a1", "b2"), told.map { it.requestId })
            assertEquals(listOf(false, true), told.map { it.isReplayed })
            assertTrue(told.all { it.operationId == "writeThing" && it.surface == Surface.Rest })
        }

    @Test
    fun aHeaderTheClientWritesAStreamsAcceptAndABudgetOfNothingAreRefusedBeforeAnyRequest() =
        runTest {
            val exchange = Scripted.Exchange(this, emptyList())
            val transport = transport(exchange)

            assertFailsWith<ConfigurationException> {
                transport.envelope(
                    write(),
                    headers =
                        listOf("idempotency-key" to "mine"),
                )
            }
            assertFailsWith<ConfigurationException> {
                transport.envelope(
                    write(),
                    headers =
                        listOf("Accept" to "text/event-stream"),
                )
            }
            assertFailsWith<ConfigurationException> {
                transport.envelope(write(), options = RequestOptions(timeout = Duration.ZERO))
            }
            assertFailsWith<ConfigurationException> {
                transport.envelope(
                    write(conditional = true),
                    ifMatch = "\"d4\n2\"",
                )
            }
            assertEquals(0, exchange.sent.size)
        }

    /**
     * An exchange that refuses a header as OkHttp does as the attempt leaves, a name outside visible ASCII and
     * a value outside printable ASCII and the tab, counting every attempt it is handed.
     */
    private class StrictExchange : HttpExchange {
        var sent = 0
            private set

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += 1
            for ((name, value) in request.headers) {
                require(name.isNotEmpty() && name.all { it in '!'..'~' }) { "Unexpected char in header name $name" }
                require(value.all { it == '\t' || it in ' '..'~' }) { "Unexpected char in header value of $name" }
            }
            return ExchangeResponse(201, emptyMap(), ExchangeBody("""{"data":null}""".encodeToByteArray()))
        }
    }

    /**
     * A header the platform's HTTP stack cannot send fails as the attempt leaves, which the transport would read
     * as a request that never arrived and repeat: the client refuses it itself, before any request, a tag in
     * Latin-1, which the contract lets a header carry, a caller's header of the same, a name that is no token, a
     * header the stack writes itself, a language and a cookie a share token's function answers among them.
     */
    @Test
    fun aHeaderThePlatformCannotSendIsRefusedBeforeAnyRequestAndNeverRepeated() =
        runTest {
            val strict = StrictExchange()
            val transport = transport(strict, retry = quick())
            val refused =
                listOf<suspend () -> Unit>(
                    { transport.envelope(write(conditional = true), ifMatch = "\"café\"") },
                    { transport.envelope(write(), headers = listOf("X-Note" to "déjà vu")) },
                    { transport.envelope(write(), headers = listOf("X Note" to "a")) },
                    { transport.envelope(write(), headers = listOf("X-Note:" to "a")) },
                    { transport.envelope(write(), headers = listOf("Upgrade" to "websocket")) },
                    { transport(strict, language = "fr-CH, fé").envelope(write()) },
                    {
                        transport(strict, Credential.Share("share_abc") { "visit=été" })
                            .envelope(write(method = "GET", key = false))
                    },
                )
            for (call in refused) assertFailsWith<ConfigurationException> { call() }

            assertEquals(0, strict.sent, "nothing is sent, and nothing repeated")
            transport.envelope(
                write(conditional = true),
                ifMatch = "W/\"cafe\"",
                headers = listOf("X-Note" to "deja vu"),
            )
            assertEquals(1, strict.sent)
        }

    /** What every request names the SDK that made it by. */
    private val identity = "mountaya-kotlin/$CLIENT_VERSION"

    @Test
    fun everyAttemptOfACallNamesTheSdkAndTheVersionTheModulesArePublishedAt() =
        runTest {
            val exchange =
                Scripted.Exchange(this, listOf(Scripted.Reply.Drop(), Scripted.Reply.Answer(201, body = empty)))
            transport(exchange, retry = quick()).envelope(write())

            assertEquals(
                listOf(identity, identity),
                exchange.sent.map { it.request.header("X-Mountaya-Client") },
            )
        }

    @Test
    fun aDataApiCallNamesTheSdkOnEveryAttemptItsDocumentSentAgainIncluded() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(
                            200,
                            body = """{"errors":[{"message":"PersistedQueryNotFound"}]}""".encodeToByteArray(),
                        ),
                        Scripted.Reply.Answer(200, body = noRoutes),
                    ),
                )
            transport(exchange, Scripted.session).query(directions(), DataSchema.DirectionsResult.serializer())

            assertEquals(
                listOf(identity, identity),
                exchange.sent.map { it.request.header("X-Mountaya-Client") },
            )
        }

    @Test
    fun anEventStreamsOpenAndItsReopenNameTheSdk() =
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
            val followed = OperationDeclaration("followThings", "GET")
            val feed = OperationFacts(followed, "/v1/things/events", SuccessKind.EventStream)
            val call =
                OperationCall(feed, feed.path, emptyList(), emptyList(), RequestBody.Empty, options = RequestOptions())

            assertFailsWith<NotFoundException> { transport(exchange, Scripted.session).records(call).collect() }
            assertEquals(2, exchange.sent.size)
            assertEquals(
                "1-0",
                exchange.sent
                    .last()
                    .request
                    .header("Last-Event-ID"),
            )
            assertEquals(
                listOf(identity, identity),
                exchange.sent.map { it.request.header("X-Mountaya-Client") },
            )
        }

    @Test
    fun anOperationThatTakesNoCredentialIsSentNoneAndStillNamesTheSdkAndItsLanguage() =
        runTest {
            val exchange = Scripted.Exchange(this, listOf(Scripted.Reply.Answer(200, body = empty)))
            val declaration =
                OperationDeclaration(
                    "postOrganizationLookup",
                    "POST",
                    credentials = CredentialDeclaration(takesNoCredential = true),
                )
            val lookup = OperationFacts(declaration, "/v1/organizations/lookup")
            transport(exchange, language = "fr").envelope(lookup)
            val sent = exchange.sent.single().request

            assertNull(sent.header("X-API-Key"))
            assertNull(sent.header("X-Session-Token"))
            assertEquals("fr", sent.header("Accept-Language"))
            assertEquals(identity, sent.header("X-Mountaya-Client"))
        }

    /** The header is the client's, as its language is, so a caller naming it in any case is refused. */
    @Test
    fun aCallerNamingTheSdksHeaderIsRefusedBeforeAnyRequest() =
        runTest {
            val exchange = Scripted.Exchange(this, emptyList())
            val transport = transport(exchange)

            for (name in listOf("X-Mountaya-Client", "x-mountaya-client", "X-MOUNTAYA-CLIENT")) {
                val refusal =
                    assertFailsWith<ConfigurationException> {
                        transport.envelope(write(), headers = listOf(name to "mountaya-kotlin/9.9.9"))
                    }
                assertTrue(refusal.message.orEmpty().startsWith("`$name` is a header the client writes itself"), name)
            }
            assertEquals(0, exchange.sent.size)
        }

    @Test
    fun theLanguageRidesEveryRequestAndAVisitorsCookiesOnlyUnderAShareTokenThatAsks() =
        runTest {
            val exchange = Scripted.Exchange(this, emptyList(), exhausted = Scripted.Reply.Answer(200, body = empty))
            val read = write(method = "GET", key = false)
            transport(exchange, language = "fr-CH, fr;q=0.9").envelope(read)
            transport(
                exchange,
                Credential.Share("share_abc") { url -> "visit=${url.toString().substringAfter(".com")}" },
            ).envelope(read)

            assertEquals("fr-CH, fr;q=0.9", exchange.sent[0].request.header("Accept-Language"))
            assertNull(exchange.sent[0].request.header("Cookie"))
            assertEquals("visit=/v1/things", exchange.sent[1].request.header("Cookie"))
            assertNull(exchange.sent[1].request.header("X-Share-Token"))
        }

    /**
     * Every Data API query is a read over its POST, so a share token that asks for the visitor's cookies sends one
     * on the visitor's session alone, though the Data API requires a session token: the server judges the cookie.
     */
    @Test
    fun aDataApiQueryUnderAShareTokenThatAsksForCookiesGoesOutOnTheVisitorsSessionAlone() =
        runTest {
            val exchange = Scripted.Exchange(this, emptyList(), exhausted = Scripted.Reply.Answer(200, body = noRoutes))
            val visitor = Credential.Share("share_abc") { "visit=7" }

            val answered = transport(exchange, visitor).query(directions(), DataSchema.DirectionsResult.serializer())

            assertEquals(emptyList(), answered.routes)
            assertEquals(
                "POST",
                exchange.sent
                    .single()
                    .request.method,
            )
            assertEquals(
                "visit=7",
                exchange.sent
                    .single()
                    .request
                    .header("Cookie"),
            )
            assertNull(
                exchange.sent
                    .single()
                    .request
                    .header("X-Share-Token"),
            )
            assertNull(
                exchange.sent
                    .single()
                    .request
                    .header("X-API-Key"),
            )
        }

    @Test
    fun aRequestWaitsForAFirstSessionTokenItsSourceReportsOnItsWay() =
        runTest {
            val source =
                object : SessionTokenSource {
                    val state = MutableStateFlow(SessionSnapshot(isMinting = true))
                    override val snapshot: StateFlow<SessionSnapshot> = state
                }
            val exchange = Scripted.Exchange(this, listOf(Scripted.Reply.Answer(200, body = empty)))
            val transport = transport(exchange, Credential.Publishable("pk_test_abc", SessionToken.Source(source)))
            launch {
                delay(100.milliseconds)
                source.state.value = SessionSnapshot(token = "sess_minted")
            }
            transport.envelope(write(method = "GET", key = false))

            assertEquals(listOf(100.milliseconds), exchange.sent.map { it.at })
            assertEquals(
                "sess_minted",
                exchange.sent
                    .first()
                    .request
                    .header("X-Session-Token"),
            )
        }

    @Test
    fun aDataApiCallSendsItsDigestAloneFirstAndItsDocumentWhereTheServerAsks() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(
                            200,
                            body = """{"errors":[{"message":"PersistedQueryNotFound"}]}""".encodeToByteArray(),
                        ),
                        Scripted.Reply.Answer(
                            200,
                            body = """{"data":{"directions":{"routes":[]}}}""".encodeToByteArray(),
                        ),
                    ),
                )
            val transport = transport(exchange, Credential.Publishable("pk_test_abc", SessionToken.Value("sess_abc")))
            val result = transport.query(directions(), DataSchema.DirectionsResult.serializer())
            val bodies = exchange.bodies()
            val digest = persistedQueryDigest(DataOperations.directions.document)

            assertEquals(emptyList(), result.routes)
            assertEquals(
                """{"variables":{"input":{"activity":"HIKING","waypoints":[[6.86,45.83]]}},""" +
                    """"extensions":{"persistedQuery":{"version":1,"sha256Hash":"$digest"}}}""",
                exchange.sent
                    .first()
                    .request.body
                    ?.decodeToString(),
            )
            assertNull(bodies.first()["query"])
            assertEquals(
                DataOperations.directions.document,
                bodies
                    .last()["query"]
                    ?.jsonPrimitive
                    ?.content,
            )
            assertEquals(bodies.first()["variables"], bodies.last()["variables"])
            assertEquals(bodies.first()["extensions"], bodies.last()["extensions"])
        }

    /** What the Data API answers a body with no document while its persisted-query extension is off. */
    private val extensionOff =
        """{"errors":[{"message":"no operation provided","extensions":{"code":"GRAPHQL_VALIDATION_FAILED"}}]}"""
            .encodeToByteArray()

    private val noRoutes = """{"data":{"directions":{"routes":[]}}}""".encodeToByteArray()

    /** A `directions` call, its whole document and one waypoint. */
    private fun directions(): DataCall {
        val input = DataSchema.DirectionsInput(DataSchema.ActivitySlug("HIKING"), listOf(listOf(6.86, 45.83)))
        return DataCall(
            OperationTable.Data.directions,
            DataOperations.directions,
            DataOperations.directions.document,
            wireJson.encodeToString(DataSchema.DirectionsInput.serializer(), input),
            RequestOptions(),
        )
    }

    /** The JSON body of every attempt sent, in order. */
    private fun Scripted.Exchange.bodies(): List<JsonObject> =
        sent.map { attempt ->
            val text = attempt.request.body?.decodeToString() ?: ""
            wireJson.parseToJsonElement(text).jsonObject
        }

    /**
     * The extension off refuses a digest under a failing status, 422, or 400 where the answer is
     * negotiated as application/graphql-response+json, and that refusal is read before the status.
     */
    @Test
    fun aDigestRefusedUnderAFailingStatusAsTheExtensionOffIsSentAsItsDocumentAndNeverProbedAgain() =
        runTest {
            for (status in listOf(422, 400)) {
                val exchange =
                    Scripted.Exchange(
                        this,
                        listOf(
                            Scripted.Reply.Answer(status, body = extensionOff),
                            Scripted.Reply.Answer(200, body = noRoutes),
                            Scripted.Reply.Answer(200, body = noRoutes),
                        ),
                    )
                val transport = transport(exchange, Scripted.session)

                val first = transport.query(directions(), DataSchema.DirectionsResult.serializer())
                val later = transport.query(directions(), DataSchema.DirectionsResult.serializer())
                val bodies = exchange.bodies()

                assertEquals(emptyList(), first.routes)
                assertEquals(emptyList(), later.routes)
                assertEquals(3, bodies.size, "under $status")
                assertNull(bodies[0]["query"], "under $status")
                assertNotNull(bodies[0]["extensions"], "under $status")
                assertNotNull(bodies[1]["query"], "under $status")
                assertNull(bodies[1]["extensions"], "under $status")
                assertNotNull(bodies[2]["query"], "under $status")
                assertNull(bodies[2]["extensions"], "a declined protocol is not probed again, under $status")
            }
        }

    @Test
    fun anotherValidationFailureAndTheSameRefusalOfADocumentReachTheCallerUnderTheirStatus() =
        runTest {
            val other =
                """{"errors":[{"message":"Cannot query field \"conformance\" on type \"Query\".",""" +
                    """"extensions":{"code":"GRAPHQL_VALIDATION_FAILED"}}]}"""
            val refusing =
                Scripted.Exchange(this, listOf(Scripted.Reply.Answer(422, body = other.encodeToByteArray())))
            val refused =
                assertFailsWith<ApiException> {
                    transport(refusing, Scripted.session).query(directions(), DataSchema.DirectionsResult.serializer())
                }

            assertEquals(422, refused.status)
            assertEquals(ApiErrorCode("GRAPHQL_VALIDATION_FAILED"), refused.code)
            assertEquals(1, refusing.sent.size)

            // The protocol declined, the document is sent; the same answer to it is the caller's.
            val twice =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(422, body = extensionOff),
                        Scripted.Reply.Answer(422, body = extensionOff),
                    ),
                )
            val again =
                assertFailsWith<ApiException> {
                    transport(twice, Scripted.session).query(directions(), DataSchema.DirectionsResult.serializer())
                }

            assertEquals(422, again.status)
            assertEquals(2, twice.sent.size)
            assertNotNull(twice.bodies().last()["query"], "the request sends its document")
        }

    /** Runs every block on [inner], counting them, and says whether one of them is running now. */
    private class Recording(
        private val inner: CoroutineDispatcher,
    ) : CoroutineDispatcher() {
        var isRunning = false
            private set
        var dispatched = 0
            private set

        override fun dispatch(
            context: CoroutineContext,
            block: Runnable,
        ) {
            dispatched += 1
            inner.dispatch(
                context,
                Runnable {
                    isRunning = true
                    try {
                        block.run()
                    } finally {
                        isRunning = false
                    }
                },
            )
        }
    }

    /** Reads any JSON as JSON, noting for each read whether [dispatcher] was running a block as it read. */
    private class Observing(
        private val dispatcher: Recording,
    ) : KSerializer<JsonElement> {
        val seen = mutableListOf<Boolean>()

        override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

        override fun deserialize(decoder: Decoder): JsonElement {
            seen += dispatcher.isRunning
            return JsonElement.serializer().deserialize(decoder)
        }

        override fun serialize(
            encoder: Encoder,
            value: JsonElement,
        ) {
            JsonElement.serializer().serialize(encoder, value)
        }
    }

    /** An exchange answering 200 with [chunks] one at a time, noting for each read whether [dispatcher] was running a block. */
    private class Chunked(
        private val dispatcher: Recording,
        private val chunks: List<String>,
    ) : HttpExchange {
        val readOn = mutableListOf<Boolean>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val left = ArrayDeque(chunks)
            return ExchangeResponse(
                200,
                body =
                    ExchangeBody {
                        readOn += dispatcher.isRunning
                        left.removeFirstOrNull()?.encodeToByteArray()
                    },
            )
        }
    }

    /**
     * The caller's dispatcher may be the main thread: the body is read and the answer decoded on the
     * transport's own, and the caller resumes on its own once both are done.
     */
    @Test
    fun aBodyIsReadAndItsAnswerDecodedOnTheTransportsDispatcherAndNeverOnTheCallers() =
        runTest {
            val decoder = Recording(StandardTestDispatcher(testScheduler))

            val rest = Chunked(decoder, listOf("""{"data":{"name":""", """"Refuge"}}"""))
            val thing = Observing(decoder)
            val read =
                transport(rest, dispatcher = decoder).send(
                    OperationCall(
                        write(method = "GET", key = false),
                        "/v1/things",
                        emptyList(),
                        emptyList(),
                        RequestBody.Empty,
                        options = RequestOptions(),
                    ),
                    thing,
                    JsonObject.serializer().nullable,
                )

            val data = Chunked(decoder, listOf("""{"data":{"directions":""", """{"routes":[]}}}"""))
            val routes = Observing(decoder)
            val result = transport(data, Scripted.session, dispatcher = decoder).query(directions(), routes)

            assertEquals("""{"name":"Refuge"}""", read.data.toString())
            assertEquals("""{"routes":[]}""", result.toString())
            assertEquals(listOf(true, true, true), rest.readOn, "each chunk and the end are read on the dispatcher")
            assertEquals(listOf(true, true, true), data.readOn, "each chunk and the end are read on the dispatcher")
            assertEquals(listOf(true), thing.seen)
            assertEquals(listOf(true), routes.seen)
            assertEquals(4, decoder.dispatched, "one read and one decode for each call")
            assertFalse(decoder.isRunning)
        }

    /**
     * A stream collected on the main thread parses nothing there either: each chunk is framed and its records
     * decoded on the transport's dispatcher, in one hop, and each frame is handed on in the collector's coroutine,
     * which a flow's emissions must keep.
     */
    @Test
    fun aStreamsRecordsAreDecodedOnTheTransportsDispatcherOneHopAChunkAndHandedOnInTheCollectors() =
        runTest {
            val decoder = Recording(StandardTestDispatcher(testScheduler))
            val stream =
                Chunked(
                    decoder,
                    listOf(
                        "event: thing\nid: 1\ndata: {\"a\":1}\n\n",
                        "event: thing\nid: 2\ndata: {\"a\":2}\n\nevent: thing\nid: 3\ndata: {\"a\":3}\n\n",
                    ),
                )
            val thing = Observing(decoder)
            val facts =
                OperationFacts(
                    OperationDeclaration("getThingEvents", "GET"),
                    "/v1/things/events",
                    SuccessKind.EventStream,
                )
            val events =
                transport(stream, Scripted.session, dispatcher = decoder).stream(
                    OperationCall(
                        facts,
                        facts.path,
                        emptyList(),
                        emptyList(),
                        RequestBody.Empty,
                        options = RequestOptions(),
                    ),
                    thing,
                )

            val frames = events.take(3).toList()

            assertEquals(listOf("1", "2", "3"), frames.map { it.id })
            assertEquals("""{"a":2,"type":"thing"}""", frames[1].event.toString())
            assertEquals(listOf(true, true, true), thing.seen, "each record decoded on the dispatcher")
            assertEquals(2, decoder.dispatched, "one hop for each chunk")
            assertFalse(decoder.isRunning)
        }

    @Test
    fun aSuccessWhoseBodyDoesNotDecodeIsTheBaseApiExceptionTheDecodersFailureBeneathIt() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(Scripted.Reply.Answer(200, body = "not json".encodeToByteArray())),
                )
            val error =
                assertFailsWith<MountayaException> { transport(exchange).envelope(write(method = "GET", key = false)) }

            assertIs<ApiException>(error)
            assertEquals("ApiError", error.name)
            assertEquals(200, error.status)
            assertIs<SerializationException>(error.cause)
        }

    /** The failure a success answering [body] raises where it is read with [serializer]. */
    private suspend fun TestScope.undecoded(
        body: String,
        serializer: KSerializer<*>,
    ): ApiException {
        val exchange = Scripted.Exchange(this, listOf(Scripted.Reply.Answer(200, body = body.encodeToByteArray())))
        val facts = write(method = "GET", key = false)
        return assertFailsWith<ApiException> {
            transport(exchange).send(
                OperationCall(
                    facts,
                    facts.path,
                    emptyList(),
                    emptyList(),
                    RequestBody.Empty,
                    options = RequestOptions(),
                ),
                serializer,
                JsonObject.serializer().nullable,
            )
        }
    }

    /**
     * Fails where a message, a print or a stack trace of [error], or of any failure beneath it, holds [secret] or a
     * piece of it, its first or its last 16 characters: a decoder that copies a window of its input copies a piece.
     */
    private fun assertCarriesNone(
        secret: String,
        error: Throwable,
    ) {
        val pieces = listOf(secret, secret.take(16), secret.takeLast(16))
        var link: Throwable? = error
        while (link != null) {
            val name = link::class.simpleName
            for (piece in pieces) {
                assertFalse(piece in link.message.orEmpty(), "$name's message: ${link.message}")
                assertFalse(piece in link.toString(), "$name's print")
                assertFalse(piece in link.stackTraceToString(), "$name's stack trace")
            }
            link = link.cause
        }
    }

    /**
     * kotlinx.serialization copies the whole of an input under 200 characters into a decoding failure's message, and
     * the 120 characters around the failure otherwise: a field of the wrong type beside a secret must not carry the
     * secret into the cause a log prints, whether the answer is a session's or a share link's. The admin unit's own,
     * a setup intent's and a webhook endpoint's, are held to the same in `mountaya-admin`.
     */
    @Test
    fun aSuccessThatDoesNotDecodeCarriesNoByteOfItsBodyBeneathItsSecretsIncluded() =
        runTest {
            val token = "sess_test_4f9a2c71d8e3"
            val session =
                undecoded(
                    """{"data":{"expires_at":"2026-11-23T10:05:00Z","user_id":["u1"],"token":"$token"}}""",
                    RestSchema.Session.serializer(),
                )
            assertIs<SerializationException>(session.cause)
            assertTrue("user_id" in session.cause?.message.orEmpty(), "where the body did not decode stays said")
            assertCarriesNone(token, session)

            val shareToken = "share_6a7b9c1d2e3f4a5b6c7d8e9f"
            val link = """{"data":[{"created_at":["2026-11-14T09:12:44Z"],"share_token":"$shareToken"}]}"""
            assertCarriesNone(shareToken, undecoded(link, RestSchema.ShareLink.serializer()))

            // Over 200 characters, the decoder copies the 60 around the failure: the secret's first half follows it.
            val longer =
                """{"data":{"note":"${"a link a visitor opens to read the itinerary it was made for, ".repeat(3)}",""" +
                    """"created_at":7,"share_token":"$shareToken"}}"""
            assertTrue(longer.length > 200)
            assertCarriesNone(shareToken, undecoded(longer, RestSchema.ShareLink.serializer()))
        }

    @Test
    fun aFileComesBackWithTheContentTypeTheServerChose() =
        runTest {
            val exchange =
                Scripted.Exchange(
                    this,
                    listOf(
                        Scripted.Reply.Answer(
                            200,
                            mapOf("Content-Type" to "application/gpx+xml"),
                            "<gpx/>".encodeToByteArray(),
                        ),
                    ),
                )
            val facts = write(method = "GET", key = false)
            val file =
                transport(exchange).download(
                    OperationCall(
                        facts,
                        facts.path,
                        emptyList(),
                        emptyList(),
                        RequestBody.Empty,
                        options = RequestOptions(),
                    ),
                )

            assertEquals("application/gpx+xml", file.contentType)
            assertContentEquals("<gpx/>".encodeToByteArray(), file.bytes)
        }
}
