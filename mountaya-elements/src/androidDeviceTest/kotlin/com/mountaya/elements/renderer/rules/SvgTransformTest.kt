package com.mountaya.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/** The SVG transform reader, held as Compose's matrix on the device. */
class SvgTransformTest {
    private fun applied(
        list: String,
        x: Float,
        y: Float,
    ): Offset = svgTransform(list).map(Offset(x, y))

    /** Whether [a] is [b] to a hundred-thousandth, the precision Compose's single-precision matrix holds it at. */
    private fun near(
        a: Offset,
        b: Offset,
    ): Boolean = abs(a.x - b.x) < 1e-5f && abs(a.y - b.y) < 1e-5f

    private fun isIdentity(matrix: Matrix): Boolean = matrix.values.contentEquals(Matrix().values)

    @Test
    fun eachTransformSvgNamesItsArgumentsSeparatedByWhiteSpaceOrAComma() {
        assertTrue(near(applied("translate(2 3)", 1f, 1f), Offset(3f, 4f)))
        assertTrue(near(applied("translate(2)", 1f, 1f), Offset(3f, 1f)))
        assertTrue(near(applied("scale(2,3)", 1f, 1f), Offset(2f, 3f)))
        assertTrue(near(applied("scale(2)", 1f, 1f), Offset(2f, 2f)))
        assertTrue(near(applied("matrix(1 2 3 4 5 6)", 1f, 1f), Offset(9f, 12f)))
        assertTrue(near(applied("rotate(90)", 1f, 0f), Offset(0f, 1f)))
        assertTrue(near(applied("rotate(90 12 12)", 24f, 12f), Offset(12f, 24f)))
        assertTrue(near(applied("skewX(45)", 0f, 1f), Offset(1f, 1f)))
        assertTrue(near(applied("skewY(45)", 1f, 0f), Offset(1f, 1f)))
    }

    @Test
    fun aListAppliesItsLastTransformFirstAsSvgNestsAGroupsAroundAShapesOwn() {
        assertTrue(near(applied("translate(2 2) scale(3)", 1f, 1f), Offset(5f, 5f)))
        assertTrue(near(applied("scale(3) translate(2 2)", 1f, 1f), Offset(9f, 9f)))
        assertTrue(near(applied(" translate(1),translate(1) ", 0f, 0f), Offset(2f, 0f)))
        assertTrue(near(applied("translate ( 1 , 2 )", 0f, 0f), Offset(1f, 2f)))
    }

    @Test
    fun aListHoldingAnythingElseIsNoTransformAtAllAndSoIsAnEmptyOne() {
        for (list in listOf(
            "translate(2) foo(3)",
            "Translate(2)",
            "rotate(1 2)",
            "matrix(1 2 3)",
            "scale()",
            "translate(1 2 3)",
            "translate(1",
            "translate(1)x",
        )) {
            assertTrue(isIdentity(svgTransform(list)), list)
        }
        assertTrue(isIdentity(svgTransform("")))
        assertTrue(isIdentity(svgTransform("   ")))
    }
}
