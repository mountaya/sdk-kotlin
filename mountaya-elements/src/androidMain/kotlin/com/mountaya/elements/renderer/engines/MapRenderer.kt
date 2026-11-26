package com.mountaya.elements

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.Url
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.HexColor
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.WEB_MERCATOR_TILE_SIZE_PIXELS
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.cameraForBounds
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.themeTokenValue
import com.mountaya.geo.wrapLongitude
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdate
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.AttributionDialogManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.HillshadeLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LayoutPropertyValue
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PaintPropertyValue
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterDemSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.Source
import org.maplibre.android.style.sources.TileSet
import org.maplibre.android.style.sources.VectorSource
import org.maplibre.android.style.types.Formatted
import org.maplibre.android.tile.TileOperation
import org.maplibre.geojson.Feature
import java.lang.ref.WeakReference
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sinh
import kotlin.math.tan

/**
 * MapRenderer draws a map engine's state on MapLibre's own map, and is the surface the engine asks what
 * only a renderer knows and tells what happens on the map.
 *
 * It draws every state the engine reports: the base map's style, the camera, and each source and layer
 * above the base map in the order the one layer registry ranks them (`geo.layers.order`), every one above
 * the base map's own layers. A layer is MapLibre's style layer of its type, each property set under its
 * style name and read by MapLibre's own style parser, as a style document's is, so a patch the engine
 * merged is the property the map draws and `JsonNull` sets one back to the style's default. A value the
 * parser refuses is left unset, as MapLibre leaves it, and logged by MapLibre. The engine relabels the base
 * map's labels through it, never a layer of the map's own (`map.style.label-scope`).
 *
 * It moves MapLibre's camera as the engine hands it a move, at once, along an ease or a flight, frames a
 * box on its own canvas as `cameraForBounds` fits one (`geo.camera.fit`), and holds the camera to the
 * state's pan lock and zoom range, what the view shows kept within the lock. It answers a point query from
 * MapLibre's rendered features, layer by layer, topmost first, and projects through MapLibre's projection.
 *
 * It shows each marker and the waypoint being placed as pins of its own, views above MapLibre's map and so
 * above every layer, which no style change takes off and no relabelling reaches, on a layer beside MapLibre's own view
 * in the one the composable hosts, [view], so nothing MapLibre draws over its own view covers them or the watermark.
 * It tells the engine of each click and long click, of a hovering pointer and the next frame while one waits, of a
 * request of the map's own that failed, of a tile and a style loaded, and of the camera at rest after the reader moved
 * it; it hands [onClick] each click the engine tells, showing the label of the marker it fell on beside it, inside the
 * map's safe area and clear of the watermark's frame, and [onCameraChange] each camera at rest, whoever moved it.
 *
 * It answers an image the style names and does not hold with the stripe tile of the restricted stretch's kind
 * the id names (`map.icons.stripe-pattern`), or the marker sprite the id names, drawn from the engine's glyph
 * set (`map.icons.missing-image`), so an itinerary's restricted stretches, its waypoints and a map's points of
 * interest show on a map no editor draws. It shows each marker as its pin (`map.marker.pin`), its glyph drawn
 * from the same set. Its credit button lists what the engine credits, Mountaya first (`map.style.attribution`).
 *
 * It holds the process's request hook for the engine's client from its start to its close, beside every
 * other live map, over [okHttpClient] where the app gives one, so the map's own requests to its client's
 * Tile API carry its credential and its language, and the failures of the requests it claims, its sources'
 * tiles and what sits beside its style, reach the engine, and hands the engine the device's location
 * services where the app gave it none. It lives on the main
 * thread, where MapLibre's map does, and is driven there: [draw] by the engine's states, and [drive] by the
 * host's lifecycle.
 */
