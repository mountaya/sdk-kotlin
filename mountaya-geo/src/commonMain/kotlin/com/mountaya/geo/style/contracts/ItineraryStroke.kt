package com.mountaya.geo

import kotlinx.serialization.json.JsonElement

/**
 * ItineraryStroke is one of the three strokes an itinerary drawn from data is drawn with, as its tiles
 * are (`geo.style.itinerary-stroke`): the solid line, the provisional dashed line, and the casing
 * beneath either.
 *
 * A line's colour is a literal MapLibre parses, `#2b8a3e`, or an expression it reads from each feature,
 * `["get", "mtyaColor"]`, so one layer draws a source holding several itineraries. The casing is drawn
 * in the paint's own colour and takes none of its caller's.
 *
 * Two are equal where they are the same stroke in the same colour, a JSON value as the serialization
 * library compares it.
 */
public sealed interface ItineraryStroke {
    /** Line is the solid stroke, in [color]. */
    public class Line(
        /** The stroke's colour, a literal or an expression. */
        public val color: JsonElement,
    ) : ItineraryStroke {
        override fun equals(other: Any?): Boolean = other is Line && other.color == color

        override fun hashCode(): Int = color.hashCode()

        override fun toString(): String = "Line(color=$color)"
    }

    /**
     * Dashed is the provisional stroke, in [color]: a suggested alternative, an unchosen candidate or a
     * draft not yet confirmed, dashed and muted so it reads as secondary without reading as another kind
     * of line.
     */
    public class Dashed(
        /** The stroke's colour, a literal or an expression. */
        public val color: JsonElement,
    ) : ItineraryStroke {
        override fun equals(other: Any?): Boolean = other is Dashed && other.color == color

        override fun hashCode(): Int = color.hashCode() * 31 + 1

        override fun toString(): String = "Dashed(color=$color)"
    }

    /**
     * Casing is the halo beneath a stroke, wider than it at every stop, which keeps the line legible over
     * relief and over an overlay.
     */
    public object Casing : ItineraryStroke {
        override fun toString(): String = "Casing"
    }
}
