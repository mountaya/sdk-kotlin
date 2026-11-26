package com.mountaya.navigation

import com.mountaya.geo.FollowedRoute
import com.mountaya.geo.positionAtMeters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

/**
 * ReplayLocationSource walks the route a navigation follows at a steady speed, as a traveller would:
 * a demo, a screen recording, or a test that drives an engine end to end with no device location.
 *
 * When a route is followed it states the line's start at once, as it is told of the route or started on
 * one, before it returns, so the navigation reads the walk where it begins before it reads anything held
 * from before, a walk's arrival on the same route among them; then, at every [interval], it states the
 * position [metersPerSecond] times the time elapsed on [timeSource] since the start, along the line as
 * `positionAtMeters` places it, and it ends on the line's last position. It reads each position off the
 * route's own measure, `positionAtMeters(route, meters)`, its length the last distance the route measured,
 * so a walk measures the line no more than the route's following did, a search over its distances at each
 * tick rather than a haversine per vertex of the whole route (`replay-measure`). Each position is placed by the
 * time elapsed rather than by a stride added at each wake, so a main thread too busy to wake the walk on
 * time delays a position, never the walk: the next one stands where the speed has carried the traveller
 * meanwhile, and a clock read back in time moves the traveller nowhere. A new route is walked from its
 * own start, a navigation that follows none stops the walk, and a speed that is no number above zero, a
 * line of no length, or an interval of none stands at the start. A position the navigation refuses ends
 * the walk.
 *
 * It walks in a coroutine of [scope], the caller's, whose dispatcher is the thread the engine is driven
 * from and whose time paces the walk, and reads the time walked from [timeSource], the device's monotonic
 * clock by default. A test that paces the walk on its own virtual time under `runTest` hands its
 * scheduler's, `testScheduler.timeSource`, so a walk of an hour takes none. It is the one thing of a
 * navigation that waits on time, and the engine reads no clock at all.
 *
 * @param scope Where the walk runs, and whose time paces it.
 * @param metersPerSecond How fast the route is walked.
 * @param interval How often a position is stated.
 * @param timeSource The clock the time walked is read from, the one [scope] waits on.
 */
public class ReplayLocationSource(
    private val scope: CoroutineScope,
    private val metersPerSecond: Double,
    private val interval: Duration = 1.seconds,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : LocationSource {
    private var deliver: ((List<Double>) -> Unit)? = null

    /** The route walked, which holds its line's measure, or null where the navigation follows none. */
    private var route: FollowedRoute? = null
    private var walk: Job? = null

    /**
     * Counts the walks begun and the stops, so a walk whose start the navigation answered with a stop or
     * another route, as it was stated, goes no further.
     */
    private var walks = 0

    /** Begins walking the route followed, from its start, and every route followed after it. */
    override fun start(deliver: (position: List<Double>) -> Unit) {
        if (this.deliver != null) return
        this.deliver = deliver
        walkLine()
    }

    /** Ends the walk: nothing more is stated until the next [start]. */
    override fun stop() {
        walks += 1
        deliver = null
        walk?.cancel()
        walk = null
    }

    /** Walks [route] from its start, or ends the walk where the navigation follows none. */
    override fun follow(route: FollowedRoute?) {
        this.route = route
        walkLine()
    }

    override fun toString(): String = "ReplayLocationSource($metersPerSecond m/s every $interval)"

    /**
     * Walks the line of the route held from its start, ending any walk before it: the start is stated at once,
     * and the rest in a coroutine of its own.
     */
    private fun walkLine() {
        walks += 1
        val walking = walks
        walk?.cancel()
        walk = null
        val deliver = deliver ?: return
        val route = route ?: return
        // The line's length is the last distance its measure holds, as `routeLengthMeters` reads it.
        val length = route.measured.lastOrNull() ?: 0.0
        // A speed that is no number above zero goes nowhere, nor does a line of no length or an interval of none.
        val moves = metersPerSecond > 0.0 && metersPerSecond.isFinite() && length > 0.0 && interval > Duration.ZERO
        val started = timeSource.markNow()
        try {
            deliver(positionAtMeters(route, 0.0))
        } catch (refused: IllegalArgumentException) {
            // A line whose positions name no place, which the engine refuses: the walk ends.
            return
        }
        // A stop, another route or none, which the navigation made as the start was stated, ended this walk.
        if (!moves || walking != walks) return
        walk =
            scope.launch {
                var walked = 0.0
                try {
                    while (walked < length) {
                        delay(interval)
                        // A wait may end just as the walk is stopped, which its thread saw first.
                        ensureActive()
                        val elapsed = started.elapsedNow().toDouble(DurationUnit.SECONDS)
                        walked = minOf(length, maxOf(walked, metersPerSecond * elapsed))
                        deliver(positionAtMeters(route, walked))
                    }
                } catch (refused: IllegalArgumentException) {
                    // A line whose positions name no place, which the engine refuses: the walk ends.
                }
            }
    }
}
