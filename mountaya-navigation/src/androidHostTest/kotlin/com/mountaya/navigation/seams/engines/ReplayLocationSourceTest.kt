package com.mountaya.navigation

import com.mountaya.client.InternalMountayaApi
import com.mountaya.geo.FollowedRoute
import com.mountaya.geo.MeasureProbe
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.followedRouteOf
import com.mountaya.geo.haversineMeters
import com.mountaya.geo.routeLengthMeters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

/**
 * The measurements a replay's walk of each line may cost, whatever the number of its ticks: one, the one its route's
 * following made. A measurement is a haversine per vertex of the whole line, so one per tick would pay for the route's
 * length again on the path every tick takes (`replay-measure` in the contract's `sdk/hot-paths.yaml`).
 */
private const val REPLAY_MEASURE_BUDGET = 1

/**
 * The replay walks on its scope's time, a test's virtual time here, and reads the time walked from the
 * scheduler's clock, so a walk of minutes takes none.
 */
@OptIn(ExperimentalCoroutinesApi::class, InternalMountayaApi::class)
class ReplayLocationSourceTest {
    /** 2 km east along the equator, as a route followed. */
    private val eastward = listOf(listOf(0.0, 0.0), listOf(0.018, 0.0))

    private fun followed(line: List<List<Double>>): FollowedRoute =
        assertNotNull(followedRouteOf(NavigationRoute(line)), "the route is one a navigation follows")

    @Test
    fun aRouteIsWalkedFromItsStartAtTheSpeedGivenOnePositionPerInterval() =
        runTest {
            val source =
                ReplayLocationSource(backgroundScope, metersPerSecond = 500.0, timeSource = testScheduler.timeSource)
            val stated = mutableListOf<List<Double>>()

            source.start { stated += it }
            source.follow(followed(eastward))
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()
            source.stop()

            assertEquals(3, stated.size)
            assertEquals(listOf(0.0, 0.0), stated[0])
            assertTrue(abs(haversineMeters(listOf(0.0, 0.0), stated[1]) - 500.0) < 1e-6)
            assertTrue(abs(haversineMeters(listOf(0.0, 0.0), stated[2]) - 1_000.0) < 1e-6)
            assertTrue(routeLengthMeters(eastward) > 1_000.0)
        }

    @Test
    fun aWalkEndsOnTheRoutesLastPositionStatedOnce() =
        runTest {
            val source =
                ReplayLocationSource(
                    backgroundScope,
                    metersPerSecond = 900.0,
                    interval = 500.milliseconds,
                    timeSource = testScheduler.timeSource,
                )
            val stated = mutableListOf<List<Double>>()

            source.start { stated += it }
            source.follow(followed(eastward))
            advanceTimeBy(60.seconds)
            source.stop()

            // 2 km at 450 m a step is five steps, the last one short, and the start before them.
            assertEquals(6, stated.size)
            assertEquals(listOf(0.018, 0.0), stated.last())
            assertEquals(6, stated.map { it[0] }.toSet().size, "no position is stated twice")
        }

    @Test
    fun aNewRouteIsWalkedFromItsOwnStartAndNoneEndsTheWalk() =
        runTest {
            val source =
                ReplayLocationSource(backgroundScope, metersPerSecond = 10.0, timeSource = testScheduler.timeSource)
            val stated = mutableListOf<List<Double>>()
            source.start { stated += it }

            source.follow(followed(eastward))
            runCurrent()
            assertEquals(listOf(0.0, 0.0), stated.last())
            advanceTimeBy(3.seconds)
            source.follow(followed(listOf(listOf(1.0, 1.0), listOf(1.01, 1.0))))
            runCurrent()
            assertEquals(listOf(1.0, 1.0), stated.last())

            source.follow(null)
            val count = stated.size
            advanceTimeBy(60.seconds)
            assertEquals(count, stated.size)
            source.stop()
        }

