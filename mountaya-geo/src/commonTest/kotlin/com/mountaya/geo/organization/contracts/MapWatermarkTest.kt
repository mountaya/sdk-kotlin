package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MapWatermarkTest {
    private val drawing = GlyphDrawing(listOf(0.0, 0.0, 2.0, 1.0), emptyList())

    private fun mark(link: String = "https://www.mountaya.com/en/") =
        MapWatermark(drawing, -4.0, -4.0, 180.0, 90.0, link, "watermark.label")

    @Test
    fun twoAreEqualWhereEveryMemberIs() {
        assertEquals(mark(), mark())
        assertEquals(mark().hashCode(), mark().hashCode())
        assertNotEquals(mark(), mark("https://www.mountaya.com/"))
        assertNotEquals(
            mark(),
            MapWatermark(drawing, -0.0, -4.0, 180.0, 90.0, "https://www.mountaya.com/en/", "watermark.label"),
        )
    }
}
