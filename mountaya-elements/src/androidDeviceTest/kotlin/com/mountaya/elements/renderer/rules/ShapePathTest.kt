package com.mountaya.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.GlyphShape
import com.mountaya.geo.parseGlyph
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The SVG geometry reader, on the device whose paths it builds. */
class ShapePathTest {
    private fun data(d: String): List<String> = pathSegments(svgPathData(d))

    private fun shape(
        tag: String,
        vararg attributes: Pair<String, String>,
    ): List<String> = pathSegments(shapePath(GlyphShape(tag, mapOf(*attributes))))

    private fun bounds(path: Path?): Rect? = path?.let(::tightBounds)

    private fun near(
        a: Float,
        b: Float,
    ): Boolean = abs(a - b) < 0.01f

    @Test
    fun aMoveLinesAndACloseAbsoluteAndRelativeEachRepeatedOverFurtherPairs() {
        assertEquals(listOf("M(5,12)", "L(19,12)"), data("M5 12h14"))
        assertEquals(listOf("M(1,1)", "L(3,3)", "L(6,6)"), data("m1 1 2 2 3 3"))
        assertEquals(
            listOf("M(1,1)", "L(2,2)", "L(3,3)", "L(3,5)", "L(0,5)", "L(0,4)", "L(2,4)", "Z"),
            data("M1 1 L2 2 3 3V5H0v-1h2z"),
        )
    }

    @Test
    fun numbersAreWrittenWithNothingBetweenThemWhereASignOrAPointEndsOne() {
        assertEquals(listOf("M(1,-2.5)", "L(0.5,5)"), data("M1-2.5.5.5e1"))
        assertEquals(listOf("M(1,2)", "L(3,4)"), data("M1,2,3,4"))
        // Compose holds a path's points at single precision, so each number here is one it holds exactly.
        assertEquals(listOf("M(10,-0.5)", "L(2,0.5)"), data("M 1e1 -5E-1 L+2. .5"))
        // An `e` with no digits after it is no exponent, and then no command.
        assertEquals(listOf("M(1,2)"), data("M1 2e"))
    }

    @Test
    fun aPathDrawsUpToTheCommandHoldingItsFirstErrorAndNothingBeforeAMove() {
        assertEquals(listOf("M(0,0)"), data("M0 0 L 1"))
        assertEquals(emptyList(), data("L0 0 L 1 1"))
        assertEquals(emptyList(), data(""))
        assertEquals(listOf("M(0,0)", "L(10,0)"), data("M0 0 h10 x 5"))
        assertEquals(listOf("M(0,0)", "Z"), data("M0 0 Z 5 5"))
        assertEquals(listOf("M(0,0)"), data("M0 0 L1,,1"))
        assertEquals(listOf("M(0,0)"), data("M0 0 L1e999 1"))
    }

    @Test
    fun aPathDrawnAfterACloseStartsWhereTheClosedOneStarted() {
        assertEquals(
            listOf("M(0,0)", "L(10,0)", "L(10,10)", "Z", "M(0,0)", "L(5,5)"),
            data("M0 0L10 0 10 10z l5 5"),
        )
        assertEquals(listOf("M(2,2)", "L(3,2)", "Z", "M(3,3)", "L(4,3)"), data("M2 2h1zm1 1h1"))
    }

    @Test
    fun aSmoothCurveReflectsTheControlPointOfTheCurveBeforeItWhereItIsOne() {
        assertEquals(listOf("M(0,0)", "C(1,2 3,4 5,6)", "C(7,8 9,9 10,10)"), data("M0 0C1 2 3 4 5 6S9 9 10 10"))
        assertEquals(listOf("M(0,0)", "Q(5,5 10,0)", "Q(15,-5 20,0)"), data("M0 0Q5 5 10 0T20 0"))
        // After a line, a smooth curve's control point is the current point.
        assertEquals(listOf("M(0,0)", "L(4,0)", "C(4,0 5,1 6,0)"), data("M0 0L4 0s1 1 2 0"))
        assertEquals(listOf("M(0,0)", "Q(1,1 2,0)", "L(4,0)", "Q(4,0 6,0)"), data("M0 0Q1 1 2 0L4 0t2 0"))
    }

    @Test
    fun anArcIsDrawnAsCubicCurvesEndingWhereItEndsOnTheSideItsFlagsChoose() {
        val upper = assertNotNull(bounds(svgPathData("M0 0a5 5 0 0 1 10 0")), "the arc is drawn")
        val lower = assertNotNull(bounds(svgPathData("M0 0a5 5 0 1010 0")), "the arc is drawn")
        assertTrue(near(upper.top, -5f) && near(upper.bottom, 0f), "$upper")
        assertTrue(near(lower.bottom, 5f) && near(lower.top, 0f), "$lower")
        // The arc ends exactly where it ends, never where rounding leaves it.
        assertTrue(data("M0 0a5 5 0 0 1 10 0").last().endsWith(" 10,0)"), "${data("M0 0a5 5 0 0 1 10 0")}")
        // A whole turn but for its end is drawn in four quarters at most.
        assertTrue(data("M0 0A5 5 0 1 1 0.0001 0").size <= 5)
        // Radii too small to reach the end are scaled up until they do; a sign is dropped.
        assertTrue(near(assertNotNull(bounds(svgPathData("M0 0A1 1 0 0 1 10 0")), "the arc is drawn").height, 5f))
        assertTrue(near(assertNotNull(bounds(svgPathData("M0 0A-5 -5 0 0 1 10 0")), "the arc is drawn").top, -5f))
        // A radius of 0 is a line, and an arc ending where it starts is nothing.
        assertEquals(listOf("M(0,0)", "L(10,0)"), data("M0 0A0 5 0 0 1 10 0"))
        assertEquals(listOf("M(3,3)"), data("M3 3A5 5 0 0 1 3 3"))
        // The axes turn with the rotation the arc states.
        val turned = assertNotNull(bounds(svgPathData("M0 0A10 5 90 0 1 0 20")), "the arc is drawn")
        assertTrue(near(turned.width, 5f) && near(turned.height, 20f), "$turned")
    }

