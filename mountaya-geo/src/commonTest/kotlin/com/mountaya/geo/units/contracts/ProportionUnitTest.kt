package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ProportionUnitTest {
    /**
     * The ids are the contract's spellings, ECMA-402's where it names the unit, and a runner reads
     * a fixture's unit by them.
     */
    @Test
    fun eachUnitIsSpelledByTheContractsId() {
        assertEquals(listOf("percent", "ratio"), ProportionUnit.entries.map { it.id })
    }
}
