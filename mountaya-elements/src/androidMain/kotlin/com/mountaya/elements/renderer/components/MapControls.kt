package com.mountaya.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mountaya.geo.LayersPanelChoice
import com.mountaya.geo.LayersSelectionEngine
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.UnitPreferences
import kotlin.math.ceil

/** The room a control stack keeps between itself and the map's edges, in density-independent pixels. */
internal const val MAP_CONTROLS_PADDING_DP: Int = 10

/**
 * MapControls is a map's floating control stack, laid over the map in one of its corners, top right unless the app
 * names another, as the reader sees it whichever way the text runs. From the top it reads: the zoom pair; the layers,
 * points-of-interest and preferences toggles as one group; the app's own controls; geolocate; the compass.
 *
 * Each flag has a fixed default, which a flag the app leaves out takes and one it states, true or false, overrides: the
 * zoom pair, the compass, the layers toggle and the preferences toggle show, and the locate control, the
 * points-of-interest toggle and the layers panel's time are offered only where the app turns them on. None is read from
 * the map's organization, so what the stack shows is read off the app's code and never changes from afar.
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     MapView(map, Modifier.fillMaxSize())
 *     MapControls(map, geolocate = true) {
 *         DownloadControl(onClick = { exporting = true })
 *     }
 * }
 * ```
 *
 * Each control drives the map unless a handler of the app's stands in for its default, which a stack composed of
 * `ZoomControl`, `CompassControl`, `GeolocateControl` and `PanelControl` takes; an action control, `DownloadControl` or
 * `DirectionsControl`, drives only its handler. [enabled] false shuts every control under it, the app's own among them.
 *
 * A toggle opens its panel in place of any other, so one panel is open at a time, the app's own panel controls among
 * its controls included. [openPanel] holds which one is open, the stack's own state unless the app hoists it. The
 * layers toggle opens a `LayersPanel` over a selection the stack holds for the map, which reads the layers catalogue
 * only while the toggle stands, offering the time where [offersTime] says so and the cards' pictures [thumbnail] hands,
 * as `LayersPanel` takes them, the points-of-interest toggle a `PoiPanel` of the map's concepts, and the preferences
 * toggle a `PreferencesPanel` over [unitPreferences], or, where the app hands none, over a set the stack holds itself,
 * starting from the SDK's default units, the metric system, a speed, degrees and Celsius, in the scope's language,
 * which nothing outside the panel reads. A language pressed relabels the map either way. Each panel is laid out as the
 * embed's is (`geo.panels.layout`): 340 density-independent pixels wide, the width its corner leaves on a map no wider
 * than 576, and no taller than the map leaves it below its top.
 *
 * The stack keeps to the map's safe area, the part of it the window's insets leave visible, in whichever corner it
 * sits, 10 density-independent pixels inside its edges, and nothing of it covers the Mountaya watermark the map draws
 * in the top-left corner of that safe area, the reader's top left whichever way the text runs
 * (`geo.organization.watermark`): a stack that would reach over the mark's frame, in the top-left corner or in another
 * on a map too narrow or too short for both, is moved clear of it by the smallest move that keeps it inside the safe
 * area, and stands below the frame where none does. The map, its safe area and the frame are the map's real ones, which
 * the `MapView` drawing [map] publishes on its engine, however much more than the map the space the stack is laid in
 * holds; a stack driving no map, or one whose map no view draws yet, keeps to the safe area of the space it is laid in.
 * The frame is kept clear from the moment the stack is drawn, while the map's read of its organization waits too, so
 * nothing moves as the mark lands, and released only once the read answers that the organization holds white label; a
 * stack driving no map holds it as a waiting read does. A panel opens beside its toggle, inside the safe area and clear
 * of the frame, below it where it would reach over it, and with its top at the frame's bottom, cut by the map's edge,
 * where the safe area holds no room under it.
 *
 * Its words and its glyphs are the scope's (`MountayaScope`), and its colours the tokens' (`LocalMountayaTokens`), read
 * under the colour scheme in force.
 *
 * @param map The map the controls drive, or null for none.
 * @param modifier The host's modifier, as every composable takes one; the stack fills the space it is laid in.
 * @param zoom Whether the zoom pair stands, true unless the app turns it off.
 * @param compass Whether the compass stands, true unless the app turns it off.
 * @param geolocate Whether the locate control stands, false unless the app turns it on.
 * @param layers Whether the layers toggle stands, true unless the app turns it off.
 * @param pois Whether the points-of-interest toggle stands, false unless the app turns it on.
 * @param preferences Whether the preferences toggle stands, true unless the app turns it off.
 * @param corner The corner of the map the stack sits in.
 * @param openPanel The panel open, which the app may hoist to open and close one itself.
 * @param unitPreferences The set the preferences panel edits, where the app holds one.
 * @param offersTime Whether the layers panel offers the time, its slider pinning the moment the map's stamped overlay
 * shows, false unless the app turns it on.
 * @param thumbnail The picture each card of the layers panel shows for a terrain or a classic overlay, by its choice and
 * its slug, or null for cards of their names alone.
 * @param enabled Whether any control under the stack may be pressed.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param content The app's own controls, between the panel toggles and geolocate.
 */
