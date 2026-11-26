package com.mountaya.client

import com.mountaya.client.generated.TileTemplates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UrlTemplateTest {
    private val itinerary =
        UrlTemplate(
            operationId = "getItineraryTile",
            path = "/v1/itineraries/{itinerary_id}/{z}/{x}/{y}",
            pathParameters = listOf("itinerary_id", "z", "x", "y"),
            queryParameters = listOf("timestamp"),
            mediaTypes = listOf("application/x-protobuf"),
        )

    @Test
    fun aValueFillsItsPlaceholderAsOneEncodedSegmentAndARenderersOwnAreLeftAsWritten() {
        assertEquals(
            "/v1/itineraries/itn%2F4e5f%20%C3%A9/{z}/{x}/{y}",
            itinerary.filled(mapOf("itinerary_id" to "itn/4e5f é")),
        )
        assertEquals(itinerary.path, itinerary.filled(emptyMap()))
        assertEquals("/v1/itineraries/a/3/{x}/{y}", itinerary.filled(mapOf("itinerary_id" to "a", "z" to "3")))
    }

    @Test
    fun aValueHoldingBracesWritesNoPlaceholderAnotherFillsWhateverOrderTheyComeIn() {
        val values = mapOf("itinerary_id" to "{z}", "z" to "3")

        assertEquals("/v1/itineraries/%7Bz%7D/3/{x}/{y}", itinerary.filled(values))
        assertEquals(itinerary.filled(values), itinerary.filled(values.entries.reversed().associate { it.toPair() }))
    }

    @Test
    fun twoAreEqualWhereTheyStateTheSame() {
        val same =
            UrlTemplate(
                "getItineraryTile",
                "/v1/itineraries/{itinerary_id}/{z}/{x}/{y}",
                listOf("itinerary_id", "z", "x", "y"),
                listOf("timestamp"),
                listOf("application/x-protobuf"),
            )

        assertEquals(itinerary, same)
        assertEquals(itinerary.hashCode(), same.hashCode())
        assertNotEquals(itinerary, UrlTemplate("getItineraryTile", itinerary.path))
        assertEquals("UrlTemplate(getItineraryTile: /v1/itineraries/{itinerary_id}/{z}/{x}/{y})", itinerary.toString())
    }

    @Test
    fun thePrintedRoutesAreTemplatesOfTheTileApiEachListedOnce() {
        assertEquals(
            TileTemplates.all
                .map { it.operationId }
                .toSet()
                .size,
            TileTemplates.all.size,
        )
        assertEquals(
            "/v1/itineraries/itn_4e5f/{z}/{x}/{y}",
            TileTemplates.getItineraryTile.filled(mapOf("itinerary_id" to "itn_4e5f")),
        )
    }
}
