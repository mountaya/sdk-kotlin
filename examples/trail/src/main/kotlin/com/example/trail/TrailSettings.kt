package com.example.trail

import android.content.res.Resources

/**
 * TrailSettings is everything Trail reads from its environment, the settings of the contract's `sdk/trail.yaml`, read
 * once into the one value the app's root takes, so a test hands in its own. A setting left unset is null.
 *
 * The build turns each setting, from the environment or from `.env` beside its build script, into a string resource,
 * which [read] reads at runtime: no setting is ever committed, and none is compiled into the code.
 */
data class TrailSettings(
    /**
     * `MOUNTAYA_PUBLISHABLE_KEY`: the organization's publishable key, with the `tiles`, `publishing` and `data` scopes.
     * Without it the app shows its notice in place of the map and the search.
     */
    val publishableKey: String? = null,
    /** `MOUNTAYA_ITINERARY_ID`: a published itinerary the key reads, shown and followed in place of the demo. */
    val itineraryId: String? = null,
    /** `MOUNTAYA_SESSION_URL`: where the client asks for a session token. Without it search stays disabled. */
    val sessionUrl: String? = null,
    /** How fast Replay walks the route, in meters a second: a brisk walk's, and faster where a test hands its own. */
    val replayMetersPerSecond: Double = 10.0,
) {
    /** The reading of the settings. */
    companion object {
        /** Reads the settings the build wrote into [resources], a blank one read as unset. */
        fun read(resources: Resources): TrailSettings =
            TrailSettings(
                publishableKey = resources.getString(R.string.mountaya_publishable_key).ifBlank { null },
                itineraryId = resources.getString(R.string.mountaya_itinerary_id).ifBlank { null },
                sessionUrl = resources.getString(R.string.mountaya_session_url).ifBlank { null },
            )
    }
}
