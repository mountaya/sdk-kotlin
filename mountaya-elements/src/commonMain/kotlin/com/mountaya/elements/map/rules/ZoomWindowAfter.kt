package com.mountaya.elements

import com.mountaya.geo.ZoomBounds

/**
 * zoomWindowAfter is the zoom window a map holds once a call states [min] and [max] over the window [held]
 * (`map.camera.zoom-window`), on a renderer whose own limits are [renderer]: its floor, under which no window
 * reaches, and the ceiling it builds a map with.
 *
 * A limit kept stays where [held] has it; a limit released goes back to the renderer's own, the floor to its
 * floor and the ceiling to its ceiling, and is read as that zoom from there on; a limit moved takes its zoom.
 * A window whose floor lies at or under its ceiling is taken whatever window is held, one above it included.
 * A window that is the one held answers it, which is no move, and so nothing to tell.
 *
 * It throws, with nothing applied, a [MapException] of kind `NumberRefused` where a limit stated is not a
 * finite number, and of kind `ZoomBoundsRefused` where the floor stated lies under the renderer's floor,
 * where the floor lies above the ceiling stated beside it, where a floor stated alone lies above the ceiling
 * held, or where a ceiling stated alone lies under the floor held, in the order the contract's first SDK
 * reads them: the floor, then the ceiling, then the two together.
 */
internal fun zoomWindowAfter(
    held: ZoomBounds,
    min: ZoomLimit,
    max: ZoomLimit,
    renderer: ZoomBounds,
): ZoomBounds {
    val floor = stated(min, renderer.minZoom)
    val ceiling = stated(max, renderer.maxZoom)
    val written = "${floor ?: "kept"}...${ceiling ?: "kept"}"
    if (floor != null) {
        if (!floor.isFinite()) throw MapException(MapException.Kind.NumberRefused, written)
        if (floor < renderer.minZoom) throw MapException(MapException.Kind.ZoomBoundsRefused, written)
    }
    if (ceiling != null && !ceiling.isFinite()) throw MapException(MapException.Kind.NumberRefused, written)
    val refused =
        when {
            floor != null && ceiling != null -> floor > ceiling
            floor != null -> floor > held.maxZoom
            ceiling != null -> ceiling < held.minZoom
            else -> false
        }
    if (refused) throw MapException(MapException.Kind.ZoomBoundsRefused, written)
    return ZoomBounds(floor ?: held.minZoom, ceiling ?: held.maxZoom)
}

/** The zoom [limit] states, the renderer's own, [released], where it is released, or null where it is kept. */
private fun stated(
    limit: ZoomLimit,
    released: Double,
): Double? =
    when (limit) {
        is ZoomLimit.Kept -> null
        is ZoomLimit.Released -> released
        is ZoomLimit.Zoom -> limit.zoom
    }
