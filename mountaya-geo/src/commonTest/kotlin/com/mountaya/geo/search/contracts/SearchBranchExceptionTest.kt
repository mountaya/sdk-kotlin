package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SearchBranchExceptionTest {
    @Test
    fun aFailureHoldsItsBranchItsCodeAndItsSentenceNoneStatedByDefault() {
        val failure =
            SearchBranchException(DataSchema.SearchTarget.Pois, DataSchema.ErrorCode.BadGateway, "unreachable")

        assertEquals(DataSchema.SearchTarget.Pois, failure.target)
        assertEquals(DataSchema.ErrorCode.BadGateway, failure.code)
        assertEquals("unreachable", failure.message)
        assertNull(SearchBranchException(DataSchema.SearchTarget.Zones).code)
        assertNull(SearchBranchException(DataSchema.SearchTarget.Zones).message)
    }

    @Test
    fun twoFailuresAreEqualWhereTheySayTheSameEachTextByItsCodePoints() {
        assertEquals(
            SearchBranchException(DataSchema.SearchTarget.Pois),
            SearchBranchException(DataSchema.SearchTarget.Pois),
        )
        assertNotEquals(
            SearchBranchException(DataSchema.SearchTarget.Pois),
            SearchBranchException(DataSchema.SearchTarget.Zones),
        )
        assertNotEquals(
            SearchBranchException(DataSchema.SearchTarget.Pois, message = "é"),
            SearchBranchException(DataSchema.SearchTarget.Pois, message = "é"),
        )
        assertNotEquals(
            SearchBranchException(DataSchema.SearchTarget.Pois, DataSchema.ErrorCode.BadGateway),
            SearchBranchException(DataSchema.SearchTarget.Pois, null),
        )
    }
}
