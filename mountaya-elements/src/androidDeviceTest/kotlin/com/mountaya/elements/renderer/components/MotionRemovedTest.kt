package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.mountaya.geo.HexColor
import com.mountaya.geo.SearchRow
import com.mountaya.geo.SearchRowPayload
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

/**
 * A reader who turned animations off, Android's animator duration scale at 0, as Compose reads it for a composition: the
 * scale its effects carry, which a test states as Compose's own test rule lets it.
 */
class MotionRemovedTest {
    /** The scale Compose reads where the reader removed animations. */
    private object Removed : MotionDurationScale {
        override val scaleFactor: Float = 0f
    }

    @get:Rule
    val compose = createComposeRule(Removed)

    @Test
    fun aCompositionWhoseScaleIsZeroReadsMotionRemoved() {
        var removed: Boolean? = null
        compose.setContent { removed = motionRemoved() }

        compose.runOnIdle { assertEquals(true, removed) }
    }

    @Test
    fun aZoomStepAndACompassResetMoveTheCameraAtOnce() {
        val map = Controls.engine()
        val surface = compose.runOnUiThread { ScriptedMapSurface(map) }
        compose.setContent {
            Column {
                ZoomControl(map)
                CompassControl(map)
            }
        }

        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.onNodeWithContentDescription("Reset bearing to north").performClick()

        compose.runOnIdle {
            assertEquals(listOf<CameraMotion>(CameraMotion.Jump, CameraMotion.Jump), surface.moves.map { it.motion })
            assertEquals(13.0, map.state.value.camera.zoom, "the step lands where its ease would")
        }
    }

    @Test
    fun aLocateFramesItsFixAtOnce() {
        val located = Controls.Located(GeolocationFix.Located(listOf(6.86, 45.83), 50.0), GeolocationPermission.Granted)
        val map = Controls.engine(geolocation = located)
        val surface = compose.runOnUiThread { ScriptedMapSurface(map) }
        compose.setContent { GeolocateControl(map) }

        compose.onNodeWithContentDescription("Show my location").performClick()

        compose.waitUntil(TIMEOUT) { compose.runOnUiThread { surface.frames.isNotEmpty() } }
        compose.runOnIdle {
            assertEquals(
                Duration.ZERO,
                surface.frames
                    .single()
                    .fit.duration,
            )
        }
    }

    @Test
    fun aPickFitsOrJumpsOntoItsRowAtOnceAndEasesOrFliesOtherwise() {
        val map = Controls.engine()
        val surface = compose.runOnUiThread { ScriptedMapSurface(map) }
        val accent = HexColor("#e9501e")
        val place =
            SearchRow(
                "row:place",
                "Lac Blanc",
                emptyList(),
                null,
                emptyList(),
                SearchRowPayload.Place(listOf(6.9, 45.9), null),
            )
        val tour =
            SearchRow(
                "row:tour",
                "Tour",
                emptyList(),
                null,
                emptyList(),
                SearchRowPayload.Itinerary("t", listOf(6.8, 45.8, 6.9, 45.9), null),
            )

        compose.runOnUiThread {
            searchPick(place, map, accent, null, jumps = true)
            searchPick(tour, map, accent, null, jumps = true)
            searchPick(place, map, accent, null)
            searchPick(tour, map, accent, null)
        }

        compose.runOnIdle {
            assertEquals(listOf<CameraMotion>(CameraMotion.Jump, CameraMotion.Fly), surface.moves.map { it.motion })
            assertEquals(listOf(CameraFit(duration = Duration.ZERO), CameraFit()), surface.frames.map { it.fit })
        }
    }

    private companion object {
        /** How long a test waits for a fix the control asks for on the main thread. */
        const val TIMEOUT = 5_000L
    }
}
