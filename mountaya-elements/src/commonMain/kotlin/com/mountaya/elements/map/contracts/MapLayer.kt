package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.tileLayerRank
import com.mountaya.geo.tileSourceLayerRanks
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * MapLayer is one layer a map draws above its base map: the source it draws from, how it draws, the
 * concept it draws, and the rank it draws at.
 *
 * It is a layer as MapLibre's style document writes one: [type], the [paint] and [layout] properties
 * under their style names (`line-color`, `visibility`), a [filter] expression and the [zoom] window it
 * draws across. A layer of a greater [rank] draws above one of a lesser, two of one rank in the order
 * they were added, the later above, and every one above the base map's own layers, which take no
 * rank: the bands are `LayerRank`'s, and a tile specification's layers take the rank their source
 * layer declares (`geo.layers.order`).
 *
 * Two are equal where they state the same, each JSON value as the serialization library compares it,
 * as the generated `TileSchema.TileLayer` compares its own.
 */
public class MapLayer(
    /** The layer's id on the map, which replaces the layer added under it before. */
    public val id: String,
    /** How the layer draws: a line, a fill, a circle, a symbol, a raster or a hillshade. */
    public val type: TileSchema.TileLayerType,
    /** The id of the source the layer draws from. */
    public val source: String,
    /** The layer of a vector source's tiles it draws, or null for a source with none. */
    public val sourceLayer: String? = null,
    /**
     * The concept the layer draws, as the layers manifest names it (`parking`, `hillshade`), which several
     * layers of one overlay may share, or null for a layer that draws no named concept (`map.layers.keys`).
     */
    public val key: String? = null,
    /** The band the layer draws at, `LayerRank`'s number for one of its bands, or one between two. */
    public val rank: Int,
    /** The layer's paint properties, by their style names. */
    public val paint: Map<String, JsonElement> = emptyMap(),
    /** The layer's layout properties, by their style names. */
    public val layout: Map<String, JsonElement> = emptyMap(),
    /** The expression a feature is drawn under, or null for every feature. */
    public val filter: JsonElement? = null,
    /** The zoom window the layer draws across, or null for every zoom. */
    public val zoom: ZoomBounds? = null,
) {
    /**
     * Creates the layer a tile specification declares, drawing from the source it names or, where it names
     * none, from the source of id [source] it is listed under, at the rank `tileLayerRank` reads off [ranks],
     * the contract's table unless a consumer patches it (`map.specification.style`).
     *
     * A null paint or layout is none. A raster or a hillshade layer reads a raster source, so it carries no
     * source layer and no filter, both of which name what lies inside a vector tile; an empty source layer
     * is the wire's way of saying there is none, and its empty key the same of a concept.
     */
    public constructor(
        layer: TileSchema.TileLayer,
        source: String,
        ranks: Map<String, Int> = tileSourceLayerRanks,
    ) : this(
        id = layer.id,
        type = layer.type,
        source = layer.source ?: source,
        sourceLayer = layer.source_layer.takeUnless { readsRaster(layer.type) || it.isEmpty() },
        key = layer.key.ifEmpty { null },
        rank = tileLayerRank(layer, ranks),
        paint = layer.paint.orEmpty(),
        layout = layer.layout.orEmpty(),
        filter = layer.filter?.takeUnless { readsRaster(layer.type) }?.let(::JsonArray),
        zoom = ZoomBounds(layer.zoom_min.toDouble(), layer.zoom_max.toDouble()),
    )

    /** Whether the layer's layout hides it, `visibility` written `none`. */
    public val isHidden: Boolean
        get() {
            val visibility = layout["visibility"] as? JsonPrimitive ?: return false
            return visibility.isString && visibility.content == "none"
        }

    /** This layer with [paint] and [layout] in place of its own, everything else as it is. */
    internal fun with(
        paint: Map<String, JsonElement> = this.paint,
        layout: Map<String, JsonElement> = this.layout,
    ): MapLayer = MapLayer(id, type, source, sourceLayer, key, rank, paint, layout, filter, zoom)

    /** This layer with its layout's `visibility` written [visibility], everything else as it is. */
    internal fun withVisibility(visibility: String): MapLayer =
        with(
            layout =
                layout + ("visibility" to JsonPrimitive(visibility)),
        )

    /** Answers whether [other] is a layer whose every member is the same. */
    override fun equals(other: Any?): Boolean =
        other is MapLayer &&
            other.id == id &&
            other.type == type &&
            other.source == source &&
            other.sourceLayer == sourceLayer &&
            other.key == key &&
            other.rank == rank &&
            other.paint == paint &&
            other.layout == layout &&
            other.filter == filter &&
            other.zoom == zoom

    override fun hashCode(): Int {
        var hash = id.hashCode()
        hash = 31 * hash + type.hashCode()
        hash = 31 * hash + source.hashCode()
        hash = 31 * hash + (sourceLayer?.hashCode() ?: 0)
        hash = 31 * hash + (key?.hashCode() ?: 0)
        hash = 31 * hash + rank
        hash = 31 * hash + paint.hashCode()
        hash = 31 * hash + layout.hashCode()
        hash = 31 * hash + (filter?.hashCode() ?: 0)
        hash = 31 * hash + (zoom?.hashCode() ?: 0)
        return hash
    }

    override fun toString(): String =
        "MapLayer($id, $type, source=$source, sourceLayer=$sourceLayer, key=$key, rank=$rank, paint=$paint, " +
            "layout=$layout, filter=$filter, zoom=$zoom)"
}

/** Whether a layer of [type] reads a raster source, which holds no source layer and no feature to filter. */
private fun readsRaster(type: TileSchema.TileLayerType): Boolean =
    type == TileSchema.TileLayerType.Raster || type == TileSchema.TileLayerType.Hillshade
