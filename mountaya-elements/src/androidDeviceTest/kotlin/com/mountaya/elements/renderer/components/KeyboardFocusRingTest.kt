package com.mountaya.elements

import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * A control draws a ring 2 dp wide in the `focus-ring` token while it holds the keyboard's focus, and none where a touch
 * pressed it: Compose's input mode tells the two apart, as the web's `:focus-visible` does.
 *
 * A keyboard's first key takes the device out of touch mode, which a key Compose's test injects into the window never
 * does, and a window in touch mode hands no key to a view that holds no focus: each test presses Tab as a keyboard does,
 * through the platform's input, and gives the device back in touch mode after it, as the next test expects it.
 */
class KeyboardFocusRingTest {
    @get:Rule
    val compose = createComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @AfterTest
    fun backInTouchMode() {
        instrumentation.setInTouchMode(true)
    }

    /** Presses Tab as a keyboard does, through the platform's input, which takes the device out of touch mode. */
    private fun pressTab() {
        compose.waitForIdle()
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB)
        compose.waitForIdle()
    }

    @Composable
    private fun Button() {
        ControlChip {
            ControlButton("Zoom in", "See more detail.", onClick = {}) { Box(Modifier.size(20.dp)) }
        }
    }

    @Test
    fun aRingIsDrawnUnderTheKeyboardsFocusAloneNeverATouchs() {
        assertTrue(showsFocusRing(isFocused = true, inputMode = InputMode.Keyboard))
        assertFalse(showsFocusRing(isFocused = true, inputMode = InputMode.Touch))
        assertFalse(showsFocusRing(isFocused = false, inputMode = InputMode.Keyboard))
        assertTrue(focusRingWidth == 2.dp)
    }

    @Test
    fun aControlTheKeyboardFocusedIsRingedInTheFocusRingToken() {
        compose.setContent { Drawn("light") { Button() } }

        pressTab()
        compose.onNodeWithContentDescription("Zoom in").assertIsFocused()
        val ringed = compose.image("light")

        assertTrue(
            compose.pixel(ringed, 1.0, 24.0).isNear(color("#1c7ed6")),
            "the ring stands inside the control's edge",
        )
        assertTrue(
            compose.pixel(ringed, 6.0, 24.0).isNear(color("#ffffff")),
            "the ring is 2 dp wide, the ground beyond",
        )
    }

    @Test
    fun aControlATouchPressedDrawsNoRing() {
        compose.setContent { Drawn("light") { Button() } }

        compose.onNodeWithContentDescription("Zoom in").performClick()
        val pressed = compose.image("light")

        assertTrue(compose.pixel(pressed, 1.0, 24.0).isNear(color("#ffffff")))
    }

    @Test
    fun aConsumersAccentLeavesTheRingAsTheTableStatesIt() {
        val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
        compose.setContent {
            Drawn("dark", dark = true) { CompositionLocalProvider(LocalMountayaTokens provides tokens) { Button() } }
        }

        pressTab()
        assertTrue(compose.pixel(compose.image("dark"), 1.0, 24.0).isNear(color("#1c7ed6")))
    }
}
