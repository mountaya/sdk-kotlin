package com.mountaya.elements

import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.ZoomBounds

/** MapLibre Native's own zoom floor, which `MapView` draws on: it draws nothing under 0. */
internal const val NATIVE_ZOOM_FLOOR: Double = 0.0

/** MapLibre Native's own zoom ceiling, which `MapView` draws on: it builds a map with a ceiling of 25.5. */
internal const val NATIVE_ZOOM_CEILING: Double = 25.5

/**
 * MapState is what a map shows: its base map and the terrain it is, its camera and the limits it is held to, the language its
 * labels speak, what it draws above the base map, the sources and the layers in the order they draw, the
 * markers it shows above them, the waypoint being placed on it, and whether a refusal of access stands.
 *
 * It is the state `MapEngine` holds and `MapView` renders, so a consumer who draws a map of their own
 * reads the same state. Two are equal where they show the same, each part compared as every Geo value
 * compares, a position and a pan lock as a `List<Double>` compares, `NaN` the same as `NaN` and 0 another
 * than -0.
 */
public class MapState(
    /**
     * The base map's style document, which the renderer fetches for itself, or null before one is
     * chosen, where nothing but what the map draws above it is shown.
     */
    public val style: Url?,
    /** Where the camera stands: its center, its zoom, its pitch and its bearing. */
    public val camera: CameraPose,
    /** The language the map's labels speak (`geo.style.label-language`). */
    public val labelLanguage: RestSchema.Language,
    /** Every source the map's layers read, in the order they were added. */
    public val sources: List<MapSource> = emptyList(),
    /**
     * Every layer the map draws above its base map, bottom to top, in the order the one layer registry
     * ranks them (`geo.layers.order`).
     */
    public val layers: List<MapLayer> = emptyList(),
    /** Every marker the map shows, above everything it draws, in the order they were added, the last one on top. */
    public val markers: List<MapMarker> = emptyList(),
    /**
     * The pan lock the camera is held within, `[west, south, east, north]` as the renderer is handed it,
     * its east edge past 180 where it crosses the antimeridian, or null for none.
     */
    public val maxBounds: List<Double>? = null,
    /**
     * The zoom range the camera is held to, the renderer's own where none is set: MapLibre Native's, from 0,
     * under which it draws nothing, to 25.5 (`map.camera.zoom-window`).
     */
    public val zoomBounds: ZoomBounds = ZoomBounds(NATIVE_ZOOM_FLOOR, NATIVE_ZOOM_CEILING),
    /**
     * The lines an edit's magnet places a waypoint on, each a list of positions, none where the magnet is
     * off (`map.edit.snap`).
     */
    public val activeSegments: List<List<List<Double>>> = emptyList(),
    /** The waypoint being placed, or null where none is. */
    public val waypointEdit: WaypointEdit? = null,
    /**
     * Whether a refusal of access stands: one of the map's own requests was refused a 401, a 402 or a 403,
     * and the source it came from has loaded no tile since, or, for a refusal naming no source, no style has
     * loaded since (`map.events.access-standing`).
     */
    public val isAccessRequired: Boolean = false,
    /**
     * The slug of the Tile API's terrain the base map is, as the catalogue spells it, or null for a style of
     * the app's own, or none (`map.basemap.terrain`).
     */
    public val terrain: String? = null,
    /**
     * The variant the map draws the catalogue's terrains in: the drawn document's where the terrain's two
     * variants are two documents, else the one the app asked for last, light until it asks for dark
     * (`map.basemap.terrain`).
     */
    public val terrainVariant: TileSchema.TerrainVariant = TileSchema.TerrainVariant.Light,
) {
    /** This state with the parts given in place of its own, every other as it is. */
    internal fun copy(
        style: Url? = this.style,
        camera: CameraPose = this.camera,
        labelLanguage: RestSchema.Language = this.labelLanguage,
        sources: List<MapSource> = this.sources,
        layers: List<MapLayer> = this.layers,
        markers: List<MapMarker> = this.markers,
        maxBounds: List<Double>? = this.maxBounds,
        zoomBounds: ZoomBounds = this.zoomBounds,
        activeSegments: List<List<List<Double>>> = this.activeSegments,
        waypointEdit: WaypointEdit? = this.waypointEdit,
        isAccessRequired: Boolean = this.isAccessRequired,
        terrain: String? = this.terrain,
        terrainVariant: TileSchema.TerrainVariant = this.terrainVariant,
    ): MapState =
        MapState(
            style,
            camera,
            labelLanguage,
            sources,
            layers,
            markers,
            maxBounds,
            zoomBounds,
            activeSegments,
            waypointEdit,
            isAccessRequired,
            terrain,
            terrainVariant,
        )

    override fun equals(other: Any?): Boolean =
        other is MapState &&
            other.style == style &&
            other.camera == camera &&
            other.labelLanguage == labelLanguage &&
            other.sources == sources &&
            other.layers == layers &&
            other.markers == markers &&
            other.maxBounds == maxBounds &&
            other.zoomBounds == zoomBounds &&
            other.activeSegments == activeSegments &&
            other.waypointEdit == waypointEdit &&
            other.isAccessRequired == isAccessRequired &&
            other.terrain == terrain &&
            other.terrainVariant == terrainVariant

    override fun hashCode(): Int {
        var hash = style?.hashCode() ?: 0
        hash = 31 * hash + camera.hashCode()
        hash = 31 * hash + labelLanguage.hashCode()
        hash = 31 * hash + sources.hashCode()
        hash = 31 * hash + layers.hashCode()
        hash = 31 * hash + markers.hashCode()
        hash = 31 * hash + (maxBounds?.hashCode() ?: 0)
        hash = 31 * hash + zoomBounds.hashCode()
        hash = 31 * hash + activeSegments.hashCode()
        hash = 31 * hash + (waypointEdit?.hashCode() ?: 0)
        hash = 31 * hash + isAccessRequired.hashCode()
        hash = 31 * hash + (terrain?.hashCode() ?: 0)
        hash = 31 * hash + terrainVariant.hashCode()
        return hash
    }

    override fun toString(): String =
        "MapState(style=$style, camera=$camera, labelLanguage=$labelLanguage, sources=$sources, " +
            "layers=$layers, markers=$markers, maxBounds=$maxBounds, zoomBounds=$zoomBounds, " +
            "activeSegments=$activeSegments, waypointEdit=$waypointEdit, isAccessRequired=$isAccessRequired, " +
            "terrain=$terrain, terrainVariant=$terrainVariant)"
}
