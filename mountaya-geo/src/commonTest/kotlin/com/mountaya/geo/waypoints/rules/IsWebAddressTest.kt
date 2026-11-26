package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsWebAddressTest {
    private fun assertAccepted(vararg addresses: String) {
        for (address in addresses) assertTrue(isWebAddress(address), "`$address` is accepted")
    }

    private fun assertRefused(vararg addresses: String) {
        for (address in addresses) assertFalse(isWebAddress(address), "`$address` is refused")
    }

    @Test
    fun anAbsoluteHttpOrHttpsAddressWithAHostIsAccepted() {
        assertAccepted(
            "https://refuge.example/topo",
            "http://refuge.example/reserver?jour=12",
            "https://marie:secret@refuge.example:8443/",
            "https://[2001:db8::1]/",
            "https://refuge.example/carte du refuge.pdf",
            "https://refuge.example/?remise=50%",
            "https://refuge.example/caf%C3%A9",
            "https://refuge.example",
            "https://caf%C3%A9.example/",
            "https://192.168.1.10:80/",
            "https://[::ffff:192.0.2.1]/",
            "https://refuge.example:/",
        )
    }

    @Test
    fun anyOtherSchemeOrShapeIsRefused() {
        assertRefused(
            "javascript:alert(1)",
            " javascript:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "file:///etc/passwd",
            "ask at the bar",
            "",
            "refuge.example",
            "/reserver",
            "Https://refuge.example",
            "https:refuge.example",
            "https:/refuge.example",
            "https:///refuge.example",
            "https://\\refuge.example",
            "https://",
            "https://?jour=12",
            "  https://refuge.example/topo ",
        )
    }

    @Test
    fun aCharacterTheApisParserRefusesIsRefused() {
        assertRefused(
            "https://refu ge.example",
            "https://refuge.example/a\tb",
            "https://refuge.example/a\u0001b",
            "https://refuge.example/a\u007fb",
            "https://refuge.example/topo\n",
            "https://refuge.example/sale-50%-off",
            "https://refuge.example/#%",
            "https://refu%67e.example/",
        )
    }

    /** What a URL parser refuses in a host the shape lets through. */
    @Test
    fun aHostAURLParserCannotReadIsRefused() {
        assertRefused(
            "https://:8443/",
            "https://user@/",
            "https://refuge.example:99999/",
            "https://refuge.example:8a/",
            "https://[2001:db8::1/",
            "https://[2001:db8:::1]/",
            "https://[1:2:3:4:5:6:7:8:9]/",
            "https://[::1]x/",
            "https://ref<uge.example/",
            "https://ref|uge.example/",
            "https://%C3%C3.example/",
            "https://999.1.1.1/",
            "https://1.2.3.4.5/",
            "https://refuge.123/",
        )
    }
}
