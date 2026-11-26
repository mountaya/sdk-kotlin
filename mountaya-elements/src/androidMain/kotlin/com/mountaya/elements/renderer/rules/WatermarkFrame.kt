package com.mountaya.elements

import com.mountaya.geo.MapWatermark
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.isWatermarkKeptClear
import com.mountaya.geo.watermarkHeight
import com.mountaya.geo.watermarkInset
import com.mountaya.geo.watermarkWidth
import kotlin.math.hypot

/**
 * watermarkFrame answers the frame [mark] is drawn in on a map, `[x, y, width, height]` from the map's top-left corner
 * in density-independent pixels: its own frame, which `watermarkOf` measures from the top-left corner of the map's
 * safe area, moved to that corner, [safeArea], whose first two figures are `[left, top]` from the map's
 * (`geo.organization.watermark`). Its figures are physical: the mark stands at the reader's top left in a layout that
 * runs right to left too.
 */
internal fun watermarkFrame(
    mark: MapWatermark,
    safeArea: List<Double>,
): List<Double> = listOf(safeArea[0] + mark.x, safeArea[1] + mark.y, mark.width, mark.height)

/**
 * keptClearFrame answers the frame nothing of the SDK's own lays over on a map whose read of its organization stands at
 * [read], `[x, y, width, height]` in the coordinates its safe area [safeArea], `[left, top, right, bottom]`, is given in
 * (`geo.organization.watermark`): the watermark's frame, [watermarkWidth] wide and [watermarkHeight] tall, its top-left
 * corner [watermarkInset] from the safe area's on both axes, every figure physical, left the reader's left whichever
 * way the text runs.
 *
 * It is held where Geo's [isWatermarkKeptClear] holds it, from the moment the map is drawn, while the read is waiting
 * too, so a control stack, a panel, a readout or a marker's label kept clear of it does not move as the mark lands, and
 * released only once the read answers that the organization holds white label, which draws no mark: null then. A stack
 * driving no map and a panel control in no stack hold it as a waiting read does, `OrganizationRead.Waiting`.
 */
internal fun keptClearFrame(
    read: OrganizationRead,
    safeArea: List<Double>,
): List<Double>? {
    if (!isWatermarkKeptClear(read)) return null
    return listOf(
        safeArea[0] + watermarkInset,
        safeArea[1] + watermarkInset,
        watermarkWidth,
        watermarkHeight,
    )
}

/**
 * safeAreaIn answers the part of a map's safe area, [safeArea], that a space laid at [space] covers, `[left, top, right,
 * bottom]` from the space's own top-left corner, the two given `[left, top, right, bottom]` in one set of coordinates,
 * a window's: the room a control stack laid over the map keeps to, read off the map's real frame however much larger
 * than the map the space it fills is. A space the safe area does not reach keeps to the whole of itself. Every figure is
 * in one unit.
 */
internal fun safeAreaIn(
    safeArea: List<Double>,
    space: List<Double>,
): List<Double> {
    val left = maxOf(safeArea[0], space[0])
    val top = maxOf(safeArea[1], space[1])
    val right = minOf(safeArea[2], space[2])
    val bottom = minOf(safeArea[3], space[3])
    if (right <= left || bottom <= top) return listOf(0.0, 0.0, space[2] - space[0], space[3] - space[1])
    return listOf(left - space[0], top - space[1], right - space[0], bottom - space[1])
}

/**
 * mapSafeArea answers a map's safe area, the part of it the device leaves visible, `[left, top, right, bottom]` from
 * the map's own top-left corner: the window, [window] `[width, height]`, less the [insets] the operating system reports
 * to it, `[left, top, right, bottom]` from the window's edges, the status bar, a display cutout, the navigation bar and
 * every other inset among them, met with the map, which stands at [map], `[x, y, width, height]` in the window. An
 * inset reaches only the part of the map it covers: a map drawn edge to edge under the status bar starts its safe area
 * below it, and one laid out below it starts it at its own corner. The safe area never leaves the map, and is empty
 * where the insets cover it whole. Every figure is in one unit, the window's pixels or density-independent ones.
 */
internal fun mapSafeArea(
    insets: List<Double>,
    window: List<Double>,
    map: List<Double>,
): List<Double> {
    val width = map[2].coerceAtLeast(0.0)
    val height = map[3].coerceAtLeast(0.0)
    val left = (insets[0] - map[0]).coerceIn(0.0, width)
    val top = (insets[1] - map[1]).coerceIn(0.0, height)
    val right = (window[0] - insets[2] - map[0]).coerceIn(left, width)
    val bottom = (window[1] - insets[3] - map[1]).coerceIn(top, height)
    return listOf(left, top, right, bottom)
}

