package com.mountaya.conformance

import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema

/** The names no layer rule reads, which every overlay and category a fixture writes is given. */
private val noLocales = TileSchema.Locales(de = null, en = null, es = null, fr = null, it = null, ja = null, zh = null)

/**
 * The tile layer [fields] writes, as the Tile API's `TileLayer`: its `source_layer`, `type`,
 * `zoom_min` and `zoom_max` as the fixture writes them, and a placeholder in every other member the
 * type requires, which no layer rule reads.
 */
internal fun fixtureTileLayer(fields: FixtureValue.Object): TileSchema.TileLayer =
    TileSchema.TileLayer(
        id = "layer",
        key = "",
        layout = null,
        paint = null,
        source_layer = fields.textOrNull("source_layer") ?: "layer",
        type =
            fields.textOrNull("type")?.let { TileSchema.TileLayerType(it) }
                ?: TileSchema.TileLayerType.Line,
        zoom_max = wholeOrNull(fields, "zoom_max") ?: 22,
        zoom_min = wholeOrNull(fields, "zoom_min") ?: 0,
    )

/** The tile source [fields] writes, its `layers` as the fixture writes them and a placeholder elsewhere. */
internal fun fixtureTileSource(fields: FixtureValue.Object): TileSchema.TileSource =
    TileSchema.TileSource(
        id = "source",
        layers =
            fields.items("layers").map {
                fixtureTileLayer(it as? FixtureValue.Object ?: throw AssertionError("a layer is not an object"))
            },
        tiles = emptyList(),
        type = TileSchema.TileSourceType.Vector,
        zoom_max = wholeOrNull(fields, "zoom_max") ?: 14,
        zoom_min = wholeOrNull(fields, "zoom_min") ?: 0,
    )

/** The overlay of the layers manifest [slug] names, [id] its id, with a placeholder in every other member. */
internal fun fixtureOverlay(
    slug: String,
    id: String,
): TileSchema.Overlay =
    TileSchema.Overlay(
        capabilities =
            TileSchema.OverlayCapabilities(
                client = TileSchema.OverlaySupportedCapabilities(timestamp = false),
                server = TileSchema.OverlaySupportedCapabilities(timestamp = false),
            ),
        content_type = TileSchema.OverlayContentType.Png,
        id = id,
        locales = noLocales,
        slug = slug,
        sources = emptyList(),
    )

/** The layers manifest holding [overlays], with a placeholder elevation, itinerary and zone layer. */
internal fun fixtureLayers(overlays: List<TileSchema.CategoryOverlays>): TileSchema.Layers =
    TileSchema.Layers(
        elevation =
            TileSchema.Elevation(
                id = "55763749-32b4-42d5-9f62-b6c51ef54d99",
                tile_size = TileSchema.ElevationTileSize.Size512,
                url = Url("https://tiles.example.invalid/elevation"),
            ),
        itineraries = fixtureOverlay("itineraries", "itineraries"),
        overlays = overlays,
        terrains = emptyList(),
        zones = fixtureOverlay("zones", "zones"),
    )

/** The category of the layers manifest [slug] names, holding [children]. */
internal fun fixtureCategory(
    slug: String,
    children: List<TileSchema.Overlay>,
): TileSchema.CategoryOverlays =
    TileSchema.CategoryOverlays(children = children, id = slug, locales = noLocales, slug = slug)
