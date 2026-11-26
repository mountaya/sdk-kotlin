package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SecretKeyCredentialTest {
    @Test
    fun aSecretKeyIsSentAloneAndRefusedBesideAnyOtherPart() {
        assertEquals(Credential.Secret("sk_test_abc"), Credential(secretKey = "sk_test_abc"))
        assertFailsWith<ConfigurationException> {
            Credential(publishableKey = "pk_test_abc", secretKey = "sk_test_abc")
        }
        assertFailsWith<ConfigurationException> { Credential(shareToken = "share_abc", secretKey = "sk_test_abc") }
        assertFailsWith<ConfigurationException> {
            Credential(sessionToken = SessionToken.Value("sess_abc"), secretKey = "sk_test_abc")
        }
    }

    @Test
    fun aSecretKeyThatIsMissingOrHasTheWrongPrefixIsRefused() {
        assertFailsWith<ConfigurationException> { Credential(secretKey = "pk_test_abc") }
        assertFailsWith<ConfigurationException> { Credential(secretKey = "") }
        assertFailsWith<ConfigurationException> { Credential(secretKey = "sk_test\r\n") }
    }

    @Test
    fun withoutASecretKeyTheOverloadIsThePlainFactory() {
        val plain = Credential(publishableKey = "pk_test_abc", secretKey = null)

        assertEquals(Credential.Publishable("pk_test_abc"), plain)
        assertFailsWith<ConfigurationException> { Credential(secretKey = null) }
    }

    @Test
    fun aPositionalKeyIsStillThePublishableKeyOnTheJvm() {
        assertEquals(Credential.Publishable("pk_test_abc"), Credential("pk_test_abc"))
    }

    @Test
    fun aSecretKeyAuthenticatesNoRouteAMapFetchesAndIsRefusedThereAlone() {
        val client = Mountaya(Credential(secretKey = "sk_test_abc"))

        assertFailsWith<ConfigurationException> {
            client.mapRequestHeaders(Url("https://tiles.mountaya.com/v1/zones/1/2/3"))
        }
        assertNull(client.mapRequestHeaders(Url("https://example.com/v1/zones/1/2/3")))
        client.close()
    }
}
