package com.mountaya.conformance

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.itineraryRoute
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/**
 * `geo.camera.itinerary-route`: the route a fly-through follows over the case's `segments`, or over
 * the one its `segmentId` names. Each segment is built as the generated `RestSchema.ItinerarySegment`
 * the API answers, its `geojson` the case's own, a bare LineString or a GeometryCollection as the wire
 * writes it, and every other member it requires a placeholder the rule never reads. The route answers
 * its `line`, and its `elevations` and `jumps` only where it states them, or null where there is none.
 */
object GeoCameraItineraryRouteRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val segments =
            input.items("segments").map { item ->
                val segment = item as? FixtureValue.Object ?: throw AssertionError("a segment is not an object")
                segment(
                    id = segment.text("id"),
                    position = segment.number("position").toInt(),
                    geometry =
                        fixtureGeometry(
                            segment["geojson"] ?: throw AssertionError("a segment has no `geojson`"),
                        ),
                )
            }
        val route =
            itineraryRoute(segments, input.textOrNull("segmentId"))
                ?: return ConformanceOutcome.Value(FixtureValue.Null)
        val stated = mutableListOf<Pair<String, FixtureValue>>("line" to FixtureValue.Array(route.line.map(::numbers)))
        if (route.elevations.isNotEmpty()) stated += "elevations" to numbers(route.elevations)
        if (route.jumps.isNotEmpty()) stated += "jumps" to numbers(route.jumps.map { it.toDouble() })
        return ConformanceOutcome.Value(FixtureValue.Object(stated))
    }

    private fun numbers(values: List<Double>) = FixtureValue.Array(values.map { FixtureValue.Number(it) })

    private fun segment(
        id: String,
        position: Int,
        geometry: GeoJsonGeometry,
    ): RestSchema.ItinerarySegment =
        RestSchema.ItinerarySegment(
            altitude_max = null,
            altitude_min = null,
            ascent = null,
            color = null,
            created_at = Instant.fromEpochMilliseconds(0),
            created_by = RestSchema.RelationToUser(id = "user"),
            descent = null,
            description = null,
            distance = null,
            duration = null,
            geojson = geometry,
            gradient_max = null,
            gradient_min = null,
            group_id = null,
            id = id,
            itinerary_id = "itinerary",
            mode = RestSchema.SegmentMode.Linestring,
            name = id,
            position = position,
            revision = 1,
            road_access_info = null,
            slope_info = null,
            speed = null,
            speed_info = null,
            start_at = null,
            steps = null,
            surface_info = null,
            trail_difficulty_info = null,
            way_type_info = null,
        )
}
