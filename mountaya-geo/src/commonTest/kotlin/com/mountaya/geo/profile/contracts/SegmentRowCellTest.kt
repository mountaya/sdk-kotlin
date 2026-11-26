package com.mountaya.geo.profile.contracts

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.AltitudeRange
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.SegmentRowCell
import com.mountaya.geo.SegmentTableColumn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SegmentRowCellTest {
    @Test
    fun aCellNamesItsColumn() {
        val cells: List<SegmentRowCell> =
            listOf(
                SegmentRowCell.Segment("Col"),
                SegmentRowCell.Profile(ProfileRange(0.0, 1.0), null),
                SegmentRowCell.Distance(null, null),
                SegmentRowCell.Elevation(null, null, null),
                SegmentRowCell.Altitude(null, null),
                SegmentRowCell.Duration(null),
                SegmentRowCell.Difficulty(null),
            )

        assertEquals(SegmentTableColumn.entries.toList(), cells.map { it.column })
    }

    @Test
    fun twoCellsAreEqualWhereTheyStateTheSameInOneColumn() {
        assertEquals<SegmentRowCell>(
            SegmentRowCell.Distance(8_200.0, 24_300.0),
            SegmentRowCell.Distance(8_200.0, 24_300.0),
        )
        assertNotEquals<SegmentRowCell>(
            SegmentRowCell.Distance(8_200.0, 24_300.0),
            SegmentRowCell.Distance(8_200.0, null),
        )
        assertNotEquals<SegmentRowCell>(
            SegmentRowCell.Altitude(null, 1.0),
            SegmentRowCell.Altitude(AltitudeRange(1.0, 2.0), null),
        )
        assertEquals<SegmentRowCell>(
            SegmentRowCell.Difficulty(RestSchema.TrailDifficultyType("T3")),
            SegmentRowCell.Difficulty(RestSchema.TrailDifficultyType("T3")),
        )
        assertNotEquals<SegmentRowCell>(
            SegmentRowCell.Difficulty(RestSchema.TrailDifficultyType("a")),
            SegmentRowCell.Segment("a"),
        )
        assertEquals(SegmentRowCell.Duration(3_600.0).hashCode(), SegmentRowCell.Duration(3_600.0).hashCode())
    }
}
