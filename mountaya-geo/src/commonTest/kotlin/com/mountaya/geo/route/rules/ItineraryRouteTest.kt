package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ItineraryRouteTest {
    /** A segment as the API answers one, holding [geometry] at [position] in the chain. */
    private fun segment(
        id: String,
        position: Int,
        geometry: GeoJsonGeometry?,
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

    private fun line(vararg positions: List<Double>) = GeoJsonLineString(positions.toList())

    @Test
    fun aLineIsTheRoute() {
        assertEquals(
            RouteGeometry(listOf(listOf(6.0, 45.0), listOf(6.5, 45.5), listOf(7.0, 46.0))),
            itineraryRoute(listOf(segment("a", 0, line(listOf(6.0, 45.0), listOf(6.5, 45.5), listOf(7.0, 46.0))))),
        )
    }

    @Test
    fun theElevationsAreStatedOnlyWhereEveryVertexCarriesOne() {
        val climbing = line(listOf(6.0, 45.0, 1_200.0), listOf(6.5, 45.5, 1_350.0), listOf(7.0, 46.0, 1_500.0))
        val gapped = line(listOf(6.0, 45.0, 1_200.0), listOf(6.5, 45.5), listOf(7.0, 46.0, 1_500.0))
        val unmeasured = line(listOf(6.0, 45.0, 1_200.0), listOf(6.5, 45.5, Double.NaN))
        val flat = listOf(listOf(6.0, 45.0), listOf(6.5, 45.5), listOf(7.0, 46.0))

        assertEquals(
            RouteGeometry(flat, listOf(1_200.0, 1_350.0, 1_500.0)),
            itineraryRoute(listOf(segment("a", 0, climbing))),
        )
        assertEquals(RouteGeometry(flat), itineraryRoute(listOf(segment("a", 0, gapped))))
        assertEquals(RouteGeometry(flat.take(2)), itineraryRoute(listOf(segment("a", 0, unmeasured))))
    }

    /** The vertex each line after the first starts on is a jump, the duplicate vertex included. */
    @Test
    fun theSegmentsAreLaidEndToEndInChainOrderNamingEachJump() {
        val first = segment("a", 0, line(listOf(6.0, 45.0), listOf(6.5, 45.5)))
        val second = segment("b", 1, line(listOf(6.5, 45.5), listOf(7.0, 46.0)))
        val expected =
            RouteGeometry(
                listOf(listOf(6.0, 45.0), listOf(6.5, 45.5), listOf(6.5, 45.5), listOf(7.0, 46.0)),
                jumps = listOf(2),
            )

        assertEquals(expected, itineraryRoute(listOf(first, second)))
        assertEquals(expected, itineraryRoute(listOf(second, first)))
    }

    @Test
    fun twoSegmentsAtOnePositionKeepTheOrderTheyAreListedIn() {
        val first = segment("a", 0, line(listOf(6.0, 45.0), listOf(6.5, 45.5)))
        val second = segment("b", 0, line(listOf(7.0, 46.0), listOf(7.5, 46.5)))

        assertEquals(listOf(6.0, 45.0), itineraryRoute(listOf(first, second))?.line?.first())
        assertEquals(listOf(7.0, 46.0), itineraryRoute(listOf(second, first))?.line?.first())
    }

    @Test
    fun everyLineASegmentHoldsIsFlownAndItsViasAreNot() {
        val collection =
            GeoJsonGeometryCollection(
                listOf(
                    GeoJsonMultiLineString(
                        listOf(
                            listOf(listOf(6.0, 45.0), listOf(6.01, 45.0)),
                            listOf(listOf(6.02, 45.0), listOf(6.03, 45.0)),
                        ),
                    ),
                    line(listOf(6.04, 45.0), listOf(6.05, 45.0)),
                    GeoJsonMultiPoint(listOf(listOf(7.0, 46.0))),
                    GeoJsonPoint(listOf(8.0, 47.0)),
                ),
            )
        val expected =
            RouteGeometry(
                listOf(
                    listOf(6.0, 45.0),
                    listOf(6.01, 45.0),
                    listOf(6.02, 45.0),
                    listOf(6.03, 45.0),
                    listOf(6.04, 45.0),
                    listOf(6.05, 45.0),
                ),
                jumps = listOf(2, 4),
            )

        assertEquals(expected, itineraryRoute(listOf(segment("a", 0, collection))))
    }

    @Test
    fun oneSegmentIsFlownByItsId() {
        val first = segment("a", 0, line(listOf(6.0, 45.0), listOf(6.5, 45.5)))
        val second = segment("b", 1, line(listOf(7.0, 46.0), listOf(7.5, 46.5)))

        assertEquals(
            RouteGeometry(listOf(listOf(7.0, 46.0), listOf(7.5, 46.5))),
            itineraryRoute(listOf(first, second), "b"),
        )
        assertNull(itineraryRoute(listOf(first, second), "missing"))
    }

    /** A vertex that names no place is left out with its elevation, and the others keep theirs. */
    @Test
    fun aVertexThatNamesNoPlaceIsLeftOut() {
        val holed =
            line(listOf(6.0, 45.0, 1_000.0), listOf(Double.NaN, 45.0, 1_100.0), listOf(6.5), listOf(7.0, 46.0, 1_200.0))

        assertEquals(
            RouteGeometry(listOf(listOf(6.0, 45.0), listOf(7.0, 46.0)), listOf(1_000.0, 1_200.0)),
            itineraryRoute(listOf(segment("a", 0, holed))),
        )
        // A line whose every vertex names no place opens no line, so it names no jump.
        val empty = segment("b", 1, line(listOf(Double.NaN, 45.0)))
        val after = segment("c", 2, line(listOf(8.0, 47.0), listOf(8.5, 47.5)))
        assertEquals(listOf(2), itineraryRoute(listOf(segment("a", 0, holed), empty, after))?.jumps)
    }

    @Test
    fun fewerThanTwoPlacesIsNoRoute() {
        assertNull(itineraryRoute(emptyList()))
        assertNull(itineraryRoute(listOf(segment("a", 0, line(listOf(6.0, 45.0))))))
        assertNull(itineraryRoute(listOf(segment("a", 0, null))))
        assertNull(
            itineraryRoute(listOf(segment("a", 0, GeoJsonMultiPoint(listOf(listOf(6.0, 45.0), listOf(7.0, 46.0)))))),
        )
        // Two lines of one vertex each are a route of one leg, a jump.
        assertEquals(
            RouteGeometry(listOf(listOf(6.0, 45.0), listOf(7.0, 46.0)), jumps = listOf(1)),
            itineraryRoute(listOf(segment("a", 0, line(listOf(6.0, 45.0))), segment("b", 1, line(listOf(7.0, 46.0))))),
        )
    }
}
