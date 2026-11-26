package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ShapePaintTest {
    private fun paint(
        width: Double,
        miter: Double = 4.0,
        filled: Boolean = false,
    ): ShapePaint =
        ShapePaint(
            isFilled = filled,
            isStroked = true,
            strokeWidth = width,
            lineCap = ShapePaint.LineCap.Round,
            lineJoin = ShapePaint.LineJoin.Round,
            miterLimit = miter,
            fillRule = ShapePaint.FillRule.NonZero,
        )

    @Test
    fun eachKeywordIsTheOneSvgSpells() {
        assertEquals(listOf("butt", "round", "square"), ShapePaint.LineCap.entries.map { it.id })
        assertEquals(listOf("arcs", "bevel", "miter", "miter-clip", "round"), ShapePaint.LineJoin.entries.map { it.id })
        assertEquals(listOf("nonzero", "evenodd"), ShapePaint.FillRule.entries.map { it.id })
    }

    @Test
    fun twoPaintsAreEqualWhereTheyHoldTheSameNumbersNanAsNanAnd0ApartFromMinus0() {
        assertEquals(paint(2.0), paint(2.0))
        assertEquals(paint(Double.NaN), paint(Double.NaN))
        assertEquals(paint(Double.NaN).hashCode(), paint(Double.NaN).hashCode())
        assertNotEquals(paint(0.0), paint(-0.0))
        assertNotEquals(paint(2.0, miter = 4.0), paint(2.0, miter = 10.0))
        assertNotEquals(paint(2.0, filled = true), paint(2.0))
    }
}
