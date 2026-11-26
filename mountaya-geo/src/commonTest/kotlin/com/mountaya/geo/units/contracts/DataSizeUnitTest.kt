package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class DataSizeUnitTest {
    /**
     * The ids are the contract's spellings, ECMA-402's where it names the unit, and a runner reads
     * a fixture's unit by them.
     */
    @Test
    fun eachUnitIsSpelledByTheContractsId() {
        assertEquals(
            listOf(
                "bit",
                "byte",
                "kibibyte",
                "kilobit",
                "kilobyte",
                "mebibyte",
                "megabit",
                "megabyte",
            ),
            DataSizeUnit.entries.map {
                it.id
            },
        )
    }
}
