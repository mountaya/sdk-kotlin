package com.mountaya.geo.search.contracts

import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.SearchBranchException
import com.mountaya.geo.SearchListState
import com.mountaya.geo.SearchListStatus
import com.mountaya.geo.SearchSection
import com.mountaya.geo.SearchSectionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchListStateTest {
    @Test
    fun aListStateHoldsItsRowsItsFailedSectionsItsFlagsAndItsStatus() {
        val failed =
            SearchSection(
                DataSchema.SearchTarget.Zones,
                SearchSectionState.Failed(SearchBranchException(DataSchema.SearchTarget.Zones)),
            )
        val state = SearchListState(emptyList(), listOf(failed), false, true, SearchListStatus.Error)

        assertTrue(state.rows.isEmpty())
        assertEquals(listOf(DataSchema.SearchTarget.Zones), state.failed.map { it.id })
        assertFalse(state.isNothingFound)
        assertTrue(state.hasFallbackNote)
        assertEquals<SearchListStatus?>(SearchListStatus.Error, state.status)
        assertEquals(SearchListState(emptyList(), listOf(failed), false, true, SearchListStatus.Error), state)
    }
}
