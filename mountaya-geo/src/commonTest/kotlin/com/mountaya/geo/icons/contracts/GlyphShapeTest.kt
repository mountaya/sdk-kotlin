package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GlyphShapeTest {
    @Test
    fun twoShapesAreEqualWhereTheyHoldTheSameTagAndAttributesWhateverTheirOrder() {
        val one = GlyphShape("circle", linkedMapOf("cx" to "1", "r" to "2"))
        val other = GlyphShape("circle", linkedMapOf("r" to "2", "cx" to "1"))
        assertEquals(one, other)
        assertEquals(one.hashCode(), other.hashCode())
        assertNotEquals(one, GlyphShape("ellipse", one.attributes))
    }
}
