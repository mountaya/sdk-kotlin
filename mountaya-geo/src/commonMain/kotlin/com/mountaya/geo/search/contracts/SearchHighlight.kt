package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchHighlight is what a map draws for the result a reader picked from a search, so the answer stays on the
 * map while the list is read (`map.search.result-highlight`), as picking the result marks it (`geo.search.pick`):
 * a place, a point of interest, or a way.
 *
 * A position is `[longitude, latitude]` and a box `[west, south, east, north]`, as a search row's payload holds
 * them, a box across the antimeridian written with its west edge east of its east one. One is drawn at a time,
 * above everything else on the map, in the map's accent.
 *
 * Two are equal where they are of one kind holding the same values, a list of numbers compared as every list of
 * the geo layer is, `NaN` the same as `NaN` and 0 another than -0.
 */
public sealed interface SearchHighlight {
    /**
     * Place is the point-of-interest pin carrying a point, at [point]: how a place is marked, and an itinerary's
     * start and a zone's point, none of which has a category of its own.
     */
    public class Place(
        /** Where the pin stands. */
        public val point: List<Double>,
    ) : SearchHighlight {
        override fun equals(other: Any?): Boolean = other is Place && other.point == point

        override fun hashCode(): Int = point.hashCode()

        override fun toString(): String = "SearchHighlight.Place(point=$point)"
    }

    /**
     * Poi is a point of interest's own pin, in its category's glyph and colour, at [point], the pin the tiles draw
     * it with; one whose category the table styles none of, or that states none, is marked as a place.
     */
    public class Poi(
        /** Where the pin stands. */
        public val point: List<Double>,
        /** The point of interest's category, as the Data API spells it, or null where it states none. */
        public val category: DataSchema.POICategory?,
    ) : SearchHighlight {
        override fun equals(other: Any?): Boolean = other is Poi && other.point == point && other.category == category

        override fun hashCode(): Int = point.hashCode() * 31 + category.hashCode()

        override fun toString(): String = "SearchHighlight.Poi(point=$point, category=$category)"
    }

    /**
     * Way is a way's line, [coordinates], with a dot at each of its two ends where it traces two positions or
     * more, else its extent, [bounds], washed and outlined dashed, else a pin at its [anchor].
     */
    public class Way(
        /** The way's anchor, where its pin stands when it draws neither a line nor an extent. */
        public val anchor: List<Double>,
        /** The way's extent, or null where it states none. */
        public val bounds: List<Double>?,
        /** The way's line as the search traced it, or null where it states none. */
        public val coordinates: List<List<Double>>?,
    ) : SearchHighlight {
        override fun equals(other: Any?): Boolean =
            other is Way && other.anchor == anchor && other.bounds == bounds && other.coordinates == coordinates

        override fun hashCode(): Int = (anchor.hashCode() * 31 + bounds.hashCode()) * 31 + coordinates.hashCode()

        override fun toString(): String =
            "SearchHighlight.Way(anchor=$anchor, bounds=$bounds, coordinates=$coordinates)"
    }
}
