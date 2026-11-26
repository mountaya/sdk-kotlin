package com.mountaya.elements

import com.mountaya.geo.ElevationPoint
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.lineBetween
import kotlin.test.Test
import kotlin.test.assertEquals

class LegTargetTest {
    private val points =
        (0..4).map { index ->
            ElevationPoint(index * 250.0, 1000.0 + index, listOf(6.0, 45.0 + index * 0.00225), index)
        }
    private val series = ElevationSeries(points, 1000.0, 1000.0, 1004.0)

    /** A leg's row pins the marker at the sample nearest its end, the earlier of two as near, and lights the leg. */
    @Test
    fun aLegPinsTheSampleNearestItsEndAndLightsItsStretch() {
        assertEquals(MapTarget(points[2].lngLat, lineBetween(points, 0.0, 400.0)), legTarget(series, 0.0, 400.0))
        assertEquals(MapTarget(points[2].lngLat, lineBetween(points, 0.0, 625.0)), legTarget(series, 0.0, 625.0))
    }

    /** A place's row pins the marker at the sample nearest its pass and lights nothing. */
    @Test
    fun aPassPinsTheSampleNearestItAndLightsNothing() {
        assertEquals(MapTarget(points[3].lngLat), passTarget(series, 700.0))
        assertEquals(MapTarget(), passTarget(ElevationSeries(emptyList(), 0.0, null, null), 700.0))
    }
}
