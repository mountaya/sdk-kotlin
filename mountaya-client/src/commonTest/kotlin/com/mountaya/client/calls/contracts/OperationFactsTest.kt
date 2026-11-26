package com.mountaya.client

import com.mountaya.client.generated.OperationTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class OperationFactsTest {
    @Test
    fun factsNameTheOperationTheyDescribe() {
        val facts =
            OperationFacts(
                surface = Surface.Rest,
                operationId = "getItinerary",
                method = "GET",
                path = "/v1/itineraries/{itinerary_id}",
                idempotent = true,
                credentials = CredentialDeclaration(acceptsShareToken = true),
                timeoutMs = 30_000,
                declaresIdempotencyKey = false,
                isConditional = false,
                answersNotModified = true,
                pagination = null,
                success = SuccessKind.Envelope,
                successResponses = listOf(SuccessResponse(200, listOf("X-Request-Id"))),
                errorStatuses = listOf(401, 404),
            )

        assertEquals("OperationFacts(rest getItinerary: GET /v1/itineraries/{itinerary_id})", facts.toString())
        assertEquals(true, facts.credentials.acceptsShareToken)
        assertEquals(listOf(SuccessResponse(200, listOf("X-Request-Id"))), facts.successResponses)
    }

    @Test
    fun theBudgetIsTheOperationsOwnAndAnEventStreamDeclaresNone() {
        assertEquals(30.seconds, facts(30_000).timeout)
        assertNull(facts(null).timeout)
    }

    private fun facts(timeoutMs: Int?) =
        OperationFacts(
            Surface.Rest,
            "getSignals",
            "GET",
            "/v1/signals",
            true,
            CredentialDeclaration(),
            timeoutMs,
            false,
            false,
            false,
            null,
            SuccessKind.EventStream,
            listOf(SuccessResponse(200)),
            emptyList(),
        )

    @Test
    fun theFactsADeclarationStatesAreTheOnesTheTransportReadsItsPathTheCallers() {
        val declaration =
            OperationDeclaration(
                operationId = "getThing",
                method = "PATCH",
                surface = Surface.Tiles,
                idempotent = true,
                timeout = 55.seconds,
                declaresIdempotencyKey = true,
                isConditional = true,
            )
        val facts = OperationFacts(declaration, "/v1/things/th")

        assertEquals(Surface.Tiles, facts.surface)
        assertEquals("getThing", facts.operationId)
        assertEquals("PATCH", facts.method)
        assertEquals("/v1/things/th", facts.path)
        assertTrue(facts.idempotent)
        assertEquals(55.seconds, facts.timeout)
        assertTrue(facts.declaresIdempotencyKey)
        assertTrue(facts.isConditional)
        assertEquals(SuccessKind.Envelope, facts.success)
        assertNull(facts.pagination)
        assertEquals(emptyList(), facts.successResponses)
        assertEquals(SuccessKind.EventStream, OperationFacts(declaration, "/v1/x", SuccessKind.EventStream).success)
    }

    @Test
    fun aDataApiOperationACallerDeclaresIsSentAsEveryPrintedOneIsWaitingTheBudgetItStates() {
        val weather = DataOperation("weather", "WeatherInput!", FieldSelection { field("temperature") }, 20.seconds)
        val facts = OperationFacts(weather)
        val printed = OperationTable.Data.directions

        assertEquals(Surface.Data, facts.surface)
        assertEquals("weather", facts.operationId)
        assertEquals(printed.method, facts.method)
        assertEquals(printed.path, facts.path)
        assertEquals(printed.idempotent, facts.idempotent)
        assertEquals(printed.credentials, facts.credentials)
        assertEquals(SuccessKind.Graphql, facts.success)
        assertEquals(printed.successResponses, facts.successResponses)
        assertEquals(20.seconds, facts.timeout)
        assertEquals(20_000, facts.timeoutMs)
        assertNull(OperationFacts(DataOperation("weather", "WeatherInput!", weather.selection)).timeout)
    }
}
