package com.mountaya.geo

import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CameraForBoundsTest {
    /** The zoom window a GL JS map is built with when it is given none. */
    private val world = ZoomBounds(-2.0, 22.0)

    /** A valley, 0.15 degrees wide and 0.1 high. */
    private val valley = listOf(6.8, 45.8, 6.95, 45.9)

    /** A hut's surroundings, a few hundred meters across. */
    private val hut = listOf(6.864, 45.828, 6.866, 45.8295)

    private val canvas = listOf(800.0, 600.0)

    private fun assertNear(
        expected: Double,
        actual: Double?,
        what: String,
    ) {
        assertTrue(actual != null && abs(expected - actual) <= 1e-9, "$what: $actual, where $expected is the rule's")
    }

    @Test
    fun theWorkedExampleTheHeightBindsAndTheCentreIsTheBoxsProjectedMiddle() {
        val fitted = cameraForBounds(valley, canvas, world, PixelPadding(48.0), maxZoom = 15.0)

        // The height binds: 504 pixels over the projected 0.1 degrees of latitude at 45.85.
        assertNear(11.269337026503, fitted?.zoom, "zoom")
        assertNear(6.875, fitted?.center?.get(0), "longitude")
        // The projected middle lies north of the arithmetic one, where a degree is drawn taller.
        assertNear(45.850022473736, fitted?.center?.get(1), "latitude")
        // No padding frames closer.
        assertNear(11.520875793499, cameraForBounds(valley, canvas, world)?.zoom, "unpadded zoom")
    }

    @Test
    fun theFitsCeilingHoldsFirstThenTheWindowWhoseCeilingHoldsUnderAHigherOne() {
        assertEquals(15.0, cameraForBounds(hut, canvas, world, PixelPadding(20.0), maxZoom = 15.0)?.zoom)
        assertEquals(
            16.0,
            cameraForBounds(hut, canvas, ZoomBounds(-2.0, 16.0), PixelPadding(10.0), maxZoom = 20.0)?.zoom,
        )
        // A box of one point fits at every zoom, so it is framed at the ceiling, on its point.
        val point = cameraForBounds(listOf(6.86, 45.83, 6.86, 45.83), canvas, ZoomBounds(-2.0, 18.0))
        assertEquals(18.0, point?.zoom)
        assertNear(6.86, point?.center?.get(0), "longitude")
        assertNear(45.83, point?.center?.get(1), "latitude")
    }

    @Test
    fun theWindowsFloorHoldsABoxTooWide() {
        val fitted = cameraForBounds(listOf(-10.0, 35.0, 30.0, 60.0), listOf(400.0, 300.0), ZoomBounds(6.0, 22.0))

        assertEquals(6.0, fitted?.zoom)
        assertNear(10.0, fitted?.center?.get(0), "longitude")
        assertNear(49.040931781425, fitted?.center?.get(1), "latitude")
    }

    @Test
    fun unevenPaddingMovesTheCentreByHalfTheDifferenceAtTheZoomTheCameraLandsAt() {
        val fitted =
            cameraForBounds(
                valley,
                listOf(1000.0, 600.0),
                world,
                PixelPadding(top = 20.0, right = 20.0, bottom = 200.0, left = 300.0),
                maxZoom = 16.0,
            )
        assertNear(10.861912711334, fitted?.zoom, "zoom")
        // West of the middle, for the wider inset on the left, and south of it, for the bottom's.
        assertNear(6.822106897827, fitted?.center?.get(0), "longitude")
        assertNear(45.826333220768, fitted?.center?.get(1), "latitude")

        // At the window's floor the offset is the floor's, not the zoom the box would fit at.
        val held =
            cameraForBounds(
                listOf(-10.0, 35.0, 30.0, 60.0),
                listOf(400.0, 300.0),
                ZoomBounds(6.0, 22.0),
                PixelPadding(top = 10.0, right = 20.0, bottom = 90.0, left = 120.0),
            )
        assertEquals(6.0, held?.zoom)
        assertNear(9.45068359375, held?.center?.get(0), "longitude at the floor")
        assertNear(48.752026937686, held?.center?.get(1), "latitude at the floor")

        // At the window's ceiling, under a higher ceiling of the fit's own, the same.
        val capped =
            cameraForBounds(
                hut,
                canvas,
                ZoomBounds(-2.0, 16.0),
                PixelPadding(top = 20.0, right = 100.0, bottom = 160.0, left = 20.0),
                maxZoom = 20.0,
            )
        assertEquals(16.0, capped?.zoom)
        assertNear(6.865429153442, capped?.center?.get(0), "longitude at the ceiling")
        assertNear(45.828226688918, capped?.center?.get(1), "latitude at the ceiling")
    }

    /**
     * The corner a renderer's own fit lands elsewhere in, at another padding than the fixture's: it
     * measures the offset at the zoom before the window holds it. The rule measures it at the zoom the
     * camera lands at, 40 pixels there.
     */
    @Test
    fun theOffsetIsMeasuredAtTheZoomTheWindowHoldsTheCameraAtNeverTheOneTheBoxAskedFor() {
        val padding = PixelPadding(top = 20.0, right = 20.0, bottom = 80.0, left = 100.0)
        val floored =
            cameraForBounds(listOf(-10.0, 35.0, 30.0, 60.0), listOf(400.0, 300.0), ZoomBounds(6.0, 22.0), padding)
        val capped = cameraForBounds(hut, canvas, ZoomBounds(-2.0, 16.0), PixelPadding(10.0, 10.0, 50.0, 90.0), 20.0)

        assertEquals(6.0, floored?.zoom)
        // Forty pixels west at zoom 6, on a world 512 times 2^6 pixels wide: 0.439453125 degrees.
        assertNear(10.0 - 40.0 / (512.0 * 2.0.pow(6)) * 360.0, floored?.center?.get(0), "longitude at the floor")
        assertNear(48.824409702077, floored?.center?.get(1), "latitude at the floor")
        assertEquals(16.0, capped?.zoom)
        assertNear(6.865 - 40.0 / (512.0 * 2.0.pow(16)) * 360.0, capped?.center?.get(0), "longitude at the ceiling")
        assertNear(45.828600486659, capped?.center?.get(1), "latitude at the ceiling")
    }

    @Test
    fun aBoxAcrossTheAntimeridianIsCentredPast180AndASouthernBoxOnItsMiddle() {
        val across = cameraForBounds(listOf(179.8, -17.0, 180.1, -16.0), canvas, world)
        assertNear(179.95, across?.center?.get(0), "longitude")
        assertNear(-16.500646259183, across?.center?.get(1), "latitude")
        assertNear(8.659981770471, across?.zoom, "zoom")

        val south = cameraForBounds(listOf(-70.2, -33.6, -70.0, -33.3), listOf(640.0, 480.0), world, PixelPadding(40.0))
        assertNear(-33.450129715122, south?.center?.get(1), "latitude")
        assertNear(9.611426075196, south?.zoom, "zoom")
    }

    @Test
    fun aPaddingThatLeavesNoRoomOrABoxOrCanvasOfTheWrongLengthFramesNothing() {
        assertNull(cameraForBounds(valley, canvas, world, PixelPadding(300.0)))
        assertNull(
            cameraForBounds(valley, canvas, world, PixelPadding(top = 0.0, right = 400.0, bottom = 0.0, left = 400.0)),
        )
        assertNull(cameraForBounds(valley, canvas, world, PixelPadding(Double.NaN)))
        assertNull(cameraForBounds(listOf(6.8, 45.8, 6.95), canvas, world))
        assertNull(cameraForBounds(valley, listOf(800.0), world))
        assertNull(cameraForBounds(valley, listOf(0.0, 600.0), world))
        assertNull(cameraForBounds(valley, listOf(800.0, Double.NaN), world))
    }

    @Test
    fun anEdgeThatIsNoNumberIsKeptUnknown() {
        val fitted =
            assertNotNull(cameraForBounds(listOf(Double.NaN, 45.8, 6.95, 45.9), canvas, world), "the finite bounds fit")

        assertTrue(fitted.zoom.isNaN(), "$fitted")
        assertTrue(fitted.center[0].isNaN(), "$fitted")
    }
}
