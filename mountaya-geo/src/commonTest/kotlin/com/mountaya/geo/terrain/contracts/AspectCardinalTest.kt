package com.mountaya.geo.terrain.contracts

import com.mountaya.geo.AspectCardinal
import kotlin.test.Test
import kotlin.test.assertEquals

class AspectCardinalTest {
    @Test
    fun theDirectionsRunClockwiseFromNorthAnEighthOfATurnApartEachByItsCode() {
        assertEquals(listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW"), AspectCardinal.entries.map { it.code })
        AspectCardinal.entries.forEachIndexed { index, direction -> assertEquals(index * 45.0, direction.heading) }
    }
}
