package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SteepnessClassTest {
    @Test
    fun eachClassIsSpelledByItsIdAndStartsWhereThePreviousEnds() {
        assertEquals(listOf("25-30", "30-35", "35-40", "40-45", "45+"), SteepnessClass.entries.map { it.id })
        assertEquals(listOf(25.0, 30.0, 35.0, 40.0, 45.0), SteepnessClass.entries.map { it.fromDegrees })
    }
}
