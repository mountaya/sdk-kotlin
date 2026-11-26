package com.mountaya.geo

import kotlin.math.log2

/** The zoom an altitude that converts to none answers, the safe mid zoom `geo/zoom-altitude.json` states. */
internal const val SAFE_MID_ZOOM: Double = 12.0

/**
 * The greatest zoom an altitude converts to, `constants.zoomCeiling` of `geo/zoom-altitude.json`, which the
 * fixture's conformance runner holds this to: 400 m at 45.9 degrees is zoom 16.
 */
internal const val ALTITUDE_ZOOM_CEILING: Double = 16.0

/**
 * The least zoom an altitude converts to, `constants.zoomFloor` of `geo/zoom-altitude.json`, which the fixture's
 * conformance runner holds this to, so the same altitude is the same zoom in every SDK.
 */
internal const val ALTITUDE_ZOOM_FLOOR: Double = 4.0

/**
 * altitudeToZoom is the Web Mercator zoom at which a camera [altitudeMeters] above the ground it
 * looks at, at [latitude] in degrees, shows that ground, on a canvas [viewportHeightPixels] tall:
 * the zoom whose ground resolution, [groundMetersPerPixel] at the latitude, spans the altitude
 * across the camera's distance from the screen, one and a half canvas heights. So the same altitude
 * is a different zoom at the equator and at sixty degrees.
 *
 * The answer is held within [4, 16], the zooms a flight is framed at. An altitude that is no
 * finite number above zero, or a conversion that yields no finite zoom (a latitude that is `NaN`
 * or past a pole), answers the safe mid zoom, 12. A height that is not a finite number above zero
 * is no canvas, and the nominal 800-pixel one is read instead. The inverse is [zoomToAltitude].
 */
public fun altitudeToZoom(
    altitudeMeters: Double,
    latitude: Double,
    viewportHeightPixels: Double = DEFAULT_VIEWPORT_HEIGHT_PIXELS,
): Double {
    if (!(altitudeMeters.isFinite() && altitudeMeters > 0.0)) return SAFE_MID_ZOOM
    // The zoom at which one pixel spans the altitude over the camera's distance from the screen:
    // 2^zoom is the resolution at zoom 0 over that span.
    val zoom = log2(groundMetersPerPixel(0.0, latitude) * cameraDistancePixels(viewportHeightPixels) / altitudeMeters)
    if (!zoom.isFinite()) return SAFE_MID_ZOOM
    return zoom.coerceIn(ALTITUDE_ZOOM_FLOOR, ALTITUDE_ZOOM_CEILING)
}
