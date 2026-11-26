package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.test.TestScope

/**
 * `geo.search.pick`: what picking the case's `payload` does, how the camera moves and what the map's search highlight
 * is handed, each null where nothing is, and a member the pick does not state absent. The zoom a point is flown to is
 * held to `POINT_ZOOM`. The entry is one a consumer only observes, as a map's camera and its mark, so it runs from
 * Geo's own tests.
 */
object GeoSearchPickRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(case, mapOf("pointZoom" to FixtureValue.Number(POINT_ZOOM)))
        val written = case.fields.objectOrNull("payload") ?: throw AssertionError("the case has no `payload`")
        val pick = pickTarget(payload(written))
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "camera" to (pick.camera?.let(::camera) ?: FixtureValue.Null),
                    "mark" to (pick.mark?.let(::mark) ?: FixtureValue.Null),
                ),
            ),
        )
    }

    /** A payload as the fixture writes it, by its `kind`, each member it does not state null. */
    private fun payload(written: FixtureValue.Object): SearchRowPayload {
        fun numbers(key: String): List<Double>? =
            written[key]?.takeUnless { it is FixtureValue.Null }?.let { fixturePosition(it, key) }

        fun point(key: String): List<Double> = numbers(key) ?: throw AssertionError("the payload has no `$key`")
        return when (val kind = written.text("kind")) {
            "place" -> {
                SearchRowPayload.Place(point("point"), numbers("bounds"))
            }

            "poi" -> {
                SearchRowPayload.Poi(point("point"), written.textOrNull("category")?.let { DataSchema.POICategory(it) })
            }

            "way" -> {
                SearchRowPayload.Way(
                    point("anchor"),
                    numbers("bounds"),
                    (written["coordinates"] as? FixtureValue.Array)?.items?.map { fixturePosition(it, "coordinates") },
                )
            }

            "itinerary" -> {
                SearchRowPayload.Itinerary(written.text("itineraryId"), numbers("bounds"), numbers("start"))
            }

            "zone" -> {
                SearchRowPayload.Zone(written.text("zoneId"), numbers("point"), numbers("bounds"))
            }

            "collection" -> {
                SearchRowPayload.Collection(
                    written.text("collectionId"),
                    written.textOrNull("name"),
                    written.textOrNull("description"),
                    (written["isPrivate"] as? FixtureValue.Bool)?.value,
                )
            }

            else -> {
                throw AssertionError("`$kind` is no kind of payload")
            }
        }
    }

    /** A camera move as the fixture expects one. */
    private fun camera(camera: SearchPickCamera): FixtureValue =
        when (camera) {
            is SearchPickCamera.Fit -> {
                FixtureValue.Object(listOf("bounds" to numbers(camera.bounds), "kind" to FixtureValue.Text("fit")))
            }

            is SearchPickCamera.Fly -> {
                FixtureValue.Object(
                    listOf(
                        "center" to numbers(camera.center),
                        "kind" to FixtureValue.Text("fly"),
                        "zoom" to FixtureValue.Number(camera.zoom),
                    ),
                )
            }
        }

    /** A mark as the fixture expects one, a way's extent and line only where it states them. */
    private fun mark(mark: SearchHighlight): FixtureValue =
        when (mark) {
            is SearchHighlight.Place -> {
                FixtureValue.Object(listOf("kind" to FixtureValue.Text("place"), "point" to numbers(mark.point)))
            }

            is SearchHighlight.Poi -> {
                FixtureValue.Object(
                    buildList {
                        mark.category?.let { add("category" to FixtureValue.Text(it.value)) }
                        add("kind" to FixtureValue.Text("poi"))
                        add("point" to numbers(mark.point))
                    },
                )
            }

            is SearchHighlight.Way -> {
                FixtureValue.Object(
                    buildList {
                        add("anchor" to numbers(mark.anchor))
                        mark.bounds?.let { add("bounds" to numbers(it)) }
                        mark.coordinates?.let { line -> add("coordinates" to FixtureValue.Array(line.map(::numbers))) }
                        add("kind" to FixtureValue.Text("way"))
                    },
                )
            }
        }

    /** [numbers] as a fixture's array of numbers. */
    private fun numbers(numbers: List<Double>): FixtureValue =
        FixtureValue.Array(numbers.map { FixtureValue.Number(it) })
}
