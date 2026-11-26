package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GlyphPaintTest {
    private fun set(
        paint: GlyphSet.Paint,
        width: Double = 2.0,
    ): GlyphSet =
        assertNotNull(GlyphSet.of("case", paint, strokeWidth = width, markup = emptyMap()), "the case's set is a set")

    private fun shape(
        vararg attributes: Pair<String, String>,
        tag: String = "path",
    ): GlyphShape = GlyphShape(tag, mapOf(*attributes))

    private fun paint(
        filled: Boolean,
        stroked: Boolean,
        width: Double = 2.0,
        cap: ShapePaint.LineCap = ShapePaint.LineCap.Round,
        join: ShapePaint.LineJoin = ShapePaint.LineJoin.Round,
        miter: Double = 4.0,
        rule: ShapePaint.FillRule = ShapePaint.FillRule.NonZero,
    ): ShapePaint = ShapePaint(filled, stroked, width, cap, join, miter, rule)

    @Test
    fun aStrokeSetStrokesEachShapeInTheCurrentColourAtItsWidthAndFillsNone() {
        assertEquals(
            paint(filled = false, stroked = true),
            glyphPaint(shape("d" to "M5 12h14"), set(GlyphSet.Paint.Stroke)),
        )
        assertEquals(
            paint(filled = false, stroked = true, width = 3.0),
            glyphPaint(shape("d" to "M3 3h12"), set(GlyphSet.Paint.Stroke, width = 3.0)),
        )
    }

    @Test
    fun aFillSetFillsEachShapeInTheCurrentColourAndStrokesNone() {
        assertEquals(
            paint(filled = true, stroked = false),
            glyphPaint(shape("d" to "M2 2h20v20H2z"), set(GlyphSet.Paint.Fill)),
        )
    }

    @Test
    fun aShapeThatStatesNothingIsDrawnInTheSetsCapsJoinsMiterLimitAndFillRule() {
        for (paint in GlyphSet.Paint.entries) {
            val drawn = glyphPaint(GlyphShape("circle", emptyMap()), set(paint))
            assertEquals(ShapePaint.LineCap.Round, drawn.lineCap)
            assertEquals(ShapePaint.LineJoin.Round, drawn.lineJoin)
            assertEquals(4.0, drawn.miterLimit)
            assertEquals(ShapePaint.FillRule.NonZero, drawn.fillRule)
        }
    }

    @Test
    fun aDotAStrokeSetFillsOfItsOwnIsStrokedAroundTooUnlessItStatesNoStroke() {
        val stroke = set(GlyphSet.Paint.Stroke)

        assertEquals(
            paint(filled = true, stroked = true),
            glyphPaint(shape("r" to ".5", "fill" to "currentColor", tag = "circle"), stroke),
        )
        assertEquals(
            paint(filled = true, stroked = false),
            glyphPaint(shape("r" to ".5", "fill" to "currentColor", "stroke" to "none", tag = "circle"), stroke),
        )
    }

    @Test
    fun aFillSetStrokesAShapeThatAsksForAStrokeAtTheSetsWidthWithRoundEnds() {
        assertEquals(
            paint(filled = false, stroked = true, width = 1.5),
            glyphPaint(
                shape("d" to "M12 17h.01", "fill" to "none", "stroke" to "currentColor"),
                set(GlyphSet.Paint.Fill, width = 1.5),
            ),
        )
    }

    @Test
    fun aShapesOwnCapJoinMiterLimitAndFillRuleReplaceTheSetsForItAlone() {
        val stroke = set(GlyphSet.Paint.Stroke, width = 3.0)

        assertEquals(
            paint(
                filled = false,
                stroked = true,
                width = 3.0,
                cap = ShapePaint.LineCap.Square,
                join = ShapePaint.LineJoin.Miter,
                miter = 10.0,
            ),
            glyphPaint(
                shape(
                    "d" to "M3 3h12",
                    "stroke-linecap" to "square",
                    "stroke-linejoin" to "miter",
                    "stroke-miterlimit" to "10",
                ),
                stroke,
            ),
        )
        assertEquals(
            paint(filled = true, stroked = false, rule = ShapePaint.FillRule.EvenOdd),
            glyphPaint(shape("d" to "M0 0h24v24H0zM6 6v12h12V6z", "fill-rule" to "evenodd"), set(GlyphSet.Paint.Fill)),
        )
        assertEquals(
            ShapePaint.LineJoin.MiterClip,
            glyphPaint(shape("d" to "M0 0", "stroke-linejoin" to "miter-clip"), stroke).lineJoin,
        )
        assertEquals(4.0, glyphPaint(shape("d" to "M0 0", "stroke-miterlimit" to "4."), stroke).miterLimit)
    }

    @Test
    fun whatAShapeStatesIsReadWithoutRegardToCase() {
        assertEquals(
            paint(
                filled = true,
                stroked = false,
                cap = ShapePaint.LineCap.Butt,
                join = ShapePaint.LineJoin.Bevel,
                rule = ShapePaint.FillRule.EvenOdd,
            ),
            glyphPaint(
                shape(
                    "d" to "M0 0h1",
                    "fill" to "CURRENTCOLOR",
                    "stroke" to "None",
                    "stroke-linecap" to "BUTT",
                    "stroke-linejoin" to "Bevel",
                    "fill-rule" to "EVENODD",
                ),
                set(GlyphSet.Paint.Stroke),
            ),
        )
        assertEquals(
            ShapePaint.FillRule.EvenOdd,
            glyphPaint(shape("d" to "M0 0", "fill-rule" to " EvenOdd\t"), set(GlyphSet.Paint.Fill)).fillRule,
        )
    }

    @Test
    fun aValueTheParseWouldDropIsNoStatementWhoeverBuiltTheShape() {
        val stroke = set(GlyphSet.Paint.Stroke)

        assertEquals(
            paint(filled = false, stroked = true),
            glyphPaint(
                shape(
                    "d" to "M0 0",
                    "fill" to "#ffffff",
                    "stroke" to "red",
                    "stroke-linecap" to "flat",
                    "stroke-linejoin" to "sharp",
                    "stroke-miterlimit" to "0.5",
                    "fill-rule" to "odd",
                ),
                stroke,
            ),
        )
        assertEquals(4.0, glyphPaint(shape("d" to "M0 0", "stroke-miterlimit" to "1e1"), stroke).miterLimit)
        // A shape never states a width: the width is the set's.
        assertEquals(2.0, glyphPaint(shape("d" to "M0 0", "stroke-width" to "9"), stroke).strokeWidth)
    }

    @Test
    fun eachShapeOfAGlyphIsPaintedAlone() {
        val stroke = set(GlyphSet.Paint.Stroke)
        val shapes =
            parseGlyph(
                """<path d="M12 2v20"/><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/>""" +
                    """<path d="M2 12h20" stroke-linecap="square"/>""",
            )

        assertEquals(
            listOf(
                paint(filled = false, stroked = true),
                paint(filled = true, stroked = false),
                paint(filled = false, stroked = true, cap = ShapePaint.LineCap.Square),
            ),
            shapes.map { glyphPaint(it, stroke) },
        )
    }
}
