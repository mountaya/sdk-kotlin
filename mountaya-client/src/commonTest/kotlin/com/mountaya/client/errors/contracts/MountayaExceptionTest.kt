package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

class MountayaExceptionTest {
    @Test
    fun eachClassNamesTheContractsClass() {
        assertEquals("NotFoundError", ApiException.of(404).name)
        assertEquals("ApiError", ApiException.of(418).name)
        assertEquals("ConnectionError", ConnectionException().name)
        assertEquals("ConfigurationError", ConfigurationException("x").name)
        assertEquals("SelectionError", SelectionException(listOf("nope"), "x").name)
        assertEquals("DataError", DataException(emptyList()).name)
    }

    @Test
    fun theMessageCarriesTheFailureThenTheNextStepItsStatusNames() {
        val failure = ApiException.of(429, listOf(ErrorEntry("A ceiling is reached.", ApiErrorCode("LIMIT_REACHED"))))

        assertEquals("429 LIMIT_REACHED: A ceiling is reached. $CEILING_STEP", failure.message)
        assertEquals(
            "404 NOT_FOUND: Itinerary not found.",
            ApiException.of(404, listOf(ErrorEntry("Itinerary not found.", ApiErrorCode("NOT_FOUND")))).message,
        )
    }

    @Test
    fun itIsThrownAndCaughtAsAKotlinException() {
        val caught =
            assertFailsWith<MountayaException> {
                throw ConfigurationException("no credential")
            }

        assertIs<ConfigurationException>(caught)
        assertEquals("no credential", caught.message)
    }

    @Test
    fun anUnderlyingFailureRidesAlongAsTheCause() {
        val underlying = IllegalStateException("socket closed")

        assertSame(underlying, ConnectionException(cause = underlying).cause)
        assertSame(underlying, ApiException.of(503, cause = underlying).cause)
        assertSame(underlying, DataException(emptyList(), cause = underlying).cause)
    }

    @Test
    fun aFailurePrintsTheContractsNameForItsClassThenItsMessage() {
        val ceiling = RateLimitException(listOf(ErrorEntry("Seats used up.", ApiErrorCode("LIMIT_REACHED"))))

        assertEquals("RateLimitError: 429 LIMIT_REACHED: Seats used up. $CEILING_STEP", ceiling.toString())
        assertEquals("ConfigurationError: no credential", ConfigurationException("no credential").toString())
    }

    private companion object {
        /** What a ceiling's message ends on. */
        const val CEILING_STEP =
            "No wait lifts a ceiling, so this call is not repeated: free a place in what the error's metadata names, " +
                "or change the organization's agreement with Mountaya (https://mountaya.com/en/contact/) where it " +
                "sets that ceiling."
    }
}
