package com.mountaya.geo

/**
 * zoomIntoRange is the zoom a camera at [zoom] moves to so that [window] paints
 * (`geo.camera.zoom-into-range`): a specification paints across its layers' window, `paintZoomRange`,
 * and outside it draws nothing and reports nothing, so a switch that turns it on from too far out would
 * read as a dead control.
 *
 * A camera below the window moves up to its floor and one above it down to its ceiling, the nearer edge,
 * the shortest move that shows the content. Null where the camera stays: inside the window, its edges
 * included, and before a window with an end that is no finite number or a floor above its ceiling, which
 * no move satisfies. A zoom that is no number moves nowhere either.
 */
public fun zoomIntoRange(
    zoom: Double,
    window: ZoomBounds,
): Double? {
    val floor = window.minZoom
    val ceiling = window.maxZoom
    if (!floor.isFinite() || !ceiling.isFinite() || floor > ceiling) return null
    if (zoom < floor) return floor
    if (zoom > ceiling) return ceiling
    return null
}
