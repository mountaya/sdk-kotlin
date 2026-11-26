package com.mountaya.elements

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * The segments of [path] as a test reads them, each written as its kind and the points it draws to: `M(x,y)`,
 * `L(x,y)`, `Q(cx,cy x,y)`, `C(c1x,c1y c2x,c2y x,y)` and `Z`, a whole number written with no fraction.
 */
internal fun pathSegments(path: Path?): List<String> {
    if (path == null) return emptyList()
    return segmentsOf(path).mapNotNull { segment ->
        val points = segment.points

        fun at(index: Int): String = "${number(points[2 * index])},${number(points[2 * index + 1])}"

        when (segment.type) {
            PathSegment.Type.Move -> "M(${at(0)})"
            PathSegment.Type.Line -> "L(${at(1)})"
            PathSegment.Type.Quadratic -> "Q(${at(1)} ${at(2)})"
            PathSegment.Type.Conic -> "K(${at(1)} ${at(2)})"
            PathSegment.Type.Cubic -> "C(${at(1)} ${at(2)} ${at(3)})"
            PathSegment.Type.Close -> "Z"
            PathSegment.Type.Done -> null
        }
    }
}

/** Every segment of [path], in drawing order, a conic read as the quadratic curves Compose reads it as. */
internal fun segmentsOf(path: Path): List<PathSegment> {
    val segments = mutableListOf<PathSegment>()
    val reading = path.iterator()
    while (reading.hasNext()) segments += reading.next()
    return segments
}

private fun number(value: Float): String {
    val whole = value.toLong()
    return if (whole.toFloat() == value) "$whole" else "$value"
}

/**
 * The bounds of the outline [path] draws, its curves' extremes included and their control points left out,
 * as a path's own bounds are on Apple's platforms, or null for a path that draws nothing. Android's own
 * bounds hold every control point.
 */
internal fun tightBounds(path: Path): Rect? {
    var left = Float.POSITIVE_INFINITY
    var top = Float.POSITIVE_INFINITY
    var right = Float.NEGATIVE_INFINITY
    var bottom = Float.NEGATIVE_INFINITY

    fun take(
        x: Float,
        y: Float,
    ) {
        left = min(left, x)
        top = min(top, y)
        right = max(right, x)
        bottom = max(bottom, y)
    }

    for (segment in segmentsOf(path)) {
        val p = segment.points
        when (segment.type) {
            PathSegment.Type.Move -> {
                take(p[0], p[1])
            }

            PathSegment.Type.Line -> {
                take(p[2], p[3])
            }

            PathSegment.Type.Quadratic, PathSegment.Type.Conic -> {
                take(p[4], p[5])
                for (t in quadraticExtremes(p[0], p[2], p[4]) + quadraticExtremes(p[1], p[3], p[5])) {
                    take(quadratic(p[0], p[2], p[4], t), quadratic(p[1], p[3], p[5], t))
                }
            }

            PathSegment.Type.Cubic -> {
                take(p[6], p[7])
                for (t in cubicExtremes(p[0], p[2], p[4], p[6]) + cubicExtremes(p[1], p[3], p[5], p[7])) {
                    take(cubic(p[0], p[2], p[4], p[6], t), cubic(p[1], p[3], p[5], p[7], t))
                }
            }

            else -> {}
        }
    }
    return if (left > right) null else Rect(left, top, right, bottom)
}

private fun quadratic(
    a: Float,
    b: Float,
    c: Float,
    t: Float,
): Float = (1 - t) * (1 - t) * a + 2 * (1 - t) * t * b + t * t * c

private fun cubic(
    a: Float,
    b: Float,
    c: Float,
    d: Float,
    t: Float,
): Float = (1 - t) * (1 - t) * (1 - t) * a + 3 * (1 - t) * (1 - t) * t * b + 3 * (1 - t) * t * t * c + t * t * t * d

/** Where a quadratic curve along one axis turns, strictly inside it. */
private fun quadraticExtremes(
    a: Float,
    b: Float,
    c: Float,
): List<Float> {
    val denominator = a - 2 * b + c
    if (denominator == 0f) return emptyList()
    return listOf((a - b) / denominator).filter { it > 0f && it < 1f }
}

/** Where a cubic curve along one axis turns, strictly inside it. */
private fun cubicExtremes(
    a: Float,
    b: Float,
    c: Float,
    d: Float,
): List<Float> {
    val qa = (-a + 3 * b - 3 * c + d).toDouble()
    val qb = (2 * (a - 2 * b + c)).toDouble()
    val qc = (b - a).toDouble()
    val roots =
        if (qa == 0.0) {
            if (qb == 0.0) emptyList() else listOf(-qc / qb)
        } else {
            val discriminant = qb * qb - 4 * qa * qc
            if (discriminant < 0) {
                emptyList()
            } else {
                listOf((-qb + sqrt(discriminant)) / (2 * qa), (-qb - sqrt(discriminant)) / (2 * qa))
            }
        }
    return roots.filter { it > 0.0 && it < 1.0 }.map { it.toFloat() }
}
