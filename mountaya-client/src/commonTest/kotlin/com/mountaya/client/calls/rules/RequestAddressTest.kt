package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class RequestAddressTest {
    @Test
    fun thePathFollowsTheOriginItsOwnPrefixIncluded() {
        assertEquals(
            "https://api.mountaya.com/v1/things",
            RequestAddress.url(Url("https://api.mountaya.com/"), "/v1/things", emptyList()).toString(),
        )
        assertEquals(
            "https://proxy.test/mountaya/v1/things",
            RequestAddress.url(Url("https://proxy.test/mountaya"), "v1/things", emptyList()).toString(),
        )
    }

    @Test
    fun aQueryNameAndValueKeepOnlyTheUnreservedCharactersAsTheyAre() {
        val url =
            RequestAddress.url(
                Url("https://api.mountaya.com"),
                "/v1/things",
                listOf(
                    "q" to "a+b&c=d é",
                    "sort" to "-name",
                ),
            )

        assertEquals("https://api.mountaya.com/v1/things?q=a%2Bb%26c%3Dd%20%C3%A9&sort=-name", url.toString())
    }
}
