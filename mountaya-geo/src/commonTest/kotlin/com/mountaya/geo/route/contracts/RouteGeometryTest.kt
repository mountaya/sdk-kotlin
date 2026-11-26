package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame

class RouteGeometryTest {
    private fun route() =
        RouteGeometry(
            line = listOf(listOf(6.0, 45.0), listOf(6.1, 45.1), listOf(6.2, 45.2)),
            elevations = listOf(1_000.0, 1_100.0, 1_200.0),
            jumps = listOf(2),
        )

    @Test
    fun aFreshCopyIsTheSameRoute() {
        val first = route()
        val second = route()

        assertNotSame(first, second)
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun anEditedRouteIsAnother() {
        val first = route()

        assertNotEquals(
            first,
            RouteGeometry(
                listOf(listOf(6.0, 45.0), listOf(6.11, 45.1), listOf(6.2, 45.2)),
                first.elevations,
                first.jumps,
            ),
        )
        assertNotEquals(
            first,
            RouteGeometry(first.line + listOf(listOf(6.3, 45.3)), first.elevations + 1_300.0, first.jumps),
        )
        assertNotEquals(first, RouteGeometry(first.line, listOf(1_000.0, 1_150.0, 1_200.0), first.jumps))
        assertNotEquals(first, RouteGeometry(first.line, jumps = first.jumps))
        assertNotEquals(first, RouteGeometry(first.line, first.elevations, listOf(1)))
    }

    @Test
    fun noListIsAnEmptyOne() {
        val line = listOf(listOf(6.0, 45.0), listOf(6.1, 45.1))

        assertEquals(RouteGeometry(line, jumps = emptyList()), RouteGeometry(line))
        assertEquals(RouteGeometry(line, elevations = emptyList()), RouteGeometry(line))
    }

    @Test
    fun eachValueIsComparedAsTheSameNumber() {
        val unplaced = listOf(listOf(6.0, 45.0), listOf(Double.NaN, 45.1))

        assertEquals(RouteGeometry(unplaced), RouteGeometry(unplaced.map { it.toList() }))
        assertEquals(RouteGeometry(unplaced).hashCode(), RouteGeometry(unplaced.map { it.toList() }).hashCode())
        assertNotEquals(RouteGeometry(listOf(listOf(0.0, 45.0))), RouteGeometry(listOf(listOf(-0.0, 45.0))))
        assertNotEquals(RouteGeometry(listOf(listOf(6.0, 45.0))), RouteGeometry(listOf(listOf(6.0, 45.0, 1_000.0))))
    }

    @Test
    fun theRouteIsWrittenLineFirst() {
        assertEquals(
            "RouteGeometry(line=[[6.0, 45.0], [6.1, 45.1], [6.2, 45.2]], elevations=[1000.0, 1100.0, 1200.0], jumps=[2])",
            route().toString(),
        )
    }
}
