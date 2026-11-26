package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShownSearchSnapshotTest {
    @Test
    fun aListShowsTheSearchsAnswerWhereItNamesTheQueryTypedTrimmed() {
        val answer = SearchSnapshot("chamonix", isSettled = true)
        val stand = SearchSnapshot("zermatt", isSearching = true)

        assertTrue(shownSearchSnapshot(answer, " chamonix ", stand).isSettled)
        assertEquals("zermatt", shownSearchSnapshot(answer, "zermatt", stand).query)
        assertEquals(
            "zermatt",
            shownSearchSnapshot(answer, "Chamonix", stand).query,
            "letter case is part of the query",
        )
        assertEquals(
            "zermatt",
            shownSearchSnapshot(SearchSnapshot("café"), "café", stand).query,
            "a query is compared by its code points",
        )
    }
}
