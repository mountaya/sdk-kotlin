package com.mountaya.geo

import kotlin.jvm.JvmInline

/**
 * ElevationEncoding is how a pixel of an elevation tile stands for a height, the `encoding` the elevation source's
 * TileJSON document names (`geo.terrain.elevation-decode`), kept as the document spells it, so an encoding the
 * contract adds after this build is held and reads no height rather than being refused.
 */
@JvmInline
public value class ElevationEncoding(
    /** The encoding as the TileJSON document spells it, compared whole. */
    public val value: String,
) {
    /** The encodings the contract reads a height under. */
    public companion object {
        /** Mapbox's Terrain-RGB, the style specification's default for a `raster-dem` source. */
        public val Mapbox: ElevationEncoding = ElevationEncoding("mapbox")

        /** Mapzen's Terrarium. */
        public val Terrarium: ElevationEncoding = ElevationEncoding("terrarium")
    }
}
