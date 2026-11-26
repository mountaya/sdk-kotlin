package com.mountaya.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mountaya.geo.CameraPose
import com.mountaya.geo.themeTokenValue
import com.mountaya.geo.watermarkOf
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap

/**
 * MapView is a map, in Jetpack Compose over MapLibre Native: it draws the state a `MapEngine` holds,
 * its base map, its camera held to its zoom range and pan lock, its labels, every source and layer above
 * the base map in the order the one layer registry ranks them, and the markers above them, and tells the
 * engine where the reader moved the camera to.
 *
 * ```
 * @Composable
 * fun TrailMap(mountaya: Mountaya) {
 *     val map = remember { MapEngine(mountaya) }
 *     LaunchedEffect(map) { map.setTerrain() }
 *     MapView(map, Modifier.fillMaxSize())
 * }
 * ```
 *
 * The map's own requests, for its style, its tiles, its sprites and its glyphs, carry the engine's
 * client's credential and language where they go to the Tile API, and nothing where they go to any
 * other host. They are MapLibre's, made by its own loader on its own schedule: none waits for a first
 * session token, none is reported to the client's `onResponse`, and each is repeated as MapLibre
 * repeats a failed request (SPEC.md section 11). [okHttpClient] is the app's own client for them, a
 * cache, a proxy or a pinned certificate of its own, which the map's credential is added to: MapLibre
 * keeps one client for the process, so an app gives its own here, never to MapLibre's
 * `HttpRequestUtil`, and the map shown last decides.
 *
 * The view follows the lifecycle of the composition's owner, and lets MapLibre's map go when it leaves
 * the composition; the engine is the app's, and outlives it. [onReady] is handed MapLibre's own
 * `MapLibreMap` once its first style is loaded, which is how an app reaches what this view does not
 * wrap: it is the one place a MapLibre type is published.
 *
 * [onClick] is handed each click on the map the engine tells, the place clicked and the marker it fell on,
 * where it fell on one, whose label the view then shows beside it; a double tap zooms, and is no click. A
 * click while the engine places a waypoint places it instead, and is told to no handler. Every other thing
 * that happens on the map, a long press, the features a click lands on and those a mouse or a stylus hovers
 * over, a refusal of access and a failed request, is told by the engine's `events`. [onCameraChange] is
 * handed the camera each time it comes to rest, after the reader moved it or the app did, and never frame by
 * frame; the engine's `state` holds the camera the reader moved too.
 *
 * It draws the Mountaya watermark in the top-left corner of its safe area, the part of the map the window's own insets
 * leave visible, the reader's top left whichever way the text runs, so a map drawn edge to edge keeps it clear of the
 * status bar and a display cutout, once the engine's read of
 * its organization answers without white label, fails or has gone one attempt's budget without an answer, and at once
 * where its client holds no publishable key (`geo.organization.watermark`): drawn natively from the contract's drawing,
 * above the map and its markers and beneath a marker's label and the readout, clipped to the map, and linked to
 * Mountaya's site in the scope's language, which a press opens outside the app through the device's own opener, an
 * `ACTION_VIEW` intent, never through a handler the app provides. Nothing the app passes hides, moves or replaces it,
 * and its organization's answer serves it alone: no control, unit, language, base map or colour is read from it. A
 * long press, or a secondary click, opens the readout at its point unless [readout] is false (`map.readout.open`): the
 * point of interest, the zone and the classic overlay's band the map draws there, the terrain read off the elevation
 * tiles at the detail zoom, and the coordinates with a copy action, its top-left corner at the point, flipped left or
 * up to stay inside the safe area and moved clear of the watermark's frame, which it keeps clear while the read waits
 * too and until an answer of white label; a tap or the start of
 * a camera move closes it. The press is told to the engine's `events` either way. The label a tapped marker shows keeps
 * to the safe area and clear of the frame the same way, and shows only while its marker's point stands on the map, never
 * held at the map's edge without its pin. The view publishes where it laid the map out, and its safe
 * area, on the engine, so a `MapControls` stack and its panels keep to the map's real frame wherever the app lays
 * them. The engine's `organizationRead` says where the read stands, so the app keeps its own views clear of the frame
 * as the view keeps its own.
 *
 * Its credit button lists what the engine credits, Mountaya first, and stands 4 dp inside the safe area's bottom-right
 * corner, MapLibre's logo inside its bottom-left one, so no system bar covers either. Where the app gave the engine no
 * geolocation of its own, the view hands it the device's location services, `DeviceGeolocation`, which a
 * locate reads without ever asking the reader for access. Its markers are drawn in the elements' tokens around
 * it, read under the colour scheme in force: a marker naming no colour, and the waypoint being placed, in the
 * `accent`, and a marker's label in `tooltip-fg` on `tooltip-bg` (`LocalMountayaTokens`, `geo.style.theme-tokens`).
 *
 * ```
 * MapView(map, Modifier.fillMaxSize(), onClick = { selected = it.marker }, onCameraChange = { camera = it })
 * ```
 *
 * @param engine The map to draw. A new engine handed to the same view is drawn in place of the last, its organization's
 * read begun.
 * @param modifier The host's modifier, as every composable takes one.
 * @param okHttpClient The app's own client for MapLibre's requests, read when the view is first composed,
 * or null for MapLibre's defaults.
 * @param readout Whether a long press opens the readout, true unless the app turns it off.
 * @param onReady Handed MapLibre's map once its first style is loaded.
 * @param onClick Handed each click on the map that no edit takes, as the view was composed last.
 * @param onCameraChange Handed the camera each time it comes to rest, as the view was composed last.
 */
