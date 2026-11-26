package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BuildInfoBandsTest {
    /** 0.01 degrees of longitude at 45.85 degrees north. */
    private val step = 0.7745162686035524

    private fun segment(
        geojson: GeoJsonGeometry?,
        surface: List<Pair<IntRange, String>>,
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
            id = "s",
            itinerary_id = "i",
            mode = RestSchema.SegmentMode.Linestring,
            name = "s",
            position = 0,
            revision = 1,
            road_access_info = null,
            slope_info = null,
            speed = null,
            speed_info = null,
            start_at = null,
            steps = null,
            surface_info =
                surface.map { (range, value) ->
                    RestSchema.SurfaceInfo(range.first, range.last, RestSchema.SurfaceType(value))
                },
            trail_difficulty_info = null,
            way_type_info = null,
        )

    private fun line(vararg longitudes: Double) = GeoJsonLineString(longitudes.map { listOf(it, 45.85) })

    private fun bands(vararg segments: RestSchema.ItinerarySegment) =
        buildInfoBands(segments.toList()) { segment ->
            segment.surface_info?.map {
                InfoRange(it.from_index, it.to_index, it.value.value)
            }
        }

    private fun assertBands(
        expected: List<Triple<Double, Double, String>>,
        actual: List<InfoBand<String>>,
    ) {
        assertEquals(expected.map { it.third }, actual.map { it.value })
        expected.zip(actual).forEach { (want, band) ->
            assertEquals(want.first, band.startKm, 1e-9)
            assertEquals(want.second, band.endKm, 1e-9)
        }
    }

    @Test
    fun eachSegmentAdvancesTheAxisByItsLinesBandOrNoBand() {
        assertBands(
            listOf(Triple(step, 2 * step, "asphalt")),
            bands(segment(line(6.86, 6.87), emptyList()), segment(line(6.87, 6.88), listOf(0..1 to "asphalt"))),
        )
    }

    @Test
    fun aBandButtingOnOneOfTheSameValueIsMergedAcrossSegments() {
        assertBands(
            listOf(Triple(0.0, 3 * step, "gravel")),
            bands(
                segment(line(6.86, 6.87, 6.88), listOf(0..1 to "gravel", 1..2 to "gravel")),
                segment(
                    line(6.88, 6.89),
                    listOf(0..1 to "gravel"),
                ),
            ),
        )
    }

    @Test
    fun aBandOfNoLengthIsDroppedAndAnIndexPastTheLineReadsItsLastVertex() {
        assertBands(
            listOf(Triple(step, 2 * step, "ground")),
            bands(segment(line(6.86, 6.87, 6.87, 6.88), listOf(1..1 to "asphalt", 1..2 to "gravel", 2..3 to "ground"))),
        )
        assertBands(listOf(Triple(0.0, step, "asphalt")), bands(segment(line(6.86, 6.87), listOf(-3..40 to "asphalt"))))
    }

    @Test
    fun theLinesOfAMultiLineStringAreLaidEndToEnd() {
        val geometry = GeoJsonMultiLineString(listOf(line(6.86, 6.87).coordinates, line(6.9, 6.91).coordinates))

        assertBands(
            listOf(Triple(0.0, step, "asphalt"), Triple(step, 2 * step, "ground")),
            bands(segment(geometry, listOf(0..1 to "asphalt", 1..2 to "gravel", 2..3 to "ground"))),
        )
    }

    @Test
    fun aSegmentOfFewerThanTwoVerticesYieldsNoBand() {
        assertEquals(
            emptyList(),
            bands(
                segment(line(6.86), listOf(0..0 to "asphalt")),
                segment(
                    null,
                    listOf(0..1 to "asphalt"),
                ),
            ),
        )
    }
}
