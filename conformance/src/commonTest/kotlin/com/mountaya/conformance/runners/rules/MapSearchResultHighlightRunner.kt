package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapSource
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.SearchHighlight
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonPrimitive

/**
 * `map.search.result-highlight`: what a map draws for the case's `payload` in its `accent`, over a glyph set that draws
 * a point or none as its `hasPointGlyph` says, through the published engine's `setSearchHighlight`: the one source the
 * highlight draws from, written as MapLibre's style specification writes a GeoJSON source, its layers in the order
 * they stack, and the band they draw at. Every width, opacity, blur, radius, dash, glyph, the band and the source are
 * read back through what the engine draws for a way's line, a way's extent and a place, under a set that holds a point
 * and one that holds none, since the rule's constants are the engine's own.
 */
object MapSearchResultHighlightRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val held = style()
        holdDeclaredConstants(case, rules = mapOf("rank" to held.first), values = held.second)
        val input = case.fields
        val written = input.objectOrNull("payload") ?: throw AssertionError("the case has no `payload`")
        val accent = HexColor(input.text("accent"))
        val hasPointGlyph = (input["hasPointGlyph"] as? FixtureValue.Bool)?.value ?: false
        return ConformanceOutcome.Value(drawn(highlight(written), accent, hasPointGlyph).value)
    }

    /** What a map draws for a picked result: its source and its layers in the order they stack. */
    private class Drawn(
        val source: MapSource,
        val layers: List<MapLayer>,
    ) {
        /** The drawing as the fixture writes it: `sources`, `layers` and `rank`. */
        val value: FixtureValue
            get() {
                val data =
                    (source.kind as? MapSource.Kind.GeoJson)?.features?.let { collection ->
                        FixtureValue.Object(
                            listOf(
                                "features" to
                                    FixtureValue.Array(
                                        collection.features.map { feature ->
                                            FixtureValue.Object(
                                                listOf(
                                                    "geometry" to
                                                        (feature.geometry?.let(::writtenGeometry) ?: FixtureValue.Null),
                                                    "properties" to fixtureValueOf(feature.properties),
                                                    "type" to FixtureValue.Text("Feature"),
                                                ),
                                            )
                                        },
                                    ),
                                "type" to FixtureValue.Text("FeatureCollection"),
                            ),
                        )
                    } ?: FixtureValue.Null
                return FixtureValue.Object(
                    listOf(
                        "layers" to FixtureValue.Array(layers.map(MapContentFixtures::styleLayer)),
                        "rank" to
                            (
                                layers.firstOrNull()?.let {
                                    FixtureValue.Number(
                                        it.rank.toDouble(),
                                    )
                                } ?: FixtureValue.Null
                            ),
                        "sources" to
                            FixtureValue.Object(
                                listOf(
                                    source.id to
                                        FixtureValue.Object(
                                            listOf("data" to data, "type" to FixtureValue.Text("geojson")),
                                        ),
                                ),
                            ),
                    ),
                )
            }
    }

    /**
     * Draws [highlight] in [accent] on a map whose glyph set draws a point where [hasPointGlyph] says so, and answers
     * the
     * one source and the layers it holds for it. A drawing whose layers stand at more than one band, or which adds more
     * than one source, fails.
     */
    private fun drawn(
        highlight: SearchHighlight,
        accent: HexColor,
        hasPointGlyph: Boolean,
    ): Drawn {
        val map = MapEngine(MapFixtures.client(), glyphs = glyphs(hasPointGlyph), geolocation = InertGeolocation())
        map.setSearchHighlight(highlight, accent)
        val state = map.state.value
        val source = state.sources.singleOrNull() ?: throw AssertionError("the map drew ${state.sources.size} sources")
        val layers = state.layers.filter { it.source == source.id }
        if (layers.size != state.layers.size || layers.map { it.rank }.toSet().size != 1) {
            throw AssertionError("the map drew layers apart from its source or its band")
        }
        map.close()
        return Drawn(source, layers)
    }

    /** The bundled marker set, or that set without the point a place's pin carries. */
    private fun glyphs(hasPointGlyph: Boolean): GlyphSet {
        val marker = GlyphSet.marker
        if (hasPointGlyph) return marker
        return GlyphSet.of(
            "markers-without-point",
            marker.paint,
            marker.strokeWidth,
            marker.viewBox,
            marker.markup - "point",
        )
            ?: throw AssertionError("the marker set without its point cannot be drawn")
    }

    /**
     * The highlight's constants as a map draws them, read back through what it draws: a way's line, a way's extent,
     * and a place's pin under a set that holds a point and one that holds none.
     */
    private fun style(): Pair<Double, Map<String, FixtureValue>> {
        val accent = HexColor("#1c7ed6")
        val line =
            drawn(
                SearchHighlight.Way(listOf(6.85, 45.82), null, listOf(listOf(6.8, 45.8), listOf(6.9, 45.85))),
                accent,
                true,
            )
        val extent = drawn(SearchHighlight.Way(listOf(6.5, 45.5), listOf(6.0, 45.0, 7.0, 46.0), null), accent, true)
        val place = drawn(SearchHighlight.Place(listOf(6.86, 45.83)), accent, true)
        val fallback = drawn(SearchHighlight.Place(listOf(6.86, 45.83)), accent, false)

        fun layer(
            drawn: Drawn,
            role: String,
        ): MapLayer =
            drawn.layers.firstOrNull { it.id == "${drawn.source.id}-$role" }
                ?: throw AssertionError("the map drew no `$role` layer")

        fun value(
            layer: MapLayer,
            name: String,
            paint: Boolean = true,
        ): FixtureValue = (if (paint) layer.paint else layer.layout)[name]?.let(::fixtureValueOf) ?: FixtureValue.Null

        fun glyph(drawn: Drawn): FixtureValue {
            val image =
                (layer(drawn, "place").layout["icon-image"] as? JsonPrimitive)?.content
                    ?: throw AssertionError("a pin names no image")
            // `poi-icon-`, the glyph, a hyphen and six hexadecimal digits.
            return FixtureValue.Text(image.removePrefix("poi-icon-").dropLast(7))
        }
        val halo = layer(line, "halo")
        val casing = layer(line, "casing")
        val ends = layer(line, "endpoints")
        val pin = layer(place, "place")
        return (
            place.layers
                .firstOrNull()
                ?.rank
                ?.toDouble() ?: -1.0
        ) to
            mapOf(
                "casing" to
                    FixtureValue.Object(
                        listOf(
                            "color" to value(casing, "line-color"),
                            "width" to value(casing, "line-width"),
                        ),
                    ),
                "ends" to
                    FixtureValue.Object(
                        listOf(
                            "radius" to value(ends, "circle-radius"),
                            "strokeWidth" to value(ends, "circle-stroke-width"),
                        ),
                    ),
                "extent" to
                    FixtureValue.Object(
                        listOf(
                            "dash" to value(layer(extent, "bounds"), "line-dasharray"),
                            "fillOpacity" to value(layer(extent, "bounds-fill"), "fill-opacity"),
                            "width" to value(layer(extent, "bounds"), "line-width"),
                        ),
                    ),
                "halo" to
                    FixtureValue.Object(
                        listOf(
                            "blur" to value(halo, "line-blur"),
                            "opacity" to value(halo, "line-opacity"),
                            "width" to value(halo, "line-width"),
                        ),
                    ),
                "line" to FixtureValue.Object(listOf("width" to value(layer(line, "line"), "line-width"))),
                "pin" to FixtureValue.Object(listOf("iconSize" to value(pin, "icon-size", paint = false))),
                "placeFallbackGlyph" to glyph(fallback),
                "placeGlyph" to glyph(place),
                "sourceId" to FixtureValue.Text(place.source.id),
            )
    }

    /** A picked result as the fixture writes it, by its `kind`. */
    private fun highlight(written: FixtureValue.Object): SearchHighlight {
        fun numbers(key: String): List<Double>? =
            written[key]?.takeUnless { it is FixtureValue.Null }?.let { fixturePosition(it, key) }

        fun point(key: String): List<Double> = numbers(key) ?: throw AssertionError("the payload has no `$key`")
        return when (val kind = written.text("kind")) {
            "place" -> {
                SearchHighlight.Place(point("point"))
            }

            "poi" -> {
                SearchHighlight.Poi(point("point"), written.textOrNull("category")?.let { DataSchema.POICategory(it) })
            }

            "way" -> {
                SearchHighlight.Way(
                    point("anchor"),
                    numbers("bounds"),
                    (written["coordinates"] as? FixtureValue.Array)?.items?.map { fixturePosition(it, "coordinates") },
                )
            }

            else -> {
                throw AssertionError("`$kind` is no kind of picked result")
            }
        }
    }
}
