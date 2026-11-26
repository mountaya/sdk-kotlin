package com.mountaya.geo

import com.mountaya.geo.generated.ElementTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ThemeTokenTest {
    @Test
    fun aTokenStatesItsLightValueAndItsDarkOneOnlyWhereItHasOneOfItsOwn() {
        val standard = ElementTokens.standard

        assertNull(ThemeToken(light = 3).dark)
        assertEquals(HexColor("#1c7ed6"), standard.accent.light)
        assertNull(standard.accent.dark)
        assertEquals(HexColor("#ffffff"), standard.controlBg.light)
        assertEquals(HexColor("#25262b"), standard.controlBg.dark)
        assertEquals(listOf(3.0, 3.0), standard.profileCursorDash.light)
        assertEquals(ThemeToken(light = 3, dark = 4), ThemeToken(light = 3, dark = 4))
        assertNotEquals(ThemeToken(light = 3), ThemeToken(light = 3, dark = 3))
    }

    @Test
    fun aConsumerRestylesASubsetOnACopyOfTheStandardTokensTheRestLeftAsTheyWere() {
        val tokens =
            ElementTokens.standard.copy {
                accent = ThemeToken(light = HexColor("#e9501e"))
                controlBg = ThemeToken(light = controlBg.light, dark = HexColor("#000000"))
            }

        assertNotEquals(ElementTokens.standard, tokens)
        assertEquals(HexColor("#e9501e"), tokens.accent.light)
        assertEquals(ElementTokens.standard.controlBg.light, tokens.controlBg.light)
        assertEquals(HexColor("#000000"), tokens.controlBg.dark)
        assertEquals(ElementTokens.standard.panelBg, tokens.panelBg)
        assertEquals(HexColor("#1c7ed6"), ElementTokens.standard.accent.light)
        assertEquals(ElementTokens.standard, ElementTokens.standard.copy {})
        assertEquals(ElementTokens.standard.hashCode(), ElementTokens.standard.copy {}.hashCode())
    }
}
