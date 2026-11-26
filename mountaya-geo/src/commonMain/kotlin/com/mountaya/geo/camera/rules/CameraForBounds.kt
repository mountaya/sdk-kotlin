package com.mountaya.geo

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atanh
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

/**
 * cameraForBounds answers the viewport that fits [bounds] in a [canvas], `[width, height]` in screen
 * pixels, less its [padding] (`geo.camera.fit`): computed rather than asked of a renderer, since a
 * renderer's own fit takes no zoom ceiling and may keep the bearing, so every SDK lands one fit on one
 * camera. The viewport faces north by its type, and a camera framing it keeps the pitch it has.
 *
 * [bounds] is `[west, south, east, north]` in degrees, in the canonical form `normalizeBoundingBox`
 * writes, which a map reads any box in before it frames it: each axis ordered, one across the
 * antimeridian written with its east edge past 180, and its latitudes within the Web Mercator limit.
 * The fit does not normalize. [maxZoom] is the fit's ceiling, the window's where it is null, and [window]
 * the zoom window the camera is held to.
 *
 * The box is projected on the Web Mercator world, one tile of `WEB_MERCATOR_TILE_SIZE_PIXELS` at zoom 0,
 * and the zoom is the highest at which that projection fits the canvas less its padding: the base-2
 * logarithm of the lesser of the room across over the box's projected width and the room down over its
 * projected height, each over the tile size. It is held under the fit's ceiling, then within the window.
 * The centre is the box's projected middle moved by half the difference of opposite paddings,
 * `(left - right) / 2` pixels west and `(top - bottom) / 2` pixels north at the zoom the camera lands
 * at, then unprojected; a box of one point fits at every zoom and is framed at the ceiling. A box across
 * the antimeridian is centred past 180 where its middle is, which a renderer reads as the same meridian.
 *
 * Null where the padding leaves the canvas no room, a canvas or a padding that is no number included,
 * and where the box is not four numbers or the canvas not two. An edge of the box that is `NaN` is kept
 * unknown: it answers a viewport holding `NaN` rather than a guess.
 */
public fun cameraForBounds(
    bounds: List<Double>,
    canvas: List<Double>,
    window: ZoomBounds,
    padding: PixelPadding = PixelPadding(0.0),
    maxZoom: Double? = null,
): MapViewport? {
    if (bounds.size != 4 || canvas.size != 2) return null
    val roomX = canvas[0] - padding.left - padding.right
    val roomY = canvas[1] - padding.top - padding.bottom
    if (!(roomX > 0.0 && roomY > 0.0)) return null

    // The box on the world square, which is one tile at zoom 0, from 0 at its west and north edges to 1
    // at its east and south ones.
    val x0 = worldX(bounds[0])
    val x1 = worldX(bounds[2])
    val y0 = worldY(bounds[3])
    val y1 = worldY(bounds[1])
    val fits = log2(minimumNumber(roomX / (x1 - x0), roomY / (y1 - y0)) / WEB_MERCATOR_TILE_SIZE_PIXELS)
    val ceiling = minimumNumber(maxZoom ?: window.maxZoom, window.maxZoom)
    val zoom = maximumNumber(minimumNumber(fits, ceiling), window.minZoom)

    // The inset an uneven padding leaves on one side moves the middle by half the difference, in pixels
    // at the zoom the camera lands at.
    val worldPixels = WEB_MERCATOR_TILE_SIZE_PIXELS * 2.0.pow(zoom)
    val x = (x0 + x1) / 2 - (padding.left - padding.right) / 2 / worldPixels
    val y = (y0 + y1) / 2 - (padding.top - padding.bottom) / 2 / worldPixels
    return MapViewport(listOf((x - 0.5) * DEGREES_PER_TURN, latitudeAt(y)), zoom)
}

/** A longitude's place across the Web Mercator world, from 0 at -180 to 1 at 180, past either where the longitude is. */
private fun worldX(longitude: Double): Double = longitude / DEGREES_PER_TURN + 0.5

/** A latitude's place down the Web Mercator world, from 0 at its north edge to 1 at its south one. */
private fun worldY(latitude: Double): Double = 0.5 - atanh(sin(latitude * RADIANS_PER_DEGREE)) / (2 * PI)

/** The latitude at a place down the Web Mercator world, the inverse of [worldY]. */
private fun latitudeAt(y: Double): Double = asin(tanh(PI * (1 - 2 * y))) * DEGREES_PER_RADIAN

/** The lesser of [a] and [b], `NaN` where either is, so an unknown edge stays unknown on every target. */
private fun minimumNumber(
    a: Double,
    b: Double,
): Double = if (a.isNaN() || b.isNaN()) Double.NaN else min(a, b)

/** The greater of [a] and [b], `NaN` where either is, so an unknown edge stays unknown on every target. */
private fun maximumNumber(
    a: Double,
    b: Double,
): Double = if (a.isNaN() || b.isNaN()) Double.NaN else max(a, b)
