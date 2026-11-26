package com.mountaya.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.PanelLayout
import com.mountaya.geo.panelLayoutOf
import kotlin.math.ceil
import kotlin.math.round

/**
 * PanelControl is a control that opens a panel of its own beside it, floating over the map, and closes it again: the
 * layers, points-of-interest and preferences toggles of `MapControls`, or a panel of the app's own.
 *
 * In a control stack, it opens its panel in place of any other the stack holds open, so one panel is open at a time,
 * the stack's own and the app's alike; anywhere else it holds its own. Its button is held while the panel is open, and
 * read so by accessibility services. [label] names it, and [help] says what a press does. A panel a stack opens is laid
 * out over the map the stack lies over, as the embed lays out its own (`geo.panels.layout`). A panel never covers the
 * Mountaya watermark's frame (`geo.organization.watermark`): in a stack it keeps clear of the frame of the stack's map,
 * and a control in no stack keeps its panel inside its window's safe area and clear of a frame at that safe area's top
 * left, held as a waiting read holds it, since it knows of no map. The panel takes the focus while it is open, so a
 * press outside it or the back gesture closes it, as the platform's own popups close. [enabled] false disables it.
 *
 * ```kotlin
 * MapControls(map, zoom = true) {
 *     PanelControl(MapPanel("filters"), label = "Filters", glyph = "controls") { HutFilters() }
 * }
 * ```
 *
 * @param panel The panel's name, one of the stack's or the app's own.
 * @param label What accessibility services read the control as.
 * @param glyph The role of the glyph of the scope's control set the control draws.
 * @param modifier The host's modifier, as every composable takes one.
 * @param help What a press does, as accessibility services read it.
 * @param enabled Whether it may be pressed at all.
 * @param content The panel, drawn while it is open.
 */
@Composable
public fun PanelControl(
    panel: MapPanel,
    label: String,
    glyph: String,
    modifier: Modifier = Modifier,
    help: String = "",
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    ControlChip(modifier) { PanelToggle(panel, label, glyph, help = help, enabled = enabled, content = content) }
}

/**
 * The toggle of the panel [panel] names, standing on the chip around it, its panel beside it while open: the stack's
 * toggles stand together on one chip, and a lone control on one of its own.
 */
@Composable
internal fun PanelToggle(
    panel: MapPanel,
    label: String,
    glyph: String,
    modifier: Modifier = Modifier,
    help: String = "",
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scope = LocalMountayaScope.current
    val stack = LocalMountayaOpenPanel.current
    val stacked = LocalMountayaMapBounds.current
    val density = LocalDensity.current
    var isOpenAlone by remember { mutableStateOf(false) }
    var laid by remember { mutableStateOf<Rect?>(null) }
    val isOpen = isPanelOpen(panel, stack, isOpenAlone)
    val toggle = {
        if (stack == null) isOpenAlone = !isOpenAlone else togglePanel(panel, stack)
    }
    // A panel a stack opens is laid out over the map's safe area, beside its toggle and clear of the watermark's frame.
    val layout = stacked?.let { panelLayoutBeside(laid ?: it.safeArea, it, density) }
    // A control in no stack keeps its panel to its window's safe area and clear of a frame held as a waiting read holds it.
    val bounds = stacked ?: windowBounds()
    Box(modifier.onGloballyPositioned { laid = Rect(it.positionInWindow(), it.size.toSize()) }) {
        ControlButton(label = label, help = help, onClick = toggle, enabled = enabled, isHeld = isOpen) { color ->
            Glyph(glyph, set = scope.controlGlyphs, size = 20.dp, color = color)
        }
        if (isOpen) {
            val fillInset = with(density) { MAP_CONTROLS_PADDING_DP.dp.toPx() }
            val beside =
                remember(bounds, layout?.fill, fillInset) { BesideAnchor(bounds, layout?.fill == true, fillInset) }
            Popup(
                popupPositionProvider = beside,
                onDismissRequest = { if (isPanelOpen(panel, stack, isOpenAlone)) toggle() },
                properties = PopupProperties(focusable = true),
            ) {
                CompositionLocalProvider(LocalMountayaPanelLayout provides layout, content = content)
            }
        }
    }
}

/**
 * The window a panel control in no stack is laid in, in its own pixels, as the bounds its panel keeps to: the window
 * itself, its safe area, measured as a map measures its own ([safeAreaOf]), and the watermark's frame at that safe area's
 * top-left corner, held as a waiting read holds it, since the control knows of no map whose read could release it
 * (`geo.organization.watermark`); or null before the window is measured.
 */
@Composable
private fun windowBounds(): MapBounds? {
    val root = LocalView.current.rootView
    val density = LocalDensity.current.density
    val window = Rect(0f, 0f, root.width.toFloat(), root.height.toFloat())
    val area = safeAreaOf(window.takeIf { root.width > 0 && root.height > 0 }) ?: return null
    val frame = keptClearFrame(OrganizationRead.Waiting, area)
    return MapBounds(
        map = window,
        safeArea =
            Rect(
                area[0].toFloat() * density,
                area[1].toFloat() * density,
                area[2].toFloat() * density,
                area[3].toFloat() * density,
            ),
        keepClear =
            frame?.let {
                Rect(
                    it[0].toFloat() * density,
                    it[1].toFloat() * density,
                    (it[0] + it[2]).toFloat() * density,
                    (it[1] + it[3]).toFloat() * density,
                )
            },
    )
}

