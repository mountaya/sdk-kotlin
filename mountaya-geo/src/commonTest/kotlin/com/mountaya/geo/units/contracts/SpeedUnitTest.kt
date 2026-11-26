package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SpeedUnitTest {
    /**
     * The ids are the contract's spellings, ECMA-402's where it names the unit, and a runner reads
     * a fixture's unit by them.
     */
    @Test
    fun eachUnitIsSpelledByTheContractsId() {
        assertEquals(
            listOf(
                "kilometer-per-hour",
                "meter-per-second",
                "mile-per-hour",
                "minute-per-kilometer",
                "minute-per-mile",
            ),
            SpeedUnit.entries.map {
                it.id
            },
        )
    }
}
