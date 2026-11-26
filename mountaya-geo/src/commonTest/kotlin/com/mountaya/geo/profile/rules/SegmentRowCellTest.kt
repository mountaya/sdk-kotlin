package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentRowCellTest {
    private val t3 = RestSchema.TrailDifficultyType("T3")

    private fun row(
        color: String? = "#e8590c",
        min: Double? = 1_035.0,
        max: Double? = 1_850.0,
    ): ItinerarySegmentRow =
        ItinerarySegmentRow(
            key = "s3",
            id = "s3",
            name = "Col de la Seigne",
            color = color,
            index = 2,
            from = 16_100.0,
            to = 24_300.0,
            metrics =
                ItinerarySegmentRowMetrics(
                    distance = 24_300.0,
                    sectionDistance = 8_200.0,
                    ascent = 1_531.0,
                    descent = 1_408.0,
                    sectionAscent = 595.0,
                    sectionDescent = 34.0,
                    altitudeMin = min,
                    altitudeMax = max,
                    duration = 32_400.0,
                    difficulty = t3,
                ),
        )

    @Test
    fun theDefaultColumnsAndEveryColumnOfALeg() {
        assertEquals(
            listOf(
                SegmentTableColumn.Segment,
                SegmentTableColumn.Profile,
                SegmentTableColumn.Distance,
                SegmentTableColumn.Elevation,
            ),
            defaultSegmentTableColumns,
        )
        assertEquals(
            listOf(
                SegmentRowCell.Segment("Col de la Seigne"),
                SegmentRowCell.Profile(ProfileRange(16_100.0, 24_300.0), "#e8590c"),
                SegmentRowCell.Distance(8_200.0, 24_300.0),
                SegmentRowCell.Elevation(595.0, 34.0, 1_531.0),
                SegmentRowCell.Altitude(AltitudeRange(1_035.0, 1_850.0), null),
                SegmentRowCell.Duration(32_400.0),
                SegmentRowCell.Difficulty(t3),
            ),
            SegmentTableColumn.entries.map { segmentRowCell(row(), it) },
        )
    }

    @Test
    fun aBandWithOneEndKnownStatesThatEndTheFloorFirstAndNoneStatesNone() {
        assertEquals(
            SegmentRowCell.Altitude(null, 1_850.0),
            segmentRowCell(row(min = null), SegmentTableColumn.Altitude),
        )
        assertEquals(
            SegmentRowCell.Altitude(null, 1_035.0),
            segmentRowCell(row(max = null), SegmentTableColumn.Altitude),
        )
        assertEquals(
            SegmentRowCell.Altitude(null, 1_035.0),
            segmentRowCell(row(max = Double.NaN), SegmentTableColumn.Altitude),
        )
        assertEquals(
            SegmentRowCell.Altitude(null, null),
            segmentRowCell(row(min = null, max = null), SegmentTableColumn.Altitude),
        )
    }

    @Test
    fun aLegWithNoColourIsLitInTheTablesOwn() {
        assertEquals(
            SegmentRowCell.Profile(ProfileRange(16_100.0, 24_300.0), null),
            segmentRowCell(row(color = null), SegmentTableColumn.Profile),
        )
    }
}