internal class MapRenderer(
    private val engine: MapEngine,
    /** MapLibre's own view, which the composable hosts inside [view]. */
    val mapView: MapView,
    okHttpClient: OkHttpClient?,
    private val onReady: (MapLibreMap) -> Unit,
) : MapSurface {
    /** MapLibre's map, null until the view has made it. */
    var map: MapLibreMap? = null
        private set

    /** The state last drawn, null before the first. */
    var drawn: MapState? = null
        private set

    /** The style MapLibre holds once it is loaded, which takes sources and layers, or null while one loads. */
    private var style: Style? = null

    /** The id of each feature the loaded style's GeoJSON sources hold inline, as its document writes it, by source. */
    private var writtenIds: Map<String, Map<String, QueriedFeature.Id>> = emptyMap()

    /** The style asked of MapLibre, counted so a style superseded before it loaded is never drawn on. */
    private var styleAsked = 0
    private var styleShown: Url? = null

    /**
     * Whether the style asked last was asked for again, its request refused under a credential the client no longer
     * sends, which happens once an ask.
     */
    private var isStyleAskedAgain = false

    /**
     * The style ask whose request the hook told a failure of, a refusal, a failure or one given up, or 0 where it
     * told none: a style that fails to load with none told was answered, and MapLibre did not read the document.
     */
    private var styleFailureHeard = 0
    private var isReady = false
    private var isClosed = false

    /** The camera last drawn or reported, which a state holding it does not move again. */
    private var camera: CameraPose? = null
    private var isGesture = false

    /** The state last applied to MapLibre's map, its limits and its pins, null before the map is made. */
    private var applied: MapState? = null

    /**
     * The state the layer the pins stand on is laid out from: the one applied to MapLibre's map, or before MapLibre has
     * handed it over, the one drawn last, so the watermark is laid out on a map MapLibre has not made yet too.
     */
    private val laid: MapState?
        get() = applied ?: drawn

    /** The zoom range and the pan lock last handed to MapLibre's map, null before the first. */
    private var heldLimits: Pair<ZoomBounds, List<Double>?>? = null

    /** Told each click the engine tells, which no edit took: the composable's `onClick`, as the host composed it last. */
    var onClick: ((MapTap) -> Unit)? = null

    /** Told where the camera came to rest after each move, whoever moved it: the composable's `onCameraChange`. */
    var onCameraChange: ((CameraPose) -> Unit)? = null

    /**
     * The Mountaya watermark (`geo.organization.watermark`), drawn above every pin and beneath the label a marker
     * shows, at its frame from the top-left corner of the map's safe area, [safeArea], or null where the map draws
     * none. A new one is laid out at once, with the pins, whether or not MapLibre has handed its map over: the mark
     * follows the read of the map's organization alone, and stands on the safe area, never on the map's projection.
     */
    var watermark: WatermarkView? = null
        set(value) {
            if (value === field) return
            field = value
            val state = laid ?: return
            if (!isClosed) annotate(state)
        }

    /**
     * The map's safe area, the part of the map the device leaves visible, `[left, top, right, bottom]` in
     * density-independent pixels from the map's own top-left corner, or null before the composable measured it: the
     * window's insets the map lies under, as the composable reads them, which the watermark's frame is measured from and
     * a marker's label, MapLibre's credit button and its logo keep to. A safe area moved lays the mark out again,
     * places the label again and moves the credit button and the logo with it.
     */
    var safeArea: List<Double>? = null
        set(value) {
            if (value == field) return
            field = value
            if (isClosed) return
            placeOrnaments()
            val state = laid ?: return
            if (watermark != null) annotate(state) else place()
        }

    /**
     * The watermark's frame a marker's label keeps clear of, `[x, y, width, height]` in density-independent pixels from
     * the map's own top-left corner, or null where the map keeps none clear, its organization holding white label
     * (`geo.organization.watermark`). A frame moved places the label again.
     */
    var keepClear: List<Double>? = null
        set(value) {
            if (value == field) return
            field = value
            if (!isClosed && applied != null) place()
        }

    /**
     * The colour a marker naming none is drawn in, and the ring of the waypoint being placed: the elements' `accent`
     * token as the host's tokens state it, read under the scheme in force (`geo.style.theme-tokens`), which the
     * composable hands over as it is composed. A new one draws again every pin it colours, and only those.
     */
    var accent: HexColor = themeTokenValue(ElementTokens.standard.accent, ThemeScheme.Light)
        set(value) {
            if (value == field) return
            field = value
            val state = applied ?: return
            if (!isClosed) annotate(state)
        }

    /**
     * The ground a marker's label is drawn on: the elements' `tooltip-bg` token as the host's tokens state it, read
     * under the scheme in force (`geo.style.theme-tokens`), which the composable hands over as it is composed, as
     * `sdk-swift` and `sdk-dart` draw theirs. A new one paints the label again.
     */
    var tooltipBg: HexColor = themeTokenValue(ElementTokens.standard.tooltipBg, ThemeScheme.Light)
        set(value) {
            if (value == field) return
            field = value
            paintCallout(callout, value, tooltipFg)
        }

    /**
     * The colour a marker's label is written in: the elements' `tooltip-fg` token, read as [tooltipBg] is. A new one
     * paints the label again.
     */
    var tooltipFg: HexColor = themeTokenValue(ElementTokens.standard.tooltipFg, ThemeScheme.Light)
        set(value) {
            if (value == field) return
            field = value
            paintCallout(callout, tooltipBg, value)
        }

    /** The pins of the markers and of the waypoint being placed, and a label, drawn above every layer of MapLibre's map. */
    val pins: PinLayer = PinLayer(mapView.context)

    /**
     * The view the composable hosts: MapLibre's own, [mapView], and above it, beside it, the layer the pins, the
     * watermark and a marker's label stand on, [pins]. The layer stands outside MapLibre's view, so nothing MapLibre
     * draws over its own view reaches it: MapLibre covers its view in its load colour until its style has loaded and
     * three frames are drawn, and for good where its surface never draws, and the mark shows above that from the first
     * draw.
     */
    val view: FrameLayout = FrameLayout(mapView.context)

    /**
     * The image of each look the markers shown carry, drawn once and shared by every marker of that look, and kept for
     * as long as a marker shown carries it, its glyph drawn from the engine's glyph set on [accent] where it names no
     * colour.
     */
    internal val pinImages: PinImages<Bitmap> =
        PinImages { look -> markerPinImage(look.pin, accent, engine.glyphs, look.densityDpi) }

    /** The label of the marker last clicked, shown beside it, in [tooltipFg] on [tooltipBg]. */
    val callout: TextView = calloutOf(mapView.context).also { paintCallout(it, tooltipBg, tooltipFg) }

    /** The id of the marker whose label is shown, or null where none is. */
    private var calloutMarker: String? = null

    /** The bounds MapLibre's camera target was last held within, so a camera move hands them again only where they moved. */
    private var target: LatLngBounds? = null

    /** True while the renderer hands MapLibre a camera limit, whose own move is no reader's. */
    private var limiting = false

    private val density: Float
        get() = mapView.resources.displayMetrics.density

    /** Where the host's lifecycle has brought MapLibre's view. */
    private var lifecycle = Lifecycle.State.INITIALIZED

    private val sources = mutableMapOf<String, Drawn<Source, MapSource>>()
    private val layers = mutableMapOf<String, Drawn<Layer, MapLayer>>()

    /** The label of each symbol layer of the base map, as its style wrote it. */
    private val baseLabels = mutableMapOf<String, JsonElement>()

    /** Tells the engine of the next frame drawn while a hovering pointer waits on one, or null while none is asked for. */
    private var frameCallback: Choreographer.FrameCallback? = null

    /** The main thread's queue, which a failure heard on a thread of the request hook's is handed to the engine through. */
    private val main = Handler(Looper.getMainLooper())

    /**
     * The failures the request hook hears for the map, on OkHttp's threads, carried to the main thread a batch at a
     * time, one hop for every failure heard before it runs, and each told to the engine there, in the order heard.
     */
    internal val heard: HeardFailures

    /**
     * The map's hold on the process's request hook, from the renderer's start to its close: its client's
     * credential for the requests its Tile API claims, the app's hook, the app's own client, and what the map
     * claims of the answers the hook hears, written as each state is drawn.
     */
    private val hold: MapRequestHook.Hold

    /** The sources and the style the claims were last read from, so a state that moves neither reads none again. */
    private var claimedFrom: Pair<List<MapSource>, Url?>? = null

    init {
        // What the process's hook holds reaches the renderer weakly, so a map its view let go of is never kept.
        val self = WeakReference(this)
        val queue = main
        val failures =
            HeardFailures({ queue.post(it) }) { failure ->
                self.get()?.failed(failure.url, failure.status, failure.superseded, failure.cause)
            }
        heard = failures
        hold =
            MapRequestHook.Hold(
                engine.client,
                engine.transformRequest,
                okHttpClient,
            ) { url, status, superseded, cause ->
                failures.hear(HeardFailures.Failure(url, status, superseded, cause))
            }
        claim(engine.state.value)
        MapRequestHook.install(hold)
        engine.attach(this)
        if (engine.deviceGeolocation == null) engine.deviceGeolocation = DeviceGeolocation(mapView.context)
        // The pins take no touch, so every gesture reaches MapLibre's map under them.
        pins.isClickable = false
        pins.isFocusable = false
        // The layer stands beside MapLibre's view, above it, never inside it, where MapLibre's load colour covers it.
        view.addView(
            mapView,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        view.addView(
            pins,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        mapView.addOnLayoutChangeListener { _, left, top, right, bottom, wasLeft, wasTop, wasRight, wasBottom ->
            val resized = right - left != wasRight - wasLeft || bottom - top != wasBottom - wasTop
            val map = map
            val state = applied
            if (resized && map != null && state != null) limit(map, state)
            // The ornaments' margins are measured from the view's edges, so a view resized places them again, once its
            // layout is done rather than within it.
            if (resized) mapView.post { placeOrnaments() }
            place()
        }
        // MapLibre asks for an image a style names and does not hold as it lays a symbol out, on the main
        // thread, and draws the one added under its id before it goes on.
        mapView.addOnStyleImageMissingListener { id -> answerMissingImage(id) }
        // A base map whose document did not arrive gives the map back the one shown, which the renderer asks
        // MapLibre for again and draws the map's content on as it loads (`map.basemap.replay`). The hook posts what
        // it hears of the style's request before MapLibre reads the answer, so the failure is read after it, and is
        // none where that request was refused under a credential the client no longer sends and the style asked
        // for again. A style whose request the hook told nothing of was answered, a document MapLibre did not read,
        // and is told as a failure of its own address carrying no status, as `sdk-swift` tells it.
        mapView.addOnDidFailLoadingMapListener {
            if (isClosed) return@addOnDidFailLoadingMapListener
            val asked = styleAsked
            main.post {
                if (isClosed || asked != styleAsked) return@post
                val style = styleShown
                if (style != null && styleFailureHeard != asked) engine.requestFailed(style.toString(), null, null)
                engine.styleFailed()
            }
        }
        // A tile parsed is a tile of its source loaded, which ends that source's refusal of access
        // (`map.events.access-standing`). MapLibre calls it on the main thread, where its map lives.
        mapView.addOnTileActionListener { operation, _, _, _, _, _, source ->
            if (operation == TileOperation.EndParse && !isClosed) engine.tileLoaded(source)
        }
        // A pointer hovers without pressing alone, a mouse's, a trackpad's or a stylus's over the screen; a
        // finger never does, so a touch screen tells no hover.
        mapView.setOnHoverListener { _, event ->
            hovered(event)
            false
        }
        mapView.getMapAsync { made -> if (!isClosed) attach(made) }
        draw(engine.state.value)
    }

    /** Draws every state the engine reports, and hands on every click it tells, for as long as the caller's coroutine runs. */
    suspend fun follow() {
        coroutineScope {
            launch { engine.state.collect { draw(it) } }
            launch { engine.events().collect { told(it) } }
        }
    }

    /** Draws [state], each part that differs from the state drawn before. */
    fun draw(state: MapState) {
        if (isClosed) return
        drawn = state
        claim(state)
        val map = map ?: return
        val before = applied
        applied = state
        if (styleAsked == 0 || styleShown != state.style) show(map, state.style)
        val limited = hold(map, state)
        if (camera != state.camera) move(map, state.camera)
        // The first state applied sets the limits the map opens with, and the camera with them; a later
        // change of limits may move the camera the state holds, which the engine is told of.
        if (limited && before != null) reportHeldCamera(map, state.camera)
        if (before == null || before.markers != state.markers || before.waypointEdit != state.waypointEdit) {
            annotate(state)
        }
        val style = style ?: return
        reconcile(state, style)
    }

    /**
     * Drives MapLibre's view to [state] of the host's lifecycle, each step between the two in order: its
     * creation, its start, its resumption, and back down to its destruction, which is the last.
     */
    fun drive(state: Lifecycle.State) {
        while (lifecycle != state && lifecycle != Lifecycle.State.DESTROYED) {
            lifecycle =
                if (lifecycle < state) {
                    when (lifecycle) {
                        Lifecycle.State.INITIALIZED -> Lifecycle.State.CREATED.also { mapView.onCreate(null) }
                        Lifecycle.State.CREATED -> Lifecycle.State.STARTED.also { mapView.onStart() }
                        else -> Lifecycle.State.RESUMED.also { mapView.onResume() }
                    }
                } else {
                    when (lifecycle) {
                        Lifecycle.State.RESUMED -> Lifecycle.State.STARTED.also { mapView.onPause() }
                        Lifecycle.State.STARTED -> Lifecycle.State.CREATED.also { mapView.onStop() }
                        Lifecycle.State.CREATED -> Lifecycle.State.DESTROYED.also { mapView.onDestroy() }
                        else -> Lifecycle.State.DESTROYED
                    }
                }
        }
    }

    /**
     * The image the map draws for an id its style names and does not hold, at the screen's density: the stripe
     * tile of the restricted stretch's kind the id names (`map.icons.stripe-pattern`), the marker sprite the id
     * names, drawn from the engine's glyph set (`map.icons.missing-image`, `map.icons.marker-sprite`), or null
     * where it names neither, a kind the vocabulary does not hold or a glyph the set cannot draw, which MapLibre
     * leaves missing.
     */
    fun missingImage(id: String): Bitmap? {
        val densityDpi = mapView.resources.displayMetrics.densityDpi
        readRoadAccessStripeImageId(id)?.let { kind ->
            return stripePatternImage(roadAccessStripePattern(kind), densityDpi)
        }
        val set = engine.glyphs
        val named = readMarkerImageId(id, set) ?: return null
        val sprite = describeMarkerSprite(named.glyph, named.color, set) ?: return null
        val figures = glyphFigures(named.glyph, set) ?: return null
        return markerSpriteImage(sprite, figures, densityDpi)
    }

    /**
     * The pin [marker] is shown as, its glyph drawn from the engine's glyph set, on [accent] where it names no
     * colour, at the screen's density (`map.marker.pin`): the image of its look a marker shown carries already, or
     * one drawn for it.
     */
    fun pinImage(marker: MapMarker): Bitmap =
        pinImages.imageOf(marker, engine.glyphs, accent, mapView.resources.displayMetrics.densityDpi)

    /**
     * Answers MapLibre's request for the image [id], where it names a marker sprite, by adding the sprite to
     * the style under the id at once, which MapLibre draws as it goes on laying the symbol out; any other id
     * is left missing, as MapLibre leaves it.
     */
    private fun answerMissingImage(id: String) {
        if (isClosed) return
        val style = map?.style ?: return
        missingImage(id)?.let { style.addImage(id, it) }
    }

    /**
     * Stops drawing and destroys MapLibre's view, its map with it, and lets the engine go of it, so a map that
     * outlives its view holds no view. The engine is the app's, and stays as it is.
     */
    fun close() {
        if (isClosed) return
        isClosed = true
        MapRequestHook.release(hold)
        frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
        frameCallback = null
        engine.detach(this)
        drive(Lifecycle.State.DESTROYED)
    }

    /**
     * MapLibre's projection of the map as it is drawn, from a position to the point of the view it is
     * drawn at and back, in density-independent pixels, which an edit's magnet measures in
     * (`map.edit.snap`). A position that names no place, or a map not made yet, reads as no point.
     */
    override val projection: ScreenProjection
        get() {
            val density = mapView.resources.displayMetrics.density
            return ScreenProjection(
                point = { position ->
                    val longitude = position.getOrElse(0) { 0.0 }
                    val latitude = position.getOrElse(1) { 0.0 }
                    val projected = map?.projection
                    if (projected == null || !longitude.isFinite() || !latitude.isFinite() ||
                        latitude !in -90.0..90.0
                    ) {
                        listOf(Double.NaN, Double.NaN)
                    } else {
                        val point = projected.toScreenLocation(LatLng(latitude, longitude))
                        listOf(point.x.toDouble() / density, point.y.toDouble() / density)
                    }
                },
                position = { point ->
                    val x = point.getOrElse(0) { 0.0 }
                    val y = point.getOrElse(1) { 0.0 }
                    val projected = map?.projection
                    if (projected == null || !x.isFinite() || !y.isFinite()) {
                        listOf(Double.NaN, Double.NaN)
                    } else {
                        val drawnAt =
                            projected.fromScreenLocation(
                                PointF((x * density).toFloat(), (y * density).toFloat()),
                            )
                        listOf(drawnAt.longitude, drawnAt.latitude)
                    }
                },
            )
        }

    /**
     * Places a point the pointer holds at [position] as an edit's magnet places it, on the nearest point
     * of [lines] as the map draws them where it lies within the magnet's radius on the screen, and under
     * the pointer otherwise (`map.edit.snap`).
     */
    fun snapped(
        position: List<Double>,
        lines: List<List<List<Double>>>,
    ): EditSnap = snapToActiveSegments(position, lines, projection)

    /**
     * Tells the engine of a click at [point] of the view, `[x, y]` in density-independent pixels, where
     * MapLibre's projection reads the place clicked: it closes the readout, places the waypoint being edited, or
     * is told as a click, and again as the features it landed on, which [told] hands [onClick].
     */
    fun clicked(point: List<Double>) {
        if (isClosed) return
        engine.clicked(point, projection.position(point))
    }

    /**
     * Tells the engine of a long press at [point] of the view, with MapLibre's camera zoom: it is told as a long click
     * where no edit takes it, and opens the readout there where the host leaves it on.
     */
    fun longClicked(point: List<Double>) {
        if (isClosed) return
        val zoom = map?.cameraPosition?.zoom ?: engine.state.value.camera.zoom
        engine.longClicked(point, projection.position(point), zoom)
    }

    /**
     * Reads a hovering pointer's [event]: a move tells the engine where it moved, which the next frame tells
     * with the features of an itinerary under it, and a leave tells it the pointer left.
     */
    fun hovered(event: MotionEvent) {
        if (isClosed) return
        val point = listOf(event.x.toDouble() / density, event.y.toDouble() / density)
        when (event.actionMasked) {
            MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> {
                engine.pointerMoved(point, projection.position(point))
                awaitFrame()
            }

            MotionEvent.ACTION_HOVER_EXIT -> {
                engine.pointerLeft(point, projection.position(point))
            }
        }
    }

    /** Tells the engine a frame was drawn while a hovering pointer waits on one, and asks for the next while one still waits. */
    private fun frameDrawn() {
        frameCallback = null
        if (isClosed) return
        engine.frameDrawn()
        if (engine.isHoverWaiting) awaitFrame()
    }

    /** Asks the platform's frame clock for the next frame, once, which [frameDrawn] tells the engine of. */
    private fun awaitFrame() {
        if (frameCallback != null || isClosed) return
        val callback = Choreographer.FrameCallback { frameDrawn() }
        frameCallback = callback
        Choreographer.getInstance().postFrameCallback(callback)
    }

    /**
     * Tells the engine one of the map's own requests failed, to [url], carrying [status] where an answer
     * carried one, from the source of the map's own whose tiles it was where its address lies under one of
     * their templates, a base map's source named by none, and sent under a credential the client no longer
     * sends where [superseded] is true, and carrying what the client's read of its credential threw, [cause], where
     * it never left for want of one. Where the engine gives the failure up as a refusal of a credential the map no
     * longer sends and it is the style's, the style is asked for again, once an ask, so its request leaves under the
     * credential the client sends now.
     */
    fun failed(
        url: String,
        status: Int?,
        superseded: Boolean = false,
        cause: Throwable? = null,
    ) {
        if (isClosed) return
        val style = isStyle(url)
        if (style) styleFailureHeard = styleAsked
        val source = drawn?.sources?.firstOrNull { source -> tilePrefixes(source).any(url::startsWith) }
        if (engine.requestFailed(url, status, source?.id, superseded, cause) && style) askStyleAgain()
    }

    /** Whether [url] is the address of the style asked of MapLibre last, as OkHttp writes an address. */
    private fun isStyle(url: String): Boolean {
        val asked = styleShown?.toString()?.toHttpUrlOrNull() ?: return false
        return styleAsked > 0 && url.toHttpUrlOrNull() == asked
    }

    /**
     * Asks MapLibre again for the style asked last, whose request was refused under a credential the client no
     * longer sends, once an ask: the failure MapLibre reads of the refused request is then none, since it is read
     * after this ask.
     */
    private fun askStyleAgain() {
        val map = map ?: return
        if (isStyleAskedAgain) return
        show(map, styleShown)
        isStyleAskedAgain = true
    }

    /** Writes what the map drawing [state] claims of the answers the request hook hears, where its sources or its style moved. */
    private fun claim(state: MapState) {
        val from = state.sources to state.style
        val held = claimedFrom
        if (held != null && held.first === from.first && held.second == from.second) return
        claimedFrom = from
        hold.claimed = claimedAddresses(state)
    }

    /**
     * Reads an event the engine told: a click is handed to [onClick], the label of the marker it fell on
     * shown beside it, and a click anywhere else taking the label off.
     */
    private fun told(event: MapEvent) {
        if (isClosed || event !is MapEvent.Click) return
        val tap = event.tap
        val marker =
            engine.state.value.markers
                .lastOrNull { it.id == tap.marker }
        calloutMarker = marker?.takeIf { it.label != null }?.id
        callout.text = marker?.label.orEmpty()
        place()
        onClick?.invoke(tap)
    }

    override val zoomLimits: ZoomBounds get() = ZoomBounds(NATIVE_ZOOM_FLOOR, NATIVE_ZOOM_CEILING)

    override fun move(
        target: CameraTarget,
        motion: CameraMotion,
    ) {
        // The engine holds where the move lands, so the state that holds it moves nothing again.
        camera = engine.state.value.camera
        val map = map ?: return
        // The limits the engine holds now bound the move, not those the last state drawn handed MapLibre: a
        // move made under a lock lifted in the same turn would otherwise be held under it.
        hold(map, engine.state.value)
        val landing = target.applied(if (mapView.width > 0 && mapView.height > 0) shown(map) else pose(map))
        val update = cameraUpdate(landing) ?: return
        when (motion) {
            is CameraMotion.Jump -> map.moveCamera(update)
            is CameraMotion.Ease -> ease(map, update, motion.duration?.inWholeMilliseconds)
            is CameraMotion.Fly -> map.animateCamera(update)
        }
    }

    override fun frame(
        bounds: List<Double>,
        fit: CameraFit,
        window: ZoomBounds,
    ): CameraPose? {
        val canvas = listOf(mapView.width / density.toDouble(), mapView.height / density.toDouble())
        val viewport =
            cameraForBounds(bounds, canvas, window, fit.padding ?: PixelPadding(0.0), fit.maxZoom) ?: return null
        val map = map
        val framed =
            CameraPose(
                viewport.center,
                viewport.zoom,
                map?.let { pose(it).pitch } ?: engine.state.value.camera.pitch,
                0.0,
            )
        // A map not made yet moves to the camera the state holds as soon as it is.
        if (map != null) {
            camera = framed
            hold(map, engine.state.value)
            cameraUpdate(framed)?.let { ease(map, it, fit.duration?.inWholeMilliseconds) }
        }
        return framed
    }

    override fun holdsLayer(id: String): Boolean = style?.getLayer(id) != null

    override fun holdsSource(id: String): Boolean = style?.getSource(id) != null

    override fun renderedFeatures(
        point: List<Double>,
        layers: List<String>,
    ): List<QueriedFeature> {
        val map = map ?: return emptyList()
        val style = style ?: return emptyList()
        if (point.size < 2 || !point[0].isFinite() || !point[1].isFinite()) return emptyList()
        val pixel = PointF((point[0] * density).toFloat(), (point[1] * density).toFloat())
        // MapLibre answers a feature with no layer, so each layer is asked alone, the topmost first, which
        // keeps the features of a layer drawn above another before that other's.
        return style.layers.asReversed().filter { it.id in layers }.flatMap { layer ->
            map.queryRenderedFeatures(pixel, layer.id).mapNotNull { queriedFeature(it, layer) }
        }
    }

    override val symbolLabels: List<SymbolLabel>
        get() {
            val style = style ?: return emptyList()
            return style.layers.mapNotNull { layer ->
                if (layer !is SymbolLayer) return@mapNotNull null
                val written =
                    baseLabels[layer.id] ?: jsonOf(layer.textField.value)?.takeUnless { it is JsonNull }
                        ?: return@mapNotNull null
                // The base map's label is kept as its style wrote it, before a language rewrites it; the map's
                // own is read as it is drawn, and never relabelled.
                if (!layers.containsKey(layer.id)) baseLabels[layer.id] = written
                SymbolLabel(layer.id, written)
            }
        }

    override fun relabel(
        id: String,
        label: JsonElement?,
    ) {
        val layer = style?.getLayer(id) as? SymbolLayer ?: return
        val value = label ?: baseLabels[id] ?: return
        layer.setProperties(LayoutPropertyValue(TEXT_FIELD, javaValue(value)))
    }

    override val baseSources: List<CreditedSource>
        get() {
            val style = style ?: return emptyList()
            val zoom = map?.let(::shownZoom) ?: return emptyList()
            // A style's sources keep no order MapLibre publishes, so each is read in the order the first layer
            // drawing it is drawn, bottom to top.
            val order = mutableListOf<String>()
            val drawnSources = mutableSetOf<String>()
            for (layer in style.layers) {
                if (layers.containsKey(layer.id)) continue
                val source = sourceOf(layer) ?: continue
                if (source !in order) order += source
                if (layer.visibility.value != "none" && zoom >= layer.minZoom && zoom < layer.maxZoom) {
                    drawnSources += source
                }
            }
            return order.mapNotNull { id ->
                if (sources.containsKey(id)) return@mapNotNull null
                style
                    .getSource(
                        id,
                    )?.let { CreditedSource(it.attribution.takeIf(String::isNotEmpty), id in drawnSources) }
            }
        }

    /** MapLibre's camera update to [pose], or null where its centre names no place. */
    private fun cameraUpdate(pose: CameraPose): CameraUpdate? =
        cameraPosition(pose)?.let(CameraUpdateFactory::newCameraPosition)

    /** MapLibre's camera standing at [pose], or null where its centre names no place. */
    private fun cameraPosition(pose: CameraPose): CameraPosition? {
        val longitude = pose.center.getOrNull(0) ?: return null
        val latitude = pose.center.getOrNull(1) ?: return null
        if (!longitude.isFinite() || !latitude.isFinite() || latitude !in -90.0..90.0) return null
        // MapLibre Android's camera is a zoom, never an eye's altitude, so the four are set together.
        return CameraPosition
            .Builder()
            .target(LatLng(latitude, longitude))
            .zoom(pose.zoom)
            .tilt(pose.pitch)
            .bearing(pose.bearing)
            .build()
    }

    /**
     * Eases MapLibre's camera by [update] over [stated] milliseconds, MapLibre GL JS's own half second where
     * none is stated; an ease of no time is a move at once, which MapLibre Android's ease refuses to make.
     */
    private fun ease(
        map: MapLibreMap,
        update: CameraUpdate,
        stated: Long?,
    ) {
        val milliseconds = (stated ?: EASE_MILLISECONDS).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        if (milliseconds <= 0) map.moveCamera(update) else map.easeCamera(update, milliseconds)
    }

    /**
     * A feature MapLibre found, as a query answers it, drawn by [layer], or null for one whose geometry it
     * cannot read. MapLibre Android writes every feature's id as text: a vector tile's id, which the tile
     * format makes a whole number, is read back as that number, and a GeoJSON source's is read back as the
     * style's document writes it, a number or a text (`writtenFeatureIds`), the text it is where the document
     * writes none, its data an address.
     */
    private fun queriedFeature(
        feature: Feature,
        layer: Layer,
    ): QueriedFeature? {
        val written =
            try {
                Json.parseToJsonElement(feature.toJson()) as? JsonObject
            } catch (unreadable: SerializationException) {
                null
            } ?: return null
        val geometry =
            try {
                written["geometry"]?.let { Json.decodeFromJsonElement(GeoJsonGeometry.serializer(), it) }
            } catch (unreadable: SerializationException) {
                null
            } catch (unreadable: IllegalArgumentException) {
                null
            } ?: return null
        val sourceLayer = sourceLayerOf(layer)
        val id =
            feature.id()?.takeIf { it.isNotEmpty() }?.let { text ->
                // A GeoJSON source's id is read back as the style's document writes it, a number or a text.
                val written = if (sourceLayer == null) sourceOf(layer)?.let { writtenIds[it]?.get(text) } else null
                val number = if (sourceLayer != null) text.toULongOrNull()?.toDouble() else null
                written ?: if (number != null) QueriedFeature.Id.Number(number) else QueriedFeature.Id.Text(text)
            }
        val properties = (written["properties"] as? JsonObject).orEmpty()
        return QueriedFeature(properties, geometry, layer.id, sourceLayer, id)
    }

    /**
     * Shows the state's markers as their pins, the last one on top, and the pin of the waypoint being placed above them,
     * in one change of the layer they stand on. Each look is drawn once, its image shared by every marker carrying it,
     * and a marker shown already keeps its pin where its look stands, moved where its position changed: only a look
     * not shown before is drawn.
     */
    private fun annotate(state: MapState) {
        val shown =
            (0 until pins.childCount)
                .mapNotNull { pins.getChildAt(it) as? MarkerPinView }
                .associateBy { it.marker }
        val images =
            pinImages.show(state.markers, engine.glyphs, accent, mapView.resources.displayMetrics.densityDpi)
        val children = mutableListOf<Pair<View, FrameLayout.LayoutParams>>()
        for ((marker, image) in state.markers.zip(images)) {
            val kept = shown[marker.id]?.takeIf { it.image === image }
            val pin =
                kept?.also { it.position = marker.position }
                    ?: MarkerPinView(mapView.context, marker.id, marker.position, image)
            val size = (pin.side * density).roundToInt()
            children += pin to FrameLayout.LayoutParams(size, size)
        }
        val placing = state.waypointEdit?.position
        if (placing != null && placing.size > 1) {
            val color = cssColor(accent.hex, accent.opacity)?.toArgb() ?: Color.BLUE
            val pin = DraftPinView(mapView.context, placing, color)
            val size = (pin.side * density).roundToInt()
            children += pin to FrameLayout.LayoutParams(size, size)
        }
        // A label follows its marker as it changes, and goes with it.
        val labelled = state.markers.lastOrNull { it.id == calloutMarker }?.label
        if (labelled == null) calloutMarker = null
        callout.text = labelled.orEmpty()
        // The label takes the room its words need, and is placed where it stands clear of the mark.
        val labelParams =
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        // The watermark stands above every pin and beneath the label a marker shows, at its frame in the safe area. The
        // platform draws a layer's children by their height before their order, and hands a touch to the highest first,
        // so the mark is raised as high as a pin, and the label as high as the mark: each above what it follows.
        watermark?.let { shown ->
            val frame = watermarkFrame(shown.mark, safeArea ?: listOf(0.0, 0.0))
            shown.elevation = WATERMARK_ELEVATION_DP * density
            shown.translationX = (frame[0] * density).toFloat()
            shown.translationY = (frame[1] * density).toFloat()
            children +=
                shown to
                FrameLayout.LayoutParams(
                    (frame[2] * density).roundToInt(),
                    (frame[3] * density).roundToInt(),
                )
        }
        children += callout to labelParams
        pins.show(children)
        place()
    }

    /**
     * Places each pin where MapLibre's map draws its position now, and the label shown beside its marker, past it toward
     * the end its text runs to, inside the map's safe area and clear of the watermark's frame ([markerLabelOrigin]), so
     * the label is drawn above the mark and never over it. A marker whose point stands off the map shows no label
     * ([isMarkerLabelShown]) until the camera brings it back, never one held at the map's edge without its pin. Before
     * MapLibre hands its map over, and where it never does, no pin and no label has a place, so none shows; the
     * watermark stands on the safe area alone, and shows.
     */
    private fun place() {
        val projected = map?.projection
        var labelled: PointF? = null
        for (index in 0 until pins.childCount) {
            val pin = pins.getChildAt(index) as? PinView ?: continue
            pin.visibility = if (projected == null) View.INVISIBLE else View.VISIBLE
            if (projected == null) continue
            val point = projected.toScreenLocation(LatLng(pin.position[1], pin.position[0]))
            pin.translationX = point.x - pin.layoutParams.width / 2f
            pin.translationY = point.y - pin.layoutParams.height / 2f
            if (pin.marker != null && pin.marker == calloutMarker) labelled = point
        }
        // The label follows its marker's point on the map, and shows nowhere while the point stands off it.
        labelled =
            labelled?.takeIf {
                isMarkerLabelShown(
                    anchor = listOf(it.x.toDouble(), it.y.toDouble()),
                    map = listOf(mapView.width.toDouble(), mapView.height.toDouble()),
                )
            }
        callout.visibility = if (labelled == null) View.GONE else View.VISIBLE
        if (labelled == null) return
        // Measured as the layer measures it, within the layer's own size, so the place reckoned is the one it is laid at.
        callout.measure(within(pins.width), within(pins.height))
        val scale = density.toDouble()
        val bounds =
            safeArea?.map { it * scale }
                ?: listOf(0.0, 0.0, mapView.width.toDouble(), mapView.height.toDouble())
        val origin =
            markerLabelOrigin(
                anchor = listOf(labelled.x.toDouble(), labelled.y.toDouble()),
                label = listOf(callout.measuredWidth.toDouble(), callout.measuredHeight.toDouble()),
                offset = MARKER_LABEL_OFFSET_DP * scale,
                // The layer runs left to right whatever the map's direction, so the label reads the map's own.
                isRightToLeft = mapView.layoutDirection == View.LAYOUT_DIRECTION_RTL,
                bounds = bounds,
                keepClear = keepClear?.map { it * scale },
            )
        // Rounded up, so a label moved clear of the frame never reaches a pixel over it.
        callout.translationX = ceil(origin[0]).toFloat()
        callout.translationY = ceil(origin[1]).toFloat()
    }

    /** The measure a child of the layer takes along an edge of the layer [size] pixels long, none before it is laid out. */
    private fun within(size: Int): Int =
        if (size > 0) {
            View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST)
        } else {
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        }

    /**
     * Hands MapLibre's map the zoom range and the pan lock of [state] where they differ from those it was
     * handed last, and answers whether they did.
     */
    private fun hold(
        map: MapLibreMap,
        state: MapState,
    ): Boolean {
        val wanted = state.zoomBounds to state.maxBounds
        if (heldLimits == wanted) return false
        heldLimits = wanted
        limit(map, state)
        return true
    }

    /**
     * Hands MapLibre the camera's limits for [state], the view's size and the zoom shown: its zoom range,
     * from 0 at least and to 25.5 at most, which MapLibre Android takes and refuses any other, raised to
     * the zoom at which the view fits within the pan lock; and the bounds the lock leaves the camera's
     * target at the zoom shown (MapLibre Android bounds the target and never the screen).
     */
    private fun limit(
        map: MapLibreMap,
        state: MapState,
    ) {
        limiting = true
        try {
            val lock = state.maxBounds
            val ceiling = min(max(state.zoomBounds.maxZoom, NATIVE_ZOOM_FLOOR), NATIVE_ZOOM_CEILING)
            val floor = min(max(max(state.zoomBounds.minZoom, NATIVE_ZOOM_FLOOR), coverZoom(lock)), ceiling)
            // MapLibre refuses a floor above its ceiling, so the ceiling is set first where the floor rises
            // past the one held.
            if (floor > map.maxZoomLevel) {
                map.setMaxZoomPreference(ceiling)
                map.setMinZoomPreference(floor)
            } else {
                map.setMinZoomPreference(floor)
                map.setMaxZoomPreference(ceiling)
            }
            target(map, lock)
        } finally {
            limiting = false
        }
    }

    /** Hands MapLibre the bounds [lock] leaves the camera's target at the zoom shown, or none, where they moved. */
    private fun target(
        map: MapLibreMap,
        lock: List<Double>?,
    ) {
        val bounds = lock?.let { targetBounds(it, shownZoom(map)) }
        if (bounds == target) return
        target = bounds
        val was = limiting
        limiting = true
        try {
            map.setLatLngBoundsForCameraTarget(bounds)
        } finally {
            limiting = was
        }
    }

    /**
     * The bounds the camera's target is held within so the view, at [zoom], stays within [lock],
     * `[west, south, east, north]`, measured on Web Mercator. The target stands at the middle of
     * MapLibre's frame, of which the view is the top-left part ([frame]), so the view reaches half the
     * frame ahead of the target, west and north, and the rest of its own size behind it, east and south:
     * the lock is narrowed by each, or the target held where the view stands centred on the lock along an
     * axis the view is wider than. A view not laid out yet holds the target within the lock itself. The
     * view is read as it faces north and looks straight down.
     */
    private fun targetBounds(
        lock: List<Double>,
        zoom: Double,
    ): LatLngBounds {
        // A density-independent pixel's share of Web Mercator's world at [zoom].
        val pixel = if (zoom.isFinite()) 1.0 / (WEB_MERCATOR_TILE_SIZE_PIXELS * 2.0.pow(zoom)) else 0.0
        val (frameWidth, frameHeight) = frame()
        val viewWidth = mapView.width / density.toDouble()
        val viewHeight = mapView.height / density.toDouble()
        val west = mercatorX(lock[0])
        val east = mercatorX(lock[2])
        val north = mercatorY(lock[3])
        val south = mercatorY(lock[1])
        val (left, right) = narrowed(west, east, frameWidth / 2.0 * pixel, (viewWidth - frameWidth / 2.0) * pixel)
        val (top, bottom) = narrowed(north, south, frameHeight / 2.0 * pixel, (viewHeight - frameHeight / 2.0) * pixel)
        return LatLngBounds.from(latitudeOf(top), longitudeOf(right), latitudeOf(bottom), longitudeOf(left))
    }

    /**
     * MapLibre's own frame, its width and height in density-independent pixels. MapLibre Android sizes its
     * map in whole ones, the view's size rounded up, and stands its camera's target at the frame's middle,
     * so the view shows the frame's top-left part and the target stands up to half a pixel right of and
     * below the view's middle.
     */
    private fun frame(): Pair<Double, Double> =
        ceil((mapView.width.toFloat() / density).toDouble()) to ceil((mapView.height.toFloat() / density).toDouble())

    /**
     * The zoom MapLibre's camera stands at: the one it caches, held within the zoom range it holds now,
     * since a range handed to it moves its camera without refreshing the camera it caches ([shown]).
     */
    private fun shownZoom(map: MapLibreMap): Double {
        val least = map.minZoomLevel
        val most = map.maxZoomLevel
        val cached = map.cameraPosition.zoom
        return if (least > most) most else cached.coerceIn(least, most)
    }

    /**
     * Where MapLibre's camera stands, read off MapLibre rather than off the camera it caches
     * (`MapLibreMap.cameraPosition`), which MapLibre Android refreshes after a move of its own and never
     * after one its limits make: the target is the place drawn at the middle of its [frame], the zoom
     * [shownZoom]'s, and the tilt and bearing, which no limit moves, the cached ones.
     */
    private fun shown(map: MapLibreMap): CameraPose {
        val cached = pose(map)
        val (frameWidth, frameHeight) = frame()
        val middle =
            map.projection.fromScreenLocation(
                PointF((frameWidth / 2.0 * density).toFloat(), (frameHeight / 2.0 * density).toFloat()),
            )
        return CameraPose(listOf(middle.longitude, middle.latitude), shownZoom(map), cached.pitch, cached.bearing)
    }

    /**
     * The zoom at which the view fits within [lock] on both axes, under which the view would show more than
     * the lock holds; 0 where there is no lock or the view is not laid out.
     */
    private fun coverZoom(lock: List<Double>?): Double {
        if (lock == null || mapView.width <= 0 || mapView.height <= 0) return NATIVE_ZOOM_FLOOR
        val across = (mercatorX(lock[2]) - mercatorX(lock[0])) * WEB_MERCATOR_TILE_SIZE_PIXELS
        val down = (mercatorY(lock[1]) - mercatorY(lock[3])) * WEB_MERCATOR_TILE_SIZE_PIXELS
        if (across <= 0.0 || down <= 0.0) return NATIVE_ZOOM_FLOOR
        return log2(max(mapView.width / density / across, mapView.height / density / down))
    }

    /**
     * Tells the engine where MapLibre holds the camera, where its limits moved it from [wanted], so the
     * state is the map as it is shown. It tells nothing where the engine no longer holds [wanted]: a camera
     * the app set since is the one to show, and the renderer moves to it as it draws the state that holds it.
     */
    private fun reportHeldCamera(
        map: MapLibreMap,
        wanted: CameraPose,
    ) {
        // A map not yet laid out holds no camera worth reading.
        if (mapView.width <= 0 || mapView.height <= 0 || wanted.center.size < 2) return
        // The flow hands the renderer the newest state, but a state drawn may still be one the engine has
        // left, its camera set again since; held over that camera, the report would undo it.
        if (engine.state.value.camera != wanted) return
        val shown = shown(map)
        // A longitude is compared as the meridian it names, whichever turn of the world each is written in.
        val moved =
            abs(wrapLongitude(shown.center[0] - wanted.center[0])) > HELD_TOLERANCE ||
                abs(shown.center[1] - wanted.center[1]) > HELD_TOLERANCE ||
                abs(shown.zoom - wanted.zoom) > HELD_TOLERANCE
        if (!moved) return
        camera = shown
        engine.cameraMoved(shown)
    }

    /**
     * PinView is a pin of the renderer's own, a view standing above every layer of MapLibre's map, centred on
     * [position]. It takes no touch, so a click on it reaches MapLibre's map.
     */
    internal abstract class PinView(
        context: Context,
        /** The id of the marker it is, or null for the waypoint being placed. */
        val marker: String?,
        /** Where the pin stands, `[longitude, latitude]`, which a marker moved changes on the pin it keeps. */
        var position: List<Double>,
        /** The pin's side, in density-independent pixels. */
        val side: Float,
    ) : View(context) {
        init {
            isClickable = false
            isFocusable = false
        }
    }

    /**
     * PinLayer is the layer the pins, the watermark and the label stand on, above MapLibre's map, which takes a new set
     * of them in one change: every view laid out once, a view it holds already kept in its window and moved rather than
     * drawn again. It clips what it holds to the map, so the watermark's margin falls outside it.
     *
     * It is laid out left to right whatever way the text around the map runs, so each child it holds stands at the
     * map's left edge before the renderer moves it to its place, every place reckoned from the map's top-left corner: the
     * mark stays at the reader's top left and a pin over its position in a layout that runs right to left too.
     */
    internal class PinLayer(
        context: Context,
    ) : FrameLayout(context) {
        init {
            clipChildren = true
            layoutDirection = LAYOUT_DIRECTION_LTR
        }

        /**
         * Shows [children] in their order, bottom to top, in place of the views it shows, laid out in one pass. A view
         * it shows already stays attached where it keeps its place among those [children] keeps, and takes its new
         * parameters there; only a view [children] drops is taken off, and only a view it does not show, or one standing
         * out of the order, is added.
         */
        fun show(children: List<Pair<View, LayoutParams>>) {
            val wanted = children.mapTo(HashSet()) { it.first }
            for (index in childCount - 1 downTo 0) {
                val child = getChildAt(index)
                if (child !in wanted) removeViewInLayout(child)
            }
            children.forEachIndexed { index, (child, params) ->
                when {
                    getChildAt(index) === child -> {
                        child.layoutParams = params
                    }

                    child.parent === this -> {
                        removeViewInLayout(child)
                        addViewInLayout(child, index, params, true)
                    }

                    else -> {
                        addViewInLayout(child, index, params, true)
                    }
                }
            }
            requestLayout()
            invalidate()
        }
    }

    /**
     * MarkerPinView is how a marker is shown: its pin, [image], centred on its position, over a soft shadow that
     * lifts it off the map, as the web map's pin is (`map.marker.pin`). The shadow is the platform's own, cast by
     * the disc's outline raised [PIN_ELEVATION_DP].
     */
    internal class MarkerPinView(
        context: Context,
        marker: String,
        position: List<Double>,
        /** The pin as drawn. */
        val image: Bitmap,
    ) : PinView(context, marker, position, MARKER_PIN_DIAMETER.toFloat()) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val box = Rect()

        init {
            outlineProvider =
                object : ViewOutlineProvider() {
                    override fun getOutline(
                        view: View,
                        outline: Outline,
                    ) {
                        outline.setOval(0, 0, view.width, view.height)
                    }
                }
            elevation = PIN_ELEVATION_DP * resources.displayMetrics.density
        }

        override fun onDraw(canvas: Canvas) {
            box.set(0, 0, width, height)
            canvas.drawBitmap(image, null, box, paint)
        }
    }

    /** DraftPinView is how the waypoint being placed is shown: a white disc ringed in [color], the accent. */
    internal class DraftPinView(
        context: Context,
        position: List<Double>,
        /** The ring's colour, as Android writes one. */
        val color: Int,
    ) : PinView(context, null, position, DRAFT_PIN_SIDE_DP) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            val ring = DRAFT_RING_DP * resources.displayMetrics.density
            val radius = min(width, height) / 2f
            paint.color = color
            canvas.drawCircle(width / 2f, height / 2f, radius, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(width / 2f, height / 2f, radius - ring, paint)
        }
    }

    /** Takes MapLibre's map once its view has made it, and draws the state held on it. */
    private fun attach(made: MapLibreMap) {
        map = made
        made.addOnCameraMoveStartedListener { reason ->
            isGesture =
                reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE
            // A camera move closes the readout as it starts, the reader's or the application's.
            engine.cameraMoveStarted()
        }
        made.addOnCameraMoveListener {
            val lock = applied?.maxBounds
            if (!limiting && lock != null) target(made, lock)
            place()
        }
        made.addOnCameraIdleListener {
            val pose = pose(made)
            if (isGesture) {
                isGesture = false
                camera = pose
                engine.cameraMoved(pose)
            }
            place()
            onCameraChange?.invoke(pose)
        }
        made.addOnMapClickListener { clickedAt ->
            val point = made.projection.toScreenLocation(clickedAt)
            clicked(listOf(point.x.toDouble() / density, point.y.toDouble() / density))
            // The app's own listeners, handed MapLibre's map through `onReady`, are told the click too.
            false
        }
        made.addOnMapLongClickListener { pressedAt ->
            val point = made.projection.toScreenLocation(pressedAt)
            longClicked(listOf(point.x.toDouble() / density, point.y.toDouble() / density))
            false
        }
        // The credit button lists what the engine credits, Mountaya first, in place of the sources MapLibre
        // reads off the style alone.
        made.uiSettings.setAttributionDialogManager(Credits(mapView.context, made))
        // MapLibre's own compass shows on a map turned off north, beside the one `MapControls` draws, so it is
        // turned off, as `sdk-swift` and `sdk-dart` turn theirs off; the credit button sits bottom-right, where
        // theirs sit, MapLibre's logo keeping the bottom-left, both inside the map's safe area. Both corners are
        // physical, as `MapCorner`'s are, so a layout that runs right to left moves neither.
        made.uiSettings.isCompassEnabled = false
        made.uiSettings.attributionGravity = Gravity.BOTTOM or Gravity.RIGHT
        made.uiSettings.logoGravity = Gravity.BOTTOM or Gravity.LEFT
        placeOrnaments()
        drawn?.let(::draw)
    }

    /**
     * Places MapLibre's credit button, and its logo where it shows, [CREDIT_MARGIN_DP] inside the map's safe
     * area, [safeArea], or inside the view's edges before the composable has measured it. MapLibre lays its
     * ornaments out from its view's edges and applies no window inset, so on a map drawn edge to edge the
     * navigation bar would cover them, where `sdk-swift`'s MapLibre anchors its ornaments to the safe area and
     * `sdk-dart` places its credits inside it. Each margin is set on all four edges, so either ornament keeps
     * inside the safe area in whichever corner its gravity puts it, whichever way the layout runs.
     */
    private fun placeOrnaments() {
        val made = map ?: return
        if (isClosed) return
        val (left, top, right, bottom) = ornamentInsets().map { ((it + CREDIT_MARGIN_DP) * density).roundToInt() }
        // MapLibre sets the first margin it is handed as the start's too and the third as the end's, so in a layout
        // that runs right to left the left edge's is handed third.
        val rightToLeft = mapView.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val start = if (rightToLeft) right else left
        val end = if (rightToLeft) left else right
        made.uiSettings.setAttributionMargins(start, top, end, bottom)
        if (made.uiSettings.isLogoEnabled) made.uiSettings.setLogoMargins(start, top, end, bottom)
    }

    /**
     * How far the map's safe area stands inside each edge of MapLibre's view, `[left, top, right, bottom]` in
     * density-independent pixels: none on any edge before the composable measured it, and none on the right and
     * bottom edges before the view is laid out, whose size they are read from.
     */
    private fun ornamentInsets(): List<Double> {
        val area = safeArea ?: return listOf(0.0, 0.0, 0.0, 0.0)
        if (mapView.width <= 0 || mapView.height <= 0) return listOf(area[0], area[1], 0.0, 0.0)
        val width = mapView.width / density.toDouble()
        val height = mapView.height / density.toDouble()
        return listOf(
            area[0],
            area[1],
            (width - area[2]).coerceAtLeast(0.0),
            (height - area[3]).coerceAtLeast(0.0),
        )
    }

    /**
     * Credits is the map's credit button: a sheet of what the engine credits (`map.style.attribution`), each
     * credit's link opened in the app the device opens a link in, where MapLibre's own lists its style's
     * sources alone. A sheet is shown only over an activity still on screen.
     */
    internal inner class Credits(
        private val context: Context,
        map: MapLibreMap,
    ) : AttributionDialogManager(context, map) {
        private var dialog: AlertDialog? = null

        override fun onClick(view: View) {
            if (context is Activity && context.isFinishing) return
            dialog =
                creditSheet(context, engine.credits) { link -> openLinkOutside(context, link) }.show()
        }

        override fun onStop() {
            dialog?.takeIf { it.isShowing }?.dismiss()
            dialog = null
        }
    }

    /**
     * Asks MapLibre for the base map of [url], or for a blank style where it is null, which MapLibre
     * fetches nothing for: given no style, its map would show a demonstration style of its own, fetched
     * from the network.
     */
    private fun show(
        map: MapLibreMap,
        url: Url?,
    ) {
        style = null
        writtenIds = emptyMap()
        sources.clear()
        layers.clear()
        baseLabels.clear()
        styleShown = url
        isStyleAskedAgain = false
        val asked = ++styleAsked
        val builder =
            if (url ==
                null
            ) {
                Style.Builder().fromJson(BLANK_STYLE)
            } else {
                Style.Builder().fromUri(url.toString())
            }
        map.setStyle(builder) { loaded ->
            if (asked != styleAsked || isClosed) return@setStyle
            style = loaded
            writtenIds = writtenFeatureIds(loaded.json)
            drawn?.let { state -> reconcile(state, loaded) }
            // The engine relabels the base map the style brought in, and ends the refusals it ends.
            engine.styleLoaded()
            if (!isReady) {
                isReady = true
                onReady(map)
            }
        }
    }

    /** Moves the camera to [pose], at once, where its center names a place. */
    private fun move(
        map: MapLibreMap,
        pose: CameraPose,
    ) {
        camera = pose
        cameraUpdate(pose)?.let(map::moveCamera)
    }

    /** Where MapLibre's camera stands now. */
    private fun pose(map: MapLibreMap): CameraPose {
        val position = map.cameraPosition
        val target = position.target
        return CameraPose(
            listOf(target?.longitude ?: Double.NaN, target?.latitude ?: Double.NaN),
            position.zoom,
            position.tilt,
            position.bearing,
        )
    }

    /**
     * Brings the map's own sources and layers to [state]'s: each taken off, added or changed, and every
     * layer in the registry's order above the base map's. A layer's label is drawn as it states it, in no
     * language the map relabels (`map.style.label-scope`).
     */
    private fun reconcile(
        state: MapState,
        style: Style,
    ) {
        // A source that is gone, or serves another kind of tiles, is taken off with every layer drawing
        // from it; GeoJSON the app holds is given its new features in place; a source equal to the one drawn
        // is sent nothing (`sourcePushes`).
        val pushes = sourcePushes(sources.mapValues { (_, held) -> held.value }, state.sources)
        for (source in pushes.refreshed) {
            val shapes = sources[source.id]?.made as? GeoJsonSource ?: continue
            val features = source.kind as? MapSource.Kind.GeoJson ?: continue
            shapes.setGeoJson(geoJsonText(features.features))
            sources[source.id] = Drawn(shapes, source)
        }
        for ((id, held) in layers.toList()) {
            if (held.value.source in pushes.removed || state.layers.none { it.id == id }) {
                style.removeLayer(held.made)
                layers.remove(id)
            }
        }
        for (id in pushes.removed) {
            sources.remove(id)?.let { style.removeSource(it.made) }
        }
        for (source in pushes.added) {
            val made = makeSource(source)
            style.addSource(made)
            sources[source.id] = Drawn(made, source)
        }

        // A layer whose type, source or source layer moved, or whose filter is gone, is made again; any
        // other change is set property by property on the layer drawn. A new layer is placed above the
        // own layer before it in the registry's order, or below every own layer where it is the first. A
        // layer draws once its source stands, one of the map's own or one the base map's style brought,
        // whose features a point query reads back as the style's document writes them; one whose source
        // neither holds waits for it.
        var previous: String? = null
        for (layer in state.layers) {
            val held = layers[layer.id]
            if (held != null && sameShape(held.value, layer)) {
                if (held.value != layer) {
                    set(layer, held.made, held.value)
                    layers[layer.id] = Drawn(held.made, layer)
                }
                previous = layer.id
                continue
            }
            if (held != null) {
                style.removeLayer(held.made)
                layers.remove(layer.id)
            }
            if (!sources.containsKey(layer.source) && style.getSource(layer.source) == null) continue
            val made = makeLayer(layer) ?: continue
            set(layer, made, null)
            val above = previous
            val lowest = style.layers.firstOrNull { layers.containsKey(it.id) }
            when {
                above != null -> style.addLayerAbove(made, above)
                lowest != null -> style.addLayerBelow(made, lowest.id)
                else -> style.addLayer(made)
            }
            layers[layer.id] = Drawn(made, layer)
            previous = layer.id
        }

        // The map's own layers draw above the base map's, in the registry's order: where the order on the
        // map is another, they are taken off and added again in it.
        val order = state.layers.mapNotNull { layers[it.id] }
        val onMap = style.layers.filter { layers.containsKey(it.id) }.map { it.id }
        if (onMap == order.map { it.value.id }) return
        for (held in order) style.removeLayer(held.made)
        for (held in order) style.addLayer(held.made)
    }

    /** Whether two layers are drawn by the same style layer: of one type, from one source and source layer, and filtered while it is. */
    private fun sameShape(
        a: MapLayer,
        b: MapLayer,
    ): Boolean =
        a.type == b.type && a.source == b.source && a.sourceLayer == b.sourceLayer &&
            !(a.filter != null && b.filter == null)

    /** MapLibre's source for [source]. */
    private fun makeSource(source: MapSource): Source =
        when (val kind = source.kind) {
            is MapSource.Kind.Vector -> {
                VectorSource(source.id, tileSet(kind.tiles, kind.zoom, source.attribution))
            }

            // A raster stating no tile size is left to MapLibre's own, 512.
            is MapSource.Kind.Raster -> {
                val tiles = tileSet(kind.tiles, kind.zoom, source.attribution)
                kind.tileSize?.let { RasterSource(source.id, tiles, it) } ?: RasterSource(source.id, tiles)
            }

            is MapSource.Kind.RasterDem -> {
                val tiles = tileSet(kind.tiles, kind.zoom, source.attribution)
                kind.tileSize?.let { RasterDemSource(source.id, tiles, it) } ?: RasterDemSource(source.id, tiles)
            }

            is MapSource.Kind.GeoJson -> {
                GeoJsonSource(source.id, geoJsonText(kind.features))
            }
        }

    /** A TileJSON of [tiles], served across [zoom], crediting [attribution]. */
    private fun tileSet(
        tiles: List<String>,
        zoom: ZoomBounds,
        attribution: String?,
    ): TileSet =
        TileSet(TILE_JSON, *tiles.toTypedArray()).apply {
            minZoom = zoom.minZoom.toFloat()
            maxZoom = zoom.maxZoom.toFloat()
            if (attribution != null) this.attribution = attribution
        }

    /** MapLibre's style layer of [layer]'s type, drawing from its source and source layer, or null for a type it does not draw. */
    private fun makeLayer(layer: MapLayer): Layer? {
        val sourceLayer = layer.sourceLayer
        return when (layer.type.value) {
            "line" -> {
                LineLayer(layer.id, layer.source).also { if (sourceLayer != null) it.setSourceLayer(sourceLayer) }
            }

            "fill" -> {
                FillLayer(layer.id, layer.source).also { if (sourceLayer != null) it.setSourceLayer(sourceLayer) }
            }

            "circle" -> {
                CircleLayer(layer.id, layer.source).also {
                    if (sourceLayer !=
                        null
                    ) {
                        it.setSourceLayer(sourceLayer)
                    }
                }
            }

            "symbol" -> {
                SymbolLayer(layer.id, layer.source).also {
                    if (sourceLayer !=
                        null
                    ) {
                        it.setSourceLayer(sourceLayer)
                    }
                }
            }

            "fill-extrusion" -> {
                FillExtrusionLayer(layer.id, layer.source).also {
                    if (sourceLayer !=
                        null
                    ) {
                        it.setSourceLayer(sourceLayer)
                    }
                }
            }

            "heatmap" -> {
                HeatmapLayer(layer.id, layer.source).also {
                    if (sourceLayer !=
                        null
                    ) {
                        it.setSourceLayer(sourceLayer)
                    }
                }
            }

            "raster" -> {
                RasterLayer(layer.id, layer.source)
            }

            "hillshade" -> {
                HillshadeLayer(layer.id, layer.source)
            }

            else -> {
                null
            }
        }
    }

    /**
     * Sets every property of [layer] on MapLibre's style layer, under its style name, and resets one [was]
     * held that [layer] no longer states; then its filter and its zoom window.
     */
    private fun set(
        layer: MapLayer,
        made: Layer,
        was: MapLayer?,
    ) {
        val values = mutableListOf<PropertyValue<*>>()
        for (name in was?.paint?.keys.orEmpty()) {
            if (name !in layer.paint) values += PaintPropertyValue<Any?>(name, null)
        }
        for (name in was?.layout?.keys.orEmpty()) {
            if (name !in layer.layout) values += LayoutPropertyValue<Any?>(name, null)
        }
        for ((name, value) in layer.paint) values += PaintPropertyValue(name, javaValue(value))
        for ((name, value) in layer.layout) values += LayoutPropertyValue(name, javaValue(value))
        made.setProperties(*values.toTypedArray())
        val filter = layer.filter
        if (filter != null && filter != was?.filter) expressionOf(filter)?.let { setFilter(made, it) }
        made.minZoom = (layer.zoom?.minZoom ?: 0.0).toFloat()
        made.maxZoom = (layer.zoom?.maxZoom ?: MAX_ZOOM).toFloat()
    }

    /** Sets [filter] on a style layer of a type that takes one. */
    private fun setFilter(
        made: Layer,
        filter: Expression,
    ) {
        when (made) {
            is LineLayer -> made.setFilter(filter)
            is FillLayer -> made.setFilter(filter)
            is CircleLayer -> made.setFilter(filter)
            is SymbolLayer -> made.setFilter(filter)
            is FillExtrusionLayer -> made.setFilter(filter)
            is HeatmapLayer -> made.setFilter(filter)
        }
    }

    /** [filter] as MapLibre's expression: an array as the expression or legacy filter it writes, a boolean as itself, anything else none. */
    private fun expressionOf(filter: JsonElement): Expression? =
        when {
            filter is JsonArray -> Expression.Converter.convert(filter.toString())
            filter is JsonPrimitive && filter.booleanOrNull != null -> Expression.literal(filter.booleanOrNull == true)
            else -> null
        }

    /** A drawn object of MapLibre's, and the value it was drawn from. */
    private class Drawn<Made, Value>(
        val made: Made,
        val value: Value,
    )
}

