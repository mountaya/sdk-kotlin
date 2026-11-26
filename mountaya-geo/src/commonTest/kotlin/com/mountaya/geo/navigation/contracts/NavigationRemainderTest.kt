package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NavigationRemainderTest {
    @Test
    fun twoRemaindersAreEqualWhereBothAreTheSameNumber() {
        assertEquals(NavigationRemainder(1_200.0, 600.0), NavigationRemainder(1_200.0, 600.0))
        assertEquals(NavigationRemainder(1_200.0, 600.0).hashCode(), NavigationRemainder(1_200.0, 600.0).hashCode())
        assertEquals(NavigationRemainder(Double.NaN, null), NavigationRemainder(Double.NaN, null))
        assertNotEquals(NavigationRemainder(0.0, 0.0), NavigationRemainder(0.0, -0.0))
        assertNotEquals(NavigationRemainder(0.0, 0.0), NavigationRemainder(0.0, null))
        assertEquals("NavigationRemainder(distance=0.0, duration=null)", NavigationRemainder(0.0, null).toString())
    }
}
