package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProfileRangeTest {
    @Test
    fun twoStretchesAreEqualWhereBothEndsAreTheSameNumbersAndHashAlike() {
        val range = ProfileRange(200.0, 800.0)

        assertEquals(ProfileRange(200.0, 800.0), range)
        assertEquals(ProfileRange(200.0, 800.0).hashCode(), range.hashCode())
        assertNotEquals(ProfileRange(800.0, 200.0), range)
        assertEquals(ProfileRange(Double.NaN, 1.0), ProfileRange(Double.NaN, 1.0))
        assertNotEquals(ProfileRange(-0.0, 1.0), ProfileRange(0.0, 1.0))
    }

    @Test
    fun aStretchIsASpanItsEndsCanBeMovedOn() {
        val span: ProfileSpan<*> = ProfileRange(1.0, 2.0)
        val moved = span.spanning(0.0, span.to)

        assertEquals(0.0, moved.from)
        assertEquals(2.0, moved.to)
    }
}