/** The id of the source a style [layer] draws from, or null for a layer that draws from none, a background. */
private fun sourceOf(layer: Layer): String? =
    when (layer) {
        is LineLayer -> layer.sourceId
        is FillLayer -> layer.sourceId
        is CircleLayer -> layer.sourceId
        is SymbolLayer -> layer.sourceId
        is FillExtrusionLayer -> layer.sourceId
        is HeatmapLayer -> layer.sourceId
        is RasterLayer -> layer.sourceId
        is HillshadeLayer -> layer.sourceId
        else -> null
    }

/** The source layer a style [layer] reads, where it reads a vector source, and null otherwise. */
private fun sourceLayerOf(layer: Layer): String? =
    when (layer) {
        is LineLayer -> layer.sourceLayer
        is FillLayer -> layer.sourceLayer
        is CircleLayer -> layer.sourceLayer
        is SymbolLayer -> layer.sourceLayer
        is FillExtrusionLayer -> layer.sourceLayer
        is HeatmapLayer -> layer.sourceLayer
        else -> null
    }?.takeIf { it.isNotEmpty() }

/** The style a map shows before it is given a base map: nothing at all, which MapLibre fetches nothing for. */
private const val BLANK_STYLE = """{"version":8,"sources":{},"layers":[]}"""

