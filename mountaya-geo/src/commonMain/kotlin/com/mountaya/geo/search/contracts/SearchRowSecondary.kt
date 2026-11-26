package com.mountaya.geo

/**
 * SearchRowSecondary is the second line under a search result's title: a sentence worded for the
 * reader, an itinerary's distance and ascent for the surface to state in its reader's units, or a
 * collection's count of itineraries for the surface to pluralize in its language.
 *
 * Two are equal where they are the same kind holding the same values, each number the same number.
 */
public sealed interface SearchRowSecondary {
    /** Text is a sentence: a category, a figure, the place's region and country. */
    public class Text(
        /** The sentence. */
        public val text: String,
    ) : SearchRowSecondary {
        override fun equals(other: Any?): Boolean = other is Text && other.text == text

        override fun hashCode(): Int = text.hashCode()

        override fun toString(): String = "SearchRowSecondary.Text($text)"
    }

    /** Metric is an itinerary's figures, each null where the search answered none. */
    public class Metric(
        /** The itinerary's length, in meters. */
        public val distance: Double?,
        /** The itinerary's ascent, in meters. */
        public val ascent: Int?,
    ) : SearchRowSecondary {
        override fun equals(other: Any?): Boolean =
            other is Metric && sameNumberOrNull(other.distance, distance) && other.ascent == ascent

        override fun hashCode(): Int = distance.hashCode() * 31 + ascent.hashCode()

        override fun toString(): String = "SearchRowSecondary.Metric(distance=$distance, ascent=$ascent)"
    }

    /** Count is a collection's count of itineraries, null where the search answered none. */
    public class Count(
        /** How many itineraries the collection holds. */
        public val itineraries: Int?,
    ) : SearchRowSecondary {
        override fun equals(other: Any?): Boolean = other is Count && other.itineraries == itineraries

        override fun hashCode(): Int = itineraries.hashCode()

        override fun toString(): String = "SearchRowSecondary.Count($itineraries)"
    }
}
