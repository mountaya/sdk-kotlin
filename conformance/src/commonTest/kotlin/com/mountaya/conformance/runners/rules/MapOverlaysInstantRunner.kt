package com.mountaya.conformance

import com.mountaya.client.BaseUrls
import com.mountaya.client.Url
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.MapSource
import com.mountaya.elements.ScriptedMapSurface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * `map.overlays.instant`: what the map engine draws for a pinned instant, on the scripted surface, over a client whose
 * Tile API is the case's `tilesBaseUrl` and serves a manifest of its `overlays`: the classic ones in one classic
 * category and the enrichments in the category keyed `enrichment`, each one vector source named after its slug of one
 * fill layer at every zoom, its `tiles` as stated and its two `capabilities` from `server` and `client`. `overlay` is
 * `setOverlay`, or `clearOverlay` for null, `add` is `addEnrichment`, `instant` is `setInstant`, `switch` asks for a style
 * of its own, `setStyle`, which the surface then loads, as `map.basemap.replay`'s runner plays a switch, and `fail` has
 * the surface report the renderer's failure, with its `status`, of the tile 3/4/5 of the first address the map draws the
 * source from, under the credential the client sends, as `map.events.access-refused`'s runner plays one. After each step
 * it answers the instant the map holds, the classic overlay whose layer the state holds, the engine's enrichments, the
 * addresses each overlay drawn is drawn from, read off the sources the map hands its renderer, never off a rule, and,
 * for a `fail`, the refusal and the error the map told.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapOverlaysInstantRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val overlays =
            input
                .items(
                    "overlays",
                ).map { it as? FixtureValue.Object ?: throw AssertionError("an overlay is no object") }
        val classic = overlays.filter { it.text("kind") == "classic" }.map { it.text("slug") }
        val body = manifest(overlays)
        val tiles = input.text("tilesBaseUrl")
        val map =
            MapEngine(
                MapFixtures.client(BaseUrls(tiles = Url(tiles)), MapContentFixtures.Serving { 200 to body }),
                geolocation = InertGeolocation(),
            )
        val surface = ScriptedMapSurface(map)
        surface.loadStyle()
        val told = mutableListOf<MapEvent>()
        val reading =
            scope.launch(
                UnconfinedTestDispatcher(scope.testScheduler),
            ) { map.events().collect { told += it } }
        var switches = 0
        val answers = mutableListOf<FixtureValue>()
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            val heard = told.size
            var failed = false
            when (val name = step.text("step")) {
                "overlay" -> {
                    step.textOrNull("slug")?.let { map.setOverlay(it) } ?: map.clearOverlay()
                }

                "add" -> {
                    map.addEnrichment(step.text("slug"))
                }

                "instant" -> {
                    map.setInstant(TimeFixtures.instant(step.textOrNull("at")))
                }

                "switch" -> {
                    switches += 1
                    map.setStyle(Url("https://tiles.test/base-$switches.json"))
                    surface.loadStyle()
                }

                "fail" -> {
                    failed = true
                    val source = step.text("source")
                    val address =
                        tilesOf(map, source)?.firstOrNull()
                            ?: throw AssertionError("the map draws no source `$source` to fail a tile of")
                    val url = address.replace("{z}", "3").replace("{x}", "4").replace("{y}", "5")
                    surface.failRequest(url, step.number("status").toInt(), source)
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
            val drawn =
                classic.filter { slug ->
                    map.state.value.layers
                        .any { it.id == "$slug-layer" }
                }
            if (drawn.size > 1) throw AssertionError("two classic overlays are drawn: $drawn")
            answers +=
                MapFixtures.objectOf(
                    "enrichments" to textArray(map.enrichments),
                    "instant" to TimeFixtures.written(map.instant),
                    "overlay" to (drawn.firstOrNull()?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    "tiles" to
                        FixtureValue.Object(
                            (drawn + map.enrichments).map { slug ->
                                slug to textArray(tilesOf(map, slug) ?: throw AssertionError("`$slug` has no tiles"))
                            },
                        ),
                    "told" to if (failed) toldOf(told.drop(heard)) else FixtureValue.Null,
                )
        }
        map.close()
        reading.join()
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }

    /** The addresses the map draws the source [id] from, as it hands them its renderer, or null where it draws none. */
    private fun tilesOf(
        map: MapEngine,
        id: String,
    ): List<String>? =
        when (
            val kind =
                map.state.value.sources
                    .firstOrNull { it.id == id }
                    ?.kind
        ) {
            is MapSource.Kind.Vector -> kind.tiles
            is MapSource.Kind.Raster -> kind.tiles
            is MapSource.Kind.RasterDem -> kind.tiles
            else -> null
        }

    /** What a failure told, as the fixture writes it: the refusal, `{ status, url }`, or null, and whether an error was. */
    private fun toldOf(events: List<MapEvent>): FixtureValue {
        var refused: FixtureValue = FixtureValue.Null
        var error = false
        for (event in events) {
            when (event) {
                is MapEvent.AccessRefused -> {
                    if (refused !is FixtureValue.Null) throw AssertionError("two refusals were told")
                    refused =
                        MapFixtures.objectOf(
                            "status" to FixtureValue.Number(event.status.toDouble()),
                            "url" to FixtureValue.Text(event.url),
                        )
                }

                is MapEvent.Failure -> {
                    error = true
                }

                else -> {}
            }
        }
        return MapFixtures.objectOf("accessRefused" to refused, "error" to FixtureValue.Bool(error))
    }

    /** The manifest the case's [overlays] make, as the Tile API answers it, every member no rule reads a placeholder. */
    private fun manifest(overlays: List<FixtureValue.Object>): String {
        fun overlay(written: FixtureValue.Object): JsonObject {
            val slug = written.text("slug")

            fun resolves(side: String): JsonObject = JsonObject(mapOf("timestamp" to JsonPrimitive(written.bool(side))))
            val layer =
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive("$slug-layer"),
                        "key" to JsonPrimitive(slug),
                        "layout" to JsonNull,
                        "paint" to JsonNull,
                        "source_layer" to JsonPrimitive(slug),
                        "type" to JsonPrimitive("fill"),
                        "zoom_max" to JsonPrimitive(24),
                        "zoom_min" to JsonPrimitive(0),
                    ),
                )
            val source =
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive(slug),
                        "layers" to JsonArray(listOf(layer)),
                        "tiles" to JsonArray(fixtureTexts(written.items("tiles"), "`tiles`").map(::JsonPrimitive)),
                        "type" to JsonPrimitive("vector"),
                        "zoom_max" to JsonPrimitive(14),
                        "zoom_min" to JsonPrimitive(0),
                    ),
                )
            return JsonObject(
                mapOf(
                    "capabilities" to JsonObject(mapOf("client" to resolves("client"), "server" to resolves("server"))),
                    "content_type" to JsonPrimitive("application/x-protobuf"),
                    "id" to JsonPrimitive(slug),
                    "locales" to JsonObject(emptyMap()),
                    "slug" to JsonPrimitive(slug),
                    "sources" to JsonArray(listOf(source)),
                ),
            )
        }

        fun category(
            slug: String,
            kind: String,
        ): JsonObject =
            JsonObject(
                mapOf(
                    "children" to JsonArray(overlays.filter { it.text("kind") == kind }.map(::overlay)),
                    "id" to JsonPrimitive(slug),
                    "locales" to JsonObject(emptyMap()),
                    "slug" to JsonPrimitive(slug),
                ),
            )
        val unused =
            JsonObject(
                mapOf(
                    "capabilities" to
                        JsonObject(
                            mapOf(
                                "client" to JsonObject(mapOf("timestamp" to JsonPrimitive(false))),
                                "server" to JsonObject(mapOf("timestamp" to JsonPrimitive(false))),
                            ),
                        ),
                    "content_type" to JsonPrimitive("application/x-protobuf"),
                    "id" to JsonPrimitive("none"),
                    "locales" to JsonObject(emptyMap()),
                    "slug" to JsonPrimitive("none"),
                    "sources" to JsonArray(emptyList()),
                ),
            )
        val elevation =
            JsonObject(
                mapOf(
                    "id" to JsonPrimitive("elevation"),
                    "tile_size" to JsonPrimitive(512),
                    "url" to JsonPrimitive("https://tiles.test/elevation.json"),
                ),
            )
        val catalogue =
            JsonObject(
                mapOf(
                    "elevation" to elevation,
                    "itineraries" to unused,
                    "overlays" to
                        JsonArray(listOf(category("conditions", "classic"), category("enrichment", "enrichment"))),
                    "terrains" to JsonArray(emptyList()),
                    "zones" to unused,
                ),
            )
        return JsonObject(mapOf("data" to catalogue)).toString()
    }
}