/** The TileJSON version a source's tiles are declared in. */
private const val TILE_JSON = "2.2.0"

/** The style property a symbol layer's label is. */
private const val TEXT_FIELD = "text-field"

/** The greatest zoom a style layer draws at, where its window names none. */
private const val MAX_ZOOM = 24.0

/** How long an ease takes where it states no duration, in milliseconds: MapLibre GL JS's own. */
private const val EASE_MILLISECONDS = 500L

/** The width and height of the pin of the waypoint being placed, in density-independent pixels. */
private const val DRAFT_PIN_SIDE_DP = 24f

/** How high a marker's pin stands above the map, the shadow it casts, in density-independent pixels. */
private const val PIN_ELEVATION_DP = 2f

/** The ring around the pin of the waypoint being placed, in density-independent pixels. */
private const val DRAFT_RING_DP = 4f

/**
 * How far past its marker's point a marker's label starts, past the pin, in density-independent pixels, toward the end
 * its text runs to, as `sdk-swift`'s `markerLabelOffset` and `sdk-dart`'s label offset place it.
 */
private const val MARKER_LABEL_OFFSET_DP = 18.0

/**
 * How far the watermark is raised over the layer it stands on, in density-independent pixels: as high as a pin, so the
 * platform, which draws a layer's children by their height before their order and hands a touch to the highest first,
 * draws it above every pin, which it follows on the layer, and hands it a press over one. Its view has no outline, so
 * it casts no shadow of the platform's: its drawing carries its own.
 */
