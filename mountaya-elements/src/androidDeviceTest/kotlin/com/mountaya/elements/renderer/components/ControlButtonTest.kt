package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ControlButtonTest {
    @get:Rule
    val compose = createComposeRule()

    /** A control on its chip, a block filling its face. */
    @Composable
    private fun Button(
        enabled: Boolean = true,
        isBusy: Boolean = false,
        isHeld: Boolean = false,
    ) {
        ControlChip {
            ControlButton("Zoom in", "See more detail.", onClick = {
            }, enabled = enabled, isBusy = isBusy, isHeld = isHeld) { color ->
                Box(Modifier.size(20.dp).background(color))
            }
        }
    }

    @Test
    fun aFaceIsDrawnInTheControlsColourTheAccentWhileBusyDimmedWhileDisabled() {
        assertEquals(ElementTokens::controlFg, controlFaceToken(isEnabled = true, isBusy = false))
        assertEquals(ElementTokens::accent, controlFaceToken(isEnabled = true, isBusy = true))
        assertEquals(ElementTokens::controlFgDisabled, controlFaceToken(isEnabled = false, isBusy = true))
        assertNull(controlGroundToken(isHovered = false, isHeld = false))
        assertEquals(ElementTokens::controlBgHover, controlGroundToken(isHovered = true, isHeld = false))
        assertEquals(ElementTokens::controlBgHover, controlGroundToken(isHovered = false, isHeld = true))
    }

    @Test
    fun aControlIsDrawnInTheTokensUnderTheSchemeInForceTheTouchTargetAcross() {
        compose.setContent {
            Column {
                Drawn("light") { Button() }
                Drawn("dark", dark = true) { Button() }
            }
        }

        compose.onNodeWithTag("light").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        val light = compose.image("light")
        val dark = compose.image("dark")
        assertTrue(compose.pixel(light, 24.0, 24.0).isNear(color("#212529")))
        assertTrue(compose.pixel(light, 6.0, 24.0).isNear(color("#ffffff")))
        assertTrue(compose.pixel(dark, 24.0, 24.0).isNear(color("#c1c2c5")))
        assertTrue(compose.pixel(dark, 6.0, 24.0).isNear(color("#25262b")))
    }

    @Test
    fun aBusyFaceIsTheAccentADisabledOneDimmedAHeldOneOnTheHoverGround() {
        compose.setContent {
            Column {
                Drawn("busy") { Button(isBusy = true) }
                Drawn("disabled") { Button(enabled = false, isBusy = true) }
                Drawn("held") { Button(isHeld = true) }
                Drawn("shut") { CompositionLocalProvider(LocalMountayaControlsEnabled provides false) { Button() } }
            }
        }

        assertTrue(compose.pixel(compose.image("busy"), 24.0, 24.0).isNear(color("#1c7ed6")))
        assertTrue(compose.pixel(compose.image("disabled"), 24.0, 24.0).isNear(color("#adb5bd")))
        assertTrue(compose.pixel(compose.image("held"), 6.0, 24.0).isNear(color("#f1f3f5")))
        assertTrue(
            compose.pixel(compose.image("shut"), 24.0, 24.0).isNear(color("#adb5bd")),
            "a stack shut shuts its controls",
        )
    }

    @Test
    fun aConsumersTokensAreTheOnesDrawnAValueItStatesStandingInBothSchemes() {
        val tokens =
            ElementTokens.standard.copy {
                controlBg = ThemeToken(light = HexColor("#000000"))
                controlFg = ThemeToken(light = HexColor("#e9501e"))
            }
        compose.setContent {
            Drawn("dark", dark = true) { CompositionLocalProvider(LocalMountayaTokens provides tokens) { Button() } }
        }

        val dark = compose.image("dark")
        assertTrue(compose.pixel(dark, 24.0, 24.0).isNear(color("#e9501e")))
        assertTrue(compose.pixel(dark, 6.0, 24.0).isNear(color("#000000")))
    }
}
