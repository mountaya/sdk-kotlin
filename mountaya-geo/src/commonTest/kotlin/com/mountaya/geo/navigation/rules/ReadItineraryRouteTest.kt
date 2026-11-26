package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ReadItineraryRouteTest {
    private fun step(
        from: Int,
        to: Int,
        type: String,
        name: String = "-",
    ) = RestSchema.ItineraryDirectionsStep(
        distance = 100.0,
        duration = 60.0,
        exit_number = null,
        from_index = from,
        name = name,
        to_index = to,
        type = RestSchema.ManeuverType(type),
    )

    private fun segment(
        position: Int,
        geometry: GeoJsonGeometry?,
        steps: List<RestSchema.ItineraryDirectionsStep>? = null,
        duration: Double? = 120.0,
        id: String = "segment-$position",
    ) = RestSchema.ItinerarySegment(
        altitude_max = null,
        altitude_min = null,
        ascent = null,
        color = null,
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        descent = null,
        description = null,
        distance = null,
        duration = duration,
        geojson = geometry,
        gradient_max = null,
        gradient_min = null,
        group_id = null,
        id = id,
        itinerary_id = "itinerary",
        mode = RestSchema.SegmentMode.Directions,
        name = id,
        position = position,
        revision = 1,
        road_access_info = null,
        slope_info = null,
        speed = null,
        speed_info = null,
        start_at = null,
        steps = steps,
        surface_info = null,
        trail_difficulty_info = null,
        way_type_info = null,
    )

    private fun waypoint(
        position: Int,
        name: String?,
        point: List<Double>?,
    ) = RestSchema.ItineraryWaypoint(
        amenities = emptyList(),
        color = null,
        contact = null,
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        description = null,
        geojson = point?.let { GeoJsonPoint(it) },
        icon = null,
        id = "waypoint-$position",
        itinerary_id = "itinerary",
        media = null,
        mode = RestSchema.WaypointMode.Point,
        name = name,
        position = position,
        properties = null,
        revision = 1,
        timing = null,
    )

    private val first = listOf(listOf(6.0, 45.0, 1000.0), listOf(6.01, 45.0), listOf(6.02, 45.0, 1100.0))
    private val second = listOf(listOf(6.02, 45.0, 1105.0), listOf(6.03, 45.0), listOf(6.04, 45.0))
    private val ofFirst =
        listOf(step(0, 1, "depart", "Route des Chavants"), step(1, 2, "turn-left"), step(2, 2, "goal"))
    private val ofSecond = listOf(step(0, 1, "depart"), step(1, 2, "turn-right", "Route Blanche"), step(2, 2, "goal"))

    /**
     * Two lines sharing a vertex are joined on it, the earlier line's, its elevation included, in chain order whatever
     * order they are listed in; the arrival between them is a waypoint named by the place standing there, and a
     * departure naming no way after it is folded into it.
     */
    @Test
    fun segmentsAreJoinedOnTheVertexTheyShareTheirArrivalBetweenAWaypointWithTheDepartureFoldedIn() {
        val route =
            readItineraryRoute(
                listOf(
                    segment(1, GeoJsonLineString(second), ofSecond),
                    segment(
                        0,
                        GeoJsonGeometryCollection(listOf(GeoJsonLineString(first), GeoJsonMultiPoint(first))),
                        ofFirst,
                    ),
                ),
                listOf(waypoint(0, "Refuge de Bellachat", listOf(6.0201, 45.0))),
            )

        assertEquals(first + second.drop(1), route?.line)
        assertEquals(240.0, route?.duration)
        assertEquals(
            listOf(
                NavigationStep(0, 1, RestSchema.ManeuverType.Depart, "Route des Chavants", 100.0, 60.0),
                NavigationStep(1, 2, RestSchema.ManeuverType.TurnLeft, null, 100.0, 60.0),
                NavigationStep(2, 3, waypointManeuver, "Refuge de Bellachat", 200.0, 120.0),
                NavigationStep(3, 4, RestSchema.ManeuverType.TurnRight, "Route Blanche", 100.0, 60.0),
                NavigationStep(4, 4, RestSchema.ManeuverType.Goal, null, 100.0, 60.0),
            ),
            route?.steps,
        )
    }

    /**
     * A line starting away from the one before it keeps its first vertex, and its first step reaches back across the
     * gap.
     */
    @Test
    fun aLineStartingAwayFromTheOneBeforeItReachesBackAcrossTheGap() {
        val away = listOf(listOf(6.05, 45.0), listOf(6.06, 45.0))
        val route =
            readItineraryRoute(
                listOf(
                    segment(0, GeoJsonLineString(first), ofFirst),
                    segment(
                        1,
                        GeoJsonLineString(away),
                        listOf(step(0, 1, "depart", "Chemin du Lac"), step(1, 1, "goal")),
                    ),
                ),
                emptyList(),
            )

        assertEquals(first + away, route?.line)
        val departure =
            route?.steps?.first { it.maneuver == RestSchema.ManeuverType.Depart && it.name == "Chemin du Lac" }
        assertEquals(2, departure?.fromIndex)
        assertEquals(4, departure?.toIndex)
        assertEquals(null, route?.steps?.first { it.maneuver == waypointManeuver }?.name, "no place stands there")
    }

    /**
     * A segment holding no line is passed over with its duration, one with a line stating none makes the route state
     * none, and an itinerary holding no line is no route.
     */
    @Test
    fun theDurationIsNoneWhereASegmentWithALineStatesNoneAndNoLineIsNoRoute() {
        val passedOver = segment(2, GeoJsonMultiPoint(second), duration = null)

        assertEquals(
            120.0,
            readItineraryRoute(listOf(segment(0, GeoJsonLineString(first)), passedOver), emptyList())?.duration,
        )
        assertNull(
            readItineraryRoute(
                listOf(segment(0, GeoJsonLineString(first)), segment(1, GeoJsonLineString(second), duration = null)),
                emptyList(),
            )?.duration,
        )
        assertNull(readItineraryRoute(listOf(passedOver, segment(3, null)), emptyList()))
        assertNull(readItineraryRoute(emptyList(), emptyList()))
    }

    /**
     * Two segments at one position keep the order they are listed in, and of two places as near the first names the
     * joint.
     */
    @Test
    fun twoAtOnePositionKeepTheirListedOrderAndOfTwoPlacesAsNearTheFirstNamesTheJoint() {
        val route =
            readItineraryRoute(
                listOf(segment(0, GeoJsonLineString(second), id = "b"), segment(0, GeoJsonLineString(first), id = "a")),
                listOf(
                    waypoint(0, "-", listOf(6.04, 45.0)),
                    waypoint(1, "Lac Blanc", listOf(6.04, 45.0001)),
                    waypoint(2, "Lac Noir", listOf(6.04, 44.9999)),
                ),
            )

        assertEquals(second.first(), route?.line?.first())
        assertEquals("Lac Blanc", route?.steps?.first { it.maneuver == waypointManeuver }?.name)
    }
}
