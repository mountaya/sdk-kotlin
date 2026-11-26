package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.ZoomBounds

/**
 * MapSource is where a map's layers read their data: the Tile API's tiles, a raster's, an elevation
 * raster's, or GeoJSON the app holds.
 *
 * A tile source's `tiles` are the URL templates a renderer fetches for itself, `{z}`, `{x}` and `{y}`
 * in their places, as a tile specification answers them: a template on the Tile API's origin is
 * fetched with the client's credential, and one on any other host with none. Two are equal where they
 * state the same, as every Geo value compares.
 */
public class MapSource(
    /** The source's id, which a layer draws from it by. */
    public val id: String,
    /** What the source serves. */
    public val kind: Kind,
    /** The credit the source's data asks for, or null where it asks none. */
    public val attribution: String? = null,
) {
    /**
     * Creates the source a tile specification declares, its layers apart: a vector source, a raster's, or
     * an elevation raster's, of the size it states, the renderer's own where it states none
     * (`map.specification.style`).
     *
     * It throws a [MapException] of kind `TypeRefused`, naming the type as the answer wrote it, for a type the
     * contract does not declare, which a newer Tile API may add: read as one this build knows, its tiles would
     * be fetched and decoded as what they are not, with nothing reported.
     */
    public constructor(source: TileSchema.TileSource) : this(source.id, kindOf(source), source.attribution)

    /** Kind is what a source serves. Two kinds are equal where they serve the same. */
    public sealed interface Kind {
        /** Vector tiles, at the zoom levels they are served at. */
        public class Vector(
            /** The tiles' URL templates. */
            public val tiles: List<String>,
            /** The zoom levels the tiles are served at. */
            public val zoom: ZoomBounds,
        ) : Kind {
            override fun equals(other: Any?): Boolean = other is Vector && other.tiles == tiles && other.zoom == zoom

            override fun hashCode(): Int = tiles.hashCode() * 31 + zoom.hashCode()

            override fun toString(): String = "Vector($tiles, $zoom)"
        }

        /** Raster tiles of [tileSize] pixels, the renderer's own size where it is null, at the zoom levels they are served at. */
        public class Raster(
            /** The tiles' URL templates. */
            public val tiles: List<String>,
            /** The side of a tile, in pixels, or null for the renderer's own. */
            public val tileSize: Int?,
            /** The zoom levels the tiles are served at. */
            public val zoom: ZoomBounds,
        ) : Kind {
            override fun equals(other: Any?): Boolean =
                other is Raster && other.tiles == tiles && other.tileSize == tileSize && other.zoom == zoom

            override fun hashCode(): Int = (tiles.hashCode() * 31 + (tileSize ?: 0)) * 31 + zoom.hashCode()

            override fun toString(): String = "Raster($tiles, $tileSize, $zoom)"
        }

        /**
         * RasterDem is an elevation raster's tiles of [tileSize] pixels, the renderer's own size where it is null,
         * which a hillshade shades.
         */
        public class RasterDem(
            /** The tiles' URL templates. */
            public val tiles: List<String>,
            /** The side of a tile, in pixels, or null for the renderer's own. */
            public val tileSize: Int?,
            /** The zoom levels the tiles are served at. */
            public val zoom: ZoomBounds,
        ) : Kind {
            override fun equals(other: Any?): Boolean =
                other is RasterDem && other.tiles == tiles && other.tileSize == tileSize && other.zoom == zoom

            override fun hashCode(): Int = (tiles.hashCode() * 31 + (tileSize ?: 0)) * 31 + zoom.hashCode()

            override fun toString(): String = "RasterDem($tiles, $tileSize, $zoom)"
        }

        /** GeoJson is the features the app holds, drawn as they are. */
        public class GeoJson(
            /** The features. */
            public val features: GeoJsonFeatureCollection,
        ) : Kind {
            override fun equals(other: Any?): Boolean = other is GeoJson && other.features == features

            override fun hashCode(): Int = features.hashCode()

            override fun toString(): String = "GeoJson($features)"
        }
    }

    override fun equals(other: Any?): Boolean =
        other is MapSource && other.id == id && other.kind == kind && other.attribution == attribution

    override fun hashCode(): Int = (id.hashCode() * 31 + kind.hashCode()) * 31 + (attribution?.hashCode() ?: 0)

    override fun toString(): String = "MapSource($id, $kind, attribution=$attribution)"

    private companion object {
        /** What [source] serves, by its type, refusing a type the contract does not declare. */
        fun kindOf(source: TileSchema.TileSource): Kind {
            val zoom = ZoomBounds(source.zoom_min.toDouble(), source.zoom_max.toDouble())
            val size = source.size?.value
            return when (source.type) {
                TileSchema.TileSourceType.Vector -> Kind.Vector(source.tiles, zoom)
                TileSchema.TileSourceType.Raster -> Kind.Raster(source.tiles, size, zoom)
                TileSchema.TileSourceType.RasterDem -> Kind.RasterDem(source.tiles, size, zoom)
                else -> throw MapException(MapException.Kind.TypeRefused, source.type.value)
            }
        }
    }
}
