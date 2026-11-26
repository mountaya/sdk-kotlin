package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class ItineraryResourceTest {
    private class Unreachable : Exception()

    @Test
    fun aResolvedResourceHoldsEveryProjectionOfItsItineraryAtOnce() {
        val itinerary =
            ResolvedItinerary(
                restItinerary(
                    segments = listOf(restSegment("s1", geojson = lineString(listOf(6.0, 45.0), listOf(6.01, 45.0)))),
                ),
            )
        val resource: ItineraryResource = ItineraryResource.Resolved(itinerary)

        val shown = assertIs<ItineraryResource.Resolved>(resource).itinerary
        assertEquals(itinerary, shown)
        assertEquals(listOf("s1"), shown.legs.map { it.id })
        assertEquals<ItineraryResource>(ItineraryResource.Resolved(itinerary), resource)
    }

    /** A failure holds what its source threw, which is no value to compare: two failures are two states. */
    @Test
    fun aFailedResourceHoldsWhatItsSourceThrewAndIsEqualToItselfAlone() {
        val cause = Unreachable()
        val resource = ItineraryResource.Failed(cause)

        assertIs<Unreachable>(resource.cause)
        assertEquals(resource, resource)
        assertNotEquals(ItineraryResource.Failed(cause), resource)
    }

    @Test
    fun anIdleAndAResolvingResourceHoldNothing() {
        for (resource in listOf(ItineraryResource.Idle, ItineraryResource.Resolving)) {
            assertEquals(resource, resource)
        }
        assertNotEquals<ItineraryResource>(ItineraryResource.Idle, ItineraryResource.Resolving)
    }
}
