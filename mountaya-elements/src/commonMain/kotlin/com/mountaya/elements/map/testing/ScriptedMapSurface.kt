package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.cameraForBounds
import kotlinx.serialization.json.JsonElement

/**
 * ScriptedMapSurface draws a map engine's state nowhere, in place of the MapLibre map `MapView` draws it
 * on, so a test reads what the engine does with no renderer: it answers what a renderer would from what the
 * test states, records what the engine asks of it, and plays what happens on a canvas, a gesture, a frame, a
 * failed request, a tile or a style loaded, which the engine tells as `MapView`'s renderer has it tell them.
 *
 * ```
 * val map = MapEngine(mountaya)
 * val surface = ScriptedMapSurface(map)
 * surface.rendered = listOf(QueriedFeature(mapOf("segment_id" to JsonPrimitive("climb")), line, layer = "route"))
 * map.addLayer(MapLayer("route", TileSchema.TileLayerType.Line, "trail", rank = 50))
 * surface.click(listOf(120.0, 80.0), listOf(6.86, 45.83))
 * ```
 *
 * Its canvas measures in density-independent pixels and its projection, unless the test states one, reads a
 * position as its point, a longitude an x and a latitude a y. A fit lands where `cameraForBounds` frames the
 * box on its canvas, facing north and keeping the pitch the engine holds. It is driven from the thread the
 * engine is, and the engine draws on it from the moment it is made until another surface is.
 *
 * @param engine The map it draws.
 * @param canvas The size the map is drawn at, `[width, height]` in density-independent pixels.
 * @param zoomLimits The zoom limits of the renderer it plays, MapLibre Native's unless the test states another's.
 */
