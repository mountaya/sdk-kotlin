package com.mountaya.elements

import com.mountaya.geo.PixelPadding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class CameraFitTest {
    @Test
    fun aFitStatingNothingFramesWithNoPaddingTheWindowsCeilingAndTheRenderersPace() {
        val fit = CameraFit()

        assertNull(fit.padding)
        assertNull(fit.maxZoom)
        assertNull(fit.duration)
    }

    @Test
    fun twoFitsAreEqualWhereTheyStateTheSameMembersEachTheSameNumber() {
        assertEquals(CameraFit(maxZoom = Double.NaN), CameraFit(maxZoom = Double.NaN))
        assertEquals(CameraFit(maxZoom = Double.NaN).hashCode(), CameraFit(maxZoom = Double.NaN).hashCode())
        assertNotEquals(CameraFit(maxZoom = 15.0), CameraFit())
        assertNotEquals(CameraFit(padding = PixelPadding(0.0)), CameraFit(), "a padding of 0 is stated")
        assertEquals(CameraFit(padding = PixelPadding(48.0)), CameraFit(padding = PixelPadding(48.0)))
        assertNotEquals(CameraFit(duration = Duration.ZERO), CameraFit(duration = 1.milliseconds))
    }
}