/**
 * clearOfFrame answers where a box of [size], `[width, height]`, placed at [origin], `[x, y]`, stands once clear of
 * [frame], the watermark's `[x, y, width, height]`, inside [bounds], `[left, top, right, bottom]`, the room it keeps to
 * (`geo.organization.watermark`).
 *
 * A box that lays over no part of the frame stays where it is. One that does is moved by the smallest move that clears
 * it and keeps it inside [bounds]: below the frame or to its right, the frame standing in the safe area's top-left
 * corner. Where neither keeps it inside [bounds], it stands with its top at the frame's bottom, where the map's edge
 * cuts it, so the mark is never covered. Every figure is in one unit.
 */
internal fun clearOfFrame(
    origin: List<Double>,
    size: List<Double>,
    frame: List<Double>,
    bounds: List<Double>,
): List<Double> {
    if (!overlaps(origin, size, frame)) return origin
    val below = listOf(origin[0], maxOf(origin[1], frame[1] + frame[3]))
    val beside = listOf(maxOf(origin[0], frame[0] + frame[2]), origin[1])
    val fitsBelow = below[1] + size[1] <= bounds[3]
    val fitsBeside = beside[0] + size[0] <= bounds[2]
    if (fitsBelow && fitsBeside) {
        val down = hypot(below[0] - origin[0], below[1] - origin[1])
        val across = hypot(beside[0] - origin[0], beside[1] - origin[1])
        return if (down <= across) below else beside
    }
    return if (fitsBeside) beside else below
}

/**
 * controlStackOrigin answers where the top-left corner of a control stack of [stack], `[width, height]`, stands in
 * [corner] of a map whose safe area is [safeArea], `[left, top, right, bottom]` from the map's corner: [inset] inside
 * the safe area's edges at that corner, whichever corner it is, so a status bar, a cutout or a navigation bar never
 * covers it.
 *
 * A stack that would then lay over [keepClear], the watermark's frame `[x, y, width, height]` while the map keeps it
 * clear, is moved clear of it, as far from it as from the safe area's edges, [inset], by the smallest move that keeps it
 * inside the safe area ([clearOfFrame]), in whichever corner it sits (`geo.organization.watermark`): a stack in the
 * top-left corner starts below the frame, and one in the top-right corner of a map too narrow for both stands below it
 * too. Every figure is in one unit.
 */
internal fun controlStackOrigin(
    corner: MapCorner,
    stack: List<Double>,
    safeArea: List<Double>,
    inset: Double,
    keepClear: List<Double>?,
): List<Double> {
    val room = listOf(safeArea[0] + inset, safeArea[1] + inset, safeArea[2] - inset, safeArea[3] - inset)
    val left =
        when (corner) {
            MapCorner.TopLeft, MapCorner.BottomLeft -> room[0]
            MapCorner.TopRight, MapCorner.BottomRight -> room[2] - stack[0]
        }
    val top =
        when (corner) {
            MapCorner.TopLeft, MapCorner.TopRight -> room[1]
            MapCorner.BottomLeft, MapCorner.BottomRight -> room[3] - stack[1]
        }
    val origin = listOf(left, top)
    if (keepClear == null) return origin
    val widened =
        listOf(keepClear[0] - inset, keepClear[1] - inset, keepClear[2] + 2 * inset, keepClear[3] + 2 * inset)
    return clearOfFrame(origin, stack, widened, room)
}

/**
 * readoutOrigin answers where the top-left corner of a readout's card of [card], `[width, height]`, stands, pressed at
 * [point], `[x, y]` (`map.readout.open`): its top-left corner at the point, flipped to the left of the point where it
 * would pass the right edge of [bounds], the map's safe area `[left, top, right, bottom]`, and above it where it would
 * pass its bottom one, and moved back inside a safe area too small for either. A card that would then lay over
 * [keepClear], the watermark's frame `[x, y, width, height]` while the map keeps it clear, is moved clear of it by the
 * smallest move ([clearOfFrame]), and where nothing inside the safe area clears it, the card opens with its top at the
 * frame's bottom and the map's edge cuts it. Every figure is in one unit.
 */
internal fun readoutOrigin(
    point: List<Double>,
    card: List<Double>,
    bounds: List<Double>,
    keepClear: List<Double>?,
): List<Double> {
    val x = if (point[0] + card[0] > bounds[2]) point[0] - card[0] else point[0]
    val y = if (point[1] + card[1] > bounds[3]) point[1] - card[1] else point[1]
    val placed =
        listOf(
            x.coerceIn(bounds[0], maxOf(bounds[0], bounds[2] - card[0])),
            y.coerceIn(bounds[1], maxOf(bounds[1], bounds[3] - card[1])),
        )
    if (keepClear == null) return placed
    return clearOfFrame(placed, card, keepClear, bounds)
}

/**
 * markerLabelOrigin answers where the top-left corner of the label a marker shows, of [label], `[width, height]`, stands
 * once it is laid over the map (`sdk/SPEC.md` section 15): [offset] past [anchor], the marker's point `[x, y]`, toward
 * the end its text runs to, to its right in a layout that runs left to right and to its left where [isRightToLeft],
 * centred on the point from top to bottom, then held inside [bounds], the map's safe area `[left, top, right, bottom]`,
 * so the device covers no part of it, as `sdk-swift`'s `markerLabelPlacement` and `sdk-dart`'s `markerLabelOrigin` place
 * it. A label that would then lay over [keepClear], the watermark's frame `[x, y, width, height]` while the map keeps it
 * clear, is moved clear of it by the smallest move, as the readout is ([clearOfFrame]); where nothing inside the safe
 * area clears it, it stands with its top at the frame's bottom and the map's edge cuts it, so it is drawn above the
 * mark and never over it. Every figure is in one unit, physical whichever way the text runs.
 */
