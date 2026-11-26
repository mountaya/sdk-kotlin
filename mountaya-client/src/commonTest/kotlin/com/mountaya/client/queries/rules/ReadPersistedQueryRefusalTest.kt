package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadPersistedQueryRefusalTest {
    private val digestOnly = PersistedQueryAttempt("abc", sendsDocument = false)
    private val withDocument = PersistedQueryAttempt("abc", sendsDocument = true)
    private val documentAlone = PersistedQueryAttempt(null, sendsDocument = true)
    private val noOperation = ErrorEntry("no operation provided", ApiErrorCode("GRAPHQL_VALIDATION_FAILED"))

    private fun read(
        vararg errors: ErrorEntry,
        attempt: PersistedQueryAttempt = digestOnly,
    ) = readPersistedQueryRefusal(errors.toList(), attempt)

    @Test
    fun aRequestForTheDocumentIsReadByItsMessageOrItsCode() {
        assertEquals(PersistedQueryRefusal.Register, read(ErrorEntry("PersistedQueryNotFound", null)))
        assertEquals(
            PersistedQueryRefusal.Register,
            read(ErrorEntry("asks for it", ApiErrorCode("PERSISTED_QUERY_NOT_FOUND"))),
        )
    }

    @Test
    fun aDeclinedProtocolIsReadByItsMessageItsCodeOrTheDataApisOwnEntry() {
        assertEquals(PersistedQueryRefusal.Unsupported, read(ErrorEntry("PersistedQueryNotSupported", null)))
        assertEquals(
            PersistedQueryRefusal.Unsupported,
            read(ErrorEntry("off", ApiErrorCode("PERSISTED_QUERY_NOT_SUPPORTED"))),
        )
        assertEquals(PersistedQueryRefusal.Unsupported, read(noOperation))
    }

    @Test
    fun aParseFailureAndTheSameCodeWithAnotherMessageAreOrdinaryFailures() {
        assertNull(read(ErrorEntry("unexpected token", ApiErrorCode("GRAPHQL_PARSE_FAILED"))))
        assertNull(read(ErrorEntry("Cannot query field.", ApiErrorCode("GRAPHQL_VALIDATION_FAILED"))))
        assertNull(read(ErrorEntry("no operation provided", null)))
        assertNull(read(ErrorEntry("No operation provided", ApiErrorCode("GRAPHQL_VALIDATION_FAILED"))))
    }

    /**
     * Only a digest-only attempt is asked for its document or declined by the Data API's own entry,
     * and an attempt that sent no digest reads no refusal at all.
     */
    @Test
    fun anAttemptThatSentItsDocumentIsNeverAskedForItAndOneWithNoDigestReadsNone() {
        assertNull(read(ErrorEntry("PersistedQueryNotFound", null), attempt = withDocument))
        assertNull(read(noOperation, attempt = withDocument))
        assertEquals(
            PersistedQueryRefusal.Unsupported,
            read(ErrorEntry("PersistedQueryNotSupported", null), attempt = withDocument),
        )
        assertNull(read(ErrorEntry("PersistedQueryNotSupported", null), attempt = documentAlone))
        assertNull(read(noOperation, attempt = documentAlone))
    }

    @Test
    fun anOrdinaryErrorIsNoRefusal() {
        assertNull(read(ErrorEntry("boom", ApiErrorCode("INTERNAL_SERVER_ERROR"))))
        assertNull(read())
    }

    @Test
    fun theWordsAreMatchedExactly() {
        assertNull(read(ErrorEntry("persistedquerynotfound", null)))
        assertNull(read(ErrorEntry("boom", ApiErrorCode("persisted_query_not_found"))))
        assertNull(read(ErrorEntry("PersistedQueryNotFound: try again", null)))
    }

    @Test
    fun aRefusalAmongOrdinaryErrorsIsStillRead() {
        assertEquals(
            PersistedQueryRefusal.Register,
            read(ErrorEntry("boom", null), ErrorEntry("PersistedQueryNotFound", null)),
        )
    }

    @Test
    fun aDeclinedProtocolIsReadBeforeARequestForTheDocument() {
        assertEquals(
            PersistedQueryRefusal.Unsupported,
            read(ErrorEntry("PersistedQueryNotFound", null), ErrorEntry("PersistedQueryNotSupported", null)),
        )
    }

    @Test
    fun theDataApisOwnDeclineBesideARequestForTheDocumentIsADecline() {
        assertEquals(PersistedQueryRefusal.Unsupported, read(ErrorEntry("PersistedQueryNotFound", null), noOperation))
    }
}
