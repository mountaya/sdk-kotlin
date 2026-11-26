package com.mountaya.geo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationRouteSourceTest {
    @Test
    fun aSourceIsTheHostsSuspendingCallHandedTheRequestAsTheHostMadeIt() =
        runTest {
            val asked = mutableListOf<List<List<Double>>>()
            val source =
                NavigationRouteSource<List<List<Double>>> { waypoints ->
                    yield()
                    asked += waypoints
                    NavigationRoute(line = waypoints)
                }
            val engine = RouteFollowingEngine(backgroundScope, source)
            val waypoints = listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682))

            engine.start(waypoints)
            runCurrent()

            assertEquals(listOf(waypoints), asked)
            assertEquals(NavigationStatus.Navigating, engine.state.value.status)
            assertEquals(
                waypoints,
                engine.state.value.route
                    ?.line,
            )
            engine.close()
        }
}
