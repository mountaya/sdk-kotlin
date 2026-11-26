package com.mountaya.geo

import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileLengthTest {
    @Test
    fun aGeometrysLengthIsWhatItsSeriesWalksJumpsCostingNothing() {
        val two =
            GeoJsonMultiLineString(
                listOf(
                    listOf(listOf(6.0, 45.0), listOf(6.0, 45.005)),
                    listOf(listOf(7.0, 46.0), listOf(7.0, 46.005, 1_000.0)),
                ),
            )

        assertEquals(buildElevationSeries(two).totalDistance, profileLength(two), 1e-9)
        assertEquals(2 * 555.9746332230782, profileLength(two), 1e-6)
        assertEquals(0.0, profileLength(null))
        assertEquals(0.0, profileLength(GeoJsonPoint(listOf(6.0, 45.0))))
    }
}
