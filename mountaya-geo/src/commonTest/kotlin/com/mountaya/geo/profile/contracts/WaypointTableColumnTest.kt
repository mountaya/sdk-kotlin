package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class WaypointTableColumnTest {
    @Test
    fun eachColumnIsSpelledAsTheContractSpellsIt() {
        assertEquals(
            listOf("waypoint", "profile", "distance", "elevation", "altitude", "amenities", "cutoff", "properties"),
            WaypointTableColumn.entries.map { it.id },
        )
    }
}
