package com.mountaya.elements

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import com.mountaya.geo.GlyphShape
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * shapePath answers the outline of one shape of a glyph, as SVG draws it, in the units of its set's box
 * and before its transform, which a renderer applies as it draws so a stroke's width scales with the shape
 * as SVG scales it; null where the shape draws nothing.
 *
 * A `path` is its path data; a `circle`, an `ellipse` and a `rect` are the shapes SVG 2 lays them out as,
 * a radius left out of an ellipse or a rectangle's corner taking the other's and a corner held within half
 * the rectangle's side; a `line` is its two ends; a `polyline` and a `polygon` their points, a polygon
 * closed. A shape of no size, a circle of no radius or a rectangle of no width, draws nothing. A length is a
 * number, its unit `px` where it states one, and a length no number reads as one left out. The geometry is
 * SVG's own grammar, read here rather than by Compose's `PathParser`, which draws no part of a path in
 * error and reads no arc as SVG lays out one of no radius, so this SDK draws every edge as `sdk-swift`
 * does: the contract parses a glyph into shapes and stops at their attributes (`geo.icons.glyph-parse`).
 */
internal fun shapePath(shape: GlyphShape): Path? {
    fun length(name: String): Double? = shape.attributes[name]?.let(::svgLength)
    return when (shape.tag) {
        "path" -> {
            shape.attributes["d"]?.let(::svgPathData)
        }

        "circle" -> {
            val r = length("r")?.takeIf { it > 0.0 } ?: return null
            val cx = length("cx") ?: 0.0
            val cy = length("cy") ?: 0.0
            Path().apply { addOval(rect(cx - r, cy - r, cx + r, cy + r)) }
        }

        "ellipse" -> {
            val rx = (length("rx") ?: length("ry"))?.takeIf { it > 0.0 } ?: return null
            val ry = (length("ry") ?: length("rx"))?.takeIf { it > 0.0 } ?: return null
            val cx = length("cx") ?: 0.0
            val cy = length("cy") ?: 0.0
            Path().apply { addOval(rect(cx - rx, cy - ry, cx + rx, cy + ry)) }
        }

        "rect" -> {
            val width = length("width")?.takeIf { it > 0.0 } ?: return null
            val height = length("height")?.takeIf { it > 0.0 } ?: return null
            val x = length("x") ?: 0.0
            val y = length("y") ?: 0.0
            val bounds = rect(x, y, x + width, y + height)
            // A corner a radius states below 0 is no corner; one left out takes the other's.
            val rx = length("rx")?.takeIf { it >= 0.0 }
            val ry = length("ry")?.takeIf { it >= 0.0 }
            val cornerX = min(rx ?: ry ?: 0.0, width / 2.0)
            val cornerY = min(ry ?: rx ?: 0.0, height / 2.0)
            Path().apply {
                if (cornerX > 0.0 && cornerY > 0.0) {
                    addRoundRect(RoundRect(bounds, CornerRadius(cornerX.toFloat(), cornerY.toFloat())))
                } else {
                    addRect(bounds)
                }
            }
        }

        "line" -> {
            Path().apply {
                moveTo((length("x1") ?: 0.0).toFloat(), (length("y1") ?: 0.0).toFloat())
                lineTo((length("x2") ?: 0.0).toFloat(), (length("y2") ?: 0.0).toFloat())
            }
        }

        "polyline", "polygon" -> {
            val points = svgPoints(shape.attributes["points"].orEmpty())
            val first = points.firstOrNull() ?: return null
            Path().apply {
                moveTo(first.x, first.y)
                for (point in points.drop(1)) lineTo(point.x, point.y)
                if (shape.tag == "polygon") close()
            }
        }

        else -> {
            null
        }
    }
}

/** The rectangle from ([left], [top]) to ([right], [bottom]), at Compose's precision. */
private fun rect(
    left: Double,
    top: Double,
    right: Double,
    bottom: Double,
): Rect = Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

/**
 * svgLength reads a length as a glyph's geometry states one: a number, past the white space at its ends, in
 * user units or in `px`, which are the same; null for anything else.
 */
internal fun svgLength(written: String): Double? {
    val scanner = SvgScanner(written)
    scanner.skipWhiteSpace()
    val number = scanner.number() ?: return null
    scanner.skip("px")
    scanner.skipWhiteSpace()
    return if (scanner.isAtEnd) number else null
}

/**
 * svgPoints reads a polyline's or a polygon's points, coordinates separated by white space or a comma, up to
 * the first that is no number: an odd coordinate left over is dropped, as SVG draws a list in error up to
 * its error.
 */
