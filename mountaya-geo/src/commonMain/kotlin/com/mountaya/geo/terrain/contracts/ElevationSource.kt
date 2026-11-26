package com.mountaya.geo

/**
 * ElevationSource is the elevation raster a map reads heights off (`geo.terrain.elevation-sample`): the [encoding]
 * and the deepest [zoom] its TileJSON document names, `getElevationTileJson`'s `encoding` and `maxzoom`, and the
 * [tileSize] the layers catalogue declares for it, `elevation.tile_size`.
 *
 * Two are equal where every member is.
 */
public class ElevationSource(
    /** The encoding the pixels stand for heights in, or null where the document names none, which reads `mapbox`. */
    public val encoding: ElevationEncoding?,
    /** The edge of a tile, in pixels. */
    public val tileSize: Int,
    /** The deepest zoom the source serves, the one its terrain is read at. */
    public val zoom: Int,
) {
    override fun equals(other: Any?): Boolean =
        other is ElevationSource && other.encoding == encoding && other.tileSize == tileSize && other.zoom == zoom

    override fun hashCode(): Int = (encoding.hashCode() * 31 + tileSize) * 31 + zoom

    override fun toString(): String = "ElevationSource(encoding=$encoding, tileSize=$tileSize, zoom=$zoom)"
}
