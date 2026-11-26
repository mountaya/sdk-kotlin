package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchSectionTest {
    @Test
    fun aSectionIsIdentifiedByItsBranchAndHoldsNoRowUnlessHandedSome() {
        val section = SearchSection(DataSchema.SearchTarget.Zones, SearchSectionState.Settled)

        assertEquals(DataSchema.SearchTarget.Zones, section.id)
        assertTrue(section.rows.isEmpty())
        assertTrue(section.state.isSettled)
        val row = SearchRow("zone:z", "Couloir", emptyList(), null, emptyList(), SearchRowPayload.Zone("z", null, null))
        assertEquals(
            listOf(row),
            SearchSection(DataSchema.SearchTarget.Zones, SearchSectionState.Settled, listOf(row)).rows,
        )
        assertEquals(
            SearchSection(DataSchema.SearchTarget.Zones, SearchSectionState.Settled, listOf(row)),
            SearchSection(DataSchema.SearchTarget.Zones, SearchSectionState.Settled, listOf(row)),
        )
    }
}
