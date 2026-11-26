package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.PLACE_POINT_COLOR
import com.mountaya.geo.POI_RESULT_POINT_COLOR
import com.mountaya.geo.SUGGESTION_ALTERNATIVE_COLOR
import com.mountaya.geo.SUGGESTION_CHOSEN_COLOR
import com.mountaya.geo.directionsGeoJson
import com.mountaya.geo.isochronesGeoJson
import com.mountaya.geo.matrixGeoJson
import com.mountaya.geo.placesGeoJson
import com.mountaya.geo.poisGeoJson
import com.mountaya.geo.suggestionsGeoJson
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json

/**
 * `geo.layers.data-geojson`: the FeatureCollection the case's Data API `result` of its `kind` is drawn
 * as. The result is decoded by the Data API's own serializer from the JSON the fixture writes, a cell
 * that is no finite number included, which a JSON text cannot carry and the fixture writes as a
 * `$number`; a matrix's `input` is built as the Data API's, its activity, locations, metrics, sources
 * and destinations as the fixture writes them. The fixture's colours are held to the ones Geo
 * publishes.
 */
object GeoLayersDataGeojsonRunner : ConformanceRunner {
    private val json = Json { allowSpecialFloatingPointValues = true }

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "placePointColor" to FixtureValue.Text(PLACE_POINT_COLOR),
                    "poiResultPointColor" to FixtureValue.Text(POI_RESULT_POINT_COLOR),
                    "suggestionAlternativeColor" to FixtureValue.Text(SUGGESTION_ALTERNATIVE_COLOR),
                    "suggestionChosenColor" to FixtureValue.Text(SUGGESTION_CHOSEN_COLOR),
                ),
        )
        val input = case.fields
        val result = fixtureJson(input["result"] ?: throw AssertionError("the case has no `result`"))
        val drawn =
            when (val kind = input.text("kind")) {
                "isochrones" -> {
                    isochronesGeoJson(json.decodeFromJsonElement(DataSchema.IsochronesResult.serializer(), result))
                }

                "directions" -> {
                    directionsGeoJson(json.decodeFromJsonElement(DataSchema.DirectionsResult.serializer(), result))
                }

                "suggestions" -> {
                    val routes = json.decodeFromJsonElement(DataSchema.SuggestionsResult.serializer(), result)
                    wholeOrNull(input, "chosen")?.let { suggestionsGeoJson(routes, it) } ?: suggestionsGeoJson(routes)
                }

                "places" -> {
                    placesGeoJson(json.decodeFromJsonElement(DataSchema.GeocodeResult.serializer(), result))
                }

                "pois" -> {
                    poisGeoJson(json.decodeFromJsonElement(DataSchema.PoisResult.serializer(), result))
                }

                "matrix" -> {
                    matrixGeoJson(
                        matrixInput(input.objectOrNull("input")),
                        json.decodeFromJsonElement(DataSchema.MatrixResult.serializer(), result),
                    )
                }

                else -> {
                    throw AssertionError("`$kind` is no kind of answer")
                }
            }
        return ConformanceOutcome.Value(collectionValue(drawn))
    }

    private fun matrixInput(fields: FixtureValue.Object?): DataSchema.MatrixInput {
        val written = fields ?: throw AssertionError("a matrix case has no `input`")

        fun indexes(key: String): List<Int>? =
            written[key]?.let {
                (it as FixtureValue.Array).items.map { index ->
                    (index as FixtureValue.Number).value.toInt()
                }
            }
        return DataSchema.MatrixInput(
            activity = DataSchema.ActivitySlug(written.text("activity")),
            locations =
                written.items("locations").map {
                    (it as FixtureValue.Array).items.map { value ->
                        (value as FixtureValue.Number).value
                    }
                },
            metrics = fixtureTexts(written.items("metrics"), "`metrics`").map { DataSchema.MatrixMetric(it) },
        ) {
            sources = indexes("sources")
            destinations = indexes("destinations")
        }
    }

    private fun collectionValue(collection: GeoJsonFeatureCollection): FixtureValue =
        FixtureValue.Object(
            listOf(
                "features" to
                    FixtureValue.Array(
                        collection.features.map { feature ->
                            FixtureValue.Object(
                                listOf(
                                    "geometry" to (feature.geometry?.let(::writtenGeometry) ?: FixtureValue.Null),
                                    "properties" to fixtureValueOf(feature.properties),
                                    "type" to FixtureValue.Text("Feature"),
                                ),
                            )
                        },
                    ),
                "type" to FixtureValue.Text("FeatureCollection"),
            ),
        )
}
