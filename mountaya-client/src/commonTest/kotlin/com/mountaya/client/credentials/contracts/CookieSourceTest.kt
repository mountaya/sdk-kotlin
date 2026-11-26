package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CookieSourceTest {
    @Test
    fun aSourceAnswersTheCookieForTheAddressAskedOrNone() {
        val source: CookieSource = { url ->
            if (url.toString().startsWith("https://api.mountaya.com")) "visit=1" else null
        }

        assertEquals("visit=1", source(Url("https://api.mountaya.com/v1/things")))
        assertNull(source(Url("https://tiles.mountaya.com/v1/zones")))
    }
}
