package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeclaredQueryTest {
    @Test
    fun theFirstCursorAndLimitTheCallerNamesAreTheOnesTheWalkReadsBack() {
        val query = DeclaredQuery(listOf("kind" to "hut", "cursor" to "c0", "limit" to "9", "cursor" to "c9"))

        assertEquals("c0", query.pageCursor)
        assertEquals(9, query.pageLimit)
    }

    @Test
    fun aPageSendsTheCallersOtherPairsInTheirOrderThenTheWalksCursorAndLimit() {
        val query = DeclaredQuery(listOf("limit" to "9", "kind" to "hut", "cursor" to "c0", "sort" to "-name"))
        query.pageCursor = "c2"
        query.pageLimit = 2

        assertEquals(listOf("kind" to "hut", "sort" to "-name", "cursor" to "c2", "limit" to "2"), query.pairs)
    }

    @Test
    fun aFirstPageSendsNoCursorAndALimitThatIsNoNumberIsNone() {
        val query = DeclaredQuery(listOf("limit" to "many"))

        assertNull(query.pageCursor)
        assertNull(query.pageLimit)
        assertEquals(emptyList(), query.pairs)
        query.pageLimit = 50
        assertEquals(listOf("limit" to "50"), query.pairs)
    }
}