internal fun svgPoints(written: String): List<Offset> {
    val scanner = SvgScanner(written)
    val points = mutableListOf<Offset>()
    scanner.skipWhiteSpace()
    while (true) {
        val x = scanner.number() ?: break
        scanner.skipSeparator()
        val y = scanner.number() ?: break
        points += Offset(x.toFloat(), y.toFloat())
        scanner.skipSeparator()
    }
    return points
}

/** A point of the plane, at the precision a path is read at before Compose holds it. */
private class Point(
    val x: Double,
    val y: Double,
)

/** One piece of a segment a path command draws, from the point before it. */
private sealed interface Piece {
    class Line(
        val to: Point,
    ) : Piece

    class Quadratic(
        val control: Point,
        val to: Point,
    ) : Piece

    class Cubic(
        val control1: Point,
        val control2: Point,
        val to: Point,
    ) : Piece
}

/**
 * svgPathData answers the outline SVG path data [data] draws, up to the command holding its first error, as
 * SVG draws a path in error: data that opens with no move draws nothing.
 *
 * Every command of SVG 1.1 is read, absolute and relative: a move, whose further pairs are lines, a line, a
 * horizontal and a vertical line, a cubic and a quadratic curve and their smooth forms, which reflect the
 * control point of the curve before them where it is one of theirs, an elliptical arc and a close. A command
 * repeats over further arguments without being written again. A number is SVG's: a sign, digits with at
 * most one decimal point and an exponent, two numbers written with nothing between them where the second's
 * sign or point ends the first; an arc's flags are single digits, written with nothing between them or not.
 * An arc is drawn as cubic curves of a quarter turn at most, its radii scaled up where they cannot reach its
 * end and taken without their sign, a line where a radius is 0, and nothing where it ends where it starts. A
 * path drawn after a close starts again where the closed one started. The path holds its points at single
 * precision, as Compose's paths do.
 */
internal fun svgPathData(data: String): Path {
    val scanner = SvgScanner(data)
    val path = Path()
    var current = Point(0.0, 0.0)
    var start = Point(0.0, 0.0)
    // The second control point of the cubic curve, or the control point of the quadratic curve, drawn last,
    // which a smooth curve after it reflects.
    var lastCubic: Point? = null
    var lastQuadratic: Point? = null
    var command: Char? = null
    var moved = false
    var closed = false

    while (true) {
        scanner.skipSeparator()
        if (scanner.isAtEnd) break
        val written = scanner.command()
        val repeated = command
        command =
            when {
                written != null -> {
                    written
                }

                // A further argument repeats the command, a move's as a line.
                repeated != null && repeated.lowercaseChar() != 'z' -> {
                    when (repeated) {
                        'M' -> 'L'
                        'm' -> 'l'
                        else -> repeated
                    }
                }

                else -> {
                    break
                }
            }
        val letter = command
        if (!moved && letter.lowercaseChar() != 'm') break
        val relative = letter.isLowerCase()
        val origin = current

        fun point(pair: Pair<Double, Double>): Point =
            if (relative) Point(origin.x + pair.first, origin.y + pair.second) else Point(pair.first, pair.second)

        var cubic: Point? = null
        var quadratic: Point? = null
        // The segment the command draws, from the current point, added once all of it is read.
        val segment = mutableListOf<Piece>()
        when (letter.lowercaseChar()) {
            'm' -> {
                val end = scanner.nextPair()?.let(::point) ?: return path
                path.moveTo(end.x.toFloat(), end.y.toFloat())
                current = end
                start = end
                moved = true
                closed = false
                lastCubic = null
                lastQuadratic = null
                continue
            }

            'z' -> {
                path.close()
                current = start
                closed = true
                lastCubic = null
                lastQuadratic = null
                continue
            }

            'l' -> {
                val end = scanner.nextPair()?.let(::point) ?: return path
                segment += Piece.Line(end)
                current = end
            }

            'h' -> {
                val x = scanner.nextNumber() ?: return path
                current = Point(if (relative) origin.x + x else x, origin.y)
                segment += Piece.Line(current)
            }

            'v' -> {
                val y = scanner.nextNumber() ?: return path
                current = Point(origin.x, if (relative) origin.y + y else y)
                segment += Piece.Line(current)
            }

            'c' -> {
                val control1 = scanner.nextPair()?.let(::point) ?: return path
                val control2 = scanner.nextPair()?.let(::point) ?: return path
                val end = scanner.nextPair()?.let(::point) ?: return path
                segment += Piece.Cubic(control1, control2, end)
                cubic = control2
                current = end
            }

            's' -> {
                val control2 = scanner.nextPair()?.let(::point) ?: return path
                val end = scanner.nextPair()?.let(::point) ?: return path
                val control1 = lastCubic?.let { reflected(it, origin) } ?: origin
                segment += Piece.Cubic(control1, control2, end)
                cubic = control2
                current = end
            }

            'q' -> {
                val control = scanner.nextPair()?.let(::point) ?: return path
                val end = scanner.nextPair()?.let(::point) ?: return path
                segment += Piece.Quadratic(control, end)
                quadratic = control
                current = end
            }

            't' -> {
                val end = scanner.nextPair()?.let(::point) ?: return path
                val control = lastQuadratic?.let { reflected(it, origin) } ?: origin
                segment += Piece.Quadratic(control, end)
                quadratic = control
                current = end
            }

            'a' -> {
                val arc = scanner.nextArc() ?: return path
                val end = point(arc.end)
                addArc(segment, origin, end, abs(arc.rx), abs(arc.ry), arc.rotation, arc.largeArc, arc.sweep)
                current = end
            }

            else -> {
                return path
            }
        }
        // A drawing command after a close starts a path where the closed one started.
        if (closed) {
            path.moveTo(start.x.toFloat(), start.y.toFloat())
            closed = false
        }
        for (piece in segment) {
            when (piece) {
                is Piece.Line -> {
                    path.lineTo(piece.to.x.toFloat(), piece.to.y.toFloat())
                }

                is Piece.Quadratic -> {
                    path.quadraticTo(
                        piece.control.x.toFloat(),
                        piece.control.y.toFloat(),
                        piece.to.x.toFloat(),
                        piece.to.y.toFloat(),
                    )
                }

                is Piece.Cubic -> {
                    path.cubicTo(
                        piece.control1.x.toFloat(),
                        piece.control1.y.toFloat(),
                        piece.control2.x.toFloat(),
                        piece.control2.y.toFloat(),
                        piece.to.x.toFloat(),
                        piece.to.y.toFloat(),
                    )
                }
            }
        }
        lastCubic = cubic
        lastQuadratic = quadratic
    }
    return path
}

