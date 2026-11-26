package com.mountaya.geo

/** The altitude a zoom that converts to none answers, the safe mid altitude `geo/zoom-altitude.json` states. */
internal const val SAFE_MID_ALTITUDE_METERS: Double = 1_000.0

/**
 * zoomToAltitude is the altitude in meters, above the ground it looks at, at which a camera at
 * [latitude] in degrees shows that ground at the Web Mercator [zoom], on a canvas
 * [viewportHeightPixels] tall: the ground resolution, [groundMetersPerPixel] at the zoom and the
 * latitude, across the camera's distance from the screen, one and a half canvas heights.
 *
 * It is the inverse of [altitudeToZoom], and holds the altitude to no range, since a clearance
 * pass reads eye heights against terrain in meters. A zoom that is no finite number, or a
 * conversion that yields no altitude above zero (a latitude that is `NaN` or past a pole), answers
 * the safe mid altitude, 1,000 m. A height that is not a finite number above zero is no canvas,
 * and the nominal 800-pixel one is read instead.
 */
public fun zoomToAltitude(
    zoom: Double,
    latitude: Double,
    viewportHeightPixels: Double = DEFAULT_VIEWPORT_HEIGHT_PIXELS,
): Double {
    if (!zoom.isFinite()) return SAFE_MID_ALTITUDE_METERS
    val altitude = groundMetersPerPixel(zoom, latitude) * cameraDistancePixels(viewportHeightPixels)
    return if (altitude.isFinite() && altitude > 0.0) altitude else SAFE_MID_ALTITUDE_METERS
}