private const val WATERMARK_ELEVATION_DP = PIN_ELEVATION_DP

/**
 * How far a marker's label is raised over the layer, in density-independent pixels: as high as the watermark, which it
 * follows on the layer, so it is drawn above the mark. Its view has no outline, so it casts no shadow of the
 * platform's, as `sdk-swift` and `sdk-dart` draw theirs with none.
 */
private const val CALLOUT_ELEVATION_DP = WATERMARK_ELEVATION_DP

/**
 * The space between MapLibre's credit button, or its logo, and the edges of the map's safe area, MapLibre's own margin
 * from its view's edges, in density-independent pixels.
 */
private const val CREDIT_MARGIN_DP = 4f

/** How far, in degrees and in zoom levels, MapLibre's camera may stand from the state's before it is held elsewhere. */
private const val HELD_TOLERANCE = 1e-7

/**
 * The corner radius of a marker's label, in density-independent pixels, as `sdk-swift`'s and `sdk-dart`'s labels round
 * theirs.
 */
private const val CALLOUT_RADIUS_DP = 4f

/**
 * The size a marker's label is written at, in scaled pixels: the 13 points of `sdk-swift`'s footnote and `sdk-dart`'s
 * label, so the label takes the room it takes in theirs.
 */
private const val CALLOUT_TEXT_SP = 13f