public class ScriptedMapSurface(
    engine: MapEngine,
    canvas: List<Double> = listOf(800.0, 600.0),
    zoomLimits: ZoomBounds = ZoomBounds(0.0, 25.5),
) {
    /** Move is a camera move the engine handed the surface: the members it states, and how it moves. Two are equal where both are. */
    public class Move(
        /** The members the move states. */
        public val target: CameraTarget,
        /** How the camera moves there. */
        public val motion: CameraMotion,
    ) {
        override fun equals(other: Any?): Boolean = other is Move && other.target == target && other.motion == motion

        override fun hashCode(): Int = target.hashCode() * 31 + motion.hashCode()

        override fun toString(): String = "Move($target, $motion)"
    }

    /**
     * Frame is a box the engine asked the surface to frame, in canonical form, and the fit it framed it with.
     * Two are equal where they frame the same box, each number the same, with one fit.
     */
    public class Frame(
        /** The box, `[west, south, east, north]`. */
        public val bounds: List<Double>,
        /** The fit the box was framed with. */
        public val fit: CameraFit,
    ) {
        override fun equals(other: Any?): Boolean = other is Frame && other.bounds == bounds && other.fit == fit

        override fun hashCode(): Int = bounds.hashCode() * 31 + fit.hashCode()

        override fun toString(): String = "Frame($bounds, $fit)"
    }

    /**
     * Label is a symbol layer of the base map the surface draws: its id and its label as its style wrote it, a
     * `text-field`. Two are equal where both are.
     */
    public class Label(
        /** The symbol layer's id. */
        public val id: String,
        /** The label as the style wrote it. */
        public val textField: JsonElement,
    ) {
        override fun equals(other: Any?): Boolean = other is Label && other.id == id && other.textField == textField

        override fun hashCode(): Int = id.hashCode() * 31 + textField.hashCode()

        override fun toString(): String = "Label($id, $textField)"
    }

    /**
     * BaseSource is a source of the base map's style as a renderer reads its credit (`map.style.attribution`): the
     * credit it states, its TileJSON's where the style names it by its TileJSON's address and states none of its
     * own, as MapLibre Native reads one, and whether a layer of the style draws it at the camera's zoom. Two are
     * equal where both are.
     */
    public class BaseSource(
        /** The credit the source states, or null where it states none. */
        public val attribution: String?,
        /** Whether a layer of the style draws the source at the camera's zoom. */
        public val isDrawn: Boolean,
    ) {
        override fun equals(other: Any?): Boolean =
            other is BaseSource && other.attribution == attribution && other.isDrawn == isDrawn

        override fun hashCode(): Int = (attribution?.hashCode() ?: 0) * 31 + isDrawn.hashCode()

        override fun toString(): String = "BaseSource($attribution, isDrawn=$isDrawn)"
    }

    private val engine: MapEngine = engine

    /** The renderer's own zoom limits the surface plays: the floor no window reaches under, and the ceiling a window released goes back to. */
    public val zoomLimits: ZoomBounds = zoomLimits

    /** The size the map is drawn at, `[width, height]` in density-independent pixels. */
    public var canvas: List<Double> = canvas

    /** The projection the map is drawn through, a position read as its point unless the test states another. */
    public var projection: ScreenProjection = ScreenProjection(point = { it }, position = { it })

    /** Every feature the surface draws under any point, topmost first, of which a query answers those its layers drew. */
    public var rendered: List<QueriedFeature> = emptyList()

    /** The id of every layer the live style holds, or null for every layer the engine draws. */
    public var liveLayers: List<String>? = null

    /** The id of every source the live style holds, or null for every source the engine holds. */
    public var heldSources: List<String>? = null

    /** Every symbol layer of the base map, bottom to top, under the map's own. */
    public var baseLabels: List<Label> = emptyList()

    /** The sources of the base map's style, in the order a renderer reads them, whose credits the map shows. */
    public var baseSources: List<BaseSource> = emptyList()

    private val movesHeld = mutableListOf<Move>()
    private val framesHeld = mutableListOf<Frame>()
    private val queriesHeld = mutableListOf<List<String>>()
    private val relabelledHeld = mutableListOf<String>()

    /** Every camera move the engine handed the surface, in order. */
    public val moves: List<Move> get() = movesHeld.toList()

    /** Every box the engine asked the surface to frame, in order. */
    public val frames: List<Frame> get() = framesHeld.toList()

    /** The layers each point query asked the surface about, in order. */
    public val queries: List<List<String>> get() = queriesHeld.toList()

    /**
     * The id of every symbol layer the engine gave a label of its own language, in order; a label put back as
     * its style wrote it is not among them.
     */
    public val relabelled: List<String> get() = relabelledHeld.toList()

    /** What the engine asks, answered from what the test states. */
    private val surface =
        object : MapSurface {
            override val zoomLimits: ZoomBounds get() = this@ScriptedMapSurface.zoomLimits

            override val projection: ScreenProjection get() = this@ScriptedMapSurface.projection

            override fun move(
                target: CameraTarget,
                motion: CameraMotion,
            ) {
                movesHeld += Move(target, motion)
            }

            override fun frame(
                bounds: List<Double>,
                fit: CameraFit,
                window: ZoomBounds,
            ): CameraPose? {
                framesHeld += Frame(bounds, fit)
                val viewport =
                    cameraForBounds(bounds, canvas, window, fit.padding ?: PixelPadding(0.0), fit.maxZoom)
                        ?: return null
                return CameraPose(viewport.center, viewport.zoom, engine.state.value.camera.pitch, 0.0)
            }

            override fun holdsLayer(id: String): Boolean =
                liveLayers?.let { id in it } ?: engine.state.value.layers
                    .any { it.id == id }

            override fun holdsSource(id: String): Boolean =
                heldSources?.let { id in it } ?: engine.state.value.sources
                    .any { it.id == id }

            override fun renderedFeatures(
                point: List<Double>,
                layers: List<String>,
            ): List<QueriedFeature> {
                queriesHeld += layers
                return rendered.filter { it.layer in layers }
            }

            override val symbolLabels: List<SymbolLabel>
                get() {
                    val base = baseLabels.map { SymbolLabel(it.id, it.textField) }
                    val own =
                        engine.state.value.layers.mapNotNull { layer ->
                            val label = layer.layout["text-field"]
                            if (layer.type != TileSchema.TileLayerType.Symbol || label == null) {
                                null
                            } else {
                                SymbolLabel(layer.id, label)
                            }
                        }
                    return base + own
                }

            override fun relabel(
                id: String,
                label: JsonElement?,
            ) {
                if (label != null) relabelledHeld += id
            }

            override val baseSources: List<CreditedSource>
                get() = this@ScriptedMapSurface.baseSources.map { CreditedSource(it.attribution, it.isDrawn) }
        }

    init {
        engine.attach(surface)
    }

    /** Plays a click at [point] of the canvas, where the renderer read [position]. */
    public fun click(
        point: List<Double>,
        position: List<Double>,
    ) {
        engine.clicked(point, position)
    }

    /** Plays a long press, or a secondary click, at [point] of the canvas, where the renderer read [position]. */
    public fun longClick(
        point: List<Double>,
        position: List<Double>,
    ) {
        engine.longClicked(point, position)
    }

    /** Plays a hovering pointer moving to [point] of the canvas, where the renderer read [position]. */
    public fun hover(
        point: List<Double>,
        position: List<Double>,
    ) {
        engine.pointerMoved(point, position)
    }

    /** Plays the next frame the map draws. */
    public fun drawFrame() {
        engine.frameDrawn()
    }

    /** Plays a hovering pointer leaving the canvas at [point], where the renderer read [position]. */
    public fun leave(
        point: List<Double>,
        position: List<Double>,
    ) {
        engine.pointerLeft(point, position)
    }

    /**
     * Plays a request the renderer made for itself failing: to [url], carrying [status] where an answer carried
     * one, for a tile of [source] where it was a tile's, and sent under a credential the client no longer sends
     * where [superseded] is true, a session token since replaced or minted after the request left under the key
     * alone, or a visitor's cookie since changed, as `MapView`'s renderer reads the credential each request left
     * with: a 401 or a 403 of such a request is told to nobody, and every other failure as it is. A request whose
     * credential could not be read never leaves, and [cause] is what the read threw: whatever [status] it would
     * have met, it is told as a `MapEvent.Failure` carrying [cause] and no status.
     */
    public fun failRequest(
        url: String?,
        status: Int?,
        source: String?,
        superseded: Boolean = false,
        cause: Throwable? = null,
    ) {
        engine.requestFailed(url, status, source, superseded, cause)
    }

    /** Plays a tile of [source] loading. */
    public fun loadTile(source: String) {
        engine.tileLoaded(source)
    }

    /** Plays a style loading, the base map the engine asked for last, its labels being [baseLabels] as it loads. */
    public fun loadStyle() {
        engine.styleLoaded()
    }

    /**
     * Plays the document of the base map the engine asked for last not arriving, which gives the map back the
     * base map shown (`map.basemap.replay`).
     */
    public fun failStyle() {
        engine.styleFailed()
    }

    override fun toString(): String = "ScriptedMapSurface(canvas=$canvas, zoomLimits=$zoomLimits)"
}
