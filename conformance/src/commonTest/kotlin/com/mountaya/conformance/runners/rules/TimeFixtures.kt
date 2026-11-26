package com.mountaya.conformance

import com.mountaya.geo.TimeSliderWindow
import com.mountaya.geo.timeSliderWindow
import kotlin.time.Instant

/**
 * TimeFixtures reads what the time fixtures write, a pin and a clock's reading, into the values Kotlin's types hold, and
 * writes an instant back as they do, in epoch milliseconds.
 *
 * Kotlin's `Instant` holds no instant that names none and Kotlin's `Clock` answers an instant on every read, so a pin
 * that names no instant, `tomorrow` or the empty string, is read as no pin, and a clock that reads no instant, NaN, as no
 * reading, each refused by the type (SPEC.md section 9).
 */
internal object TimeFixtures {
    /**
     * The reading a clock would have to answer for its track to end past the range every SDK holds a track to,
     * 8.64e15 milliseconds from the epoch: the rule lays out the track of no instant around it.
     */
    private const val READING_PAST_THE_RANGE: Long = 8_640_000_000_000_000

    /** The instant [written] names, an RFC 3339 instant with its offset, or null where it is null or names none. */
    fun instant(written: String?): Instant? =
        written?.let {
            try {
                Instant.parse(it)
            } catch (namesNone: IllegalArgumentException) {
                null
            }
        }

    /** The clock's reading [written] states in epoch milliseconds, or null for NaN, a clock that reads no instant. */
    fun reading(written: FixtureValue?): Instant? {
        val milliseconds = (written as? FixtureValue.Number)?.value ?: throw AssertionError("`nowMs` is no number")
        if (milliseconds.isNaN()) return null
        if (milliseconds != kotlin.math.floor(milliseconds)) throw AssertionError("`nowMs` is no whole millisecond")
        return Instant.fromEpochMilliseconds(milliseconds.toLong())
    }

    /**
     * The track a slider lays out around [reading], through the rule; for no reading, the track of no instant, which a
     * clock that reads no instant lays out, as the rule lays it out at a reading whose track ends past the range.
     */
    fun window(reading: Instant?): TimeSliderWindow =
        timeSliderWindow(reading ?: Instant.fromEpochMilliseconds(READING_PAST_THE_RANGE))

    /** [instant] as the fixtures write one, in epoch milliseconds, or null. */
    fun written(instant: Instant?): FixtureValue =
        instant?.let { FixtureValue.Number(it.toEpochMilliseconds().toDouble()) } ?: FixtureValue.Null
}
