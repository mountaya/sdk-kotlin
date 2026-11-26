package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class MatrixGeoJsonTest {
    private val locations = listOf(listOf(6.0, 45.0), listOf(6.1, 45.1), listOf(6.2, 45.2))

    private fun input(
        metrics: List<DataSchema.MatrixMetric> = listOf(DataSchema.MatrixMetric.Duration),
        sources: List<Int>? = null,
        destinations: List<Int>? = null,
    ) = DataSchema.MatrixInput(DataSchema.ActivitySlug("HIKING_AND_TRAIL"), locations, metrics) {
        this.sources = sources
        this.destinations = destinations
    }

    @Test
    fun eachSolvedPairOfDistinctLocationsIsAConnectorThenEachLocationAPoint() {
        val result =
            DataSchema.MatrixResult {
                durations =
                    listOf(listOf(0.0, 100.0, 300.0), listOf(100.0, 0.0, null), listOf(300.0, 200.0, 0.0))
            }
        val features = matrixGeoJson(input(), result).features
        assertEquals(8, features.size)
        val connectors = features.take(5)
        assertEquals(
            listOf(1.0, 6.0, 1.0, 6.0, 3.5),
            connectors.map {
                (it.properties[FEATURE_WIDTH_PROPERTY] as JsonPrimitive).content.toDouble()
            },
        )
        assertEquals(GeoJsonLineString(listOf(locations[2], locations[1])), connectors[4].geometry)
        assertEquals(JsonPrimitive("matrix-location"), features[5].properties[FEATURE_KIND_PROPERTY])
    }

    /** A row is the location `sources` lists at its place, a column the one `destinations` lists. */
    @Test
    fun aSubsetGridDrawsFromTheLocationsItLists() {
        val result =
            DataSchema.MatrixResult {
                distances = listOf(listOf(1000.0, 4000.0))
                durations =
                    listOf(listOf(60.0, 240.0))
            }
        val features =
            matrixGeoJson(
                input(
                    listOf(DataSchema.MatrixMetric.Distance, DataSchema.MatrixMetric.Duration),
                    listOf(1),
                    listOf(0, 2),
                ),
                result,
            ).features
        assertEquals(
            listOf(JsonPrimitive(1000.0), JsonPrimitive(4000.0)),
            features.take(2).map { it.properties["value"] },
        )
        assertEquals(GeoJsonLineString(listOf(locations[1], locations[0])), features[0].geometry)
        assertEquals(JsonPrimitive("DISTANCE"), features[0].properties["metric"])
    }

    @Test
    fun aGridWithNoSpreadDrawsAtTheLeastAndAnUnknownMetricDrawsNoConnector() {
        val flat = DataSchema.MatrixResult { durations = listOf(listOf(0.0, 50.0), listOf(50.0, 0.0)) }
        val two =
            DataSchema.MatrixInput(
                DataSchema.ActivitySlug("RUNNING"),
                locations.take(2),
                listOf(DataSchema.MatrixMetric.Duration),
            )
        assertEquals(
            listOf(JsonPrimitive(1.0), JsonPrimitive(1.0)),
            matrixGeoJson(two, flat).features.take(2).map {
                it.properties[FEATURE_WIDTH_PROPERTY]
            },
        )
        val unknown = matrixGeoJson(input(listOf(DataSchema.MatrixMetric("ENERGY"))), flat).features
        assertEquals(3, unknown.size)
    }

    @Test
    fun aWidthIsHeldWithinTheScale() {
        assertEquals(1.0, matrixLineWidth(Double.NaN, 0.0, 1.0))
        assertEquals(6.0, matrixLineWidth(10.0, 0.0, 1.0))
        assertEquals(1.0, matrixLineWidth(-10.0, 0.0, 1.0))
        assertEquals(1.0, matrixLineWidth(5.0, 5.0, 5.0))
        assertEquals(3.5, matrixLineWidth(200.0, 100.0, 300.0))
        assertEquals(1.0, matrixLineWidth(1.0, 0.0, Double.POSITIVE_INFINITY))
    }

    @Test
    fun aWidthScalesItsCostLinearlyFromTheCheapestToTheDearestOfTheGrid() {
        assertEquals(1.0, matrixLineWidth(0.0, 0.0, 100.0))
        assertEquals(2.25, matrixLineWidth(25.0, 0.0, 100.0))
        assertEquals(3.5, matrixLineWidth(50.0, 0.0, 100.0))
        assertEquals(6.0, matrixLineWidth(100.0, 0.0, 100.0))
        // One past each end is held within the scale.
        assertEquals(6.0, matrixLineWidth(101.0, 0.0, 100.0))
        assertEquals(1.0, matrixLineWidth(-1.0, 0.0, 100.0))
    }

    @Test
    fun boundsOutOfOrderAndABoundOrACostOfNoFiniteNumberDrawAtTheLeast() {
        assertEquals(1.0, matrixLineWidth(5.0, 20.0, 10.0))
        assertEquals(1.0, matrixLineWidth(5.0, Double.NEGATIVE_INFINITY, 0.0))
        assertEquals(1.0, matrixLineWidth(5.0, 0.0, Double.NaN))
        assertEquals(1.0, matrixLineWidth(5.0, Double.NaN, 100.0))
        assertEquals(1.0, matrixLineWidth(Double.POSITIVE_INFINITY, 0.0, 100.0))
        assertEquals(1.0, matrixLineWidth(Double.NEGATIVE_INFINITY, 0.0, 100.0))
    }

    /** The spread is IEEE's `max > min`, so a grid from -0 to 0 has none. */
    @Test
    fun aNegativeZeroIsACostAndABoundLikeZero() {
        assertEquals(1.0, matrixLineWidth(0.0, -0.0, 0.0))
        assertEquals(1.0, matrixLineWidth(-0.0, 0.0, 100.0))
        assertEquals(3.5, matrixLineWidth(50.0, -0.0, 100.0))
    }
}
