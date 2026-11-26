package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class BadgeSurfaceTest {
    @Test
    fun aBadgeStandsInATablesCellInAProfilesStackOrBesideALabelOfItsOwn() {
        assertEquals(listOf("cell", "stack", "labelled"), BadgeSurface.entries.map { it.id })
        assertEquals(listOf("Water", "W", null), BadgeSurface.entries.map { badgeFallback("Water", it) })
    }
}
