package com.mountaya.elements

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.ShapePaint
import com.mountaya.geo.glyphPaint
import com.mountaya.geo.parseGlyph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A glyph as the renderer draws it, on the device whose paths it builds. */
class GlyphFiguresTest {
    @Test
    fun aGlyphIsAFigureForEachShapeThatDrawsPaintedAsItsSetPaintsIt() {
        val figures = assertNotNull(glyphFigures("map-pin", GlyphSet.marker), "the map pin is drawn as figures")
        val shapes = parseGlyph(assertNotNull(GlyphSet.marker.markup["map-pin"], "the marker set holds the map pin"))

        assertEquals(shapes.size, figures.size)
        assertEquals(shapes.map { glyphPaint(it, GlyphSet.marker) }, figures.map { it.paint })
        assertTrue(figures.all { it.paint.isStroked && !it.paint.isFilled })
    }

    @Test
    fun aGlyphTheSetDoesNotHoldIsNoFiguresAShapeThatDrawsNothingIsNoFigure() {
        val set =
            assertNotNull(
                GlyphSet.of(
                    "test",
                    GlyphSet.Paint.Fill,
                    markup = mapOf("dot" to """<circle r="0"/><circle cx="4" cy="4" r="2" transform="scale(2)"/>"""),
                ),
                "the test set is a set",
            )

        assertNull(glyphFigures("hut", GlyphSet.control))
        val figures = assertNotNull(glyphFigures("dot", set), "the dot is drawn as figures")
        assertEquals(1, figures.size)
        assertTrue(figures[0].transform.values.contentEquals(Matrix().apply { scale(2f, 2f) }.values))
        assertTrue(figures[0].paint.isFilled)
    }

    @Test
    fun markupTheGlyphAllowlistDoesNotKeepDrawsNothing() {
        val set =
            assertNotNull(
                GlyphSet.of(
                    "test",
                    GlyphSet.Paint.Stroke,
                    markup = mapOf("hostile" to """<script>x</script><image href="https://example.invalid/a.png"/>"""),
                ),
                "the test set is a set",
            )

        assertEquals(emptyList(), glyphFigures("hostile", set))
    }

    @Test
    fun composeDrawsSvgsCapsAndAMiterForTheJoinsItHasNoneOf() {
        assertEquals(
            listOf(StrokeCap.Butt, StrokeCap.Round, StrokeCap.Square),
            ShapePaint.LineCap.entries.map(::composeLineCap),
        )
        assertEquals(
            listOf(StrokeJoin.Miter, StrokeJoin.Bevel, StrokeJoin.Miter, StrokeJoin.Miter, StrokeJoin.Round),
            ShapePaint.LineJoin.entries.map(::composeLineJoin),
        )
    }
}
