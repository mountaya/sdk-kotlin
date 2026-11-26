package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchFetchTest {
    @Test
    fun aFetchOfAnAppsOwnAnswersTheInputItIsHanded() =
        runTest {
            val fetch = SearchFetch { input -> DataSchema.SearchResult { query = input.query } }

            val result = fetch.search(DataSchema.SearchInput("chamonix", DataSchema.SearchTargetsInput()))
            assertEquals("chamonix", result.query)
        }
}
