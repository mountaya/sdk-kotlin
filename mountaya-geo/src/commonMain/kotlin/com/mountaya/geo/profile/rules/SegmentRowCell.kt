package com.mountaya.geo

/**
 * defaultSegmentTableColumns is the columns a segment table draws where its caller names none, the
 * `defaultColumns` `geo/profile-segment-table-cells.json` declares: the leg's name, its shape, how far and how
 * much, which is what a roadbook is read for.
 */
public val defaultSegmentTableColumns: List<SegmentTableColumn> =
    listOf(
        SegmentTableColumn.Segment,
        SegmentTableColumn.Profile,
        SegmentTableColumn.Distance,
        SegmentTableColumn.Elevation,
    )

/**
 * segmentRowCell is what [column] of a segment table states about [row]'s leg (`geo.profile.segment-table-cells`):
 * its name; the whole itinerary's sparkline with its own stretch lit in its colour; its distance over the running
 * distance; its climb and loss over the running climb alone; its altitude band, or the one end known where the
 * other is not, the floor first; its duration; and its difficulty as the itinerary writes it, never translated,
 * since the taxonomy is the itinerary's.
 */
public fun segmentRowCell(
    row: ItinerarySegmentRow,
    column: SegmentTableColumn,
): SegmentRowCell {
    val metrics = row.metrics
    return when (column) {
        SegmentTableColumn.Segment -> {
            SegmentRowCell.Segment(row.name)
        }

        SegmentTableColumn.Profile -> {
            SegmentRowCell.Profile(ProfileRange(row.from, row.to), row.color)
        }

        SegmentTableColumn.Distance -> {
            SegmentRowCell.Distance(metrics.sectionDistance, metrics.distance)
        }

        SegmentTableColumn.Elevation -> {
            SegmentRowCell.Elevation(metrics.sectionAscent, metrics.sectionDescent, metrics.ascent)
        }

        SegmentTableColumn.Altitude -> {
            val lowest = metrics.altitudeMin?.takeIf { it.isFinite() }
            val highest = metrics.altitudeMax?.takeIf { it.isFinite() }
            if (lowest != null && highest != null) {
                SegmentRowCell.Altitude(AltitudeRange(lowest, highest), null)
            } else {
                SegmentRowCell.Altitude(null, lowest ?: highest)
            }
        }

        SegmentTableColumn.Duration -> {
            SegmentRowCell.Duration(metrics.duration)
        }

        SegmentTableColumn.Difficulty -> {
            SegmentRowCell.Difficulty(metrics.difficulty)
        }
    }
}
