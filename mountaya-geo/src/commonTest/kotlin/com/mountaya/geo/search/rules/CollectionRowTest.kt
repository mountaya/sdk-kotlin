package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CollectionRowTest {
    @Test
    fun aCollectionCountsItsItinerariesAndCarriesItsFields() {
        val row =
            collectionRow(
                DataSchema.CollectionSearchResult {
                    id = "col-1"
                    name = "Alpine Classics"
                    isPrivate = true
                    count = DataSchema.CollectionCount { itineraries = 7 }
                },
                "classic",
            )
        assertEquals("collection:col-1", row?.id)
        assertEquals(listOf(7 until 14), row?.matched)
        assertEquals(SearchRowSecondary.Count(7), row?.secondary)
        assertEquals(SearchRowPayload.Collection("col-1", "Alpine Classics", null, true), row?.payload)
        assertNull(collectionRow(DataSchema.CollectionSearchResult { name = "x" }, ""))
    }
}
