package com.mountaya.client

import kotlin.time.Duration

/**
 * RefreshDelay is when a session token is next minted, counted from the moment its expiry was
 * read, and whether a timer is armed for it at all.
 *
 * A delay that is not armed belongs to a token that arrived already spent, or to a clock far
 * enough out of step to make it look so, and the delay is kept only to say how far under the
 * floor it fell.
 */
internal class RefreshDelay(
    /** How long until the token is minted again. It is negative for a token already past its expiry. */
    val delay: Duration,
    /** Whether a timer is armed for [delay]. */
    val isArmed: Boolean,
)
