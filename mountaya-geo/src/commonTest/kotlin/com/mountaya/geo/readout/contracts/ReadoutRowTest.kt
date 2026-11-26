package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ReadoutRowTest {
    @Test
    fun twoRowsAreEqualWhereEveryMemberIs() {
        val altitude = ReadoutRow.Terrain.Reading(ReadoutRow.Terrain.Reading.Kind.Altitude, 120.0, "readout.altitude")

        assertEquals(ReadoutRow.Terrain(listOf(altitude)), ReadoutRow.Terrain(listOf(altitude)))
        assertNotEquals<ReadoutRow>(ReadoutRow.ZoomIn("readout.zoomInForDetails"), ReadoutRow.Terrain(listOf(altitude)))
        assertEquals(
            ReadoutRow.Coordinates("45.9237°N 6.8652°E", "45.92370, 6.86520", "readout.coordinates").hashCode(),
            ReadoutRow.Coordinates("45.9237°N 6.8652°E", "45.92370, 6.86520", "readout.coordinates").hashCode(),
        )
        assertNotEquals(
            ReadoutRow.Zone.Window(null, null, "readout.zoneWindowFrom"),
            ReadoutRow.Zone.Window(null, null, "readout.zoneWindowUntil"),
        )
    }
}
