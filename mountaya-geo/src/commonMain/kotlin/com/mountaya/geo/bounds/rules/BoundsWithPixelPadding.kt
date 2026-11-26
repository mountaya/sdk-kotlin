package com.mountaya.geo

/**
 * boundsWithPixelPadding grows [bounds], `[west, south, east, north]` in degrees, outward by
 * [padding] in screen pixels at [zoom] and [centerLatitude]: the ground the box and its insets
 * cover together when the box is framed with that padding at that zoom.
 *
 * A pixel spans the ground resolution of 512-pixel tiles, [groundMetersPerPixel], read in the
 * degree of the same sphere: a pixel of longitude is the same span of degrees at every latitude,
 * `360 / (512 × 2^zoom)`, and a pixel of latitude that span times the cosine of the centre
 * latitude, which is held within [WEB_MERCATOR_MAX_LATITUDE] so the resolution stays positive. The
 * edges move as written, so an east edge past 180 grows past it, and nothing is reordered or
 * wrapped. It is a local linear reading, close at the zooms a route is framed at.
 *
 * A zoom that is no finite number spans no ground per pixel, so the box comes back as it is
 * written. Nothing is refused: a centre latitude that is `NaN` answers `NaN` latitudes, and a box
 * that is not four numbers answers four `NaN`s.
 */
public fun boundsWithPixelPadding(
    bounds: List<Double>,
    padding: PixelPadding,
    zoom: Double,
    centerLatitude: Double,
): List<Double> {
    if (bounds.size != 4) return List(4) { Double.NaN }
    val latitude = centerLatitude.coerceIn(-WEB_MERCATOR_MAX_LATITUDE, WEB_MERCATOR_MAX_LATITUDE)
    val degreesLongitudePerPixel = groundMetersPerPixel(zoom, 0.0) / METERS_PER_DEGREE_WEB_MERCATOR
    val degreesLatitudePerPixel = groundMetersPerPixel(zoom, latitude) / METERS_PER_DEGREE_WEB_MERCATOR
    val (west, south, east, north) = bounds
    return listOf(
        west - padding.left * degreesLongitudePerPixel,
        south - padding.bottom * degreesLatitudePerPixel,
        east + padding.right * degreesLongitudePerPixel,
        north + padding.top * degreesLatitudePerPixel,
    )
}
