package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.RestSchema
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The one walk an engine reads a position by answers what the two rules it stands for answer: the
 * progress `navigationProgressAt` read before the walk was shared, kept here as it was written, and the
 * distance `metersToRoute` measures to the route's own line, number for number, on routes that loop,
 * double back, cross the antimeridian and hold vertices that name no place.
 */
class RouteReadingAtTest {
    /** A sequence of numbers fixed by its seed, so every run walks the same routes. */
    private class Draws(
        seed: Long,
    ) {
        private var state = seed

        /** The next number in [0, 1). */
        fun next(): Double {
            state = state * 6_364_136_223_846_793_005L + 1_442_695_040_888_963_407L
            return (state ushr 11).toDouble() / (1L shl 53).toDouble()
        }

        fun between(
            low: Double,
            high: Double,
        ): Double = low + (high - low) * next()

        fun below(count: Int): Int = (next() * count).toInt().coerceAtMost(count - 1)
    }

    /** The progress as `navigationProgressAt` read it in two walks of a boxed line, before the walk was shared. */
    private fun progressAsItWasRead(
        route: FollowedRoute,
        position: List<Double>,
        previous: Double,
    ): NavigationProgress {
        val line = route.line
        val steps = route.steps
        val measured = cumulativeMeters(line)
        val total = measured.lastOrNull() ?: 0.0
        val behind = if (previous.isNaN()) 0.0 else previous.coerceIn(0.0, total)
        val point = listOf(position.getOrElse(0) { Double.NaN }, position.getOrElse(1) { Double.NaN })
        val metersPerDegreeLongitude = metersPerDegreeLongitudeAt(point[1])
        var stretchStart = -1
        var stretchEnd = -1
        var nearest = Double.POSITIVE_INFINITY
        var fraction = 0.0
        var from = -1
        for (index in line.indices) {
            if (!namesPlace(line[index])) continue
            val start = from
            from = index
            if (start < 0 || measured[index] < behind) continue
            footAround(point, metersPerDegreeLongitude, line[start], line[index]) { distance, at ->
                if (stretchStart < 0 || distance < nearest) {
                    stretchStart = start
                    stretchEnd = index
                    nearest = distance
                    fraction = at
                }
            }
        }
        var along = 0.0
        if (stretchStart >= 0) {
            val start = measured[stretchStart]
            val end = measured[stretchEnd]
            val foot = if (fraction == 1.0) end else start + fraction * (end - start)
            along = max(foot, behind)
        }
        val holding = steps.indexOfFirst { it.fromIndex <= stretchStart && stretchStart < it.toIndex }
        val step = if (holding < 0) steps.lastIndex else holding
        val current = steps.getOrNull(step)
        val stepStart = if (current == null) 0.0 else measured.getOrNull(current.fromIndex) ?: 0.0
        val stepEnd = if (current == null) total else measured.getOrNull(current.toIndex) ?: total
        val toManeuverDistance = max(0.0, stepEnd - along)
        val stepLength = stepEnd - stepStart
        val share = if (stepLength > 0.0) toManeuverDistance / stepLength else 0.0
        val toManeuverDuration = current?.duration?.let { it * share }
        var toArrivalDuration = toManeuverDuration
        for (later in step + 1 until steps.size) {
            val duration = steps[later].duration
            toArrivalDuration =
                if (toArrivalDuration == null || duration == null) null else toArrivalDuration + duration
        }
        return NavigationProgress(
            along = along,
            step = step,
            next = if (step + 1 < steps.size) step + 1 else null,
            toManeuver = NavigationRemainder(toManeuverDistance, toManeuverDuration),
            toArrival = NavigationRemainder(max(0.0, total - along), toArrivalDuration),
        )
    }

    /** Checks the walk against both rules for [route] at [position], [previous] meters along it. */
    private fun holds(
        route: FollowedRoute,
        position: List<Double>,
        previous: Double,
    ) {
        val reading = routeReadingAt(route, position, previous)
        val case = "at $position, $previous m along ${route.line}"
        assertEquals(progressAsItWasRead(route, position, previous), reading.progress, case)
        assertEquals(metersToRoute(position, listOf(GeoJsonLineString(route.line))), reading.meters, case)
        assertEquals(reading.progress, navigationProgressAt(route, position, previous), case)
        assertEquals(reading.progress, routeReadingAt(route, position, previous, measuring = false).progress, case)
    }

    /** A line of [count] vertices wandering from [start], some of which name no place where [gaps] is true. */
    private fun wandering(
        draws: Draws,
        start: List<Double>,
        count: Int,
        gaps: Boolean,
    ): List<List<Double>> {
        var longitude = start[0]
        var latitude = start[1]
        return List(count) { index ->
            if (index > 0) {
                longitude += draws.between(-0.004, 0.004)
                latitude += draws.between(-0.004, 0.004)
            }
            when {
                gaps && index > 0 && draws.next() < 0.1 -> listOf(Double.NaN, latitude)
                gaps && index > 0 && draws.next() < 0.05 -> listOf(longitude)
                else -> listOf(longitude, latitude)
            }
        }
    }