/**
 * A label shown beside a marker: a card of rounded corners, its padding 8 across and 4 down, which takes no touch and
 * casts no shadow, its colours painted by [paintCallout].
 */
private fun calloutOf(context: Context): TextView =
    TextView(context).apply {
        val density = context.resources.displayMetrics.density
        isClickable = false
        isFocusable = false
        visibility = View.GONE
        setTextSize(TypedValue.COMPLEX_UNIT_SP, CALLOUT_TEXT_SP)
        setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (4 * density).toInt())
        background = GradientDrawable().apply { cornerRadius = CALLOUT_RADIUS_DP * density }
        // Raised for the order it is drawn in alone: with no outline the platform casts no shadow of it.
        outlineProvider = null
        elevation = CALLOUT_ELEVATION_DP * density
    }

/**
 * Paints [callout] in the tooltip's colours: its card in [ground], the `tooltip-bg` token, and its words in [ink], the
 * `tooltip-fg` token, each transparent where it is no CSS hexadecimal colour, as every token the elements draw is.
 */
private fun paintCallout(
    callout: TextView,
    ground: HexColor,
    ink: HexColor,
) {
    (callout.background as? GradientDrawable)?.setColor(hexColor(ground).toArgb())
    callout.setTextColor(hexColor(ink).toArgb())
}

