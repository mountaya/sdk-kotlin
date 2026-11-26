package com.mountaya.navigation

import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.RouteFollowingEngine
import com.mountaya.geo.followedRouteOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class LocationSourceTest {
    /** A source that reads a platform, and so writes no `follow` of its own. */
    private class Platform : LocationSource {
        var deliver: ((List<Double>) -> Unit)? = null

        override fun start(deliver: (position: List<Double>) -> Unit) {
            this.deliver = deliver
        }

        override fun stop() {
            deliver = null
        }
    }

    @Test
    fun aSourceThatReadsThePlatformIsToldOfARouteAndDoesNothingWithIt() {
        val source = Platform()
        val route =
            assertNotNull(
                followedRouteOf(NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0)))),
                "the route is one a navigation follows",
            )
        val stated = mutableListOf<List<Double>>()

        source.start { stated += it }
        source.follow(route)
        source.follow(null)
        source.deliver?.invoke(listOf(0.005, 0.0))

        assertEquals(listOf(listOf(0.005, 0.0)), stated)
    }

    @Test
    fun aPositionTheEngineRefusesIsThrownBackToTheSourceThatStatedIt() =
        runTest {
            val source = Platform()
            val engine = RouteFollowingEngine<String>(backgroundScope) { NavigationRoute(listOf(listOf(0.0, 0.0))) }
            source.start { engine.setPosition(it) }

            assertFailsWith<IllegalArgumentException> { source.deliver?.invoke(listOf(Double.NaN, 0.0)) }
            engine.close()
        }
}