@Composable
public fun MapControls(
    map: MapEngine?,
    modifier: Modifier = Modifier,
    zoom: Boolean = true,
    compass: Boolean = true,
    geolocate: Boolean = false,
    layers: Boolean = true,
    pois: Boolean = false,
    preferences: Boolean = true,
    corner: MapCorner = MapCorner.TopRight,
    openPanel: MutableState<MapPanel?> = remember { mutableStateOf(null) },
    unitPreferences: MutableState<UnitPreferences>? = null,
    offersTime: Boolean = false,
    thumbnail: ((LayersPanelChoice, String) -> Painter?)? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    content: @Composable () -> Unit = {},
) {
    ProvideStrings(strings) {
        val scope = LocalMountayaScope.current
        val slots = ControlSlot.stack(zoom, layers, pois, preferences, geolocate, compass)
        val coroutines = rememberCoroutineScope()
        val selection = remember { LayersSelectionEngine(coroutines) }
        DisposableEffect(selection) { onDispose { selection.close() } }
        // The selection reads the catalogue only where the layers toggle stands to read it for.
        LaunchedEffect(map, layers) { if (layers) selection.configure(map) }
        var ownPreferences by remember { mutableStateOf<UnitPreferences?>(null) }
        val held = unitPreferences?.value ?: ownPreferences ?: UnitPreferences(scope.language.value)
        val change: (UnitPreferences) -> Unit = { next ->
            if (unitPreferences == null) ownPreferences = next else unitPreferences.value = next
        }
        var laid by remember { mutableStateOf<Rect?>(null) }
        val density = LocalDensity.current
        // The map's real frame where a view draws it, read off its engine, never off the stack's own box.
        val shown = map?.shownMap?.collectAsState()?.value
        // The space's own safe area, which a stack driving no map, or a map no view draws yet, keeps to.
        val own = safeAreaOf(laid)
        // The mark's frame is kept clear while the read waits too, so the stack does not move as the mark lands; a stack
        // driving no map holds it as a waiting read does.
        val read = map?.organizationRead?.collectAsState()?.value ?: OrganizationRead.Waiting
        val space = laid?.let { inDensityIndependent(it, density.density) }
        val area = if (shown != null && space != null) safeAreaIn(shown.safeArea, space) else own
        val frame =
            if (shown != null && space != null) {
                keptClearFrame(read, shown.safeArea)?.let { listOf(it[0] - space[0], it[1] - space[1], it[2], it[3]) }
            } else {
                area?.let { keptClearFrame(read, it) }
            }
        val bounds =
            laid?.let { box ->
                MapBounds(
                    map = shown?.let { inPixels(it.map, density.density) } ?: box,
                    safeArea = area?.let { inWindow(it, box, density.density) } ?: box,
                    keepClear = frame?.let { inWindow(framed(it), box, density.density) },
                )
            }
        val padding = MAP_CONTROLS_PADDING_DP.toDouble()
        CompositionLocalProvider(
            LocalMountayaOpenPanel provides openPanel,
            LocalMountayaControlsEnabled provides (enabled && LocalMountayaControlsEnabled.current),
            LocalMountayaMapBounds provides bounds,
        ) {
            // The stack is measured before it is placed, so it stands in its corner of the safe area and clear of the
            // frame in one layout; a stack the map cannot hold whole is cut by the map's edge.
            Layout(
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        for (slot in slots) {
                            key(slot) {
                                when (slot) {
                                    ControlSlot.Zoom -> {
                                        ZoomControl(map)
                                    }

                                    is ControlSlot.Panels -> {
                                        ControlChip {
                                            for (panel in slot.panels) {
                                                key(panel.name) {
                                                    StackPanelToggle(
                                                        panel,
                                                        map,
                                                        selection,
                                                        held,
                                                        change,
                                                        offersTime,
                                                        thumbnail,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    ControlSlot.Content -> {
                                        content()
                                    }

                                    ControlSlot.Geolocate -> {
                                        GeolocateControl(map)
                                    }

                                    ControlSlot.Compass -> {
                                        CompassControl(map)
                                    }
                                }
                            }
                        }
                    }
                },
                modifier =
                    modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .onGloballyPositioned { laid = Rect(it.positionInWindow(), it.size.toSize()) },
            ) { measurables, constraints ->
                val stack = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    val origin =
                        controlStackOrigin(
                            corner = corner,
                            stack = listOf(stack.width.toDouble(), stack.height.toDouble()),
                            safeArea =
                                area?.map { it * density.density }
                                    ?: listOf(
                                        0.0,
                                        0.0,
                                        constraints.maxWidth.toDouble(),
                                        constraints.maxHeight.toDouble(),
                                    ),
                            inset = padding * density.density,
                            keepClear = frame?.map { it * density.density },
                        )
                    // Rounded up, so a stack moved clear of the frame never reaches a pixel over it.
                    stack.place(ceil(origin[0]).toInt(), ceil(origin[1]).toInt())
                }
            }
        }
    }
}

/** [rect], in its window's pixels, as `[left, top, right, bottom]` in the window's density-independent pixels. */
private fun inDensityIndependent(
    rect: Rect,
    density: Float,
): List<Double> =
    listOf(
        rect.left.toDouble() / density,
        rect.top.toDouble() / density,
        rect.right.toDouble() / density,
        rect.bottom.toDouble() / density,
    )

/** [box], `[left, top, right, bottom]` in the window's density-independent pixels, as a rect of the window's pixels. */
private fun inPixels(
    box: List<Double>,
    density: Float,
): Rect =
    Rect(
        (box[0] * density).toFloat(),
        (box[1] * density).toFloat(),
        (box[2] * density).toFloat(),
        (box[3] * density).toFloat(),
    )

/** [box], `[left, top, right, bottom]` in density-independent pixels from the corner of [space], as a rect of the window. */
private fun inWindow(
    box: List<Double>,
    space: Rect,
    density: Float,
): Rect =
    Rect(
        space.left + (box[0] * density).toFloat(),
        space.top + (box[1] * density).toFloat(),
        space.left + (box[2] * density).toFloat(),
        space.top + (box[3] * density).toFloat(),
    )

/** [frame], `[x, y, width, height]`, as `[left, top, right, bottom]`. */
private fun framed(frame: List<Double>): List<Double> =
    listOf(
        frame[0],
        frame[1],
        frame[0] + frame[2],
        frame[1] + frame[3],
    )

/**
 * The toggle of one of the stack's own panels, on the group's chip, the layers panel offering the time where [offersTime]
 * says so and showing the pictures [thumbnail] hands.
 */
@Composable
private fun StackPanelToggle(
    panel: MapPanel,
    map: MapEngine?,
    selection: LayersSelectionEngine,
    preferences: UnitPreferences,
    onPreferencesChange: (UnitPreferences) -> Unit,
    offersTime: Boolean,
    thumbnail: ((LayersPanelChoice, String) -> Painter?)?,
) {
    val scope = LocalMountayaScope.current
    when (panel) {
        MapPanel.Layers -> {
            PanelToggle(
                panel,
                scope.word("controls.layers"),
                "layers",
                help = scope.word("tooltips.layers.description"),
            ) {
                LayersPanel(selection, offersTime = offersTime, thumbnail = thumbnail)
            }
        }

        MapPanel.Pois -> {
            PanelToggle(panel, scope.word("controls.pois"), "pin", help = scope.word("tooltips.pois.description")) {
                PoiPanel(map)
            }
        }

        MapPanel.Preferences -> {
            PanelToggle(
                panel,
                scope.word("controls.preferences"),
                "preferences",
                help = scope.word("tooltips.preferences.description"),
            ) {
                PreferencesPanel(preferences, onPreferencesChange, map = map)
            }
        }

        else -> {}
    }
}
