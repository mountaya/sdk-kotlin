package com.mountaya.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * RefreshSchedule is the contract's rule for when a session token is minted again: before it
 * expires, never after a request has failed on it, since the request that discovers an expiry is
 * the one the user is waiting on.
 *
 * The schedule reads the expiry the mint returns rather than assuming a full lifetime, because a
 * token minted moments earlier for the same key may come back with less of its life left.
 */
internal object RefreshSchedule {
    /** The most a token's remaining life is shortened by, and the wait for a token whose expiry did not parse. */
    val margin: Duration = 60.seconds

    /** The shortest delay a timer is armed for. A token whose delay falls under it is spent. */
    val floor: Duration = 5.seconds

    /** The longest the schedule goes without reading the wall clock again. */
    val longestHop: Duration = 10.minutes

    /**
     * Returns when a freshly minted token is minted again, given its remaining [lifetime], or null
     * where its expiry did not parse.
     *
     * The margin is proportional and capped: the lifetime less the smaller of [margin] and half the
     * lifetime, so a short-lived token still gets headroom rather than a delay of zero. A delay
     * under [floor] is not armed, and a lifetime of zero or less is that case. The caller then asks
     * [afterSpentTokens] instead. A lifetime that did not parse is not a lifetime of zero: the
     * token is usable and only its deadline is unknown, so it takes the flat [margin].
     */
    fun forLifetime(lifetime: Duration?): RefreshDelay {
        if (lifetime == null) return RefreshDelay(margin, isArmed = true)
        val delay = lifetime - minOf(margin, lifetime / 2)
        return RefreshDelay(delay, isArmed = delay >= floor)
    }

    /**
     * Returns when to mint again after [consecutive] tokens in a row arrived already spent,
     * counted from 1.
     *
     * Each spent token waits twice as long as the one before it, from [floor] up to [margin], so a
     * skewed clock costs the application's mint endpoint a call a minute rather than one every few
     * seconds for as long as the skew lasts.
     */
    fun afterSpentTokens(consecutive: Int): RefreshDelay {
        require(consecutive >= 1) { "A count of spent tokens starts at 1, and this one is $consecutive." }
        val doublings = consecutive - 1
        // Thirty doublings of the floor are far past the margin, and the shift stays an Int.
        val wait = if (doublings >= 30) margin else minOf(margin, floor * (1 shl doublings))
        return RefreshDelay(wait, isArmed = true)
    }

    /**
     * Returns how long to wait before reading the wall clock again on the way to [deadline], given
     * that it now reads [now]; zero means the deadline has passed and the token is minted now.
     *
     * The deadline is an instant rather than a duration, and it is walked down in hops of at most
     * [longestHop], each re-reading the clock, so a device that slept through its timer mints on
     * waking rather than on the schedule it slept through. A hop is not a mint: the cap bounds how
     * long the clock goes unread, never how often the mint endpoint is called.
     */
    fun nextHop(
        deadline: Instant,
        now: Instant,
    ): Duration = (deadline - now).coerceIn(Duration.ZERO, longestHop)
}