@Composable
public fun MapView(
    engine: MapEngine,
    modifier: Modifier = Modifier,
    okHttpClient: OkHttpClient? = null,
    readout: Boolean = true,
    onReady: ((MapLibreMap) -> Unit)? = null,
    onClick: ((MapTap) -> Unit)? = null,
    onCameraChange: ((CameraPose) -> Unit)? = null,
) {
    val context = LocalContext.current
    val ready by rememberUpdatedState(onReady)
    val renderer =
        remember(engine) {
            MapLibre.getInstance(context.applicationContext)
            MapRenderer(
                engine,
                org.maplibre.android.maps
                    .MapView(context),
                okHttpClient,
            ) { map -> ready?.invoke(map) }
        }
    val coroutines = rememberCoroutineScope()
    val readouts =
        remember(engine) {
            ReadoutEngine(coroutines, TileElevationReader(engine, imageDecoding(), ::decodeElevationImage))
        }
    // The engine opens and closes the view's readout at the gestures and moves it reads, while the view holds it.
    DisposableEffect(engine, readouts) {
        engine.readout = readouts
        onDispose { if (engine.readout === readouts) engine.readout = null }
    }
    // The engine's read of its organization begins as the view first draws it, and the watermark follows it.
    LaunchedEffect(engine) {
        try {
            engine.loadOrganization()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failed: Exception) {
            // The read's standing carries its failure, which draws the mark.
        }
    }
    val organization by engine.organizationRead.collectAsState()
    val scope = LocalMountayaScope.current
    val mark = watermarkOf(organization, scope.language)
    val label = mark?.let { scope.word(it.label) }
    val watermark =
        remember(renderer, mark, label) {
            if (mark == null ||
                label == null
            ) {
                null
            } else {
                watermarkViewOf(context, mark, label)
            }
        }
    var laid by remember { mutableStateOf<Rect?>(null) }
    val safeArea = safeAreaOf(laid)
    // The readout and a marker's label keep clear of the mark's frame while the read waits too, so nothing moves as the
    // mark lands.
    val keepClear = keptClearFrame(organization, safeArea ?: listOf(0.0, 0.0))
    // The map's real frame, published on its engine for every part of the SDK's own interface laid over it.
    val density = LocalDensity.current.density
    val shown = laid?.let { box -> safeArea?.let { shownMapOf(box, it, density) } }
    DisposableEffect(engine, shown) {
        shown?.let(engine::shown)
        onDispose { shown?.let(engine::unshown) }
    }
    // The handlers the view was composed with last, as SwiftUI hands its view's on each update.
    SideEffect {
        renderer.onClick = onClick
        renderer.onCameraChange = onCameraChange
        readouts.isEnabled = readout
        renderer.safeArea = safeArea
        renderer.keepClear = keepClear
        renderer.watermark = watermark
    }
    Box(modifier.onGloballyPositioned { laid = Rect(it.positionInWindow(), it.size.toSize()) }) {
        RendererView(renderer, Modifier.fillMaxSize())
        ReadoutPopup(engine, readouts, safeArea, keepClear, Modifier.fillMaxSize())
    }
}

/**
 * Where a map laid at [box], in its window's pixels, stands, its safe area [safeArea] given in density-independent
 * pixels from the map's own corner, both read in the window's density-independent pixels at [density].
 */
private fun shownMapOf(
    box: Rect,
    safeArea: List<Double>,
    density: Float,
): ShownMap {
    val left = box.left.toDouble() / density
    val top = box.top.toDouble() / density
    return ShownMap(
        map = listOf(left, top, box.right.toDouble() / density, box.bottom.toDouble() / density),
        safeArea = listOf(left + safeArea[0], top + safeArea[1], left + safeArea[2], top + safeArea[3]),
    )
}

/**
 * RendererView hosts [renderer]'s view in the composition, MapLibre's with the layer its pins and its watermark stand
 * on above it: it drives MapLibre's view by the lifecycle of the composition's owner, draws the engine's states for as
 * long as it is composed, hands it the accent the tokens around it read under the scheme in force, which its pins take,
 * and the tooltip's ground and text under the same scheme, which a marker's label takes, and closes the renderer when
 * it leaves. A new renderer handed to it is hosted in place of the last, its own view attached and the last one's let
 * go.
 */
@Composable
internal fun RendererView(
    renderer: MapRenderer,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    SideEffect {
        renderer.accent = themeTokenValue(tokens.accent, scheme)
        renderer.tooltipBg = themeTokenValue(tokens.tooltipBg, scheme)
        renderer.tooltipFg = themeTokenValue(tokens.tooltipFg, scheme)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(renderer) { renderer.follow() }
    DisposableEffect(lifecycle, renderer) {
        val observer = LifecycleEventObserver { owner, _ -> renderer.drive(owner.lifecycle.currentState) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    DisposableEffect(renderer) { onDispose { renderer.close() } }
    // An interop node makes its view once, so a new renderer takes a node of its own: the view it draws on is attached,
    // and the closed renderer's view leaves the window with the node that held it.
    key(renderer) { AndroidView(factory = { renderer.view }, modifier = modifier) }
}
