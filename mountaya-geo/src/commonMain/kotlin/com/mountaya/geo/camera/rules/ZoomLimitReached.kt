package com.mountaya.geo

/**
 * zoomLimitReached is which end of its zoom [window] a camera at [zoom] stands at
 * (`geo.camera.zoom-limit-reached`): the ceiling at it or above it, the floor at it or below it, both in
 * a window of one zoom, and neither otherwise. The zoom is read unrounded, so a camera a fraction under
 * the ceiling stands at neither end; a renderer holds its camera within the window, and a camera found
 * past an end stands at that end. A zoom that is no number stands at neither.
 */
public fun zoomLimitReached(
    zoom: Double,
    window: ZoomBounds,
): ZoomWindowEnd {
    val atCeiling = zoom >= window.maxZoom
    val atFloor = zoom <= window.minZoom
    return when {
        atFloor && atCeiling -> ZoomWindowEnd.Both
        atCeiling -> ZoomWindowEnd.Ceiling
        atFloor -> ZoomWindowEnd.Floor
        else -> ZoomWindowEnd.None
    }
}

/**
 * zoomControlsDisabled is the zoom controls disabled where a camera stands at [end] of its window: zoom
 * in at the ceiling, zoom out at the floor, both in a window of one zoom and none inside it. A control is
 * disabled there whoever answers its press, the map or a handler of the host's that takes the press
 * over, since either would move the camera nowhere. The `ZoomControl` composable reads it.
 */
public fun zoomControlsDisabled(end: ZoomWindowEnd): List<ZoomStep> =
    when (end) {
        ZoomWindowEnd.None -> emptyList()
        ZoomWindowEnd.Floor -> listOf(ZoomStep.ZoomOut)
        ZoomWindowEnd.Ceiling -> listOf(ZoomStep.ZoomIn)
        ZoomWindowEnd.Both -> listOf(ZoomStep.ZoomIn, ZoomStep.ZoomOut)
    }
