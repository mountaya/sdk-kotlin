package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OperationCallTest {
    private val facts =
        OperationFacts(
            Surface.Rest,
            "getHut",
            "GET",
            "/v1/huts/{hut_id}",
            true,
            CredentialDeclaration(acceptsShareToken = true),
            30_000,
            true,
            true,
            false,
            null,
            SuccessKind.Envelope,
            listOf(SuccessResponse(200)),
            emptyList(),
        )

    private fun call(
        path: String,
        idempotencyKey: String? = null,
        ifMatch: String? = null,
    ) = OperationCall(
        facts,
        path,
        listOf("limit" to "10"),
        emptyList(),
        RequestBody.Empty,
        idempotencyKey = idempotencyKey,
        ifMatch = ifMatch,
        options = RequestOptions(),
    )

    @Test
    fun aCallIsWhatItSends() {
        assertEquals(call("/v1/huts/a"), call("/v1/huts/a"))
        assertNotEquals(call("/v1/huts/a"), call("/v1/huts/b"))
        assertEquals(
            "OperationCall(GET /v1/huts/a, query=[(limit, 10)], headers=[], body=Empty)",
            call("/v1/huts/a").toString(),
        )
    }

    @Test
    fun theKeyAndTheTagACallerSetAreWhatItSends() {
        assertEquals(call("/v1/huts/a", "key-1", "\"v1\""), call("/v1/huts/a", "key-1", "\"v1\""))
        assertNotEquals(call("/v1/huts/a", "key-1"), call("/v1/huts/a", "key-2"))
        assertNotEquals(call("/v1/huts/a", ifMatch = "*"), call("/v1/huts/a"))
        assertEquals(null, call("/v1/huts/a").idempotencyKey)
    }
}
