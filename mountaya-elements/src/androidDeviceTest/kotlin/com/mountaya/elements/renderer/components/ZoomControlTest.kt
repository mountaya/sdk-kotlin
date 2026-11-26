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
import com.mountaya.geo.ZoomStep
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoomControlTest {
    @get:Rule
    val compose = createComposeRule()

    private val live = color("#212529")
    private val dimmed = color("#adb5bd")

    @Test
    fun eachHalfDrivesTheMapUnlessAHandlerStandsInForItsDefaultHalfByHalf() {
        val map = Controls.engine()
        var clicks = 0
        var handled by mutableStateOf(false)
        compose.setContent { if (handled) ZoomControl(map, onZoomInClick = { clicks += 1 }) else ZoomControl(map) }

        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.runOnIdle { assertEquals(13.0, map.state.value.camera.zoom) }
        compose.onNodeWithContentDescription("Zoom out").performClick()
        compose.runOnIdle { assertEquals(12.0, map.state.value.camera.zoom) }

        handled = true
        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(12.0, map.state.value.camera.zoom, "a handler stands in for the map's step")
        }
        compose.onNodeWithContentDescription("Zoom out").performClick()
        compose.runOnIdle { assertEquals(11.0, map.state.value.camera.zoom, "the other half still drives the map") }
    }

    @Test
    fun eachHalfFollowsTheMapsZoomWindowWhoeverAnswersItsPress() {
        val map = Controls.engine()

        compose.runOnUiThread {
            assertEquals(emptyList(), disabledZoomSteps(map.state.value))
            map.setZoomBounds(ZoomLimit.Zoom(4.0), ZoomLimit.Zoom(12.0))
            assertEquals(listOf(ZoomStep.ZoomIn), disabledZoomSteps(map.state.value))
            map.jump(CameraTarget(zoom = 4.0))
            assertEquals(listOf(ZoomStep.ZoomOut), disabledZoomSteps(map.state.value))
            map.setZoomBounds(ZoomLimit.Zoom(4.0), ZoomLimit.Zoom(4.0))
            assertEquals(listOf(ZoomStep.ZoomIn, ZoomStep.ZoomOut), disabledZoomSteps(map.state.value))
            assertEquals(emptyList(), disabledZoomSteps(null))
        }
    }

    @Test
    fun aHalfWithNeitherAMapNorAHandlerIsInertAndOneAtItsWindowsEndDisabled() {
        val map = Controls.engine()
        val ceiling = Controls.engine()
        compose.runOnUiThread { ceiling.setZoomBounds(ZoomLimit.Zoom(0.0), ZoomLimit.Zoom(12.0)) }
        compose.setContent {
            Column {
                Drawn("live") { ZoomControl(map) }
                Drawn("inert") { ZoomControl(null) }
                Drawn("handled") { ZoomControl(null, onZoomInClick = {}) }
                Drawn("ceiling") { ZoomControl(ceiling) }
                Drawn("shut") { ZoomControl(map, enabled = false) }
            }
        }

        compose.onNodeWithTag("live").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(96.dp)
        val drawn = listOf("live", "inert", "handled", "ceiling", "shut").associateWith { compose.image(it) }

        fun at(
            tag: String,
            y: Double,
        ) = compose.pixel(drawn.getValue(tag), 24.0, y)
        assertTrue(at("live", 24.0).isNear(live))
        assertTrue(at("live", 72.0).isNear(live))
        assertTrue(at("inert", 24.0).isNear(dimmed))
        assertTrue(at("inert", 72.0).isNear(dimmed))
        assertTrue(at("handled", 24.0).isNear(live))
        assertTrue(at("handled", 72.0).isNear(dimmed))
        assertTrue(at("ceiling", 24.0).isNear(dimmed))
        assertTrue(at("ceiling", 72.0).isNear(live))
        assertTrue(at("shut", 72.0).isNear(dimmed))
    }
}
