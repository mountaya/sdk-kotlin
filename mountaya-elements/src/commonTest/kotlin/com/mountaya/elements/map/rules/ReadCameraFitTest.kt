package com.mountaya.elements

import com.mountaya.geo.PixelPadding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ReadCameraFitTest {
    @Test
    fun aFitIsAnsweredAsStatedZerosAndACeilingPastTheWindowIncluded() {
        for (fit in listOf(
            CameraFit(),
            CameraFit(padding = PixelPadding(48.0), duration = Duration.ZERO),
            CameraFit(padding = PixelPadding(top = 24.0, right = 24.0, bottom = 120.0, left = 24.0), maxZoom = 15.0),
            CameraFit(padding = PixelPadding(0.0), duration = Duration.ZERO),
            CameraFit(maxZoom = 25.0),
        )) {
            assertEquals(fit, readCameraFit(fit))
        }
    }

    @Test
    fun aCeilingThatIsNoFiniteNumberANegativeDurationOrPaddingAreRefusedByName() {
        assertEquals(
            MapException(MapException.Kind.NumberRefused, "maxZoom"),
            assertFailsWith<MapException> {
                readCameraFit(
                    CameraFit(maxZoom = Double.NaN, duration = (-1).milliseconds),
                )
            },
        )
        assertEquals(
            MapException(MapException.Kind.NumberRefused, "duration"),
            assertFailsWith<MapException> {
                readCameraFit(CameraFit(padding = PixelPadding(-1.0), duration = (-250).milliseconds))
            },
        )
        for (padding in listOf(
            PixelPadding(-1.0),
            PixelPadding(top = 0.0, right = 0.0, bottom = Double.NaN, left = 0.0),
            PixelPadding(top = Double.POSITIVE_INFINITY, right = 0.0, bottom = 0.0, left = 0.0),
        )) {
            assertEquals(
                MapException(MapException.Kind.NumberRefused, "padding"),
                assertFailsWith<MapException> { readCameraFit(CameraFit(padding = padding)) },
                "$padding",
            )
        }
    }

    /**
     * A Kotlin `Duration` holds an endless one, which a Swift `Duration` cannot, so the rule refuses it
     * where the contract's endless duration is refused.
     */
    @Test
    fun anEndlessDurationIsRefusedByName() {
        assertEquals(
            MapException(MapException.Kind.NumberRefused, "duration"),
            assertFailsWith<MapException> { readCameraFit(CameraFit(duration = Duration.INFINITE)) },
        )
        assertEquals(
            MapException(MapException.Kind.NumberRefused, "duration"),
            assertFailsWith<MapException> { readCameraFit(CameraFit(duration = -Duration.INFINITE)) },
        )
    }
}
