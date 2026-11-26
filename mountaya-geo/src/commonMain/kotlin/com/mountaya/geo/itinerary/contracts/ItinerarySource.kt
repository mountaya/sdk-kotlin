package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ItinerarySource is how an itinerary is read by its id, and it is the seam that keeps the geo layer
 * from calling the network: the app's own call, `getItinerary` through its client, wired in one line,
 * answering the itinerary with its segments, its groups and its waypoints in one round trip, or
 * throwing what failed, which an element shows as its failure.
 *
 * `surface` is the surface the itinerary is read from: null for the one the credential reads by
 * default, the draft for a member of its organization, and `ReadSurface.Published` for the published
 * snapshot. A source is handed to an `ItineraryResourceCache`, one per client, which asks it in the
 * cache's own scope and shares its answer with every engine handed that cache; no read it is asked for
 * is cancelled while the scope stands.
 *
 * ```kotlin
 * val source = ItinerarySource { id, surface -> readItineraryWithYourClient(id, surface) }
 * ```
 *
 * `readItineraryWithYourClient` stands for the app's own call through its client.
 */
public fun interface ItinerarySource {
    /** Answers the itinerary [itineraryId] names on [surface], or throws what an element shows as its failure. */
    public suspend fun read(
        itineraryId: String,
        surface: RestSchema.ReadSurface?,
    ): RestSchema.ItineraryWithDetails
}