/**
 * The layout of a panel opened beside a toggle standing at [toggle] over the map [bounds] names, each in the window's
 * pixels (`geo.panels.layout`): laid out over the map's safe area as the embed lays out its own, its top inset the
 * toggle's top in it where the toggle stands in its upper half and none in its lower half, where the panel rises
 * beside it instead, and its height held to the room [panelRoom] leaves it, below the watermark's frame where the panel
 * would reach over it (`geo.organization.watermark`).
 */
private fun panelLayoutBeside(
    toggle: Rect,
    bounds: MapBounds,
    density: Density,
): PanelLayout =
    with(density) {
        val area = bounds.safeArea
        val upper = toggle.center.y - area.top <= area.height / 2
        val base =
            panelLayoutOf(
                area.width
                    .toDp()
                    .value
                    .toDouble(),
                area.height
                    .toDp()
                    .value
                    .toDouble(),
                if (upper) {
                    (toggle.top - area.top)
                        .coerceAtLeast(0f)
                        .toDp()
                        .value
                        .toDouble()
                } else {
                    0.0
                },
            )
        val fillInset = MAP_CONTROLS_PADDING_DP.dp.toPx().toDouble()
        val width =
            base.width
                ?.dp
                ?.toPx()
                ?.toDouble() ?: (area.width - 2 * fillInset).coerceAtLeast(0.0)
        val room = ltrb(area)
        val left =
            panelLeft(ltrb(toggle), ltrb(bounds.map), room, width, gapBeside(toggle.width), base.fill, fillInset)
        val clear = panelRoom(room, ltrb(bounds.map), left, width, bounds.keepClear?.let(::xywh))
        val held =
            (clear[3] - clear[1])
                .toFloat()
                .coerceAtLeast(0f)
                .toDp()
                .value
                .toDouble()
        PanelLayout(
            width = base.width,
            fill = base.fill,
            maxHeight = minOf(base.maxHeight, held),
            pictureHeight = base.pictureHeight,
            layerCardColumns = base.layerCardColumns,
            preferenceCardColumns = base.preferenceCardColumns,
        )
    }

/** The gap between a toggle [width] pixels wide and the panel opened beside it, in pixels. */
private fun gapBeside(width: Float): Double = (width / 6).coerceAtLeast(0f).toDouble()

/** [rect] as `[left, top, right, bottom]`. */
private fun ltrb(rect: Rect): List<Double> =
    listOf(rect.left.toDouble(), rect.top.toDouble(), rect.right.toDouble(), rect.bottom.toDouble())

/** [rect] as `[x, y, width, height]`. */
private fun xywh(rect: Rect): List<Double> =
    listOf(rect.left.toDouble(), rect.top.toDouble(), rect.width.toDouble(), rect.height.toDouble())

/** Whether the panel [panel] names is open: the one [stack] holds open, or, out of any stack, the control's own word [alone]. */
internal fun isPanelOpen(
    panel: MapPanel,
    stack: MutableState<MapPanel?>?,
    alone: Boolean,
): Boolean = if (stack == null) alone else stack.value == panel

/** Opens the panel [panel] names in [stack], in place of any other, or closes it where it is the one open. */
internal fun togglePanel(
    panel: MapPanel,
    stack: MutableState<MapPanel?>,
) {
    stack.value = if (stack.value == panel) null else panel
}

/**
 * BesideAnchor places a panel beside its toggle, as `geo.panels.layout` and `geo.organization.watermark` place it: over
 * the map [bounds] names, toward the inside of the map, so a stack in a right-hand corner opens its panels to its left
 * and one in a left-hand corner to its right, and a panel that [fills] a narrow map at [fillInset] pixels from its safe
 * area's left edge; top-aligned with the toggle; held inside the map's safe area; and below the watermark's frame where
 * it would reach over it, never to its right, its top at the frame's bottom where the safe area holds no room under it,
 * so the mark is never covered. A toggle in no stack hands its window's bounds, and one whose window is not yet
 * measured, [bounds] null, holds its panel within the window.
 */
internal class BesideAnchor(
    private val bounds: MapBounds?,
    private val fills: Boolean,
    private val fillInset: Float,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val window = listOf(0.0, 0.0, windowSize.width.toDouble(), windowSize.height.toDouble())
        val map = bounds?.map?.let(::ltrb) ?: window
        val area = bounds?.safeArea?.let(::ltrb) ?: map
        val room = within(area, window) ?: within(map, window) ?: window
        val toggle =
            listOf(
                anchorBounds.left.toDouble(),
                anchorBounds.top.toDouble(),
                anchorBounds.right.toDouble(),
                anchorBounds.bottom.toDouble(),
            )
        val width = popupContentSize.width.toDouble()
        val left =
            panelLeft(toggle, map, room, width, gapBeside(anchorBounds.width.toFloat()), fills, fillInset.toDouble())
        val clear = panelRoom(room, map, left, width, bounds?.keepClear?.let(::xywh))
        val top = panelTop(toggle[1], clear, popupContentSize.height.toDouble())
        // Rounded up, so a panel moved below the frame never reaches a pixel over it.
        return IntOffset(round(left).toInt(), ceil(top).toInt())
    }

    /** [box] met with [window], each `[left, top, right, bottom]`, or null where the two share no area. */
    private fun within(
        box: List<Double>,
        window: List<Double>,
    ): List<Double>? {
        val met =
            listOf(
                maxOf(box[0], window[0]),
                maxOf(box[1], window[1]),
                minOf(box[2], window[2]),
                minOf(box[3], window[3]),
            )
        return if (met[2] > met[0] && met[3] > met[1]) met else null
    }
}
