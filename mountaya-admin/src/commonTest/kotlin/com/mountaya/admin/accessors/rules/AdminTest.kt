package com.mountaya.admin

import com.mountaya.admin.generated.AdminSchema
import com.mountaya.client.ApiException
import com.mountaya.client.ConfigurationException
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * What `mountaya.admin` reaches: the admin unit's operations, each sent through the client's own transport, under its
 * credential, its language and its error classes, and answered in the admin unit's types beside the core's.
 */
class AdminTest {
    /** An exchange that answers every attempt with [status] and [body], and keeps every request it was sent. */
    private class Answering(
        private val status: Int,
        private val body: String,
    ) : HttpExchange {
        val sent = mutableListOf<ExchangeRequest>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += request
            return ExchangeResponse(
                status,
                mapOf("Content-Type" to "application/json", "X-Request-Id" to "0123456789abcdef0123456789abcdef"),
                ExchangeBody(body.encodeToByteArray()),
            )
        }
    }

    /** A client over [exchange], under a publishable key and a session token, on the test's own time. */
    private fun TestScope.client(exchange: HttpExchange): Mountaya =
        Mountaya(
            Credential.Publishable("pk_test_0a1b", SessionToken.Value("sess_test_2c3d")),
            language = "fr",
            exchange = exchange,
            dispatcher = StandardTestDispatcher(testScheduler),
        )

    @Test
    fun anAdminOperationLeavesThroughTheClientsTransportAndIsAnsweredInTheAdminUnitsTypes() =
        runTest {
            val key =
                """{"created_at":"2026-11-14T09:12:44Z","etag":"\"1\"","id":"d5a83e17-92c6-4b0f-a7d3-1e6482f9c05b",""" +
                    """"key":"pk_test_0a1b","name":"Site","organization_id":"0b8c6f1e-2d3a-4b5c-8d9e-0f1a2b3c4d5e",""" +
                    """"scopes":["tiles"]}"""
            val page = """{"pagination":{"limit":50,"next_cursor":null}}"""
            val exchange = Answering(200, """{"data":[$key],"extensions":{"metadata":$page}}""")
            val mountaya = client(exchange)

            val keys = mountaya.admin.keys.getOrganizationPublishableKeys("org/1")

            val sent = exchange.sent.single()
            assertEquals("GET", sent.method)
            assertEquals(Url("https://api.mountaya.com/v1/organizations/org%2F1/publishable-keys"), sent.url)
            assertEquals("pk_test_0a1b", sent.header("X-API-Key"))
            assertEquals("sess_test_2c3d", sent.header("X-Session-Token"))
            assertEquals("fr", sent.header("Accept-Language"))
            val listed: List<AdminSchema.PublishableKey> = keys.data
            assertEquals(listOf(AdminSchema.PublishableKeyScope.Tiles), listed.single().scopes)
            assertEquals("0123456789abcdef0123456789abcdef", keys.requestId)
            // A listing's metadata is the core's `ListMetadata`, which a walk of the core reads.
            val metadata: RestSchema.ListMetadata = keys.metadata
            assertEquals(null, metadata.pagination.next_cursor)
            mountaya.close()
        }

    /**
     * A secret key authenticates `postSession` alone, and the core refuses it before any request leaves for every
     * other operation, the admin unit's included, as it refuses a publishable key for the mint.
     */
    @Test
    fun theCoresCredentialRulesHoldEveryAdminOperation() =
        runTest {
            val exchange = Answering(200, """{"data":{}}""")
            val mountaya = client(exchange)

            assertFailsWith<ConfigurationException> {
                mountaya.admin.sessions.postSession(AdminSchema.SessionCreateParams(publishable_key = "pk_test_0a1b"))
            }
            assertTrue(exchange.sent.isEmpty(), "a refused call sends nothing")
            mountaya.close()
        }

    /**
     * kotlinx.serialization copies the whole of an input under 200 characters into a decoding failure's message, and
     * the 60 characters around the failure otherwise: a field of the wrong type beside a secret the admin unit's
     * types hold, a setup intent's or a webhook endpoint's signing secret, carries none of it into the failure the
     * core's transport raises, as a session's and a share link's carry none (`TransportTest`).
     */
    @Test
    fun anAdminAnswerThatDoesNotDecodeCarriesNoByteOfItsSecrets() =
        runTest {
            val clientSecret = "seti_1Q2w3E4r5T6y_secret_9f8e7d6c5b4a"
            val setup = Answering(200, """{"data":[{"client_secret":"$clientSecret"}]}""")
            val refused =
                assertFailsWith<ApiException> {
                    client(
                        setup,
                    ).admin.billing.postOrganizationBillingPaymentMethodSetup("org")
                }
            assertCarriesNone(clientSecret, refused)

            val signingSecret = "whsk_8f3c2a71b845d0e4c7a6b3f180d92e5a"
            val endpoint =
                """{"data":{"id":"we_8b1d","organization_id":"d5a83e17-92c6-4b0f-a7d3-1e6482f9c05b",""" +
                    """"url":"https://hooks.example.com/mountaya","enabled_events":["zone.updated"],""" +
                    """"status":"enabled","etag":"\"3\"","created_at":"2026-11-14T09:12:44Z",""" +
                    """"updated_at":"2026-11-14T09:12:44Z","disabled_at":7,"secret":"$signingSecret"}}"""
            assertTrue(endpoint.length > 200)
            val created =
                assertFailsWith<ApiException> {
                    client(Answering(200, endpoint)).admin.webhooks.postOrganizationWebhookEndpoint(
                        "org",
                        AdminSchema.WebhookEndpointCreateParams(
                            Url("https://hooks.example.com/mountaya"),
                            listOf("zone.updated"),
                        ),
                    )
                }
            assertCarriesNone(signingSecret, created)
        }

    /**
     * A body only the admin unit sends is its own component, and a component the core holds too is the core's, named
     * as the core names it: the acting user's preferences hold the core's unit of length, which the geo layer reads.
     */
    @Test
    fun anAdminBodyHoldsTheCoresComponentsAsTheCoreNamesThem() =
        runTest {
            val stored =
                """{"language":"fr","theme":"auto","length":"imperial","pace":"per-time","slope":"degrees",""" +
                    """"temperature":"celsius"}"""
            val exchange = Answering(200, """{"data":$stored}""")

            val preferences =
                client(exchange).admin.users.patchMyPreferences(
                    AdminSchema.PreferencesUpdateParams { length = RestSchema.UnitLength.Imperial },
                )

            val sent = exchange.sent.single()
            assertEquals("PATCH", sent.method)
            assertEquals("""{"length":"imperial"}""", sent.body?.decodeToString())
            val length: RestSchema.UnitLength = preferences.data.length
            assertEquals(RestSchema.UnitLength.Imperial, length)
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
}
