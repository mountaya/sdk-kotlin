package com.mountaya.geo

import kotlin.math.PI
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertEquals

class FootAroundTest {
    private val from = listOf(6.86, 45.85)
    private val to = listOf(6.9, 45.85)

    private fun foot(
        point: List<Double>,
        start: List<Double>,
        end: List<Double>,
    ): Pair<Double, Double> = footAround(point, metersPerDegreeLongitudeAt(point[1]), start, end) { d, t -> d to t }

    @Test
    fun theFootStandsWhereThePointProjectsInTheLocalPlane() {
        val (distance, fraction) = foot(listOf(6.88, 45.851), from, to)

        assertEquals(111.1949266442996, distance, 1e-9)
        assertEquals(0.5, fraction, 1e-12)
    }

    @Test
    fun aFootPastAnEndStandsOnThatEnd() {
        val (distance, fraction) = foot(listOf(6.95, 45.85), from, to)

        assertEquals(3_872.581345548356, distance, 1e-9)
        assertEquals(1.0, fraction)
        assertEquals(0.0, foot(listOf(6.8, 45.85), from, to).second)
    }

    @Test
    fun aSegmentThatIsOnePointHasItsFootThereAtTheStart() {
        val (distance, fraction) = foot(listOf(6.86, 45.851), from, from)

        assertEquals(0.0, fraction)
        assertEquals(METERS_PER_DEGREE_MEAN_SPHERE * 0.001, distance, 1e-9)
    }

    /**
     * 170 to -20 runs 190 degrees eastward past the antimeridian: its far end is 20 degrees from the
     * point the other way round, and the foot there is the one answered.
     */
    @Test
    fun theFootPastTheMeridianHalfATurnAwayIsTheNearerOne() {
        val (distance, fraction) = foot(listOf(0.0, 0.0), listOf(170.0, 0.0), listOf(-20.0, 0.0))

        assertEquals(20.0 * METERS_PER_DEGREE_MEAN_SPHERE, distance, 1e-6)
        assertEquals(1.0, fraction)
    }

    @Test
    fun aSegmentAcrossTheAntimeridianIsMeasuredInOnePiece() {
        val (distance, fraction) = foot(listOf(179.999, 0.0), listOf(-179.999, -1.0), listOf(-179.999, 1.0))

        assertEquals(222.38985329017936, distance, 1e-9)
        assertEquals(0.5, fraction, 1e-12)
    }

    @Test
    fun theDegreeOfLongitudeShrinksWithTheLatitude() {
        assertEquals(METERS_PER_DEGREE_MEAN_SPHERE, metersPerDegreeLongitudeAt(0.0))
        assertEquals(METERS_PER_DEGREE_MEAN_SPHERE * cos(60.0 * PI / 180.0), metersPerDegreeLongitudeAt(60.0), 1e-9)
    }

    /**
     * A walk over coordinates held unboxed places each segment as the form over positions does, so the
     * two read the very same numbers.
     */
    @Test
    fun theFormOverCoordinatesAnswersTheFormOverPositionsNumberForNumber() {
        val segments =
            listOf(
                Triple(listOf(6.88, 45.851), from, to),
                Triple(listOf(6.95, 45.85), from, to),
                Triple(listOf(6.86, 45.851), from, from),
                Triple(listOf(0.0, 0.0), listOf(170.0, 0.0), listOf(-20.0, 0.0)),
                Triple(listOf(179.999, 0.0), listOf(-179.999, -1.0), listOf(-179.999, 1.0)),
                Triple(listOf(-179.5, 60.0), listOf(179.5, 59.0), listOf(-178.0, 61.0)),
            )
        for ((point, start, end) in segments) {
            val perMeter = metersPerDegreeLongitudeAt(point[1])
            val unboxed =
                footAround(point[0], point[1], perMeter, start[0], start[1], end[0], end[1]) { distance, at ->
                    distance to at
                }

            assertEquals(foot(point, start, end), unboxed, "at $point to $start-$end")
        }
    }
}
