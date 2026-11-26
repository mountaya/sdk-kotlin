package com.mountaya.geo

/**
 * MapPaint is how a map paints what it draws above its base map: an itinerary's stroke and the casing
 * beneath it (`geo.style.itinerary-stroke`), the rank each tile source layer draws at
 * (`geo.layers.order`), and the colours a segment is drawn in (`geo.style.segment-colors`).
 *
 * A consumer restyles a map by copying [standard], the paint as the contract ships it
 * (`geo.style.map-paint`), with the members it changes: `MapPaint.standard.copy {
 * itinerary.casingColor = "#1b1b1b" }` changes the casing's colour and leaves every other value as
 * shipped, a list assigned, `segments.palette = listOf("#0b7285", "#e8590c")`, replaces the shipped one
 * whole, and `layerRanks += "acme_avalanche" to 40` adds a band and keeps every shipped one. A colour is
 * a literal MapLibre parses, a hex value, `rgb()` or a named colour, since a renderer's paint takes the
 * colour itself, and a width is in pixels. Its constructors are `internal`, so a member the contract
 * adds later breaks no caller.
 *
 * Two are equal where they hold the same values, each number as the same number, `NaN` the same as
 * `NaN` and 0 another than -0, as every Geo value compares.
 */
public class MapPaint internal constructor(
    /** The paint of an itinerary's stroke and of its casing. */
    public val itinerary: Itinerary,
    layerRanks: Map<String, Int>,
    /** The colours a segment is drawn in. */
    public val segments: Segments,
) {
    /**
     * The rank each tile source layer's layers draw at, by the source layer's name, the table
     * `tileLayerRank` reads: a band added draws a consumer's own tiles where it says, and a source layer
     * the table does not name draws as an overlay. A band named `__proto__`, `constructor` or `prototype`
     * is left out, as every merge of a patch leaves those names out in every SDK.
     */
    public val layerRanks: Map<String, Int> = layerRanks.filterKeys { it !in prototypeKeys }

    /**
     * Itinerary is the paint of an itinerary's stroke and of the casing beneath it, the one group
     * `itineraryStrokeStyle` reads for every stroke, so an itinerary drawn from data, a directions
     * answer and a draft are stroked alike.
     */
    public class Itinerary internal constructor(
        /** The colour of the casing beneath the stroke, which keeps a line legible over relief. */
        public val casingColor: String,
        /** The opacity the casing paints at, from 0 to 1. */
        public val casingOpacity: Double,
        /**
         * The casing's width table, a flat list of zoom and width pairs interpolated linearly on zoom,
         * wider than the stroke's at every stop as shipped.
         */
        public val casingWidthStops: List<Double>,
        /** The dash a provisional stroke paints in, its lengths alternately drawn and skipped, in line widths. */
        public val dashArray: List<Double>,
        /** The opacity a provisional stroke paints at, from 0 to 1, so it reads as secondary beside a solid one. */
        public val dashedOpacity: Double,
        /** The stroke's width table, a flat list of zoom and width pairs interpolated linearly on zoom. */
        public val lineWidthStops: List<Double>,
    ) {
        /** Builder holds every member of the paint [MapPaint.copy] starts from, each one settable. */
        public class Builder internal constructor(
            itinerary: Itinerary,
        ) {
            /** The casing's colour. */
            public var casingColor: String = itinerary.casingColor

            /** The casing's opacity. */
            public var casingOpacity: Double = itinerary.casingOpacity

            /** The casing's width table. */
            public var casingWidthStops: List<Double> = itinerary.casingWidthStops

            /** A provisional stroke's dash. */
            public var dashArray: List<Double> = itinerary.dashArray

            /** A provisional stroke's opacity. */
            public var dashedOpacity: Double = itinerary.dashedOpacity

            /** The stroke's width table. */
            public var lineWidthStops: List<Double> = itinerary.lineWidthStops

            internal fun build(): Itinerary =
                Itinerary(casingColor, casingOpacity, casingWidthStops, dashArray, dashedOpacity, lineWidthStops)
        }

        override fun equals(other: Any?): Boolean =
            other is Itinerary &&
                other.casingColor == casingColor &&
                sameNumber(other.casingOpacity, casingOpacity) &&
                other.casingWidthStops == casingWidthStops &&
                other.dashArray == dashArray &&
                sameNumber(other.dashedOpacity, dashedOpacity) &&
                other.lineWidthStops == lineWidthStops

        override fun hashCode(): Int =
            listOf(casingColor, casingOpacity, casingWidthStops, dashArray, dashedOpacity, lineWidthStops).hashCode()

        override fun toString(): String =
            "Itinerary(casingColor=$casingColor, casingOpacity=$casingOpacity, casingWidthStops=$casingWidthStops, " +
                "dashArray=$dashArray, dashedOpacity=$dashedOpacity, lineWidthStops=$lineWidthStops)"
    }

    /** Segments is the colours a segment is drawn in. */
    public class Segments internal constructor(
        /** The colour a segment that carries none of its own is drawn in. */
        public val defaultColor: String,
        /**
         * The wheel a segment's colour is read from, in order around the hue circle, which
         * `segmentColorAt` and `assignDistinctColors` take.
         */
        public val palette: List<String>,
    ) {
        /** Builder holds every member of the segments' paint [MapPaint.copy] starts from, each one settable. */
        public class Builder internal constructor(
            segments: Segments,
        ) {
            /** The colour of a segment that carries none. */
            public var defaultColor: String = segments.defaultColor

            /** The wheel a segment's colour is read from. */
            public var palette: List<String> = segments.palette

            internal fun build(): Segments = Segments(defaultColor, palette)
        }

        override fun equals(other: Any?): Boolean =
            other is Segments && other.defaultColor == defaultColor && other.palette == palette

        override fun hashCode(): Int = defaultColor.hashCode() * 31 + palette.hashCode()

        override fun toString(): String = "Segments(defaultColor=$defaultColor, palette=$palette)"
    }

    /** Builder holds every member of the paint [copy] starts from, each one settable. */
    public class Builder internal constructor(
        paint: MapPaint,
    ) {
        /** The paint of an itinerary's stroke and of its casing, each member settable. */
        public val itinerary: Itinerary.Builder = Itinerary.Builder(paint.itinerary)

        /** The rank each tile source layer draws at. */
        public var layerRanks: Map<String, Int> = paint.layerRanks

        /** The colours a segment is drawn in, each member settable. */
        public val segments: Segments.Builder = Segments.Builder(paint.segments)

        internal fun build(): MapPaint = MapPaint(itinerary.build(), layerRanks.toMap(), segments.build())
    }

    /** Answers this paint with the members [edit] sets on the [Builder] it is handed, every other kept. */
    public fun copy(edit: Builder.() -> Unit): MapPaint = Builder(this).apply(edit).build()

    override fun equals(other: Any?): Boolean =
        other is MapPaint && other.itinerary == itinerary && other.layerRanks == layerRanks &&
            other.segments == segments

    override fun hashCode(): Int = listOf(itinerary, layerRanks, segments).hashCode()

    override fun toString(): String = "MapPaint(itinerary=$itinerary, layerRanks=$layerRanks, segments=$segments)"

    /** The paint the contract ships. */
    public companion object {
        /**
         * The paint as the contract ships it: an itinerary's line 2 pixels wide at zooms 7 and 10, 3 at 13 and 5 at 16,
         * over a white casing 2 pixels wider at every stop at an opacity of 0.9, a provisional stroke dashed two widths
         * on and two off at 0.6, the ranks of `tileSourceLayerRanks`, and a segment in `DEFAULT_SEGMENT_COLOR` or a
         * colour of `livelyPalette`.
         */
        public val standard: MapPaint =
            MapPaint(
                Itinerary(
                    casingColor = "#ffffff",
                    casingOpacity = 0.9,
                    casingWidthStops = listOf(7.0, 4.0, 10.0, 4.0, 13.0, 5.0, 16.0, 7.0),
                    dashArray = listOf(2.0, 2.0),
                    dashedOpacity = 0.6,
                    lineWidthStops = listOf(7.0, 2.0, 10.0, 2.0, 13.0, 3.0, 16.0, 5.0),
                ),
                tileSourceLayerRanks,
                Segments(DEFAULT_SEGMENT_COLOR, livelyPalette),
            )
    }
}
