package com.mountaya.client

import kotlin.time.Duration

/**
 * RateLimitPolicy is one limit an answer was counted against: its quota and window from
 * `RateLimit-Policy`, and what is left of it and when it resets from `RateLimit`.
 *
 * A name is the API's to add, so one this build has never seen is read like the others:
 * `minute`, `month`, `address`, `member`, `studio-embedded` and `share` today.
 */
public class RateLimitPolicy(
    /** The policy's name, such as `minute`. */
    public val name: String,
    /** How much the window allows. */
    public val quota: Long,
    /** How long the window lasts, or null where the answer states none. */
    public val window: Duration? = null,
    /** What is left of the quota, or null where the answer states none. */
    public val remaining: Long? = null,
    /** How long until the window resets, or null where the answer states none. */
    public val reset: Duration? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is RateLimitPolicy && other.name == name && other.quota == quota && other.window == window &&
            other.remaining == remaining && other.reset == reset

    override fun hashCode(): Int =
        listOf(quota, window, remaining, reset).fold(name.hashCode()) { hash, part -> hash * 31 + part.hashCode() }

    override fun toString(): String =
        "RateLimitPolicy(name=$name, quota=$quota, window=$window, remaining=$remaining, reset=$reset)"
}
