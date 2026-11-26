package com.mountaya.elements

/**
 * The radius of an edit's magnet, in the screen's pixels: a point the pointer holds within it of a
 * line is placed on the line (`map.edit.snap`).
 */
internal const val SNAP_THRESHOLD_PX: Double = 40.0

/**
 * snapToActiveSegments places a point the pointer holds at [position]: on the nearest point of any
 * edge of [segments], the lines of the itinerary being edited, where that point lies within
 * [thresholdPx] of the pointer, the threshold itself included, and under the pointer otherwise
 * (`map.edit.snap`).
 *
 * Every distance is measured on the screen, through [projection], and the nearest point of an edge is
 * held to the edge's ends, so beyond an end the end is nearest. The edges are read in order, line by
 * line, and of two at one distance the first is kept. A line of one position has no edge, and no line
 * at all leaves the pointer where it is without reading the projection. A position missing a
 * coordinate reads it as 0, and a distance that is no number, from a position that names no place, is
 * never the nearest, so such a pointer is left where it is.
 */
internal fun snapToActiveSegments(
    position: List<Double>,
    segments: List<List<List<Double>>>,
    projection: ScreenProjection,
    thresholdPx: Double = SNAP_THRESHOLD_PX,
): EditSnap {
    if (segments.isEmpty()) return EditSnap(position, isSnapped = false)
    val pointer = pixel(projection.point(lngLat(position)))
    var nearest: Pixel? = null
    var nearestSquared = Double.POSITIVE_INFINITY
    for (line in segments) {
        if (line.size < 2) continue
        for (index in 1 until line.size) {
            val start = pixel(projection.point(lngLat(line[index - 1])))
            val end = pixel(projection.point(lngLat(line[index])))
            val foot = footOnEdge(pointer, start, end)
            val squared = squaredDistance(pointer, foot)
            if (squared < nearestSquared) {
                nearestSquared = squared
                nearest = foot
            }
        }
    }
    val snapped = nearest
    if (snapped == null || nearestSquared > thresholdPx * thresholdPx) return EditSnap(position, isSnapped = false)
    return EditSnap(lngLat(projection.position(listOf(snapped.x, snapped.y))), isSnapped = true)
}

/** A point of the screen as its two coordinates. */
private class Pixel(
    val x: Double,
    val y: Double,
)

/** A position's longitude and latitude, each 0 where the position states none. */
private fun lngLat(position: List<Double>): List<Double> =
    listOf(position.getOrElse(0) { 0.0 }, position.getOrElse(1) { 0.0 })

/** A point of the screen as its two coordinates, each 0 where the point states none. */
private fun pixel(point: List<Double>): Pixel = Pixel(point.getOrElse(0) { 0.0 }, point.getOrElse(1) { 0.0 })

/**
 * The point of the edge from [start] to [end] nearest [point]: the foot of the perpendicular, held to
 * the edge's ends, and the start of an edge of no length. A parameter that is no number stays one, as
 * the foot it names.
 */
private fun footOnEdge(
    point: Pixel,
    start: Pixel,
    end: Pixel,
): Pixel {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val squaredLength = dx * dx + dy * dy
    if (squaredLength == 0.0) return start
    val along = ((point.x - start.x) * dx + (point.y - start.y) * dy) / squaredLength
    val held =
        when {
            along > 0 -> if (along < 1) along else 1.0
            along.isNaN() -> along
            else -> 0.0
        }
    return Pixel(start.x + held * dx, start.y + held * dy)
}

/** The square of the distance between two points of the screen. */
private fun squaredDistance(
    a: Pixel,
    b: Pixel,
): Double {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return dx * dx + dy * dy
}
