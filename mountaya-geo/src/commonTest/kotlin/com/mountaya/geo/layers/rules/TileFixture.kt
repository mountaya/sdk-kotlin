package com.mountaya.geo

import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema

/**
 * TileFixture builds the Tile API's values a layer rule reads, with a placeholder in every member
 * the type requires and no rule here reads.
 */
internal object TileFixture {
    val noLocales: TileSchema.Locales =
        TileSchema.Locales(
            de = null,
            en = null,
            es = null,
            fr = null,
            it = null,
            ja = null,
            zh = null,
        )

    fun layer(
        sourceLayer: String,
        type: TileSchema.TileLayerType = TileSchema.TileLayerType.Line,
        zoomMin: Int = 0,
        zoomMax: Int = 22,
    ): TileSchema.TileLayer =
        TileSchema.TileLayer(
            id = "layer",
            key = "",
            layout = null,
            paint = null,
            source_layer = sourceLayer,
            type = type,
            zoom_max = zoomMax,
            zoom_min = zoomMin,
        )

    fun source(
        layers: List<TileSchema.TileLayer>,
        zoomMin: Int = 0,
        zoomMax: Int = 14,
    ): TileSchema.TileSource =
        TileSchema.TileSource(
            id = "source",
            layers = layers,
            tiles = emptyList(),
            type = TileSchema.TileSourceType.Vector,
            zoom_max = zoomMax,
            zoom_min = zoomMin,
        )

    fun overlay(
        slug: String,
        id: String = slug,
        locales: TileSchema.Locales = noLocales,
    ): TileSchema.Overlay =
        TileSchema.Overlay(
            capabilities =
                TileSchema.OverlayCapabilities(
                    client = TileSchema.OverlaySupportedCapabilities(timestamp = false),
                    server = TileSchema.OverlaySupportedCapabilities(timestamp = false),
                ),
            content_type = TileSchema.OverlayContentType.Png,
            id = id,
            locales = locales,
            slug = slug,
            sources = emptyList(),
        )

    fun category(
        slug: String,
        children: List<TileSchema.Overlay>,
        locales: TileSchema.Locales = noLocales,
    ): TileSchema.CategoryOverlays =
        TileSchema.CategoryOverlays(children = children, id = slug, locales = locales, slug = slug)

    fun layers(
        overlays: List<TileSchema.CategoryOverlays>,
        terrains: List<TileSchema.Terrain> = emptyList(),
    ): TileSchema.Layers =
        TileSchema.Layers(
            elevation =
                TileSchema.Elevation(
                    id = "55763749-32b4-42d5-9f62-b6c51ef54d99",
                    tile_size = TileSchema.ElevationTileSize.Size512,
                    url = Url("https://tiles.example.invalid/elevation"),
                ),
            itineraries = overlay("itineraries"),
            overlays = overlays,
            terrains = terrains,
            zones = overlay("zones"),
        )

    /** The locales of an entry named [english] in English and [french] in French. */
    fun names(
        english: String? = null,
        french: String? = null,
    ): TileSchema.Locales =
        TileSchema.Locales(
            de = null,
            en = english?.let { mapOf("name" to it) },
            es = null,
            fr = french?.let { mapOf("name" to it) },
            it = null,
            ja = null,
            zh = null,
        )

    /** The terrain [slug] names, under [locales], its variants placeholders no rule here reads. */
    fun terrain(
        slug: String,
        locales: TileSchema.Locales = noLocales,
    ): TileSchema.Terrain {
        val variant =
            TileSchema.TerrainVariantConfig(
                style = TileSchema.TerrainVariantAsStyle(url = Url("https://tiles.example.invalid/$slug/style")),
                tiles =
                    TileSchema.TerrainVariantAsTiles(
                        size = TileSchema.TerrainTileSize.Size512,
                        url = Url("https://tiles.example.invalid/$slug/tiles"),
                    ),
            )
        return TileSchema.Terrain(
            id = slug,
            locales = locales,
            slug = slug,
            variants = TileSchema.TerrainVariants(dark = variant, light = variant),
        )
    }
}
