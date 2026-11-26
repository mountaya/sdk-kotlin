package com.mountaya.client

import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.Instant

/**
 * ReconnectSchedule decides when a reader opens a stream again after an open without progress,
 * and when it gives the stream up.
 *
 * The wait is a uniform draw from zero to a ceiling that doubles per consecutive open without
 * progress, the full jitter `sdk/SPEC.md` section 2 has every retry take, for the reason given
 * there. Where that open was a 429 or a 503 naming its wait in `Retry-After`, the reopen waits that
 * instead, a wait of 0 seconds included, with section 2's draw of up to a second above it, since an
 * open sooner meets the same answer. An open that made progress, or that ended on a `reconnect`,
 * resets the count and is followed at once.
 */
internal object ReconnectSchedule {
    /** Count is what a reader counts of its opens since the last that made progress, or since the last `reconnect`. */
    class Count(
        /** Every open without progress, which the ceiling of the next wait doubles with. */
        val opens: Int = 0,
        /**
         * Those of them answered 503 on a stream whose rules hold such opens to time, which are not
         * among the reopens it stops after.
         */
        val unavailableOpens: Int = 0,
        /** When the first of those was answered, on the client's wall clock, or null before one. */
        val firstUnavailable: Instant? = null,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Count &&
                other.opens == opens &&
                other.unavailableOpens == unavailableOpens &&
                other.firstUnavailable == firstUnavailable

        override fun hashCode(): Int = (opens * 31 + unavailableOpens) * 31 + (firstUnavailable?.hashCode() ?: 0)

        override fun toString(): String =
            "Count(opens=$opens, unavailableOpens=$unavailableOpens, firstUnavailable=$firstUnavailable)"
    }

    /** Next is the count after one more open without progress, and the wait before the next open, or null where the reader stops. */
    class Next(
        val count: Count,
        val wait: Duration?,
    )

    /**
     * Counts one more open without progress, which failed with [failure], or closed where it is null,
     * and ended at [now] on the client's wall clock, and returns the count after it with the wait
     * before the next open, or with none where the reader stops instead.
     *
     * An open answered 503, on a stream whose rules hold such opens to time, stops the reader where it
     * comes `maxUnavailable` or more after the first of them; it is not counted among the reopens, and
     * an open that failed otherwise in between neither restarts that time nor counts toward it. Every
     * other open stops the reader where the opens without progress, but those answered 503, outnumber
     * the stream's reopens. Either way the ceiling doubles with every open. [draw] is a uniform draw
     * from zero to one, taken before every wait.
     */
    fun next(
        failure: MountayaException?,
        count: Count,
        now: Instant,
        rules: EventStreamRules,
        draw: Double,
    ): Next {
        val opens = count.opens + 1
        val maxUnavailable = rules.maxUnavailable
        val counted: Count
        if (maxUnavailable != null && (failure as? ApiException)?.status == 503) {
            val first = count.firstUnavailable ?: now
            counted = Count(opens, count.unavailableOpens + 1, first)
            if (now - first >= maxUnavailable) return Next(counted, null)
        } else {
            counted = Count(opens, count.unavailableOpens, count.firstUnavailable)
            val maxReopens = rules.maxReopens
            if (maxReopens != null && opens - count.unavailableOpens > maxReopens) return Next(counted, null)
        }
        val named = namedWait(failure)
        if (named != null) {
            return Next(counted, named + (draw * retryAfterJitter.toDouble(DurationUnit.MILLISECONDS)).milliseconds)
        }
        return Next(counted, drawnWait(opens, rules, draw))
    }

    /**
     * Returns the schedule's wait after [withoutProgress] opens in a row made none: a uniform draw
     * from zero to the ceiling, which starts at the stream's base, doubles per open and stops growing at
     * its most.
     */
    fun drawnWait(
        withoutProgress: Int,
        rules: EventStreamRules,
        draw: Double,
    ): Duration {
        val base = rules.baseWait.toDouble(DurationUnit.MILLISECONDS)
        val max = rules.maxWait.toDouble(DurationUnit.MILLISECONDS)
        // Doubling past the ceiling only ever reaches it, so the power needs no bound of its own.
        val grown = base * 2.0.pow(maxOf(withoutProgress, 1) - 1)
        return (draw * minOf(max, grown)).milliseconds
    }

    /**
     * Returns the wait a failed open's answer names: the whole seconds a 429's or a 503's
     * `Retry-After` states, 0 included, and null for any other failure and for one that names none.
     */
    fun namedWait(failure: MountayaException?): Duration? {
        val answered = failure as? ApiException ?: return null
        return if (answered.status == 429 || answered.status == 503) answered.retryAfter else null
    }
}
