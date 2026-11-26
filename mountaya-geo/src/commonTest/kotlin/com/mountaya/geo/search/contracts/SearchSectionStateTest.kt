package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchSectionStateTest {
    private class Refused : Exception("refused")

    @Test
    fun aStateSaysWhetherItLoadsWhetherItSettledAndWhyItFailed() {
        assertTrue(SearchSectionState.Loading.isLoading)
        assertFalse(SearchSectionState.Loading.isSettled)
        assertNull(SearchSectionState.Loading.failure)
        assertTrue(SearchSectionState.Settled.isSettled)
        assertFalse(SearchSectionState.Settled.isLoading)
        assertNull(SearchSectionState.Settled.failure)
        val refused = Refused()
        val failed = SearchSectionState.Failed(refused)
        assertTrue(!failed.isLoading && !failed.isSettled)
        assertIs<Refused>(failed.failure)
        assertEquals<SearchSectionState>(SearchSectionState.Failed(refused), failed, "the very same failure")
        assertNotEquals<SearchSectionState>(SearchSectionState.Failed(Refused()), failed)
    }
}
