package com.mountaya.geo

import com.mountaya.geo.generated.ElementTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HexColorTest {
    @Test
    fun aColourGivenNoOpacityIsOpaqueAndTwoColoursAreOneWhereBothPartsAre() {
        val opaque = HexColor("#1c7ed6")
        val veiled = HexColor("#1c7ed6", opacity = 0.12)

        assertEquals(1.0, opaque.opacity)
        assertEquals(HexColor("#1c7ed6", opacity = 1.0), opaque)
        assertEquals(HexColor("#1c7ed6", opacity = 1.0).hashCode(), opaque.hashCode())
        assertNotEquals(veiled, opaque)
        assertEquals(2, setOf(opaque, veiled, HexColor("#1c7ed6")).size)
    }

    @Test
    fun thePrintedTokensWriteEveryColourAsSixLowercaseDigitsOpaqueOrBetween0And1() {
        val standard = ElementTokens.standard
        val colors =
            ElementTokens.colorTokens.values.flatMap { token ->
                listOfNotNull(token.get(standard).light, token.get(standard).dark)
            }

        assertTrue(colors.size >= 25, "${colors.size} colours")
        for (color in colors) {
            assertTrue(Regex("#[0-9a-f]{6}").matches(color.hex), color.hex)
            assertTrue(color.opacity > 0 && color.opacity <= 1, color.toString())
        }
    }
}
