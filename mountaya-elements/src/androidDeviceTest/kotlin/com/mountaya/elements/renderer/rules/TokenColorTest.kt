package com.mountaya.elements

import androidx.compose.ui.graphics.Color
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenColorTest {
    /** A colour's red, green, blue and alpha, from 0 to 255. */
    private fun channels(color: Color): List<Int> =
        listOf(color.red, color.green, color.blue, color.alpha).map {
            (
                it *
                    255
            ).roundToInt()
        }

    @Test
    fun aTokenReadsItsLightValueUnderTheLightSchemeAndItsDarkOneUnderTheDark() {
        val standard = ElementTokens.standard

        assertEquals(listOf(255, 255, 255, 255), channels(tokenColor(standard.controlBg, ThemeScheme.Light)))
        assertEquals(listOf(0x25, 0x26, 0x2b, 255), channels(tokenColor(standard.controlBg, ThemeScheme.Dark)))
        assertEquals(listOf(0x1c, 0x7e, 0xd6, 255), channels(tokenColor(standard.accent, ThemeScheme.Dark)))
        assertEquals(listOf(0, 0, 0, 140), channels(tokenColor(standard.profileNavigatorVeil, ThemeScheme.Dark)))
    }

    @Test
    fun aConsumersValueStandsInBothSchemesAndOneThatIsNoColourIsTransparent() {
        assertEquals(
            listOf(0, 0, 0, 204),
            channels(tokenColor(ThemeToken(light = HexColor("#000000", 0.8)), ThemeScheme.Dark)),
        )
        assertEquals(0, channels(tokenColor(ThemeToken(light = HexColor("teal")), ThemeScheme.Light))[3])
    }

    @Test
    fun theDarkPlatformDrawsTheDarkTokensAndAnyOtherTheLight() {
        assertEquals(ThemeScheme.Dark, themeScheme(isDark = true))
        assertEquals(ThemeScheme.Light, themeScheme(isDark = false))
    }
}
