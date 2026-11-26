package com.mountaya.geo

import kotlin.time.Instant

/** The seconds in a minute, the step the moment of an unpinned map moves by. */
private const val SECONDS_PER_MINUTE: Long = 60

/**
 * overlayInstant is the moment a map's time-dependent surfaces show (`geo.layers.overlay-instant`): [pinned], the
 * instant a reader pinned, as it is, whatever the clock reads, behind it or ahead of it; and with none pinned the start
 * of the minute [now] falls in, the clock's reading floored to a whole minute from the epoch, so every surface that
 * reads it shows one moment, and the moment moves when the minute does. A reading before the epoch falls in the minute
 * before it.
 *
 * It reads no clock: [now] is the reading of the clock the caller reads, a `kotlin.time.Clock`, which answers an
 * instant on every read, so there is always a moment, and the contract's clock that reads no instant is refused by the
 * type (SPEC.md section 9).
 */
public fun overlayInstant(
    pinned: Instant?,
    now: Instant,
): Instant = pinned ?: Instant.fromEpochSeconds(now.epochSeconds.floorDiv(SECONDS_PER_MINUTE) * SECONDS_PER_MINUTE)
