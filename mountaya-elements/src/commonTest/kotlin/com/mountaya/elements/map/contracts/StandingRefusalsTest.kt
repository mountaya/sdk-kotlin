package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StandingRefusalsTest {
    @Test
    fun nothingStandsUntilASourcesRefusalOrOneNamingNoneDoes() {
        assertTrue(StandingRefusals().isEmpty)
        assertFalse(StandingRefusals(sources = listOf("slope")).isEmpty)
        assertFalse(StandingRefusals(unsourced = true).isEmpty)
        assertEquals(StandingRefusals(listOf("slope")), StandingRefusals(listOf("slope")))
    }
}
