package com.mountaya.elements

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The CSS colour reader, into Compose's colours on the device. */
class CssColorTest {
    private fun components(color: Color?): List<Int>? =
        color?.toArgb()?.let { listOf(it shr 16 and 0xff, it shr 8 and 0xff, it and 0xff, it ushr 24) }

    @Test
    fun aCssHexadecimalColourOfThreeSixOrEightDigitsInEitherCase() {
        assertEquals(listOf(224, 49, 49, 255), components(cssColor("#e03131")))
        assertEquals(listOf(224, 49, 49, 255), components(cssColor("#E03131")))
        assertEquals(listOf(255, 255, 255, 255), components(cssColor("#fff")))
        assertEquals(listOf(0, 0, 0, 128), components(cssColor("#00000080")))
        assertEquals(listOf(255, 255, 255, 128), components(cssColor("#ffffff", opacity = 0.5)))
    }

    @Test
    fun anythingElseIsNoColour() {
        for (written in listOf("e03131", "#e0313", "#e031311", "#ggg", "red", "", "#", "#１23")) {
            assertNull(cssColor(written), written)
        }
    }
}
