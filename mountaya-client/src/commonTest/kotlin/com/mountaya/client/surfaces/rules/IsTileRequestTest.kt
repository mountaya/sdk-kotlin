package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsTileRequestTest {
    private fun claims(
        request: String,
        base: String = "https://tiles.mountaya.com",
    ): Boolean = isTileRequest(Url(request), Url(base))

    @Test
    fun aRequestToTheTileApisOriginIsOneAStyleASpriteAndAGlyphAsATileIs() {
        assertTrue(claims("https://tiles.mountaya.com/v1/zones/12/2093/1425"))
        assertTrue(claims("https://tiles.mountaya.com/v1/terrains/outdoor/light/style"))
        assertTrue(claims("https://tiles.mountaya.com/v1/glyphs/Inter%20Regular/0-255"))
        assertTrue(claims("https://tiles.mountaya.com"))
        assertTrue(claims("https://tiles.mountaya.com/v1/zones/1/2/3?share_token=share_a#top"))
    }

    @Test
    fun theHostsCaseADefaultPortAndDotSegmentsAreNothing() {
        assertTrue(claims("https://TILES.Mountaya.com/v1/zones/1/2/3"))
        assertTrue(claims("https://tiles.mountaya.com:443/v1/zones/1/2/3"))
        assertTrue(claims("HTTPS://tiles.mountaya.com/v1/zones/1/2/3"))
        assertTrue(claims("https://tiles.example.com/tiles/x/../v1/zones", base = "https://tiles.example.com/tiles"))
        assertTrue(claims("https://tiles.example.com/./tiles/v1", base = "https://tiles.example.com/tiles"))
        assertFalse(claims("https://tiles.example.com/tiles/../private", base = "https://tiles.example.com/tiles"))
    }

    @Test
    fun anotherHostSchemeOrPortIsNotOneAHostTheOriginsNameBeginsIncluded() {
        assertFalse(claims("https://tiles.mountaya.com.example.net/v1/zones/1/2/3"))
        assertFalse(claims("https://example.com/v1/zones/1/2/3"))
        assertFalse(claims("http://tiles.mountaya.com/v1/zones/1/2/3"))
        assertFalse(claims("https://tiles.mountaya.com:8443/v1/zones/1/2/3"))
        assertFalse(claims("file:///v1/zones/1/2/3"))
        assertFalse(claims("asset://tiles.mountaya.com/v1/zones"))
        assertFalse(claims("https://tiles.mountaya.com:port/v1/zones"))
        assertFalse(claims("mailto:tiles@tiles.mountaya.com"))
    }

    @Test
    fun aRequestNamingAUserOrAPasswordIsNeverOne() {
        assertFalse(claims("https://user@tiles.mountaya.com/v1/zones/1/2/3"))
        assertFalse(claims("https://user:secret@tiles.mountaya.com/v1/zones/1/2/3"))
        assertFalse(claims("https://tiles.mountaya.com@example.com/v1/zones/1/2/3"))
    }

    @Test
    fun underABaseWithAPathThePathIsClaimedAtASegmentBoundaryATrailingSlashNothing() {
        val base = "https://example.com/tiles/"
        assertTrue(claims("https://example.com/tiles", base = base))
        assertTrue(claims("https://example.com/tiles/v1/zones/1/2/3", base = base))
        assertFalse(claims("https://example.com/tiles-private/v1/zones", base = base))
        assertFalse(claims("https://example.com/anything-else", base = base))
        assertTrue(claims("http://localhost:8080/v1/zones/1/2/3", base = "http://localhost:8080"))
        assertFalse(claims("http://localhost:8081/v1/zones/1/2/3", base = "http://localhost:8080"))
        assertTrue(claims("http://[::1]:8080/v1/zones", base = "http://[::1]:8080"))
        assertFalse(claims("http://[::1]:8081/v1/zones", base = "http://[::1]:8080"))
    }
}
