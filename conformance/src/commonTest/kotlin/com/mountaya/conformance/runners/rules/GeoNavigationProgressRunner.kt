package com.mountaya.conformance

import com.mountaya.geo.NavigationRemainder
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.followedRouteOf
import com.mountaya.geo.navigationProgressAt
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.progress`: where a traveller at the case's `position` stands on a route of its
 * `line` and `steps`, `previous` meters along it, and what is left. The case's steps are the ones the
 * route is followed by (`geo.navigation.stepless-route`), so the route is built through
 * `followedRouteOf`, which keeps steps that tile their line as they are.
 */
object GeoNavigationProgressRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val route =
            followedRouteOf(
                NavigationRoute(
                    line = input.items("line").map { fixturePosition(it, "line") },
                    steps = input.items("steps").map(::fixtureNavigationStep),
                ),
            ) ?: throw AssertionError("the case's line holds no vertex")
        val progress =
            navigationProgressAt(route, fixturePosition(input["position"], "position"), input.number("previous"))
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "along" to FixtureValue.Number(progress.along),
                    "next" to numberOrNull(progress.next?.toDouble()),
                    "step" to FixtureValue.Number(progress.step.toDouble()),
                    "toArrival" to remainderValue(progress.toArrival),
                    "toManeuver" to remainderValue(progress.toManeuver),
                ),
            ),
        )
    }

    private fun remainderValue(remainder: NavigationRemainder): FixtureValue =
        FixtureValue.Object(
            listOf(
                "distance" to FixtureValue.Number(remainder.distance),
                "duration" to numberOrNull(remainder.duration),
            ),
        )
}
