package com.mountaya.elements

import androidx.compose.ui.graphics.Matrix
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * svgTransform answers the transform an SVG transform list [list] states, each of its transforms applied to
 * a point in turn from the last to the first, as SVG nests them: a group's transform, which `parseGlyph`
 * folds before a shape's own, places the shape after the shape's own has.
 *
 * It reads `matrix`, `translate`, `scale`, `rotate`, about the origin or a centre it states, `skewX` and
 * `skewY`, each angle in degrees, separated by white space or a comma, and answers no transform at all for a
 * list that holds anything else, as a renderer reads a list in error: no part of it applies. An empty list is
 * no transform. The transform is worked out at double precision and held as Compose's matrix, at single.
 */
internal fun svgTransform(list: String): Matrix {
    val scanner = SvgScanner(list)
    var transform = Affine.identity
    scanner.skipWhiteSpace()
    while (!scanner.isAtEnd) {
        val next = nextTransform(scanner) ?: return Matrix()
        // The later transform applies to a point first.
        transform = transform.after(next)
        scanner.skipSeparator()
    }
    return transform.matrix()
}

/** An affine transform of the plane, SVG's `matrix(a b c d e f)`: x' = a x + c y + e, y' = b x + d y + f. */
private class Affine(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
    val e: Double,
    val f: Double,
) {
    /** This transform applied after [first], which applies to a point before it. */
    fun after(first: Affine): Affine =
        Affine(
            a * first.a + c * first.b,
            b * first.a + d * first.b,
            a * first.c + c * first.d,
            b * first.c + d * first.d,
            a * first.e + c * first.f + e,
            b * first.e + d * first.f + f,
        )

    /** The transform as Compose's 4 by 4 matrix, which holds it in its columns. */
    fun matrix(): Matrix =
        Matrix().also {
            it.values[Matrix.ScaleX] = a.toFloat()
            it.values[Matrix.SkewY] = b.toFloat()
            it.values[Matrix.SkewX] = c.toFloat()
            it.values[Matrix.ScaleY] = d.toFloat()
            it.values[Matrix.TranslateX] = e.toFloat()
            it.values[Matrix.TranslateY] = f.toFloat()
        }

    companion object {
        val identity = Affine(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)

        fun translation(
            x: Double,
            y: Double,
        ): Affine = Affine(1.0, 0.0, 0.0, 1.0, x, y)
    }
}

/** The transforms of a list, each with the arguments it takes at least and at most. */
private class TransformArguments(
    val name: String,
    val least: Int,
    val most: Int,
)

private val transformArguments =
    listOf(
        TransformArguments("matrix", 6, 6),
        TransformArguments("translate", 1, 2),
        TransformArguments("scale", 1, 2),
        TransformArguments("rotate", 1, 3),
        TransformArguments("skewX", 1, 1),
        TransformArguments("skewY", 1, 1),
    )

/** Reads one transform of a list, its name, white space, and its arguments in parentheses, or null where none stands next. */
private fun nextTransform(scanner: SvgScanner): Affine? {
    val named = transformArguments.firstOrNull { scanner.skip(it.name) } ?: return null
    scanner.skipWhiteSpace()
    if (!scanner.skip("(")) return null
    scanner.skipWhiteSpace()
    val arguments = mutableListOf<Double>()
    while (arguments.size < named.most) {
        if (arguments.isNotEmpty()) scanner.skipSeparator()
        arguments += scanner.number() ?: break
    }
    scanner.skipWhiteSpace()
    if (!scanner.skip(")") || arguments.size < named.least ||
        (named.name == "rotate" && arguments.size == 2)
    ) {
        return null
    }
    val radians = arguments[0] * PI / 180.0
    return when (named.name) {
        "matrix" -> {
            Affine(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5])
        }

        "translate" -> {
            Affine.translation(arguments[0], arguments.getOrElse(1) { 0.0 })
        }

        "scale" -> {
            Affine(arguments[0], 0.0, 0.0, arguments.getOrElse(1) { arguments[0] }, 0.0, 0.0)
        }

        "rotate" -> {
            val rotation = Affine(cos(radians), sin(radians), -sin(radians), cos(radians), 0.0, 0.0)
            if (arguments.size != 3) {
                rotation
            } else {
                // About a centre: moved to the origin, turned, and moved back.
                Affine
                    .translation(
                        arguments[1],
                        arguments[2],
                    ).after(rotation)
                    .after(Affine.translation(-arguments[1], -arguments[2]))
            }
        }

        "skewX" -> {
            Affine(1.0, 0.0, tan(radians), 1.0, 0.0, 0.0)
        }

        else -> {
            Affine(1.0, tan(radians), 0.0, 1.0, 0.0, 0.0)
        }
    }
}