    @Test
    fun aSpeedThatIsNoNumberAboveZeroStandsAtTheRoutesStart() =
        runTest {
            for (speed in listOf(0.0, -3.0, Double.NaN, Double.POSITIVE_INFINITY)) {
                val source =
                    ReplayLocationSource(
                        backgroundScope,
                        metersPerSecond = speed,
                        timeSource = testScheduler.timeSource,
                    )
                val stated = mutableListOf<List<Double>>()
                source.start { stated += it }
                source.follow(followed(eastward))
                advanceTimeBy(10.seconds)
                source.stop()
                assertEquals(listOf(listOf(0.0, 0.0)), stated, "at $speed m/s")
            }
        }

    @Test
    fun nothingIsStatedBeforeAStartOrAfterAStop() =
        runTest {
            val source =
                ReplayLocationSource(backgroundScope, metersPerSecond = 10.0, timeSource = testScheduler.timeSource)
            val stated = mutableListOf<List<Double>>()
            source.follow(followed(eastward))
            advanceTimeBy(5.seconds)
            assertTrue(stated.isEmpty())

            source.start { stated += it }
            assertEquals(listOf(listOf(0.0, 0.0)), stated, "the walk states its start at once, as it starts")
            advanceTimeBy(3.seconds)
            runCurrent()
            source.stop()
            val count = stated.size
            advanceTimeBy(5.seconds)

            assertTrue(count > 1)
            assertEquals(count, stated.size)
        }

    /**
     * The start is stated before the replay returns, whatever its scope's dispatcher, so a navigation told of a
     * route reads the walk where it begins before anything it held from before.
     */
    @Test
    fun aRouteFollowedIsWalkedFromItsStartStatedBeforeTheReplayReturns() =
        runTest {
            val source =
                ReplayLocationSource(backgroundScope, metersPerSecond = 10.0, timeSource = testScheduler.timeSource)
            val stated = mutableListOf<List<Double>>()
            source.start { stated += it }

            source.follow(followed(eastward))
            assertEquals(listOf(listOf(0.0, 0.0)), stated)
            source.follow(followed(listOf(listOf(1.0, 1.0), listOf(1.01, 1.0))))
            assertEquals(listOf(listOf(0.0, 0.0), listOf(1.0, 1.0)), stated, "a new route, from its own start, at once")
            advanceTimeBy(2.seconds)
            runCurrent()
            source.stop()

            assertEquals(4, stated.size)
            assertTrue(stated.drop(1).all { it[0] >= 1.0 }, "the walk goes on along the route followed last")
        }

    /**
     * A navigation that answers the start with a stop, or with another route or none, as a stop's halt does,
     * ends the walk it answered: no wake of it states anything after.
     */
    @Test
    fun aStopOrAnotherRouteMadeAsTheStartIsStatedEndsThatWalk() =
        runTest {
            val stopping =
                ReplayLocationSource(backgroundScope, metersPerSecond = 10.0, timeSource = testScheduler.timeSource)
            val stated = mutableListOf<List<Double>>()
            stopping.start { position ->
                stated += position
                stopping.stop()
            }
            stopping.follow(followed(eastward))

            val leaving =
                ReplayLocationSource(backgroundScope, metersPerSecond = 10.0, timeSource = testScheduler.timeSource)
            leaving.start { position ->
                stated += position
                leaving.follow(null)
            }
            leaving.follow(followed(eastward))
            advanceTimeBy(10.seconds)
            runCurrent()

            assertEquals(listOf(listOf(0.0, 0.0), listOf(0.0, 0.0)), stated)
            leaving.stop()
        }

    @Test
    fun aPositionTheNavigationRefusesEndsTheWalk() =
        runTest {
            val source =
                ReplayLocationSource(backgroundScope, metersPerSecond = 10.0, timeSource = testScheduler.timeSource)
            var tries = 0
            source.start {
                tries += 1
                throw IllegalArgumentException("refused")
            }
            source.follow(followed(eastward))
            advanceTimeBy(10.seconds)
            source.stop()

            assertEquals(1, tries)
        }

