package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/**
 * ReadoutRow is one row of the readout a right-click or a long press opens on a map (`geo.readout.rows`), as a model:
 * what it states and the paths of the words of the elements' `readout` section it states them under. A moment is
 * written by the locale services as a medium date with a short time in the device's time zone, a value by the
 * formatter of its measure, and a template takes its values by name, through `filledText`.
 *
 * Two rows are equal where every member is.
 */
public sealed interface ReadoutRow {
    /** Poi is the topmost point of interest: its name, its category and the reader's word for it. */
    public class Poi(
        /** The name, or null where it has none. */
        public val name: String?,
        /** The category its tile's category reads as (`geo.layers.poi-category`), or null where the table names none. */
        public val category: DataSchema.POICategory?,
        /** The reader's word for the category, as `geo.search.describe` words one. */
        public val label: String,
    ) : ReadoutRow {
        override fun equals(other: Any?): Boolean =
            other is Poi && other.name == name && other.category == category && other.label == label

        override fun hashCode(): Int = listOf(name, category, label).hashCode()

        override fun toString(): String = "Poi(name=$name, category=$category, label=$label)"
    }

    /**
     * Band is the band the classic overlay draws at the point: its overlay's name, the [measure] its [values] are
     * written in, the travel-rate form they are written at where the readout pins one, and the moment the map shows.
     */
    public class Band(
        /** The overlay's name, in the reader's language. */
        public val overlay: String,
        /** What the values are written in. */
        public val measure: ReadoutMeasure,
        /** The travel-rate form a speed is written at whatever the reader's, or null where the reader's stands. */
        public val pace: RestSchema.UnitPace?,
        /** The two bounds where they differ, else the one, each in the measure's unit. */
        public val values: List<Double>,
        /** `readout.overlayRange`, whose `{min}` and `{max}` take two values, or null for one value alone. */
        public val word: String?,
        /** The moment the map shows, or null where it shows none. */
        public val time: Moment?,
    ) : ReadoutRow {
        override fun equals(other: Any?): Boolean =
            other is Band && other.overlay == overlay && other.measure == measure && other.pace == pace &&
                sameNumbers(other.values, values) && other.word == word && other.time == time

        override fun hashCode(): Int = listOf(overlay, measure, pace, values, word, time).hashCode()

        override fun toString(): String =
            "Band(overlay=$overlay, measure=$measure, pace=$pace, values=$values, word=$word, time=$time)"
    }

    /** Terrain is the terrain read off the elevation tiles: its altitude, slope and aspect, each where it is read. */
    public class Terrain(
        /** The readings, in the order altitude, slope and aspect. */
        public val readings: List<Reading>,
    ) : ReadoutRow {
        /** Reading is one figure of the terrain, under its word. */
        public class Reading(
            /** Which figure it is. */
            public val kind: Kind,
            /** The figure: meters for an altitude, degrees for a slope, a bearing for an aspect. */
            public val value: Double,
            /** The path of its word: `readout.altitude`, `readout.slope` or `readout.aspect`. */
            public val word: String,
            /** A slope's colour on the profile's ascent ramp, or null for another figure. */
            public val color: String? = null,
            /** An aspect's compass direction, or null for another figure. */
            public val cardinal: AspectCardinal? = null,
            /** The path of an aspect's direction's word, `readout.aspects.SE`, or null for another figure. */
            public val cardinalWord: String? = null,
        ) {
            /** Kind is which figure of the terrain a reading states. */
            public enum class Kind {
                /** The height, in meters. */
                Altitude,

                /** The slope, in degrees. */
                Slope,

                /** The aspect, as a bearing. */
                Aspect,
            }

            override fun equals(other: Any?): Boolean =
                other is Reading && other.kind == kind && sameNumber(other.value, value) && other.word == word &&
                    other.color == color && other.cardinal == cardinal && other.cardinalWord == cardinalWord

            override fun hashCode(): Int = listOf(kind, value, word, color, cardinal, cardinalWord).hashCode()

            override fun toString(): String =
                "Reading(kind=$kind, value=$value, word=$word, color=$color, cardinal=$cardinal, " +
                    "cardinalWord=$cardinalWord)"
        }

        override fun equals(other: Any?): Boolean = other is Terrain && other.readings == readings

        override fun hashCode(): Int = readings.hashCode()

        override fun toString(): String = "Terrain(readings=$readings)"
    }

