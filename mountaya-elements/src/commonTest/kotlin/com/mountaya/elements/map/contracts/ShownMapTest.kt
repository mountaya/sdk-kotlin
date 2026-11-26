package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ShownMapTest {
    @Test
    fun twoPlacesAreEqualWhereEveryFigureIs() {
        val shown = ShownMap(listOf(0.0, 56.0, 390.0, 844.0), listOf(0.0, 56.0, 390.0, 810.0))

        assertEquals(ShownMap(listOf(0.0, 56.0, 390.0, 844.0), listOf(0.0, 56.0, 390.0, 810.0)), shown)
        assertEquals(
            ShownMap(listOf(0.0, 56.0, 390.0, 844.0), listOf(0.0, 56.0, 390.0, 810.0)).hashCode(),
            shown.hashCode(),
        )
        assertNotEquals(ShownMap(listOf(0.0, 0.0, 390.0, 844.0), listOf(0.0, 56.0, 390.0, 810.0)), shown)
        assertNotEquals(ShownMap(listOf(0.0, 56.0, 390.0, 844.0), listOf(0.0, 80.0, 390.0, 810.0)), shown)
    }
}
