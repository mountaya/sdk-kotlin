package com.mountaya.elements

import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema

/**
 * baseMapOf answers the base map a terrain slug names (`map.basemap.terrain`): the catalogue's terrain of
 * slug [slug], matched exactly, or its first terrain where [slug] is null, and the document of its
 * [variant], the dark one where [variant] is dark and the light one otherwise. A terrain whose two variants
 * are one document draws that document in either.
 *
 * Null where the catalogue holds no terrain of that slug, or none at all.
 */
internal fun baseMapOf(
    terrains: List<TileSchema.Terrain>,
    slug: String?,
    variant: TileSchema.TerrainVariant,
): Pair<TileSchema.Terrain, Url>? {
    val found = (if (slug == null) terrains.firstOrNull() else terrains.firstOrNull { it.slug == slug }) ?: return null
    val rendition =
        if (variant == TileSchema.TerrainVariant.Dark) found.variants.dark else found.variants.light
    return found to rendition.style.url
}
