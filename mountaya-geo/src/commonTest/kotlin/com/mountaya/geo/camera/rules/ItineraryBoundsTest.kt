package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ItineraryBoundsTest {
    private fun segment(
        position: Int,
        geojson: GeoJsonGeometry?,
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
            geojson = geojson,
            gradient_max = null,
            gradient_min = null,
            group_id = null,
            id = "s$position",
            itinerary_id = "itinerary",
            mode = RestSchema.SegmentMode.Linestring,
            name = "s$position",
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

    private fun collection(vararg geometries: GeoJsonGeometry): GeoJsonGeometry =
        GeoJsonGeometryCollection(geometries.toList())

    private fun line(vararg positions: List<Double>): GeoJsonGeometry = GeoJsonLineString(positions.toList())

    @Test
    fun theBoxSpansEverySegmentsStoredGeometryADirectionsSegmentsViasIncluded() {
        assertEquals(
            listOf(6.86, 45.83, 6.92, 45.88),
            itineraryBounds(listOf(segment(0, collection(line(listOf(6.86, 45.83), listOf(6.92, 45.88)))))),
        )
        val vias =
            collection(
                line(listOf(6.86, 45.83), listOf(6.92, 45.88)),
                GeoJsonMultiPoint(listOf(listOf(6.95, 45.79))),
            )
        assertEquals(listOf(6.86, 45.79, 6.95, 45.88), itineraryBounds(listOf(segment(0, vias))))
        // Every segment counts, whatever its place in the chain.
        assertEquals(
            listOf(6.86, 45.83, 7.1, 45.95),
            itineraryBounds(
                listOf(
                    segment(1, collection(line(listOf(7.02, 45.9), listOf(7.1, 45.95)))),
                    segment(0, collection(line(listOf(6.86, 45.83), listOf(6.92, 45.88)))),
                ),
            ),
        )
    }

    @Test
    fun aSegmentWithNoGeometryAddsNothingAndNoneAtAllFramesNothing() {
        assertEquals(
            listOf(6.86, 45.83, 6.92, 45.88),
            itineraryBounds(
                listOf(segment(0, null), segment(1, collection(line(listOf(6.86, 45.83), listOf(6.92, 45.88))))),
            ),
        )
        assertNull(itineraryBounds(listOf(segment(0, null), segment(1, null))))
        assertNull(itineraryBounds(emptyList()))
    }

    @Test
    fun aChainStraddlingTheAntimeridianIsFramedAcrossItItsEastEdgePast180() {
        assertEquals(
            listOf(179.5, -17.0, 180.5, -16.0),
            itineraryBounds(
                listOf(
                    segment(0, collection(line(listOf(179.5, -16.0), listOf(179.9, -16.5)))),
                    segment(1, collection(line(listOf(-179.9, -16.7), listOf(-179.5, -17.0)))),
                ),
            ),
        )
    }
}