internal fun markerLabelOrigin(
    anchor: List<Double>,
    label: List<Double>,
    offset: Double,
    isRightToLeft: Boolean,
    bounds: List<Double>,
    keepClear: List<Double>?,
): List<Double> {
    val left = if (isRightToLeft) anchor[0] - offset - label[0] else anchor[0] + offset
    val placed =
        listOf(
            left.coerceIn(bounds[0], maxOf(bounds[0], bounds[2] - label[0])),
            (anchor[1] - label[1] / 2).coerceIn(bounds[1], maxOf(bounds[1], bounds[3] - label[1])),
        )
    if (keepClear == null) return placed
    return clearOfFrame(placed, label, keepClear, bounds)
}

/**
 * isMarkerLabelShown answers whether the label a marker shows is drawn at all: only where its point, [anchor], `[x, y]`,
 * stands on the map, [map], `[width, height]`, from its left and top edges up to and not including its right and bottom
 * ones, as `sdk-dart`'s label reads its canvas. A marker whose point is off the map, its pin cut by the map's edge, shows
 * no label, never one held at the edge without its pin, and a point that is no finite number is on no map. Every figure
 * is in one unit.
 */
internal fun isMarkerLabelShown(
    anchor: List<Double>,
    map: List<Double>,
): Boolean {
    val x = anchor.getOrNull(0) ?: return false
    val y = anchor.getOrNull(1) ?: return false
    return x >= 0.0 && x < map[0] && y >= 0.0 && y < map[1]
}

/**
 * panelLeft answers where the left edge of a panel [width] wide stands, opened beside its toggle, [toggle], `[left, top,
 * right, bottom]`, over [map], the same, inside [room], the map's safe area the same way: a panel that [fills] a narrow
 * map at [fillInset] from the room's left edge, and any other toward the inside of the map, to the toggle's left where
 * the toggle stands in the map's right half and to its right otherwise, [gap] apart, held inside the room. Every figure
 * is in one unit.
 */
internal fun panelLeft(
    toggle: List<Double>,
    map: List<Double>,
    room: List<Double>,
    width: Double,
    gap: Double,
    fills: Boolean,
    fillInset: Double,
): Double {
    val x =
        when {
            fills -> room[0] + fillInset
            (toggle[0] + toggle[2]) / 2 > (map[0] + map[2]) / 2 -> toggle[0] - gap - width
            else -> toggle[2] + gap
        }
    return x.coerceIn(room[0], maxOf(room[0], room[2] - width))
}

/**
 * panelRoom answers the room a panel [width] wide standing at [left] opens in, `[left, top, right, bottom]`: [room], the
 * map's safe area, less what lies above the bottom of [keepClear], the watermark's frame `[x, y, width, height]`, where
 * the panel would reach over it horizontally, so a panel never lays over the mark (`geo.organization.watermark`); and
 * where the safe area holds nothing under the frame, the room from the frame's bottom to the bottom of [map], `[left,
 * top, right, bottom]`, which the map's edge cuts. Every figure is in one unit.
 */
internal fun panelRoom(
    room: List<Double>,
    map: List<Double>,
    left: Double,
    width: Double,
    keepClear: List<Double>?,
): List<Double> {
    val frame = keepClear ?: return room
    val frameBottom = frame[1] + frame[3]
    val across = left < frame[0] + frame[2] && left + width > frame[0]
    if (!across || frameBottom <= room[1]) return room
    if (frameBottom < room[3]) return listOf(room[0], frameBottom, room[2], room[3])
    return listOf(room[0], frameBottom, room[2], maxOf(frameBottom, map[3]))
}

/**
 * panelTop answers where the top edge of a panel [height] tall stands in [room], `[left, top, right, bottom]`, as
 * [panelRoom] answers it: level with its toggle's top, [toggleTop], held inside the room, and at the room's top where
 * the room is shorter than the panel. Every figure is in one unit.
 */
internal fun panelTop(
    toggleTop: Double,
    room: List<Double>,
    height: Double,
): Double = toggleTop.coerceIn(room[1], maxOf(room[1], room[3] - height))

/** Whether a box of [size], `[width, height]`, at [origin], `[x, y]`, shares any area with [frame], `[x, y, width, height]`. */
private fun overlaps(
    origin: List<Double>,
    size: List<Double>,
    frame: List<Double>,
): Boolean =
    origin[0] < frame[0] + frame[2] &&
        origin[0] + size[0] > frame[0] &&
        origin[1] < frame[1] + frame[3] &&
        origin[1] + size[1] > frame[1]
