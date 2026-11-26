package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutMeasureTest {
    @Test
    fun eachMeasureIsSpelledAsTheContractSpellsIt() {
        assertEquals(
            listOf("temperature", "humidity", "snowfall", "distance", "speed"),
            ReadoutMeasure.entries.map { it.id },
        )
    }
}