/** [point] reflected through [center]. */
private fun reflected(
    point: Point,
    center: Point,
): Point = Point(2 * center.x - point.x, 2 * center.y - point.y)

/**
 * Adds to [segment] the elliptical arc from [start] to [end] as SVG lays one out (SVG 1.1, appendix F.6), as
 * cubic curves of a quarter turn at most.
 */
private fun addArc(
    segment: MutableList<Piece>,
    start: Point,
    end: Point,
    radiusX: Double,
    radiusY: Double,
    degrees: Double,
    largeArc: Boolean,
    sweep: Boolean,
) {
    if (start.x == end.x && start.y == end.y) return
    if (radiusX <= 0.0 || radiusY <= 0.0) {
        segment += Piece.Line(end)
        return
    }
    var rx = radiusX
    var ry = radiusY
    val phi = degrees * PI / 180.0
    val cosPhi = cos(phi)
    val sinPhi = sin(phi)
    // The midpoint between the ends, in the ellipse's own axes.
    val dx = (start.x - end.x) / 2
    val dy = (start.y - end.y) / 2
    val x1 = cosPhi * dx + sinPhi * dy
    val y1 = -sinPhi * dx + cosPhi * dy
    // Radii too small to reach the end are scaled up until they do.
    val lambda = (x1 * x1) / (rx * rx) + (y1 * y1) / (ry * ry)
    if (lambda > 1) {
        rx *= sqrt(lambda)
        ry *= sqrt(lambda)
    }
    val numerator = rx * rx * ry * ry - rx * rx * y1 * y1 - ry * ry * x1 * x1
    val denominator = rx * rx * y1 * y1 + ry * ry * x1 * x1
    val coefficient = (if (largeArc == sweep) -1.0 else 1.0) * sqrt(max(numerator, 0.0) / denominator)
    val centerX1 = coefficient * rx * y1 / ry
    val centerY1 = -coefficient * ry * x1 / rx
    val centerX = cosPhi * centerX1 - sinPhi * centerY1 + (start.x + end.x) / 2
    val centerY = sinPhi * centerX1 + cosPhi * centerY1 + (start.y + end.y) / 2
    val startAngle = atan2((y1 - centerY1) / ry, (x1 - centerX1) / rx)
    val endAngle = atan2((-y1 - centerY1) / ry, (-x1 - centerX1) / rx)
    var sweepAngle = endAngle - startAngle
    if (sweep && sweepAngle < 0) {
        sweepAngle += 2 * PI
    } else if (!sweep && sweepAngle > 0) {
        sweepAngle -= 2 * PI
    }

    // A point of the unit circle, on the ellipse.
    fun onEllipse(
        x: Double,
        y: Double,
    ): Point = Point(centerX + cosPhi * rx * x - sinPhi * ry * y, centerY + sinPhi * rx * x + cosPhi * ry * y)

    val pieces = max(1, ceil(abs(sweepAngle) / (PI / 2)).toInt())
    val step = sweepAngle / pieces
    val handle = 4.0 / 3.0 * tan(step / 4)
    var angle = startAngle
    for (piece in 0 until pieces) {
        val next = angle + step
        val control1 = onEllipse(cos(angle) - handle * sin(angle), sin(angle) + handle * cos(angle))
        val control2 = onEllipse(cos(next) + handle * sin(next), sin(next) - handle * cos(next))
        // The last piece ends where the arc does, never where rounding leaves it.
        val to = if (piece == pieces - 1) end else onEllipse(cos(next), sin(next))
        segment += Piece.Cubic(control1, control2, to)
        angle = next
    }
}

