package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DataExceptionTest {
    @Test
    fun theClassifyingCodeIsTheFirstErrorsWhereItCarriesOne() {
        val coded =
            DataException(
                listOf(
                    ErrorEntry("Too many.", ApiErrorCode("TOO_MANY_REQUESTS")),
                    ErrorEntry("Other.", ApiErrorCode("INTERNAL_ERROR")),
                ),
            )
        val uncoded = DataException(listOf(ErrorEntry("boom")))

        assertEquals(ApiErrorCode("TOO_MANY_REQUESTS"), coded.code)
        assertEquals("200 TOO_MANY_REQUESTS: Too many.", coded.message)
        assertNull(uncoded.code)
        assertEquals("200: boom", uncoded.message)
    }

    @Test
    fun itComesUnderTheSuccessItWasAnsweredWith() {
        assertEquals(200, DataException().status)
        assertEquals(201, DataException(201, emptyList(), AnswerFacts(), null).status)
        assertFailsWith<IllegalArgumentException> { DataException(400, emptyList(), AnswerFacts(), null) }
    }
}
