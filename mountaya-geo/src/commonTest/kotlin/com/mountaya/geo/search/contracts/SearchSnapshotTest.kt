package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SearchSnapshotTest {
    @Test
    fun aSnapshotNamesItsQueryAndStandsIdleUnsettledAndUnbroadenedByDefault() {
        val snapshot = SearchSnapshot("chamonix")

        assertEquals("chamonix", snapshot.query)
        assertFalse(snapshot.isSearching)
        assertFalse(snapshot.isSettled)
        assertFalse(snapshot.hasFallenBack)
        assertTrue(snapshot.sections.isEmpty())
        val held =
            SearchSnapshot(
                "",
                isSearching = true,
                isSettled = true,
                hasFallenBack = true,
                sections = listOf(SearchSection(DataSchema.SearchTarget.Places, SearchSectionState.Loading)),
            )
        assertTrue(held.isSearching && held.isSettled && held.hasFallenBack)
        assertEquals(listOf(DataSchema.SearchTarget.Places), held.sections.map { it.id })
    }

    @Test
    fun twoSnapshotsAreEqualWhereEveryMemberIsAFailureByIdentity() {
        val failure = SearchBranchException(DataSchema.SearchTarget.Zones)

        fun failed(error: Throwable) =
            SearchSnapshot(
                "a",
                sections = listOf(SearchSection(DataSchema.SearchTarget.Zones, SearchSectionState.Failed(error))),
            )

        assertEquals(SearchSnapshot("a", isSettled = true), SearchSnapshot("a", isSettled = true))
        assertNotEquals(SearchSnapshot("a", isSettled = true), SearchSnapshot("a"))
        assertEquals(failed(failure), failed(failure))
        assertNotEquals(failed(failure), failed(SearchBranchException(DataSchema.SearchTarget.Zones)))
    }
}
