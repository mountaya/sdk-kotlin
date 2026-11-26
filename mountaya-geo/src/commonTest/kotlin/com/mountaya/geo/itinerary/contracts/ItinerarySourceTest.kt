package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ItinerarySourceTest {
    /** A source runs wherever the app's call runs; here it hops to another dispatcher, as a client's does. */
    @Test
    fun aSourceIsTheAppsSuspendingCallHandedTheIdAndTheSurfaceTheEngineAsksAbout() =
        runTest {
            val asked = mutableListOf<String>()
            val source =
                ItinerarySource { itineraryId, surface ->
                    withContext(Dispatchers.Default) {
                        restItinerary(
                            itineraryId,
                            segments =
                                listOf(
                                    restSegment("s1", geojson = lineString(listOf(6.0, 45.0), listOf(6.01, 45.0))),
                                ),
                        )
                    }.also { asked += "$itineraryId:${surface?.value ?: "draft"}" }
                }
            val engine =
                ItineraryResourceEngine(
                    ItineraryResourceCache(backgroundScope, source),
                    itineraryId = "itin_abc",
                    surface = RestSchema.ReadSurface.Published,
                )

            val shown = engine.state.first { it is ItineraryResource.Resolved }

            assertEquals(listOf("itin_abc:published"), asked)
            assertEquals(listOf("s1"), assertIs<ItineraryResource.Resolved>(shown).itinerary.legs.map { it.id })
            engine.close()
        }

    @Test
    fun whatASourceThrowsIsTheElementsFailure() =
        runTest {
            class Offline : Exception()
            val engine =
                ItineraryResourceEngine(
                    ItineraryResourceCache(backgroundScope) { _, _ -> throw Offline() },
                    itineraryId = "itin_abc",
                )

            val failed = engine.state.first { it is ItineraryResource.Failed }

            assertIs<Offline>(assertIs<ItineraryResource.Failed>(failed).cause)
            engine.close()
        }
}