/** A longitude's place across Web Mercator's world, 0 at the western antimeridian and 1 at the eastern one. */
private fun mercatorX(longitude: Double): Double = (longitude + 180.0) / 360.0

/** A latitude's place down Web Mercator's world, 0 at its northern edge and 1 at its southern one. */
private fun mercatorY(latitude: Double): Double {
    val radians = latitude * PI / 180.0
    return (1.0 - ln(tan(radians) + 1.0 / cos(radians)) / PI) / 2.0
}

/** The longitude at [x] across Web Mercator's world. */
private fun longitudeOf(x: Double): Double = x * 360.0 - 180.0

/** The latitude at [y] down Web Mercator's world. */
private fun latitudeOf(y: Double): Double = atan(sinh(PI * (1.0 - 2.0 * y))) * 180.0 / PI

/**
 * The span from [low] to [high] a point may take so that a reach of [ahead] before it and [behind] after it
 * stays within them, or, where the span is narrower than the reach, the one point at which the reach
 * stands centred on it.
 */
private fun narrowed(
    low: Double,
    high: Double,
    ahead: Double,
    behind: Double,
): Pair<Double, Double> {
    val first = low + ahead
    val last = high - behind
    if (first <= last) return first to last
    val centred = (low + high + ahead - behind) / 2.0
    return centred to centred
}

