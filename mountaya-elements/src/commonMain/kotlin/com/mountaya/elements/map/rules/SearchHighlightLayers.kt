package com.mountaya.elements

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.GeoJsonFeature
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayerRank
import com.mountaya.geo.SearchHighlight
import com.mountaya.geo.poiStyleOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** The source a map draws a picked search result from, and the stem of its layers' ids, each the source's followed by its role. */
internal const val SEARCH_HIGHLIGHT_SOURCE_ID: String = "mountaya-search-highlight"

/** The glyph a place's pin carries. */
internal const val SEARCH_HIGHLIGHT_PLACE_GLYPH: String = "point"

/** The glyph a place's pin carries under a glyph set that holds no point. */
internal const val SEARCH_HIGHLIGHT_FALLBACK_GLYPH: String = "map-pin"

/** The size a pin's sprite is drawn at, the size the tiles draw a point of interest at, so the two overlap where both are drawn. */
internal const val SEARCH_HIGHLIGHT_PIN_SIZE: Double = 0.7

/** The blur of the halo under a way's line, a soft wash of the accent that lifts it off whatever it runs over, in pixels. */
internal const val SEARCH_HIGHLIGHT_HALO_BLUR: Double = 6.0

/** The opacity of the halo under a way's line. */
internal const val SEARCH_HIGHLIGHT_HALO_OPACITY: Double = 0.25

/** The width of the halo under a way's line, in pixels. */
internal const val SEARCH_HIGHLIGHT_HALO_WIDTH: Double = 16.0

/** The colour of the white casing that parts a way's line from the tiles' own lines. */
internal const val SEARCH_HIGHLIGHT_CASING_COLOR: String = "#ffffff"

/** The width of the casing under a way's line, in pixels. */
internal const val SEARCH_HIGHLIGHT_CASING_WIDTH: Double = 8.0

/** The width of a way's line, in the accent, in pixels. */
internal const val SEARCH_HIGHLIGHT_LINE_WIDTH: Double = 5.0

/** The radius of the dot at each end of a way's line, in pixels. */
internal const val SEARCH_HIGHLIGHT_END_RADIUS: Double = 5.0

/** The width of the white ring around the dot at each end of a way's line, in pixels. */
internal const val SEARCH_HIGHLIGHT_END_STROKE_WIDTH: Double = 2.0

/**
 * The dash a way's extent is outlined with, a faint wash of the accent outlined dashed and thinner than a line, so it
 * reads as an approximate extent rather than the way itself.
 */
internal val searchHighlightExtentDash: List<Double> = listOf(2.0, 2.0)

/** The opacity of the wash over a way's extent. */
internal const val SEARCH_HIGHLIGHT_EXTENT_FILL_OPACITY: Double = 0.08

/** The width of the dashed outline of a way's extent, in pixels. */
internal const val SEARCH_HIGHLIGHT_EXTENT_WIDTH: Double = 2.0

/** A turn, in degrees: a box across the antimeridian is traced with its east edge a turn further east. */
private const val TURN_DEGREES = 360.0

/**
 * searchHighlightLayers answers what a map draws for [highlight], the result a reader picked from a search, in
 * [accent] (`map.search.result-highlight`): the features of its one source, and its layers in the order they stack,
 * every one at the chrome band, `LayerRank.Chrome`, above every content band.
 *
 * - A place is a pin, the marker sprite of [SEARCH_HIGHLIGHT_PLACE_GLYPH] in the accent, or of
 *   [SEARCH_HIGHLIGHT_FALLBACK_GLYPH] where the map's glyph set holds no point, [hasPointGlyph], named as `poi-icon-`
 *   followed by the glyph, a hyphen and the colour's six small hexadecimal digits, drawn whatever it overlaps
 *   (`map.icons.marker-sprite`).
 * - A point of interest is the pin of its category's own glyph in its category's own colour (`geo.layers.poi-style`),
 *   its feature carrying its `category`; one of no category the table styles is pinned as a place.
 * - A way with a line of two positions or more is that line under a halo in the accent and a white casing, with a dot
 *   at each of its two ends, which read the collection's points alone, since a circle is drawn at every position of
 *   whatever it reads; else its extent, a rectangle washed in the accent and outlined dashed, traced with its east
 *   edge a turn further east where its west edge lies east of its east one, so it runs the short way across the
 *   antimeridian; else, where the extent has an edge at no finite coordinate, no height, or no width once traced, a
 *   place's pin at its anchor.
 */