    /** Steps that tile a line of [count] vertices at random cuts, a goal on the last vertex. */
    private fun tiling(
        draws: Draws,
        count: Int,
    ): List<NavigationStep> {
        val steps = mutableListOf<NavigationStep>()
        var from = 0
        while (from < count - 1) {
            val to = minOf(count - 1, from + 1 + draws.below(4))
            val duration = if (draws.next() < 0.1) null else draws.between(10.0, 300.0)
            steps += NavigationStep(from, to, RestSchema.ManeuverType.TurnLeft, duration = duration)
            from = to
        }
        steps += NavigationStep(count - 1, count - 1, RestSchema.ManeuverType.Goal, duration = 0.0)
        return steps
    }

    private fun followed(
        line: List<List<Double>>,
        steps: List<NavigationStep> = emptyList(),
    ): FollowedRoute =
        followedRouteOf(NavigationRoute(line, steps, duration = 900.0)) ?: throw AssertionError("no route")

    @Test
    fun onWanderingRoutesTheWalkAnswersBothRules() {
        val draws = Draws(seed = 20_261_122L)
        repeat(200) { round ->
            val start = listOf(draws.between(-170.0, 170.0), draws.between(-70.0, 70.0))
            val line = wandering(draws, start, count = 2 + draws.below(30), gaps = round % 3 == 0)
            val route = if (round % 2 == 0) followed(line) else followed(line, tiling(draws, line.size))
            val length = route.measured.last()
            repeat(10) {
                val position = listOf(start[0] + draws.between(-0.05, 0.05), start[1] + draws.between(-0.05, 0.05))
                holds(route, position, draws.between(-50.0, length + 50.0))
            }
        }
    }

    /**
     * A loop passes its own start again, and a stretch far along it is as near as the first: the walk
     * reads every stretch for the distance, and only those ahead for the progress.
     */
    @Test
    fun onALoopAndAnOutAndBackTheWalkAnswersBothRules() {
        val loop =
            followed(
                listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.01, 0.01), listOf(0.0, 0.01), listOf(0.0, 0.0)),
            )
        val outAndBack = followed(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.0, 0.0)))
        val positions = listOf(listOf(0.0, 0.0), listOf(0.0001, 0.00005), listOf(0.005, 0.0002), listOf(0.02, 0.02))
        for (route in listOf(loop, outAndBack)) {
            val length = route.measured.last()
            for (previous in listOf(0.0, length / 4, length / 2, length * 0.9, length, Double.NaN, -1.0, length * 2)) {
                for (position in positions) holds(route, position, previous)
            }
        }
    }

    @Test
    fun acrossTheAntimeridianTheWalkAnswersBothRules() {
        val across = followed(listOf(listOf(179.99, 0.0), listOf(-179.99, 0.0), listOf(-179.98, 0.01)))
        val positions =
            listOf(listOf(179.995, 0.001), listOf(-179.995, -0.001), listOf(180.0, 0.0), listOf(-179.0, 0.5))
        for (position in positions) {
            holds(across, position, 0.0)
            holds(across, position, 1_500.0)
        }
    }

    /**
     * A position that names no place has no foot and is no distance from anything: every figure of its
     * progress is `NaN`, and it is infinitely far from the line, which every corridor refuses.
     */
    @Test
    fun aPositionThatNamesNoPlaceIsInfinitelyFarAndReadsNoFoot() {
        val route = followed(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0), listOf(0.02, 0.0)))
        val placeless = listOf(listOf(Double.NaN, 0.0), listOf(0.0), emptyList(), listOf(0.0, Double.POSITIVE_INFINITY))
        for (position in placeless) {
            holds(route, position, 0.0)
            assertEquals(Double.POSITIVE_INFINITY, routeReadingAt(route, position, 0.0).meters)
        }
    }

    @Test
    fun aRouteOfOneVertexHasNoStretchToBeNear() {
        val route = followed(listOf(listOf(6.0, 45.0)))

        holds(route, listOf(6.0, 45.0), 0.0)
        assertEquals(Double.POSITIVE_INFINITY, routeReadingAt(route, listOf(6.0, 45.0), 0.0).meters)
        assertEquals(0.0, routeReadingAt(route, listOf(6.0, 45.0), 0.0).progress.along)
    }

    @Test
    fun withoutMeasuringTheDistanceIsNone() {
        val route = followed(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0)))

        assertEquals(Double.POSITIVE_INFINITY, routeReadingAt(route, listOf(0.005, 0.0), 0.0, measuring = false).meters)
        assertEquals(0.0, routeReadingAt(route, listOf(0.005, 0.0), 0.0).meters)
    }
}
