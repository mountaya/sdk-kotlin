package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * SegmentRowCell is what one cell of a segment row states (`geo.profile.segment-table-cells`), as a model before
 * any of it is written: meters and seconds, a loss stated positive as the wire states it, and null where the row
 * has no figure.
 *
 * Two are equal where they stand in one column stating the same, numbers by the same-number relation and texts
 * as written.
 */
public sealed interface SegmentRowCell {
    /** The column the cell stands in. */
    public val column: SegmentTableColumn

    /** Segment is the leg's name. */
    public class Segment(
        /** The leg's name. */
        public val name: String,
    ) : SegmentRowCell {
        override val column: SegmentTableColumn get() = SegmentTableColumn.Segment

        override fun equals(other: Any?): Boolean = other is Segment && other.name == name

        override fun hashCode(): Int = column.hashCode() * 31 + name.hashCode()

        override fun toString(): String = "SegmentRowCell.Segment(name=$name)"
    }

    /** Profile is the leg's stretch of the whole itinerary's sparkline, lit in [color], the leg's, or null for the table's own. */
    public class Profile(
        /** The stretch lit. */
        public val lit: ProfileRange,
        /** The colour it is lit in, null for the table's own. */
        public val color: String?,
    ) : SegmentRowCell {
        override val column: SegmentTableColumn get() = SegmentTableColumn.Profile

        override fun equals(other: Any?): Boolean = other is Profile && other.lit == lit && other.color == color

        override fun hashCode(): Int = (column.hashCode() * 31 + lit.hashCode()) * 31 + color.hashCode()

        override fun toString(): String = "SegmentRowCell.Profile(lit=$lit, color=$color)"
    }

    /** Distance is the leg's own length over the running distance at its end. */
    public class Distance(
        /** The leg's own length, in meters. */
        public val leg: Double?,
        /** The running distance at its end, in meters. */
        public val total: Double?,
    ) : SegmentRowCell {
        override val column: SegmentTableColumn get() = SegmentTableColumn.Distance

        override fun equals(other: Any?): Boolean =
            other is Distance && sameNumberOrNull(other.leg, leg) && sameNumberOrNull(other.total, total)

        override fun hashCode(): Int = (column.hashCode() * 31 + leg.hashCode()) * 31 + total.hashCode()

        override fun toString(): String = "SegmentRowCell.Distance(leg=$leg, total=$total)"
    }

    /** Elevation is the leg's climb and loss over the running climb at its end, the climb alone. */
    public class Elevation(
        /** The meters the leg climbs. */
        public val ascent: Double?,
        /** The meters the leg loses, stated positive. */
        public val descent: Double?,
        /** The running climb at its end. */
        public val totalAscent: Double?,
    ) : SegmentRowCell {
        override val column: SegmentTableColumn get() = SegmentTableColumn.Elevation

        override fun equals(other: Any?): Boolean =
            other is Elevation &&
                sameNumberOrNull(other.ascent, ascent) &&
                sameNumberOrNull(other.descent, descent) &&
                sameNumberOrNull(other.totalAscent, totalAscent)

        override fun hashCode(): Int = listOf(column, ascent, descent, totalAscent).hashCode()

        override fun toString(): String =
            "SegmentRowCell.Elevation(ascent=$ascent, descent=$descent, totalAscent=$totalAscent)"
    }

    /** Altitude is the leg's band where both its ends are known, else the one end known, the floor first, as [single]. */
    public class Altitude(
        /** The band, where both ends are known. */
        public val band: AltitudeRange?,
        /** The one end known, where the other is not. */
        public val single: Double?,
    ) : SegmentRowCell {
        override val column: SegmentTableColumn get() = SegmentTableColumn.Altitude

        override fun equals(other: Any?): Boolean =
            other is Altitude && other.band == band && sameNumberOrNull(other.single, single)

        override fun hashCode(): Int = (column.hashCode() * 31 + band.hashCode()) * 31 + single.hashCode()

        override fun toString(): String = "SegmentRowCell.Altitude(band=$band, single=$single)"
    }

    /** Duration is the seconds the leg is expected to take. */
    public class Duration(
        /** The seconds. */
        public val seconds: Double?,
    ) : SegmentRowCell {
        override val column: SegmentTableColumn get() = SegmentTableColumn.Duration

        override fun equals(other: Any?): Boolean = other is Duration && sameNumberOrNull(other.seconds, seconds)

        override fun hashCode(): Int = column.hashCode() * 31 + seconds.hashCode()

        override fun toString(): String = "SegmentRowCell.Duration(seconds=$seconds)"
    }

    /** Difficulty is the leg's difficulty as the itinerary writes it, never translated. */
    public class Difficulty(
        /** The difficulty, as written. */
        public val text: RestSchema.TrailDifficultyType?,
    ) : SegmentRowCell {
        override val column: SegmentTableColumn get() = SegmentTableColumn.Difficulty

        override fun equals(other: Any?): Boolean = other is Difficulty && other.text == text

        override fun hashCode(): Int = column.hashCode() * 31 + text.hashCode()

        override fun toString(): String = "SegmentRowCell.Difficulty(text=$text)"
    }
}
