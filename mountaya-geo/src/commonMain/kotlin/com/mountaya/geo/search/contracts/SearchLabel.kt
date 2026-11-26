package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchLabel names one word of a search result's title or second line by the wire value it stands
 * for, so a host's own words, or the localized names its catalogue carries, word it: a place's or a
 * point of interest's category as the Data API spells it, or a way's `highway`, `sac_scale` or
 * `surface` tag as OpenStreetMap spells it, an open vocabulary.
 *
 * Two are equal where they name the same value of the same kind.
 */
public sealed interface SearchLabel {
    /** PlaceCategory is a geocoded place's category. */
    public class PlaceCategory(
        /** The category, as the Data API spells it. */
        public val value: DataSchema.PlaceCategory,
    ) : SearchLabel {
        override fun equals(other: Any?): Boolean = other is PlaceCategory && other.value == value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "SearchLabel.PlaceCategory($value)"
    }

    /** PoiCategory is a point of interest's category. */
    public class PoiCategory(
        /** The category, as the Data API spells it. */
        public val value: DataSchema.POICategory,
    ) : SearchLabel {
        override fun equals(other: Any?): Boolean = other is PoiCategory && other.value == value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "SearchLabel.PoiCategory($value)"
    }

    /** Highway is a way's `highway` tag: `path`, `track`, `footway`. */
    public class Highway(
        /** The tag's value, as OpenStreetMap spells it. */
        public val value: String,
    ) : SearchLabel {
        override fun equals(other: Any?): Boolean = other is Highway && other.value == value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "SearchLabel.Highway($value)"
    }

    /** SacScale is a way's `sac_scale` grade: `mountain_hiking`, `alpine_hiking`. */
    public class SacScale(
        /** The grade, as OpenStreetMap spells it. */
        public val value: String,
    ) : SearchLabel {
        override fun equals(other: Any?): Boolean = other is SacScale && other.value == value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "SearchLabel.SacScale($value)"
    }

    /** Surface is a way's `surface` tag, worded as the whole token, `gravel surface`, since where the word goes is the language's. */
    public class Surface(
        /** The surface, as OpenStreetMap spells it. */
        public val value: String,
    ) : SearchLabel {
        override fun equals(other: Any?): Boolean = other is Surface && other.value == value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "SearchLabel.Surface($value)"
    }
}