internal fun searchHighlightLayers(
    highlight: SearchHighlight,
    accent: HexColor,
    hasPointGlyph: Boolean,
): Pair<GeoJsonFeatureCollection, List<MapLayer>> {
    fun placePin(point: List<Double>) =
        pin(
            "place",
            point,
            if (hasPointGlyph) SEARCH_HIGHLIGHT_PLACE_GLYPH else SEARCH_HIGHLIGHT_FALLBACK_GLYPH,
            accent.hex,
            JsonObject(emptyMap()),
        )
    return when (highlight) {
        is SearchHighlight.Place -> {
            placePin(highlight.point)
        }

        is SearchHighlight.Poi -> {
            val category = highlight.category
            val style = category?.let(::poiStyleOf)
            if (category == null || style == null) {
                placePin(highlight.point)
            } else {
                pin(
                    "poi",
                    highlight.point,
                    style.glyph,
                    style.color.hex,
                    JsonObject(
                        mapOf("category" to JsonPrimitive(category.value)),
                    ),
                )
            }
        }

        is SearchHighlight.Way -> {
            val coordinates = highlight.coordinates
            val extent = tracedExtent(highlight.bounds)
            when {
                coordinates != null && coordinates.size >= 2 -> wayLine(coordinates, accent.hex)
                extent != null -> wayExtent(extent, accent.hex)
                else -> placePin(highlight.anchor)
            }
        }
    }
}

/** A pin of [glyph] on a disc of [color] at [point], its feature carrying [properties], as the layer of [role]. */
private fun pin(
    role: String,
    point: List<Double>,
    glyph: String,
    color: String,
    properties: JsonObject,
): Pair<GeoJsonFeatureCollection, List<MapLayer>> =
    GeoJsonFeatureCollection(listOf(GeoJsonFeature(GeoJsonPoint(point), properties))) to
        listOf(
            MapLayer(
                id = "$SEARCH_HIGHLIGHT_SOURCE_ID-$role",
                type = TileSchema.TileLayerType.Symbol,
                source = SEARCH_HIGHLIGHT_SOURCE_ID,
                rank = LayerRank.Chrome.rank,
                layout =
                    linkedMapOf(
                        "icon-allow-overlap" to JsonPrimitive(true),
                        "icon-ignore-placement" to JsonPrimitive(true),
                        "icon-image" to JsonPrimitive(poiIconImageId(glyph, color)),
                        "icon-size" to JsonPrimitive(SEARCH_HIGHLIGHT_PIN_SIZE),
                    ),
            ),
        )

/** A way's line, haloed and cased, with a dot at each of its two ends, the dots reading the collection's points alone. */
private fun wayLine(
    coordinates: List<List<Double>>,
    accent: String,
): Pair<GeoJsonFeatureCollection, List<MapLayer>> {
    val none = JsonObject(emptyMap())
    val features =
        listOf(GeoJsonFeature(GeoJsonLineString(coordinates), none)) +
            listOf(coordinates.first(), coordinates.last()).map { GeoJsonFeature(GeoJsonPoint(it), none) }
    val rounded: Map<String, JsonElement> =
        linkedMapOf(
            "line-cap" to JsonPrimitive("round"),
            "line-join" to JsonPrimitive("round"),
        )
    return GeoJsonFeatureCollection(features) to
        listOf(
            layer(
                "halo",
                TileSchema.TileLayerType.Line,
                paint =
                    linkedMapOf(
                        "line-blur" to JsonPrimitive(SEARCH_HIGHLIGHT_HALO_BLUR),
                        "line-color" to JsonPrimitive(accent),
                        "line-opacity" to JsonPrimitive(SEARCH_HIGHLIGHT_HALO_OPACITY),
                        "line-width" to JsonPrimitive(SEARCH_HIGHLIGHT_HALO_WIDTH),
                    ),
                layout = rounded,
            ),
            layer(
                "casing",
                TileSchema.TileLayerType.Line,
                paint =
                    linkedMapOf(
                        "line-color" to JsonPrimitive(SEARCH_HIGHLIGHT_CASING_COLOR),
                        "line-width" to JsonPrimitive(SEARCH_HIGHLIGHT_CASING_WIDTH),
                    ),
                layout = rounded,
            ),
            layer(
                "line",
                TileSchema.TileLayerType.Line,
                paint =
                    linkedMapOf(
                        "line-color" to JsonPrimitive(accent),
                        "line-width" to JsonPrimitive(SEARCH_HIGHLIGHT_LINE_WIDTH),
                    ),
                layout = rounded,
            ),
            layer(
                "endpoints",
                TileSchema.TileLayerType.Circle,
                paint =
                    linkedMapOf(
                        "circle-color" to JsonPrimitive(accent),
                        "circle-radius" to JsonPrimitive(SEARCH_HIGHLIGHT_END_RADIUS),
                        "circle-stroke-color" to JsonPrimitive(SEARCH_HIGHLIGHT_CASING_COLOR),
                        "circle-stroke-width" to JsonPrimitive(SEARCH_HIGHLIGHT_END_STROKE_WIDTH),
                    ),
                filter = expression("==", expression("geometry-type"), JsonPrimitive("Point")),
            ),
        )
}

