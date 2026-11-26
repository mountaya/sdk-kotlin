package com.mountaya.geo

/**
 * Returns [points] thinned to [maxPoints] samples at most by a uniform stride, each renumbered by
 * its place in the answer. The first and the last are always kept: slot `k` of the others keeps
 * the sample nearest `k × (count − 1) / (maxPoints − 1)`, the later of two where the slot stands
 * halfway between them. The slot is computed in whole numbers, so a half is a half: a stride in
 * floating point reads 249 × 503 / 498, which is 251.5, as 251.49999999999997.
 *
 * A ceiling under two, or a series already within it, keeps every sample.
 */
internal fun downsampleElevation(
    points: List<ElevationPoint>,
    maxPoints: Int,
): List<ElevationPoint> {
    if (maxPoints < 2 || points.size <= maxPoints) return points.mapIndexed(::renumbered)
    val last = (points.size - 1).toLong()
    val slots = (maxPoints - 1).toLong()
    val kept = ArrayList<ElevationPoint>(maxPoints)
    for (slot in 0L until slots) {
        // The nearest whole index to slot × last / slots, a half up: floor((2 × slot × last + slots) / (2 × slots)).
        val index = (2L * slot * last + slots) / (2L * slots)
        kept += renumbered(kept.size, points[index.toInt()])
    }
    kept += renumbered(kept.size, points.last())
    return kept
}

/** The sample [point] numbered [index]. */
private fun renumbered(
    index: Int,
    point: ElevationPoint,
): ElevationPoint {
    if (point.index == index) return point
    return ElevationPoint(point.distance, point.elevation, point.lngLat, index)
}
