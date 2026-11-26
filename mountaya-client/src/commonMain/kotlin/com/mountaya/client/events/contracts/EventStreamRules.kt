package com.mountaya.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** The header a reopen sends the position it resumes from in. */
internal const val LAST_EVENT_ID_HEADER = "Last-Event-ID"

/**
 * EventStreamRules is what one kind of stream does at its end, on a failure and between two opens,
 * as `sdk/SPEC.md` section 12 states it per stream.
 */
internal class EventStreamRules(
    /** The ceiling of the first wait after an open without progress, which doubles per consecutive one. */
    val baseWait: Duration,
    /** The ceiling no wait grows past. */
    val maxWait: Duration,
    /**
     * How many reopens in a row without progress a reader makes before it stops, or null where it
     * never stops on its own. An open answered 503 is not one of them where [maxUnavailable] bounds
     * such opens by time instead.
     */
    val maxReopens: Int?,
    /**
     * How long the opens answered 503 are held to, from the first of them since the last progress or
     * `reconnect`, past which the reader stops and reports that 503, or null where the reader never
     * stops on them.
     */
    val maxUnavailable: Duration?,
    /**
     * How many frames an open may deliver and still have made no progress: one on an agent run's
     * stream, which says one thing and hangs up on every try.
     */
    val framesBeforeProgress: Int,
    /** Whether a 5xx other than a 503 or a 504 ends the stream rather than reopening it. */
    val endsOnServerError: Boolean,
    /**
     * Whether the `presence.join` of the reader's own member is progress: false on an editor stream,
     * whose every open may carry the join sent as that member's first stream opens, and true on every
     * other stream.
     */
    val ownArrivalIsProgress: Boolean,
) {
    companion object {
        /**
         * An agent run's stream: it ends on `done`, its reader stops after six reopens in a row without
         * progress, since someone is waiting in front of the run, and its opens answered 503 are held to
         * two minutes instead, since a failover of the store behind the stream can outlast six reopens.
         */
        val agentRun: EventStreamRules =
            EventStreamRules(
                baseWait = 500.milliseconds,
                maxWait = 8.seconds,
                maxReopens = 6,
                maxUnavailable = 2.minutes,
                framesBeforeProgress = 1,
                endsOnServerError = true,
                ownArrivalIsProgress = true,
            )

        /**
         * A collaboration feed whose every `presence.join` is progress: an organization's list stream,
         * or a stream opened by its declaration. It has no end of its own, and its reader never stops on
         * its own, since nobody is waiting on one answer.
         */
        val collaborationFeed: EventStreamRules =
            EventStreamRules(
                baseWait = 500.milliseconds,
                maxWait = 15.seconds,
                maxReopens = null,
                maxUnavailable = null,
                framesBeforeProgress = 0,
                endsOnServerError = false,
                ownArrivalIsProgress = true,
            )

        /**
         * An itinerary's or a zone's editor stream: a collaboration feed whose reader's own member's
         * `presence.join` is no progress.
         */
        val collaborationEditor: EventStreamRules =
            EventStreamRules(
                baseWait = 500.milliseconds,
                maxWait = 15.seconds,
                maxReopens = null,
                maxUnavailable = null,
                framesBeforeProgress = 0,
                endsOnServerError = false,
                ownArrivalIsProgress = false,
            )

        /**
         * How long an open stream may deliver nothing, not an event and not a comment, before it is
         * given up: three missed heartbeats of 15 seconds.
         */
        val stallWindow: Duration = 45.seconds
    }
}
