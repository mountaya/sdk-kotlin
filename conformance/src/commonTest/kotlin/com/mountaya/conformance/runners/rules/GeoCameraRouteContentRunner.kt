package com.mountaya.conformance

import com.mountaya.geo.RouteGeometry
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.route-content`: whether the case's routes `a` and `b`, each built afresh from the
 * fixture as its own object, are the same route. A list a route leaves out is built as no list,
 * the route's empty one.
 */
object GeoCameraRouteContentRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        return ConformanceOutcome.Value(FixtureValue.Bool(route(input, "a") == route(input, "b")))
    }

    private fun route(
        input: FixtureValue.Object,
        key: String,
    ): RouteGeometry {
        val route = input.objectOrNull(key) ?: throw AssertionError("`$key` is absent")
        return RouteGeometry(
            line = route.items("line").map { fixturePosition(it, "line") },
            elevations = route["elevations"]?.let { fixturePosition(it, "elevations") } ?: emptyList(),
            jumps = route["jumps"]?.let { fixturePosition(it, "jumps") }?.map(::vertexIndex) ?: emptyList(),
        )
    }
}

/** The vertex index [value] writes, failing with a sentence where it is no whole number. */
private fun vertexIndex(value: Double): Int {
    val index = value.toInt()
    if (index.toDouble() != value) throw AssertionError("the jump $value is not a vertex index")
    return index
}
