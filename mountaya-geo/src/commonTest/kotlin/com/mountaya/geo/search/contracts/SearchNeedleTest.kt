package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchNeedleTest {
    @Test
    fun aNeedleHoldsTheFoldItWasGivenAndTwoAreEqualByIt() {
        assertEquals("evian", SearchNeedle("evian").text)
        assertEquals(SearchNeedle("evian"), SearchNeedle("evian"))
        assertNotEquals(SearchNeedle("evian"), SearchNeedle("geneve"))
    }
}