    @Test
    fun eachShapeIsTheOutlineSvgLaysItOutAsAndOneOfNoSizeDrawsNothing() {
        assertEquals(
            Rect(9f, 7f, 15f, 13f),
            bounds(
                shapePath(
                    GlyphShape(
                        "circle",
                        mapOf(
                            "cx" to "12",
                            "cy" to "10",
                            "r" to "3",
                        ),
                    ),
                ),
            ),
        )
        assertEquals(emptyList(), shape("circle", "r" to "0"))
        assertEquals(emptyList(), shape("circle", "cx" to "1"))
        assertEquals(
            Rect(1f, -2f, 9f, 2f),
            bounds(
                shapePath(
                    GlyphShape(
                        "ellipse",
                        mapOf(
                            "cx" to "5",
                            "rx" to "4",
                            "ry" to "2",
                        ),
                    ),
                ),
            ),
        )
        assertEquals(Rect(-4f, -4f, 4f, 4f), bounds(shapePath(GlyphShape("ellipse", mapOf("rx" to "4")))))
        assertEquals(
            Rect(2f, 3f, 6f, 8f),
            bounds(shapePath(GlyphShape("rect", mapOf("x" to "2", "y" to "3", "width" to "4", "height" to "5")))),
        )
        assertEquals(emptyList(), shape("rect", "width" to "4"))
        assertEquals(emptyList(), shape("rect", "width" to "4", "height" to "-1"))
        assertEquals(listOf("M(1,2)", "L(3,4)"), shape("line", "x1" to "1", "y1" to "2", "x2" to "3", "y2" to "4"))
        assertEquals(listOf("M(0,0)", "L(4,0)", "L(4,4)", "Z"), shape("polygon", "points" to "0,0 4,0 4 4"))
        assertEquals(listOf("M(0,0)", "L(4,0)"), shape("polyline", "points" to "0 0, 4 0 9"))
        assertEquals(emptyList(), shape("polyline", "points" to ""))
        assertNull(shapePath(GlyphShape("path", emptyMap())))
        assertNull(shapePath(GlyphShape("g", mapOf("d" to "M0 0h1"))))
    }

    @Test
    fun aRectanglesCornerTakesTheOtherRadiusWhereOneIsLeftOutHeldWithinItsSide() {
        fun curved(segments: List<String>): Boolean =
            segments.any {
                it.startsWith("C") || it.startsWith("Q") ||
                    it.startsWith("K")
            }

        assertTrue(curved(shape("rect", "width" to "20", "height" to "10", "rx" to "4")))
        assertFalse(curved(shape("rect", "width" to "20", "height" to "10")))
        assertEquals(
            Rect(0f, 0f, 20f, 10f),
            bounds(shapePath(GlyphShape("rect", mapOf("width" to "20", "height" to "10", "rx" to "30", "ry" to "2")))),
        )
        assertFalse(curved(shape("rect", "width" to "20", "height" to "10", "rx" to "-4")))
    }

    @Test
    fun aLengthIsANumberInUserUnitsOrPxAndAnythingElseIsOneLeftOut() {
        assertEquals(12.0, svgLength(" 12 "))
        assertEquals(12.0, svgLength("12px"))
        assertEquals(100.0, svgLength("1e2"))
        assertEquals(-0.5, svgLength("-.5"))
        assertNull(svgLength("12em"))
        assertNull(svgLength("50%"))
        assertNull(svgLength(""))
        assertEquals(listOf(Offset(1f, 2f), Offset(3f, 4f)), svgPoints("1,2 3 4 5"))
        assertEquals(listOf(Offset(1f, 2f)), svgPoints("1 2 x 3 4"))
    }

    @Test
    fun everyGlyphOfBothBundledSetsDrawsInsideIts24UnitBox() {
        for (set in listOf(GlyphSet.marker, GlyphSet.control)) {
            for ((name, markup) in set.markup) {
                var drawn: Rect? = null
                for (shape in parseGlyph(markup)) {
                    val outline = shapePath(shape) ?: continue
                    val placed = Path().apply { addPath(outline) }
                    placed.transform(svgTransform(shape.attributes["transform"].orEmpty()))
                    val shown = tightBounds(placed) ?: continue
                    drawn =
                        drawn?.let {
                            Rect(
                                minOf(it.left, shown.left),
                                minOf(it.top, shown.top),
                                maxOf(it.right, shown.right),
                                maxOf(it.bottom, shown.bottom),
                            )
                        }
                            ?: shown
                }
                assertNotNull(drawn, "${set.name}/$name draws nothing")
                assertTrue(
                    drawn.left > -0.01f && drawn.top > -0.01f && drawn.right < 24.01f && drawn.bottom < 24.01f,
                    "${set.name}/$name: $drawn",
                )
            }
        }
    }
}
