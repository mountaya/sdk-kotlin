package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigationProgressAtTest {
    /** East along the equator for two stretches, then north for two: a depart, a turn and the arrival. */
    private val line =
        listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.02, 0.0), listOf(0.02, 0.01), listOf(0.02, 0.02))
    private val measured = cumulativeMeters(line)

    private fun route(
        line: List<List<Double>>,
        steps: List<NavigationStep> = emptyList(),
        duration: Double? = null,
    ): FollowedRoute = followedRouteOf(NavigationRoute(line, steps, duration)) ?: throw AssertionError("no route")

    private fun turning(
        departs: Double? = 1_200.0,
        turns: Double? = 900.0,
        shift: Int = 0,
    ) = listOf(
        NavigationStep(0, 2 + shift, RestSchema.ManeuverType.Depart, "Chemin des Praz", 2_200.0, departs),
        NavigationStep(
            2 + shift,
            4 + shift,
            RestSchema.ManeuverType.TurnLeft,
            "Chemin du Lac",
            2_200.0,
            turns,
        ),
        NavigationStep(4 + shift, 4 + shift, RestSchema.ManeuverType.Goal, null, 0.0, 0.0),
    )

    @Test
    fun atTheStartTheWholeRouteIsLeft() {
        val progress = navigationProgressAt(route(line, turning()), listOf(0.0, 0.0), 0.0)

        assertEquals(0.0, progress.along)
        assertEquals(0, progress.step)
        assertEquals(1, progress.next)
        assertEquals(NavigationRemainder(measured[2], 1_200.0), progress.toManeuver)
        assertEquals(NavigationRemainder(measured[4], 2_100.0), progress.toArrival)
    }

    /** A step's own distance is never read: the remainders are lengths of the one line. */
    @Test
    fun theTimeLeftIsTheStepsOwnProratedByItsMeasuredLength() {
        val progress = navigationProgressAt(route(line, turning()), listOf(0.005, 0.0001), 0.0)

        assertEquals(555.9746332227937, progress.along, 1e-6)
        assertEquals(measured[2] - progress.along, progress.toManeuver.distance, 1e-9)
        assertEquals(900.0, progress.toManeuver.duration ?: Double.NaN, 1e-9)
        assertEquals(1_800.0, progress.toArrival.duration ?: Double.NaN, 1e-9)
        assertTrue(progress.toArrival.distance >= progress.toManeuver.distance)
    }

    /** The earlier stretch wins a tie, and its far end is exactly where the next stretch starts. */
    @Test
    fun onAManeuversVertexThatManeuverIsNextWithNoDistanceLeftToIt() {
        val progress = navigationProgressAt(route(line, turning()), listOf(0.02, 0.0), 0.0)

        assertEquals(measured[2], progress.along)
        assertEquals(0, progress.step)
        assertEquals(1, progress.next)
        assertEquals(NavigationRemainder(0.0, 0.0), progress.toManeuver)
    }

    @Test
    fun aNearerStretchAheadWinsAndTheStepHoldingItIsCurrent() {
        val progress = navigationProgressAt(route(line, turning()), listOf(0.0199, 0.005), 0.0)

        assertEquals(1, progress.step)
        assertEquals(2, progress.next)
        assertEquals(2_779.8731661139686, progress.along, 1e-6)
    }

    @Test
    fun progressNeverMovesBack() {
        val progress = navigationProgressAt(route(line, turning()), listOf(0.005, 0.0), 1_000.0)

        assertEquals(1_000.0, progress.along)
        assertEquals(0, progress.step)
    }

    /** A stretch that ends behind the last progress is never projected onto. */
    @Test
    fun theReturnLegOfAnOutAndBackIsNotMistakenForTheWayOut() {
        val outAndBack = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.0, 0.0))
        val there = cumulativeMeters(outAndBack)

        assertEquals(there[1] / 2, navigationProgressAt(route(outAndBack), listOf(0.005, 0.0), 0.0).along, 1e-6)
        assertEquals(
            there[1] + (there[2] - there[1]) / 2,
            navigationProgressAt(route(outAndBack), listOf(0.005, 0.0), there[1] + 1.0).along,
            1e-6,
        )
    }

    @Test
    fun aLoopStartsAtItsStartAndEndsAtItsEndOnceComeRound() {
        val loop = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.01, 0.01), listOf(0.0, 0.01), listOf(0.0, 0.0))
        val round = cumulativeMeters(loop)

        assertEquals(0.0, navigationProgressAt(route(loop), listOf(0.0, 0.0), 0.0).along)
        assertEquals(round[4], navigationProgressAt(route(loop), listOf(0.0, 0.0), 3_000.0).along)
        assertEquals(
            NavigationRemainder(0.0, 0.0),
            navigationProgressAt(route(loop, duration = 600.0), listOf(0.0, 0.0), 3_000.0).toArrival,
        )
    }

    /** The last progress is held within the route, and one that is no number reads as none at all. */
    @Test
    fun theLastProgressIsHeldWithinTheRoute() {
        val followed = route(line, turning())

        assertEquals(0.0, navigationProgressAt(followed, listOf(0.0, 0.0), Double.NaN).along)
        assertEquals(0.0, navigationProgressAt(followed, listOf(0.0, 0.0), -5.0).along)
        assertEquals(measured[4], navigationProgressAt(followed, listOf(0.0, 0.0), 1e9).along)
        assertEquals(measured[4], navigationProgressAt(followed, listOf(0.0, 0.0), Double.POSITIVE_INFINITY).along)
    }

    @Test
    fun aStepThatStatesNoDurationLeavesNoTime() {
        val untimed = navigationProgressAt(route(line, turning(departs = null)), listOf(0.005, 0.0), 0.0)
        val laterUntimed = navigationProgressAt(route(line, turning(turns = null)), listOf(0.005, 0.0), 0.0)

        assertNull(untimed.toManeuver.duration)
        assertNull(untimed.toArrival.duration)
        assertEquals(900.0, laterUntimed.toManeuver.duration ?: Double.NaN, 1e-9)
        assertNull(laterUntimed.toArrival.duration)
    }

    @Test
    fun aRouteOfOneVertexStandsAtItsEndInItsLastStep() {
        val progress = navigationProgressAt(route(listOf(listOf(0.0, 0.0)), duration = 0.0), listOf(0.001, 0.0), 0.0)

        assertEquals(0.0, progress.along)
        assertEquals(1, progress.step)
        assertNull(progress.next)
        assertEquals(NavigationRemainder(0.0, 0.0), progress.toArrival)
    }

    /**
     * As a corridor is measured, and as every walk of a line reads it: the stretch runs from the
     * vertex before to the vertex after.
     */
    @Test
    fun aVertexThatNamesNoPlaceIsBridged() {
        val bridged = listOf(line[0], line[1], listOf(Double.NaN, Double.NaN), line[2], line[3], line[4])
        val plain = navigationProgressAt(route(line, turning()), listOf(0.015, 0.0001), 0.0)
        val read = navigationProgressAt(route(bridged, turning(shift = 1)), listOf(0.015, 0.0001), 0.0)

        assertEquals(plain.along, read.along, 1e-9)
        assertEquals(plain.toArrival.distance, read.toArrival.distance, 1e-9)
        assertEquals(plain.toManeuver.duration ?: Double.NaN, read.toManeuver.duration ?: Double.NaN, 1e-9)
        assertEquals(0, read.step)
    }

    /**
     * No foot is measured from a place nobody named: every figure it measures is no number, in the
     * step holding the first stretch ahead of the last progress. An engine refuses such a position
     * before it reaches here.
     */
    @Test
    fun aPositionThatNamesNoPlaceReadsNoNumber() {
        val followed = route(line, turning())
        val nowhere = navigationProgressAt(followed, listOf(Double.NaN, 0.0), 0.0)

        assertTrue(nowhere.along.isNaN())
        assertTrue(nowhere.toArrival.distance.isNaN())
        assertTrue(nowhere.toManeuver.duration?.isNaN() ?: false)
        assertEquals(0, nowhere.step)
        assertEquals(1, navigationProgressAt(followed, listOf(Double.NaN, 0.0), 2_500.0).step)
        assertTrue(navigationProgressAt(followed, listOf(0.0), 0.0).along.isNaN())
    }
}
