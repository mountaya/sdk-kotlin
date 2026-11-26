package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.SearchLabel
import com.mountaya.geo.SearchWording
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.describePlace
import com.mountaya.geo.describePoi
import com.mountaya.geo.describeWay
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json

/**
 * `geo.search.describe`: the second line of the case's `entity` of its `kind`, read by the Data API's own serializer,
 * worded by its `wording`: `words`, the reader's word for a wire value by its kind and its value, and `units`, the
 * reader's unit preferences, English and metric where they leave one out. The elevation floor and the separator are
 * read back through the line itself, as no rule publishes either: an elevation at the floor stated and one just under
 * it, and the text between two tokens.
 */
object GeoSearchDescribeRunner : ConformanceRunner {
    private val json = Json

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val floor = case.root("constants").number("elevationFloorMeters")
        holdDeclaredConstants(
            case,
            rules = mapOf("elevationFloorMeters" to elevationFloor(floor)),
            values = mapOf("separator" to FixtureValue.Text(separator())),
        )
        val input = case.fields
        val entity = fixtureJson(input["entity"] ?: throw AssertionError("the case has no `entity`"))
        val wording = wording(input.objectOrNull("wording"))
        val line =
            when (val kind = input.text("kind")) {
                "place" -> describePlace(json.decodeFromJsonElement(DataSchema.Place.serializer(), entity), wording)
                "poi" -> describePoi(json.decodeFromJsonElement(DataSchema.POI.serializer(), entity), wording)
                "way" -> describeWay(json.decodeFromJsonElement(DataSchema.Way.serializer(), entity), wording)
                else -> throw AssertionError("`$kind` is no kind of result a line describes")
            }
        return ConformanceOutcome.Value(FixtureValue.Text(line))
    }

    /** A hut of no name [distance] meters away at [elevation] meters, titled by what it is. */
    private fun hut(
        elevation: Double,
        distance: Double,
    ): DataSchema.POI =
        DataSchema.POI {
            category = DataSchema.POICategory.Hut
            distanceMeters = distance
            this.elevation = elevation
            name = ""
            osm =
                DataSchema.OSMRef {
                    id = "1"
                    type = DataSchema.OSMType.Node
                }
            point = listOf(6.85, 45.85)
        }

    /**
     * The floor [stated] where the line states an elevation at it and gives a hut just under it its distance in its
     * place, which is the floor the rule holds, and `NaN`, which no fixture states, otherwise.
     */
    private fun elevationFloor(stated: Double): Double {
        val at = describePoi(hut(stated, 2000.0))
        val under = describePoi(hut(stated - 1, 2000.0))
        return if (at == "${stated.toLong()} m" && under == "2.0 km") stated else Double.NaN
    }

    /** The text the line writes between two tokens: a path's word and its region. */
    private fun separator(): String {
        val line =
            describeWay(
                DataSchema.Way {
                    highway = "path"
                    name = "X"
                    osm =
                        DataSchema.OSMRef {
                            id = "1"
                            type = DataSchema.OSMType.Node
                        }
                    point = listOf(6.85, 45.85)
                    region = "Valais"
                },
            )
        if (!line.startsWith("Path") || !line.endsWith("Valais")) return line
        return line.removePrefix("Path").removeSuffix("Valais")
    }

    /** A case's `wording`, none where it states none: `words` by kind and value, which the resolver answers, and `units`. */
    private fun wording(written: FixtureValue.Object?): SearchWording {
        if (written == null) return SearchWording()
        val units =
            written.objectOrNull("units")?.let { stated ->
                UnitPreferences(
                    language = stated.textOrNull("language") ?: "en",
                    length = RestSchema.UnitLength(stated.textOrNull("length") ?: "metric"),
                )
            }
        val words =
            written.objectOrNull("words")?.entries.orEmpty().associate { (kind, values) ->
                val byValue =
                    values as? FixtureValue.Object ?: throw AssertionError("the words for `$kind` are no object")
                kind to
                    byValue.entries.associate { (value, word) ->
                        value to
                            (
                                (word as? FixtureValue.Text)?.value
                                    ?: throw AssertionError("the word for `$value` is no string")
                            )
                    }
            }
        return SearchWording(units) { label ->
            when (label) {
                is SearchLabel.PlaceCategory -> words["placeCategory"]?.get(label.value.value)
                is SearchLabel.PoiCategory -> words["poiCategory"]?.get(label.value.value)
                is SearchLabel.Highway -> words["highway"]?.get(label.value)
                is SearchLabel.SacScale -> words["sacScale"]?.get(label.value)
                is SearchLabel.Surface -> words["surface"]?.get(label.value)
            }
        }
    }
}
