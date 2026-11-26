package com.mountaya.geo

import com.mountaya.geo.generated.ElementTokens
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTokenValueTest {
    @Test
    fun aTokenReadsItsLightValueUnderTheLightSchemeAndItsDarkOneUnderTheDark() {
        val standard = ElementTokens.standard

        assertEquals(HexColor("#ffffff"), themeTokenValue(standard.controlBg, ThemeScheme.Light))
        assertEquals(HexColor("#25262b"), themeTokenValue(standard.controlBg, ThemeScheme.Dark))
        // The table states no dark accent, so the dark scheme reads the light one.
        assertEquals(HexColor("#1c7ed6"), themeTokenValue(standard.accent, ThemeScheme.Dark))
        assertEquals(HexColor("#000000", 0.55), themeTokenValue(standard.profileNavigatorVeil, ThemeScheme.Dark))
        assertEquals(HexColor("#ffffff", 0.62), themeTokenValue(standard.profileNavigatorVeil, ThemeScheme.Light))
        assertEquals(HexColor("#e9501e", 0.12), themeTokenValue(standard.profileLitFill, ThemeScheme.Dark))
        assertEquals(listOf(3.0, 3.0), themeTokenValue(standard.profileCursorDash, ThemeScheme.Dark))
        assertEquals(
            listOf(HexColor("#000000", 0.05), HexColor("#000000", 0.1)),
            themeTokenValue(standard.controlShadow, ThemeScheme.Dark),
        )
    }

    @Test
    fun aConsumersValueStandsInBothSchemesAndLeavesEveryOtherTokenTheTables() {
        val tokens =
            ElementTokens.standard.copy {
                controlBg = ThemeToken(light = HexColor("#000000"))
                tooltipBg = ThemeToken(light = HexColor("#000000", 0.8))
                profileCursorDash = ThemeToken(light = listOf(4.0, 2.0))
            }

        assertEquals(HexColor("#000000"), themeTokenValue(tokens.controlBg, ThemeScheme.Light))
        assertEquals(HexColor("#000000"), themeTokenValue(tokens.controlBg, ThemeScheme.Dark))
        assertEquals(HexColor("#000000", 0.8), themeTokenValue(tokens.tooltipBg, ThemeScheme.Dark))
        assertEquals(listOf(4.0, 2.0), themeTokenValue(tokens.profileCursorDash, ThemeScheme.Dark))
        assertEquals(HexColor("#25262b"), themeTokenValue(tokens.panelBg, ThemeScheme.Dark))
    }

    @Test
    fun aConsumerMayStateItsOwnValueUnderEachScheme() {
        val token = ThemeToken(light = HexColor("#ffffff"), dark = HexColor("#101010"))

        assertEquals(HexColor("#ffffff"), themeTokenValue(token, ThemeScheme.Light))
        assertEquals(HexColor("#101010"), themeTokenValue(token, ThemeScheme.Dark))
    }
}
