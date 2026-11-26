package com.mountaya.client

import kotlin.time.Instant

/** The nanoseconds in a millisecond. */
private const val NANOSECONDS_PER_MILLISECOND = 1_000_000

/**
 * formatTimestamp writes [instant] as a request writes it: in UTC, to the millisecond,
 * `2026-03-14T09:26:53.000Z`.
 *
 * The digits past the millisecond are dropped, never rounded, so `.999999` is written `.999` and
 * never as the next second, and an instant before the epoch is floored like any other. Three
 * fraction digits are always written, so one instant is always one spelling.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, calls it from beside the core, and no
 * consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public fun formatTimestamp(instant: Instant): String {
    val whole = Instant.fromEpochSeconds(instant.epochSeconds).toString().removeSuffix("Z")
    val milliseconds = instant.nanosecondsOfSecond / NANOSECONDS_PER_MILLISECOND
    return "$whole.${milliseconds.toString().padStart(3, '0')}Z"
}

/**
 * Returns [instant] in UTC to the second, `2026-11-26T00:00:00Z`: the fraction is dropped, never
 * rounded, as it is past the millisecond in [formatTimestamp], and an instant before the epoch is
 * floored like any other.
 */
internal fun formatTimestampToTheSecond(instant: Instant): String =
    Instant.fromEpochSeconds(instant.epochSeconds).toString()
