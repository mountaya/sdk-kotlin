package com.mountaya.navigation

import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.followedRouteOf
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InertLocationSourceTest {
    @Test
    fun anInertSourceStatesNoPositionWhateverItIsAsked() {
        val source = InertLocationSource()
        val stated = mutableListOf<List<Double>>()
        val route =
            assertNotNull(
                followedRouteOf(NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0)))),
                "the route is one a navigation follows",
            )

        source.start { stated += it }
        source.follow(route)
        source.stop()
        source.stop()

        assertTrue(stated.isEmpty())
    }
}
