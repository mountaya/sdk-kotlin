package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DataCallTest {
    private val facts =
        OperationFacts(
            Surface.Data,
            "route",
            "POST",
            "/graphql",
            true,
            CredentialDeclaration(requiresSession = true),
            30_000,
            false,
            false,
            false,
            null,
            SuccessKind.Graphql,
            emptyList(),
            emptyList(),
        )
    private val operation = DataOperation("route", "RouteInput!", FieldSelection { field("distance") })

    private fun call(input: String) = DataCall(facts, operation, operation.document, input, RequestOptions())

    @Test
    fun aDataCallIsWhatItSends() {
        val input = """{"speed":4.5}"""

        assertEquals(call(input), call(input))
        assertNotEquals(call(input), call("{}"))
        assertEquals("DataCall(route, input={\"speed\":4.5})", call(input).toString())
    }
}
