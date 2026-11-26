package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnansweredSearchSnapshotTest {
    @Test
    fun aQueryALiveSearchOwesARoundReadsAsTheSkeletonOfThatRound() {
        val snapshot =
            unansweredSearchSnapshot(
                " cha ",
                true,
                listOf(DataSchema.SearchTarget.Places, DataSchema.SearchTarget.Zones),
            )

        assertEquals("cha", snapshot.query)
        assertTrue(snapshot.isSearching)
        assertTrue(!snapshot.isSettled && !snapshot.hasFallenBack)
        assertEquals(
            listOf(DataSchema.SearchTarget.Places, DataSchema.SearchTarget.Zones),
            snapshot.sections.map { it.id },
        )
        assertTrue(snapshot.sections.all { it.state.isLoading })
    }

    @Test
    fun aBlankQueryOrOneWithNoTransportOwesNoRoundAndReadsSettledWithNothing() {
        for (snapshot in listOf(
            unansweredSearchSnapshot("   ", true, listOf(DataSchema.SearchTarget.Places)),
            unansweredSearchSnapshot("cha", false, listOf(DataSchema.SearchTarget.Places)),
        )) {
            assertTrue(!snapshot.isSearching)
            assertTrue(snapshot.sections.all { it.state.isSettled })
            assertTrue(snapshot.sections.all { it.rows.isEmpty() })
        }
        assertEquals("cha", unansweredSearchSnapshot("cha", false, emptyList()).query)
    }
}
