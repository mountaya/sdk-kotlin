package com.mountaya.geo.profile.contracts

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.WaypointRowCell
import com.mountaya.geo.WaypointTableColumn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WaypointRowCellTest {
    @Test
    fun aCellNamesItsColumn() {
        val cells: List<WaypointRowCell> =
            listOf(
                WaypointRowCell.Waypoint("hut", null),
                WaypointRowCell.Profile(ProfileRange(0.0, 1.0)),
                WaypointRowCell.Distance(1.0),
                WaypointRowCell.Elevation(null, null),
                WaypointRowCell.Altitude(null),
                WaypointRowCell.Amenities(emptyList()),
                WaypointRowCell.Cutoff(null),
                WaypointRowCell.Properties(emptyList()),
            )

        assertEquals(WaypointTableColumn.entries.toList(), cells.map { it.column })
    }

    @Test
    fun twoCellsAreEqualWhereTheyStateTheSameInOneColumn() {
        assertEquals<WaypointRowCell>(WaypointRowCell.Waypoint("hut", null), WaypointRowCell.Waypoint("hut", null))
        assertNotEquals<WaypointRowCell>(WaypointRowCell.Waypoint("hut", "Hut"), WaypointRowCell.Waypoint("hut", null))
        assertNotEquals<WaypointRowCell>(
            WaypointRowCell.Amenities(listOf(RestSchema.WaypointAmenity("food"))),
            WaypointRowCell.Amenities(listOf(RestSchema.WaypointAmenity("water"))),
        )
        assertEquals(WaypointRowCell.Cutoff("12:30").hashCode(), WaypointRowCell.Cutoff("12:30").hashCode())
    }
}
