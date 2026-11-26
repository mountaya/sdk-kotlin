package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * WaypointRowCell is what one cell of a waypoint row states (`geo.profile.waypoint-table-cells`), as a model
 * before any of it is written: meters, a loss stated positive as the wire states it, and null where the row has
 * no figure.
 *
 * Two are equal where they stand in one column stating the same, numbers by the same-number relation and texts
 * as written.
 */
public sealed interface WaypointRowCell {
    /** The column the cell stands in. */
    public val column: WaypointTableColumn

    /**
     * Waypoint is the glyph the place is marked with, its own where the table's set draws it and
     * `WAYPOINT_FALLBACK_GLYPH` otherwise, and its name, null for a place with none, which is listed under the
     * `profile` word `unlabelledWaypoint`.
     */
    public class Waypoint(
        /** The glyph's name. */
        public val glyph: String,
        /** The place's name, null where it has none. */
        public val name: String?,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Waypoint

        override fun equals(other: Any?): Boolean = other is Waypoint && other.glyph == glyph && other.name == name

        override fun hashCode(): Int = (column.hashCode() * 31 + glyph.hashCode()) * 31 + name.hashCode()

        override fun toString(): String = "WaypointRowCell.Waypoint(glyph=$glyph, name=$name)"
    }

    /** Profile is the stretch reaching the place, lit on the whole itinerary's sparkline. */
    public class Profile(
        /** The stretch lit. */
        public val lit: ProfileRange,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Profile

        override fun equals(other: Any?): Boolean = other is Profile && other.lit == lit

        override fun hashCode(): Int = column.hashCode() * 31 + lit.hashCode()

        override fun toString(): String = "WaypointRowCell.Profile(lit=$lit)"
    }

    /** Distance is the running distance to the place. */
    public class Distance(
        /** The meters from the start of the itinerary. */
        public val total: Double,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Distance

        override fun equals(other: Any?): Boolean = other is Distance && sameNumber(other.total, total)

        override fun hashCode(): Int = column.hashCode() * 31 + total.hashCode()

        override fun toString(): String = "WaypointRowCell.Distance(total=$total)"
    }

    /** Elevation is the climb and loss since the previous place. */
    public class Elevation(
        /** The meters climbed since the previous place. */
        public val ascent: Double?,
        /** The meters lost since the previous place, stated positive. */
        public val descent: Double?,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Elevation

        override fun equals(other: Any?): Boolean =
            other is Elevation && sameNumberOrNull(other.ascent, ascent) && sameNumberOrNull(other.descent, descent)

        override fun hashCode(): Int = (column.hashCode() * 31 + ascent.hashCode()) * 31 + descent.hashCode()

        override fun toString(): String = "WaypointRowCell.Elevation(ascent=$ascent, descent=$descent)"
    }

    /** Altitude is the place's altitude. */
    public class Altitude(
        /** The altitude, in meters above sea level, null where none reads. */
        public val altitude: Double?,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Altitude

        override fun equals(other: Any?): Boolean = other is Altitude && sameNumberOrNull(other.altitude, altitude)

        override fun hashCode(): Int = column.hashCode() * 31 + altitude.hashCode()

        override fun toString(): String = "WaypointRowCell.Altitude(altitude=$altitude)"
    }

    /** Amenities is the services the place offers, in the order it states them. */
    public class Amenities(
        /** The services. */
        public val amenities: List<RestSchema.WaypointAmenity>,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Amenities

        override fun equals(other: Any?): Boolean = other is Amenities && other.amenities == amenities

        override fun hashCode(): Int = column.hashCode() * 31 + amenities.hashCode()

        override fun toString(): String = "WaypointRowCell.Amenities(amenities=$amenities)"
    }

    /** Cutoff is the place's cut-off as the caller's formatter writes it, null with no formatter or no cut-off. */
    public class Cutoff(
        /** The cut-off as written. */
        public val moment: String?,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Cutoff

        override fun equals(other: Any?): Boolean = other is Cutoff && other.moment == moment

        override fun hashCode(): Int = column.hashCode() * 31 + moment.hashCode()

        override fun toString(): String = "WaypointRowCell.Cutoff(moment=$moment)"
    }

    /** Properties is the place's fields, each written by its declared format. */
    public class Properties(
        /** The fields. */
        public val fields: List<WaypointField>,
    ) : WaypointRowCell {
        override val column: WaypointTableColumn get() = WaypointTableColumn.Properties

        override fun equals(other: Any?): Boolean = other is Properties && other.fields == fields

        override fun hashCode(): Int = column.hashCode() * 31 + fields.hashCode()

        override fun toString(): String = "WaypointRowCell.Properties(fields=$fields)"
    }
}