/** The arguments of one elliptical arc: its radii, the rotation of its axes in degrees, its two flags and its end. */
internal class SvgArc(
    val rx: Double,
    val ry: Double,
    val rotation: Double,
    val largeArc: Boolean,
    val sweep: Boolean,
    val end: Pair<Double, Double>,
)

/**
 * SvgScanner reads the numbers, flags and command letters of SVG's attribute grammars, character by
 * character: every character they hold is ASCII, so a character of any other script is one they do not
 * hold.
 */
internal class SvgScanner(
    private val text: String,
) {
    private var at = 0

    /** Whether every character was read. */
    val isAtEnd: Boolean get() = at >= text.length

    /** Reads past SVG's white space: a space, a tab, a line feed, a form feed and a carriage return. */
    fun skipWhiteSpace() {
        while (at < text.length && text[at] in SVG_WHITE_SPACE) at += 1
    }

    /** Reads past white space with at most one comma in it, and answers whether it read anything. */
    fun skipSeparator(): Boolean {
        val before = at
        skipWhiteSpace()
        if (at < text.length && text[at] == ',') {
            at += 1
            skipWhiteSpace()
        }
        return at != before
    }

    /** Reads past [word] where the text spells it next, and answers whether it does. */
    fun skip(word: String): Boolean {
        if (!text.startsWith(word, at)) return false
        at += word.length
        return true
    }

    /** Reads the letter of a path command where one stands next. */
    fun command(): Char? {
        val letter = text.getOrNull(at)?.takeIf { it in PATH_COMMANDS } ?: return null
        at += 1
        return letter
    }

    /** Reads an arc's flag, `0` or `1`, where one stands next. */
    fun flag(): Boolean? {
        val digit = text.getOrNull(at)?.takeIf { it == '0' || it == '1' } ?: return null
        at += 1
        return digit == '1'
    }

    /** Reads a number past the separator before it, where one stands next. */
    fun nextNumber(): Double? {
        skipSeparator()
        return number()
    }

    /** Reads two numbers past the separator before each. */
    fun nextPair(): Pair<Double, Double>? {
        val x = nextNumber() ?: return null
        val y = nextNumber() ?: return null
        return x to y
    }

    /** Reads an arc's arguments past the separator before each: its radii, the rotation of its axes in degrees, its two flags and its end. */
    fun nextArc(): SvgArc? {
        val rx = nextNumber() ?: return null
        val ry = nextNumber() ?: return null
        val rotation = nextNumber() ?: return null
        skipSeparator()
        val largeArc = flag() ?: return null
        skipSeparator()
        val sweep = flag() ?: return null
        val end = nextPair() ?: return null
        return SvgArc(rx, ry, rotation, largeArc, sweep, end)
    }

    /**
     * Reads a number where one stands next: a sign, digits with at most one decimal point and a digit on one
     * side of it at least, then an exponent where `e` is followed by digits. The text stays unread where no
     * number, or no finite one, stands next.
     */
    fun number(): Double? {
        var cursor = at

        fun digits(): Int {
            val first = cursor
            while (cursor < text.length && text[cursor] in '0'..'9') cursor += 1
            return cursor - first
        }

        if (cursor < text.length && (text[cursor] == '+' || text[cursor] == '-')) cursor += 1
        var counted = digits()
        if (cursor < text.length && text[cursor] == '.') {
            cursor += 1
            counted += digits()
        }
        if (counted == 0) return null
        if (cursor < text.length && (text[cursor] == 'e' || text[cursor] == 'E')) {
            val mantissa = cursor
            cursor += 1
            if (cursor < text.length && (text[cursor] == '+' || text[cursor] == '-')) cursor += 1
            if (digits() == 0) cursor = mantissa
        }
        val number = text.substring(at, cursor).toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null
        at = cursor
        return number
    }
}

/** SVG's white space: a space, a tab, a line feed, a form feed and a carriage return. */
private const val SVG_WHITE_SPACE = " \t\n\u000C\r"

/** The letters of SVG's path commands, absolute and relative. */
private const val PATH_COMMANDS = "MmZzLlHhVvCcSsQqTtAa"
