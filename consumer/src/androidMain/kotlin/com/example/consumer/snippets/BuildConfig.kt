package com.example.consumer.snippets

/**
 * Stands for an Android app's own `BuildConfig`, the one the documentation's Kotlin fences read their settings from
 * without declaring: an app's build writes each field from its environment or its `.env`, never committed, with
 * `buildConfigField`, as `mise run snippets` compiles those fences beside it.
 */
internal object BuildConfig {
    /** The organization's publishable key. */
    const val MOUNTAYA_PUBLISHABLE_KEY: String = "pk_your_publishable_key"

    /** Where the app asks its own backend for a session token. */
    const val MOUNTAYA_SESSION_URL: String = "https://example.com/session"

    /** A published itinerary the key reads. */
    const val MOUNTAYA_ITINERARY_ID: String = "0b6d1a4e-6d3f-4f7a-9c2e-1f0a5b8c3d21"
}
