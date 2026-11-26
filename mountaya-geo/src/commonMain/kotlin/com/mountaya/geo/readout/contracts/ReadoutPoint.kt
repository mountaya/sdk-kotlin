package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/**
 * ReadoutPoint is what a map holds at the point a right-click or a long press opens its readout at
 * (`geo.readout.rows`): the point, the camera's zoom, and each of the terrain read off the elevation tiles there, the
 * topmost point of interest and zone the map draws there, the classic overlay it draws with the band of its first
 * feature there, and the moment it shows, each null where the map holds none.
 *
 * Two are equal where every member is.
 */
public class ReadoutPoint(
    /** The point, `[longitude, latitude]`. */
    public val position: List<Double>,
    /** The camera's zoom when the readout opened. */
    public val zoom: Double,
    /** The terrain read off the elevation tiles at the point. */
    public val terrain: Terrain? = null,
    /** The topmost point of interest the map draws at the point. */
    public val poi: Poi? = null,
    /** The topmost zone the map draws at the point. */
    public val zone: Zone? = null,
    /** The classic overlay the map draws. */
    public val overlay: Overlay? = null,
    /** The band of the overlay's first feature at the point stating one. */
    public val band: Band? = null,
    /** The moment the map shows (`geo.layers.overlay-instant`). */
    public val instant: Instant? = null,
) {
    /**
     * Terrain is the ground at the point, each figure null where the reading states none: its [elevation] in meters,
     * its [slope] in degrees, and its [aspect] as a bearing, null on flat ground.
     */
    public class Terrain(
        /** The height, in meters. */
        public val elevation: Double?,
        /** The slope, in degrees. */
        public val slope: Double?,
        /** The bearing the ground falls towards, 0 north and 90 east, or null on flat ground. */
        public val aspect: Double?,
    ) {
        /** Creates the terrain [reading] states, as `terrainFromElevationTiles` reads it. */
        public constructor(reading: TerrainReading) : this(reading.elevation, reading.slope, reading.aspect)

        override fun equals(other: Any?): Boolean =
            other is Terrain && sameNumberOrNull(other.elevation, elevation) && sameNumberOrNull(other.slope, slope) &&
                sameNumberOrNull(other.aspect, aspect)

        override fun hashCode(): Int = listOf(elevation, slope, aspect).hashCode()

        override fun toString(): String = "Terrain(elevation=$elevation, slope=$slope, aspect=$aspect)"
    }

    /** Poi is a point of interest: its [name] and the [category] its tile states. */
    public class Poi(
        /** The name, empty where it has none. */
        public val name: String,
        /** The category as the tile states it, `alpine_hut`. */
        public val category: String,
    ) {
        override fun equals(other: Any?): Boolean = other is Poi && other.name == name && other.category == category

        override fun hashCode(): Int = name.hashCode() * 31 + category.hashCode()

        override fun toString(): String = "Poi(name=$name, category=$category)"
    }

    /** Zone is a zone: its name, its description, its level and the bounds of its window as its tile states them. */
    public class Zone(
        /** The name, empty where it has none. */
        public val name: String,
        /** The description, empty where it has none. */
        public val description: String,
        /** The level, as the tile writes it, a level the contract does not name held as it is. */
        public val level: RestSchema.ZoneLevel,
        /** When its window opens, or null where it states none. */
        public val start: Instant?,
        /** When its window closes, or null where it states none. */
        public val end: Instant?,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Zone && other.name == name && other.description == description && other.level == level &&
                other.start == start && other.end == end

        override fun hashCode(): Int = listOf(name, description, level, start, end).hashCode()

        override fun toString(): String =
            "Zone(name=$name, description=$description, level=$level, start=$start, end=$end)"
    }

    /** Overlay is the classic overlay the map draws: its [slug], its [name] in the reader's language, and whether it is a vector one. */
    public class Overlay(
        /** The overlay's slug. */
        public val slug: String,
        /** The overlay's name, in the reader's language. */
        public val name: String,
        /** Whether its tiles are vector tiles, whose features state a band. */
        public val isVector: Boolean,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Overlay && other.slug == slug && other.name == name && other.isVector == isVector

        override fun hashCode(): Int = (slug.hashCode() * 31 + name.hashCode()) * 31 + isVector.hashCode()

        override fun toString(): String = "Overlay(slug=$slug, name=$name, isVector=$isVector)"
    }

    /** Band is a feature's band: its `minValue` and its `maxValue`, each null or no number where it states none. */
    public class Band(
        /** The band's lower bound. */
        public val min: Double?,
        /** The band's upper bound. */
        public val max: Double?,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Band && sameNumberOrNull(other.min, min) && sameNumberOrNull(other.max, max)

        override fun hashCode(): Int = min.hashCode() * 31 + max.hashCode()

        override fun toString(): String = "Band(min=$min, max=$max)"
    }

    override fun equals(other: Any?): Boolean =
        other is ReadoutPoint && sameNumbers(other.position, position) && sameNumber(other.zoom, zoom) &&
            other.terrain == terrain && other.poi == poi && other.zone == zone && other.overlay == overlay &&
            other.band == band && other.instant == instant

    override fun hashCode(): Int = listOf(position, zoom, terrain, poi, zone, overlay, band, instant).hashCode()

    override fun toString(): String =
        "ReadoutPoint(position=$position, zoom=$zoom, terrain=$terrain, poi=$poi, zone=$zone, overlay=$overlay, " +
            "band=$band, instant=$instant)"
}
