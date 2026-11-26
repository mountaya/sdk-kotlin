package com.mountaya.elements

import com.mountaya.geo.ShapePaint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class StripePatternTest {
    private fun pattern(
        ground: String? = "#ffffff",
        segments: List<List<List<Double>>> = listOf(listOf(listOf(0.0, 16.0), listOf(16.0, 0.0))),
    ): StripePattern = StripePattern(16.0, 4.0, "#f03e3e", ground, ShapePaint.LineCap.Round, segments)

    @Test
    fun twoPatternsAreEqualWhereEveryMemberIsEveryStripesEndsTheSameNumbers() {
        assertEquals(pattern(), pattern())
        assertEquals(pattern().hashCode(), pattern().hashCode())
        assertNotEquals(pattern(ground = null), pattern())
        assertEquals(pattern(ground = null), pattern(ground = null))
        assertNotEquals(pattern(segments = listOf(listOf(listOf(0.0, 16.0), listOf(16.0, -0.0)))), pattern())
        assertNotEquals(pattern(segments = emptyList()), pattern())
        assertNotEquals(pattern(segments = listOf(listOf(listOf(0.0, 16.0)))), pattern())
    }
}
