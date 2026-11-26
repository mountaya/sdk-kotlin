package com.mountaya.geo

/**
 * The least zoom a map is ever held to, the `effectiveMinZoomFloor` `geo/zoom-bounds.json`
 * declares: a range that lets the camera pull out further is floored here.
 */
internal const val EFFECTIVE_MIN_ZOOM_FLOOR: Double = 3.0

/**
 * The headroom a range's ceiling is pulled in by, for the drift pitch and terrain give a camera's
 * effective zoom, the `zoomBuffer` `geo/zoom-bounds.json` declares.
 */
internal const val ZOOM_BUFFER: Double = 1.0

/**
 * The least span kept between the floored minimum and the buffered maximum, the
 * `effectiveZoomMinSpan` `geo/zoom-bounds.json` declares: a renderer refuses a maximum at or under
 * its minimum.
 */
internal const val EFFECTIVE_ZOOM_MIN_SPAN: Double = 1.0

/**
 * effectiveZoomBounds is the zoom range a map's camera is held to for a layer, or an
 * organization's safety rules, that serves [zoomMin] to [zoomMax]: the minimum floored at an
 * absolute 3, and the maximum pulled in by a buffer of 1 for the drift pitch and terrain give,
 * never closer to the minimum than a span of 1. The minimum, the end that bounds how far a camera
 * may pull out, keeps its value; it is the maximum that widens a narrow range.
 *
 * Nothing is refused or ordered: a zoom that is `NaN` answers `NaN` at both ends, which is what
 * the arithmetic answers.
 */
public fun effectiveZoomBounds(
    zoomMin: Double,
    zoomMax: Double,
): ZoomBounds {
    val minZoom = maxOf(zoomMin, EFFECTIVE_MIN_ZOOM_FLOOR)
    return ZoomBounds(minZoom, maxOf(zoomMax - ZOOM_BUFFER, minZoom + EFFECTIVE_ZOOM_MIN_SPAN))
}
