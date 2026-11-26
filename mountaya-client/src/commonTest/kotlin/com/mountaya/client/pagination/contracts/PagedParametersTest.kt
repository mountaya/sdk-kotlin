package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PagedParametersTest {
    @Test
    fun aPrintedListingsBuilderIsReadAndWrittenThroughItsCursorAndLimit() {
        val parameters = RestSchema.GetCollectionItinerariesParameters()
        val paged: PagedParameters = parameters
        assertNull(paged.pageCursor)

        parameters.cursor = "c5"
        parameters.limit = 3
        assertEquals("c5", paged.pageCursor)
        assertEquals(3, paged.pageLimit)

        paged.pageCursor = "c8"
        paged.pageLimit = 50
        assertEquals("c8", parameters.slot0)
        assertEquals(50, parameters.slot1)
    }
}
