package com.mountaya.conformance

import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.followedRouteOf
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.stepless-route`: the steps a route of the case's `line`, `duration` and `steps`
 * is followed by, as `followedRouteOf` answers them, or null for a route there is none to follow.
 */
object GeoNavigationSteplessRouteRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val route =
            NavigationRoute(
                line = input.items("line").map { fixturePosition(it, "line") },
                steps = input.items("steps").map(::fixtureNavigationStep),
                duration = input.numberOrNull("duration"),
            )
        val followed = followedRouteOf(route) ?: return ConformanceOutcome.Value(FixtureValue.Null)
        return ConformanceOutcome.Value(FixtureValue.Array(followed.steps.map(::navigationStepValue)))
    }
}
