package com.mountaya.geo

import com.mountaya.client.GeoJsonMultiPoint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItineraryGeometryTest {
    /**
     * A chain read out of order profiles as another itinerary of the same length, which is the kind of
     * wrong that draws cleanly.
     */
    @Test
    fun theChainIsCollectedInPositionOrderWhateverOrderItArrivedIn() {
        val second = lineString(listOf(6.0, 45.002, 1_200.0), listOf(6.0, 45.003, 1_250.0))
        val first = lineString(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.001, 1_100.0))

        val geometry =
            itineraryGeometry(
                listOf(
                    restSegment("b", position = 1, geojson = second),
                    restSegment("a", position = 0, geojson = first),
                ),
            )

        assertEquals(collectionOf(first, second), geometry)
    }

    @Test
    fun eachSegmentsGeometryIsKeptAsItStoresItACollectionNestedWhole() {
        val stored =
            collectionOf(
                lineString(listOf(6.0, 45.0), listOf(6.01, 45.0)),
                GeoJsonMultiPoint(listOf(listOf(6.0, 45.0))),
            )

        assertEquals(collectionOf(stored), itineraryGeometry(listOf(restSegment("a", geojson = stored))))
    }

    @Test
    fun aSegmentWithNoGeometryYetIsLeftOut() {
        val line = lineString(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.001, 1_100.0))

        val geometry =
            itineraryGeometry(listOf(restSegment("a", position = 0), restSegment("b", position = 1, geojson = line)))

        assertEquals(collectionOf(line), geometry)
    }

    /**
     * An empty collection reads as an itinerary of no length; none draws the state a missing itinerary
     * draws.
     */
    @Test
    fun noGeometryIsNoneNeverAnEmptyCollection() {
        assertNull(itineraryGeometry(listOf(restSegment("a"))))
        assertNull(itineraryGeometry(emptyList()))
    }

    @Test
    fun theChainProfilesAsOneCurveEachLegLaidAfterTheOneBefore() {
        val geometry =
            itineraryGeometry(
                listOf(
                    restSegment(
                        "a",
                        position = 0,
                        geojson = lineString(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.001, 1_100.0)),
                    ),
                    restSegment(
                        "b",
                        position = 1,
                        geojson = lineString(listOf(6.0, 45.001, 1_100.0), listOf(6.0, 45.002, 1_200.0)),
                    ),
                ),
            )

        val series = buildElevationSeries(geometry)

        assertEquals(listOf(1_000.0, 1_100.0, 1_100.0, 1_200.0), series.points.map { it.elevation })
        val leg = haversineMeters(listOf(6.0, 45.0), listOf(6.0, 45.001))
        assertTrue(abs(series.totalDistance - 2 * leg) <= 1e-6)
    }
}