/** [features] as RFC 7946's text, or an empty collection where they cannot be written as JSON, a coordinate that is no number among them. */
private fun geoJsonText(features: GeoJsonFeatureCollection): String =
    try {
        JsonObject(
            mapOf(
                "type" to JsonPrimitive("FeatureCollection"),
                "features" to
                    JsonArray(
                        features.features.map { feature ->
                            JsonObject(
                                mapOf(
                                    "type" to JsonPrimitive("Feature"),
                                    "geometry" to
                                        (
                                            feature.geometry?.let {
                                                Json.encodeToJsonElement(
                                                    GeoJsonGeometry.serializer(),
                                                    it,
                                                )
                                            }
                                                ?: JsonNull
                                        ),
                                    "properties" to feature.properties,
                                ),
                            )
                        },
                    ),
            ),
        ).toString()
    } catch (unwritable: SerializationException) {
        EMPTY_COLLECTION
    }

/** A feature collection of none. */
private const val EMPTY_COLLECTION = """{"type":"FeatureCollection","features":[]}"""

/**
 * [value] as MapLibre reads a style value from Java: `null` for `JsonNull`, which is no value and
 * sets a property back to its default, a string, a boolean or a number as itself, an array as an
 * object array and an object as a map, so MapLibre's style parser reads it as a style document's.
 */
private fun javaValue(value: JsonElement): Any? =
    when (value) {
        is JsonNull -> {
            null
        }

        is JsonPrimitive -> {
            when {
                value.isString -> value.content
                value.booleanOrNull != null -> value.booleanOrNull
                else -> value.doubleOrNull
            }
        }

        is JsonArray -> {
            value.map(::javaValue).toTypedArray()
        }

        is JsonObject -> {
            HashMap(value.mapValues { javaValue(it.value) })
        }
    }

/**
 * A value MapLibre answers from Java as JSON: an expression's own JSON, a formatted label's array,
 * and each array, map, string, number and boolean as itself, or null where it is none of them.
 */
private fun jsonOf(value: Any?): JsonElement? =
    when (value) {
        null -> {
            JsonNull
        }

        is String -> {
            JsonPrimitive(value)
        }

        is Boolean -> {
            JsonPrimitive(value)
        }

        is Number -> {
            JsonPrimitive(value.toDouble())
        }

        is Formatted -> {
            jsonOf(value.toArray())
        }

        is Array<*> -> {
            JsonArray(value.map { jsonOf(it) ?: return null })
        }

        is List<*> -> {
            JsonArray(value.map { jsonOf(it) ?: return null })
        }

        is Map<*, *> -> {
            JsonObject(
                value.entries.associate { (key, member) ->
                    key.toString() to
                        (jsonOf(member) ?: return null)
                },
            )
        }

        else -> {
            try {
                Json.parseToJsonElement(value.toString())
            } catch (unreadable: SerializationException) {
                null
            }
        }
    }
