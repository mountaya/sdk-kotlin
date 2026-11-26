package com.mountaya.geo

/**
 * Walks [line] from the distance [start], adding [legLength] of each leg between two vertices that
 * name a place, and answers the distance at its last vertex. [visit] is told every vertex in order,
 * with the distance it stands at: a vertex that names no place is told as null, standing at the
 * distance of the last that did, and the leg after it is measured from that one.
 *
 * The one walk of a line every rule reads distances along. A line is walked on its own: the jump
 * from the end of one line to the start of the next costs nothing, because a caller walks the next
 * from the distance this one answers.
 */
internal inline fun walkLine(
    line: List<List<Double>>,
    start: Double,
    legLength: (List<Double>, List<Double>) -> Double,
    visit: (distance: Double, vertex: List<Double>?, index: Int) -> Unit = { _, _, _ -> },
): Double {
    var distance = start
    var previous: List<Double>? = null
    for ((index, vertex) in line.withIndex()) {
        if (!namesPlace(vertex)) {
            visit(distance, null, index)
            continue
        }
        val from = previous
        if (from != null) distance += legLength(from, vertex)
        previous = vertex
        visit(distance, vertex, index)
    }
    return distance
}
