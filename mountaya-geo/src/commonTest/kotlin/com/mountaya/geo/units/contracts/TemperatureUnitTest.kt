package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class TemperatureUnitTest {
    /**
     * The ids are the contract's spellings, ECMA-402's where it names the unit, and a runner reads
     * a fixture's unit by them.
     */
    @Test
    fun eachUnitIsSpelledByTheContractsId() {
        assertEquals(listOf("celsius", "fahrenheit"), TemperatureUnit.entries.map { it.id })
    }
}
