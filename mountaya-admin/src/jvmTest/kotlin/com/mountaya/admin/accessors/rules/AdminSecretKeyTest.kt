package com.mountaya.admin

import com.mountaya.admin.generated.AdminSchema
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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A server mints a session token with its secret key through the admin unit, `mountaya.admin.sessions.postSession`:
 * the secret key is the core's credential, declared on the jvm alone, and the mint is the admin unit's operation.
 */
class AdminSecretKeyTest {
    @Test
    fun aServerMintsASessionTokenWithItsSecretKeyThroughTheAdminUnit() =
        runTest {
            val sent = mutableListOf<ExchangeRequest>()
            val session = """{"expires_at":"2026-11-23T10:05:00Z","user_id":null,"token":"sess_test_4f9a"}"""
            val exchange =
                object : HttpExchange {
                    override suspend fun send(request: ExchangeRequest): ExchangeResponse {
                        sent += request
                        return ExchangeResponse(
                            200,
                            mapOf("Content-Type" to "application/json"),
                            ExchangeBody("""{"data":$session}""".encodeToByteArray()),
                        )
                    }
                }
            val server =
                Mountaya(
                    Credential(secretKey = "sk_test_8e9f"),
                    exchange = exchange,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            val minted =
                server.admin.sessions.postSession(
                    AdminSchema.SessionCreateParams(publishable_key = "pk_test_0a1b"),
                )

            val request = sent.single()
            assertEquals("POST", request.method)
            assertEquals(Url("https://api.mountaya.com/v1/sessions"), request.url)
            assertEquals("sk_test_8e9f", request.header("X-API-Key"))
            assertNull(request.header("X-Session-Token"))
            assertEquals("""{"publishable_key":"pk_test_0a1b"}""", request.body?.decodeToString())
            // What the mint answers is the core's `Session`, which a session engine and a credential take as it is.
            val answered: RestSchema.Session = minted.data
            assertEquals("sess_test_4f9a", answered.token)
            assertEquals("sess_test_4f9a", SessionToken.Minted(answered).let { it.session.token })

            // The secret key authenticates the mint alone, and the core refuses it for any other admin operation.
            assertFailsWith<ConfigurationException> { server.admin.users.getMyPreferences() }
            assertTrue(sent.size == 1, "a refused call sends nothing")
            server.close()
        }
}
