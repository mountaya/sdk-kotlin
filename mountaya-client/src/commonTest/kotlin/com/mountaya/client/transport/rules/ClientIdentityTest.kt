package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClientIdentityTest {
    @Test
    fun aRequestNamesTheSdkByItsLanguageAndTheVersionTheModulesArePublishedAt() {
        assertEquals("X-Mountaya-Client", CLIENT_HEADER)
        assertEquals("mountaya-kotlin/$CLIENT_VERSION", ClientIdentity.value)
    }

    /** The version is the SDK's own, never the contract tag, which starts with a `v`. */
    @Test
    fun theVersionIsTheSdksOwnAndAHeaderCarriesIt() {
        assertTrue(Regex("""^\d+\.\d+\.\d+([-+][0-9A-Za-z.+-]+)?$""").matches(CLIENT_VERSION), CLIENT_VERSION)
        assertTrue(HeaderValue.isCarried(ClientIdentity.value))
    }
}
