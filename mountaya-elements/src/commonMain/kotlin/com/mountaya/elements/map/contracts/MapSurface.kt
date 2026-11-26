package com.mountaya.elements

import com.mountaya.geo.CameraPose
import com.mountaya.geo.ZoomBounds
import kotlinx.serialization.json.JsonElement

/**
 * MapSurface is what a map engine asks of whatever draws it: MapLibre's map behind `MapView`, or
 * `ScriptedMapSurface`, which a test scripts. It reads the canvas the map is drawn on and moves its camera;
 * every rule of the map is the engine's, which hands a surface what to do and reads back what only a
 * renderer knows.
 *
 * A surface tells the engine of what happens on it, a gesture, a frame, a failed request, a tile or a style
 * loaded and a camera at rest, through the engine's own methods for it. It is driven from the thread the
 * engine is.
 */
internal interface MapSurface {
    /**
     * The renderer's own zoom limits: the floor under which a window may not reach and to which a released
     * floor goes back, and the ceiling a released ceiling goes back to.
     */
    val zoomLimits: ZoomBounds

    /** The renderer's projection as the map is drawn, from a position to the point of the canvas it is drawn at and back. */
    val projection: ScreenProjection

    /** Moves the camera to the members [target] states by [motion], keeping every other member where the camera has it. */
    fun move(
        target: CameraTarget,
        motion: CameraMotion,
    )

    /**
     * Fits the camera to [bounds], a box in canonical form, with [fit], within [window], as `cameraForBounds`
     * fits one on the canvas, facing north and keeping its pitch, and answers the camera it moves to, or null
     * where the canvas leaves no room.
     */
    fun frame(
        bounds: List<Double>,
        fit: CameraFit,
        window: ZoomBounds,
    ): CameraPose?

    /** Whether the live style holds the layer [id]. */
    fun holdsLayer(id: String): Boolean

    /** Whether the live style holds the source [id]. */
    fun holdsSource(id: String): Boolean

    /** The features the layers [layers] drew at [point] of the canvas, topmost first, each once per layer that drew it. */
    fun renderedFeatures(
        point: List<Double>,
        layers: List<String>,
    ): List<QueriedFeature>

    /** The label of every symbol layer the live style holds, as its style wrote it, bottom to top, the map's own included. */
    val symbolLabels: List<SymbolLabel>

    /** Sets the label of the symbol layer [id] to [label], or back to the one its style wrote where [label] is null. */
    fun relabel(
        id: String,
        label: JsonElement?,
    )

    /**
     * Every source of the base map's style the map credits, in the order the renderer reads them, and whether a
     * layer draws it at the camera's zoom.
     */
    val baseSources: List<CreditedSource>
}
