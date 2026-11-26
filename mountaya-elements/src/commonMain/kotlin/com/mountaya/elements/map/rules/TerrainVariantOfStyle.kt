package com.mountaya.elements

import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema

/**
 * terrainVariantOfStyle answers which variant of [terrain] the document at [style] is, or null where the
 * terrain's two variants are that one document, or neither is.
 */
internal fun terrainVariantOfStyle(
    terrain: TileSchema.Terrain,
    style: Url?,
): TileSchema.TerrainVariant? {
    val light = terrain.variants.light.style.url == style
    val dark = terrain.variants.dark.style.url == style
    return when {
        light && !dark -> TileSchema.TerrainVariant.Light
        dark && !light -> TileSchema.TerrainVariant.Dark
        else -> null
    }
}