    /** Moment is the moment the map shows, under `readout.overlayTime`, whose `{moment}` takes it. */
    public class Moment(
        /** The moment. */
        public val moment: Instant,
        /** The path of its word, `readout.overlayTime`. */
        public val word: String,
    ) : ReadoutRow {
        override fun equals(other: Any?): Boolean = other is Moment && other.moment == moment && other.word == word

        override fun hashCode(): Int = moment.hashCode() * 31 + word.hashCode()

        override fun toString(): String = "Moment(moment=$moment, word=$word)"
    }

    /** Coordinates is the point, as `geo.format.lnglat` writes it, and the pair a copy writes. */
    public class Coordinates(
        /** The point, written as `formatLngLat` writes it. */
        public val text: String,
        /** The latitude then the longitude, each to five places, joined by a comma and a space. */
        public val copy: String,
        /** The path of its word, `readout.coordinates`. */
        public val word: String,
    ) : ReadoutRow {
        override fun equals(other: Any?): Boolean =
            other is Coordinates && other.text == text && other.copy == copy && other.word == word

        override fun hashCode(): Int = listOf(text, copy, word).hashCode()

        override fun toString(): String = "Coordinates(text=$text, copy=$copy, word=$word)"
    }

    /** ZoomIn is the note below the detail zoom, `readout.zoomInForDetails`. */
    public class ZoomIn(
        /** The path of its word, `readout.zoomInForDetails`. */
        public val word: String,
    ) : ReadoutRow {
        override fun equals(other: Any?): Boolean = other is ZoomIn && other.word == word

        override fun hashCode(): Int = word.hashCode()

        override fun toString(): String = "ZoomIn(word=$word)"
    }

    /** Zone is the topmost zone: its name, its description, its level in its colour, and its window. */
    public class Zone(
        /** The name, or null where it has none. */
        public val name: String?,
        /** The description, or null where it has none. */
        public val description: String?,
        /** The level, as the tile writes it, a level the contract does not name held as it is. */
        public val level: RestSchema.ZoneLevel,
        /** The path of the level's word, `readout.zoneLevels.warning`, or null for a level the table does not name. */
        public val levelWord: String?,
        /** The level's colour. */
        public val color: String,
        /** The zone's window, or null where it states neither bound. */
        public val window: Window?,
        /** The path of its word, `readout.zone`. */
        public val word: String,
    ) : ReadoutRow {
        /** Window is a zone's window: its bounds, each null where it states none, under the word that takes them. */
        public class Window(
            /** When it opens. */
            public val start: Instant?,
            /** When it closes. */
            public val end: Instant?,
            /** `readout.zoneWindowRange`, `readout.zoneWindowFrom` or `readout.zoneWindowUntil`. */
            public val word: String,
        ) {
            override fun equals(other: Any?): Boolean =
                other is Window && other.start == start && other.end == end && other.word == word

            override fun hashCode(): Int = listOf(start, end, word).hashCode()

            override fun toString(): String = "Window(start=$start, end=$end, word=$word)"
        }

        override fun equals(other: Any?): Boolean =
            other is Zone && other.name == name && other.description == description && other.level == level &&
                other.levelWord == levelWord && other.color == color && other.window == window && other.word == word

        override fun hashCode(): Int = listOf(name, description, level, levelWord, color, window, word).hashCode()

        override fun toString(): String =
            "Zone(name=$name, description=$description, level=$level, levelWord=$levelWord, color=$color, " +
                "window=$window, word=$word)"
    }
}
