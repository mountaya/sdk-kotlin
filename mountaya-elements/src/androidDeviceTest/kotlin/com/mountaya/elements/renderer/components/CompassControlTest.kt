package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.CameraPose
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompassControlTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aPressTurnsTheMapBackToNorthAndLevelUnlessAHandlerStandsInForIt() {
        val map = Controls.engine(camera = CameraPose(listOf(6.86, 45.83), 12.0, 40.0, 90.0))
        var clicks = 0
        var handled by mutableStateOf(true)
        compose.setContent { if (handled) CompassControl(map, onClick = { clicks += 1 }) else CompassControl(map) }

        compose.onNodeWithContentDescription("Reset bearing to north").performClick()
        compose.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(90.0, map.state.value.camera.bearing)
        }
        handled = false
        compose.onNodeWithContentDescription("Reset bearing to north").performClick()
        compose.runOnIdle {
            assertEquals(0.0, map.state.value.camera.bearing)
            assertEquals(0.0, map.state.value.camera.pitch)
        }
    }

    @Test
    fun theRoseTurnsAgainstTheCamerasBearingAndPointsUpWithNoMap() {
        val map = Controls.engine(camera = CameraPose(listOf(6.86, 45.83), 12.0, 0.0, 180.0))
        compose.setContent {
            Column {
                Drawn("turned") { CompassControl(map) }
                Drawn("upright") { CompassControl(null) }
            }
        }

        compose.onNodeWithTag("turned").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        val needle = color("#e9501e")
        // The rose is 34 dp across in the middle of the button: its needle's tip is 7 dp from the top of the rose's
        // square pointing up, and as far from the bottom turned around.
        assertTrue(compose.pixel(compose.image("upright"), 24.0, 16.5).isNear(needle))
        assertTrue(compose.pixel(compose.image("turned"), 24.0, 31.5).isNear(needle))
    }
}
