package com.mountaya.elements

import com.mountaya.geo.GlyphSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DescribeMarkerPinTest {
    private val set =
        assertNotNull(
            GlyphSet.of(
                "held",
                GlyphSet.Paint.Stroke,
                markup =
                    mapOf(
                        "droplet" to "<path d=\"M0 0h1\"/>",
                        "hut" to "<path d=\"M0 0h1\"/>",
                    ),
            ),
            "the set is built",
        )

    @Test
    fun noColourAndNoGlyphIsADiscInTheAccentRingedInWhiteWithADot() {
        assertEquals(
            MarkerPin(28.0, null, "#ffffff", 3.0, MarkerPin.Centre.Dot(8.0, "#ffffff")),
            describeMarkerPin(null, null, set),
        )
    }

    @Test
    fun aGlyphTheSetDrawsStandsAtTheCentreOnTheColourNamed() {
        val pin = describeMarkerPin("#123456", "hut", set)
        assertEquals("#123456", pin.fill)
        assertEquals<MarkerPin.Centre>(MarkerPin.Centre.Glyph("hut", 16.0, "#ffffff"), pin.centre)
    }

    @Test
    fun aGlyphTheSetCannotDrawGivesWayToTheDotAGlyphAloneStandsOnTheAccent() {
        assertEquals<MarkerPin.Centre>(
            MarkerPin.Centre.Dot(8.0, "#ffffff"),
            describeMarkerPin("#123456", "castle", set).centre,
        )
        assertNull(describeMarkerPin(null, "droplet", set).fill)
        assertEquals<MarkerPin.Centre>(
            MarkerPin.Centre.Glyph("droplet", 16.0, "#ffffff"),
            describeMarkerPin(null, "droplet", set).centre,
        )
        assertEquals("#e8590c", describeMarkerPin("#e8590c", null, set).fill)
    }
}