/** A way's extent as traced, `[west, south, east, north]`, washed in the accent and outlined dashed. */
private fun wayExtent(
    extent: List<Double>,
    accent: String,
): Pair<GeoJsonFeatureCollection, List<MapLayer>> {
    val (west, south, east, north) = extent
    val ring =
        listOf(listOf(west, south), listOf(east, south), listOf(east, north), listOf(west, north), listOf(west, south))
    return GeoJsonFeatureCollection(listOf(GeoJsonFeature(GeoJsonPolygon(listOf(ring)), JsonObject(emptyMap())))) to
        listOf(
            layer(
                "bounds-fill",
                TileSchema.TileLayerType.Fill,
                paint =
                    linkedMapOf(
                        "fill-color" to JsonPrimitive(accent),
                        "fill-opacity" to JsonPrimitive(SEARCH_HIGHLIGHT_EXTENT_FILL_OPACITY),
                    ),
            ),
            layer(
                "bounds",
                TileSchema.TileLayerType.Line,
                paint =
                    linkedMapOf(
                        "line-color" to JsonPrimitive(accent),
                        "line-dasharray" to JsonArray(searchHighlightExtentDash.map { JsonPrimitive(it) }),
                        "line-width" to JsonPrimitive(SEARCH_HIGHLIGHT_EXTENT_WIDTH),
                    ),
            ),
        )
}

/** A layer of the highlight's source at the chrome band, as the layer of [role]. */
private fun layer(
    role: String,
    type: TileSchema.TileLayerType,
    paint: Map<String, JsonElement> = emptyMap(),
    layout: Map<String, JsonElement> = emptyMap(),
    filter: JsonElement? = null,
): MapLayer =
    MapLayer(
        id = "$SEARCH_HIGHLIGHT_SOURCE_ID-$role",
        type = type,
        source = SEARCH_HIGHLIGHT_SOURCE_ID,
        rank = LayerRank.Chrome.rank,
        paint = paint,
        layout = layout,
        filter = filter,
    )

/**
 * A way's extent, [bounds], as the rectangle the highlight traces, or null where it traces none and the way is pinned
 * at its anchor.
 *
 * A box whose west edge lies east of its east edge crosses the antimeridian, as GeoJSON writes one, and is traced with
 * its east edge a turn further east, past 180, so the ring runs the short way over the line. A box that is not four
 * finite numbers, a south edge not south of its north edge, or an east edge as traced not east of its west edge is
 * none: the last holds two meridians that are one however they are written, 6 and 6, 180 and -180, an outline nobody
 * sees.
 */
private fun tracedExtent(bounds: List<Double>?): List<Double>? {
    if (bounds == null || bounds.size != 4 || !bounds.all { it.isFinite() }) return null
    val (west, south, east, north) = bounds
    val tracedEast = if (west > east) east + TURN_DEGREES else east
    if (south >= north || tracedEast <= west) return null
    return listOf(west, south, tracedEast, north)
}