    /**
     * Each position is placed by the time elapsed on the replay's clock, never by a stride added at each wake:
     * a clock that ran ahead of the waits, as a main thread too busy to wake the walk on time leaves it,
     * carries the traveller as far as the speed says, and a clock that stood still moves them nowhere.
     */
    @Test
    fun eachPositionIsPlacedByTheTimeElapsedOnTheClockRatherThanAStridePerWake() =
        runTest {
            val clock = TestTimeSource()
            val source = ReplayLocationSource(backgroundScope, metersPerSecond = 10.0, timeSource = clock)
            val stated = mutableListOf<List<Double>>()

            source.start { stated += it }
            source.follow(followed(eastward))
            runCurrent()
            clock += 2_500.milliseconds
            advanceTimeBy(1.seconds)
            runCurrent()
            clock += 500.milliseconds
            advanceTimeBy(1.seconds)
            runCurrent()
            advanceTimeBy(1.seconds)
            runCurrent()
            source.stop()

            val walked = stated.map { haversineMeters(listOf(0.0, 0.0), it) }
            assertEquals(4, walked.size)
            assertEquals(0.0, walked[0])
            assertEquals(25.0, walked[1], 1e-6)
            assertEquals(30.0, walked[2], 1e-6)
            assertEquals(30.0, walked[3], 1e-6, "a clock that stood still moves the traveller nowhere")
        }

    @Test
    fun anIntervalOfNoneStandsAtTheRoutesStart() =
        runTest {
            val source =
                ReplayLocationSource(
                    backgroundScope,
                    metersPerSecond = 10.0,
                    interval = Duration.ZERO,
                    timeSource = testScheduler.timeSource,
                )
            val stated = mutableListOf<List<Double>>()
            source.start { stated += it }
            source.follow(followed(eastward))
            advanceTimeBy(10.seconds)
            source.stop()

            assertEquals(listOf(listOf(0.0, 0.0)), stated)
        }

    /** [count] vertices from [start], each [step] degrees on from the one before: a straight line. */
    private fun straight(
        start: List<Double>,
        step: List<Double>,
        count: Int,
    ): List<List<Double>> = List(count) { listOf(start[0] + step[0] * it, start[1] + step[1] * it) }

    @Test
    fun hotPathBudgetReplayMeasureHalfAnHourOfTicksANewRouteAndItsWalkToItsEndMeasureEachLineOnce() =
        runTest {
            // 6.7 km east along the equator in 600 legs, walked at 2 m/s a tick a second for half an hour; then a new
            // route, 2.2 km north in 400 legs, walked from its start to its end.
            val out = straight(listOf(0.0, 0.0), listOf(0.0001, 0.0), 601)
            val home = straight(listOf(0.03, 0.001), listOf(0.0, 0.00005), 401)
            val source =
                ReplayLocationSource(backgroundScope, metersPerSecond = 2.0, timeSource = testScheduler.timeSource)
            val stated = mutableListOf<List<Double>>()
            source.start { stated += it }

            val measured =
                MeasureProbe.measuring {
                    source.follow(followed(out))
                    runCurrent()
                    advanceTimeBy(30.minutes)
                    runCurrent()
                    source.follow(followed(home))
                    runCurrent()
                    advanceTimeBy(1.hours)
                    runCurrent()
                }
            source.stop()

            // The work happened: the way out was walked eastward a tick a second, its start and 1,800 positions, and
            // the new route from its start to its last vertex, both read along the routes as their lines place them.
            val wayOut = stated.takeWhile { it[1] == 0.0 }
            assertEquals(1 + 1_800, wayOut.size)
            assertTrue(wayOut.zipWithNext().all { (before, after) -> before[0] < after[0] }, "$wayOut")
            assertEquals(3_600.0, haversineMeters(out.first(), wayOut.last()), 1e-6)
            assertEquals(home.first(), stated[wayOut.size])
            assertEquals(home.last(), stated.last())
            assertTrue(stated.size - wayOut.size > 1_000, "the way home was walked a tick at a time")
            // Each line walked was measured once at least and its budget at most, whatever the number of ticks.
            val outward = measured.count { it === out }
            val homeward = measured.count { it === home }
            assertTrue(
                outward in 1..REPLAY_MEASURE_BUDGET && homeward in 1..REPLAY_MEASURE_BUDGET,
                "the way out measured $outward times and the way home $homeward, of ${measured.size} measurements",
            )
            // And nothing else was: a copy of a line, measured on the path a tick takes, counts against the budget too.
            assertEquals(outward + homeward, measured.size, "every measurement: ${measured.size}")
        }
}
