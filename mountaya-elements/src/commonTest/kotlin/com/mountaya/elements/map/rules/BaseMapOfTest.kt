package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BaseMapOfTest {
    private val terrains = listOf(BaseMapTerrain.outdoors, BaseMapTerrain.satellite)

    @Test
    fun aSlugNamesItsTerrainNoneTheFirstInTheVariantAskedFor() {
        val outdoors =
            assertNotNull(
                baseMapOf(terrains, "outdoors", TileSchema.TerrainVariant.Light),
                "the catalogue holds the base map",
            )
        assertEquals("outdoors", outdoors.first.slug)
        assertEquals("https://t.test/outdoors.json", outdoors.second.toString())
        val dark =
            assertNotNull(baseMapOf(terrains, null, TileSchema.TerrainVariant.Dark), "the catalogue holds the base map")
        assertEquals("outdoors", dark.first.slug)
        assertEquals("https://t.test/outdoors-dark.json", dark.second.toString())
        val shared =
            assertNotNull(
                baseMapOf(terrains, "satellite", TileSchema.TerrainVariant.Dark),
                "the catalogue holds the base map",
            )
        assertEquals("https://t.test/satellite.json", shared.second.toString())
        val unknown =
            assertNotNull(
                baseMapOf(terrains, "outdoors", TileSchema.TerrainVariant("dusk")),
                "the catalogue holds the base map",
            )
        assertEquals("https://t.test/outdoors.json", unknown.second.toString(), "light unless dark")
    }

    @Test
    fun aSlugTheCatalogueDoesNotHoldAndACatalogueOfNoTerrainNameNone() {
        assertNull(baseMapOf(terrains, "topo", TileSchema.TerrainVariant.Light))
        assertNull(baseMapOf(emptyList(), null, TileSchema.TerrainVariant.Light))
        assertNull(baseMapOf(terrains, "Outdoors", TileSchema.TerrainVariant.Light))
    }
}

/** The terrains of a catalogue the base map tests read, each by its slug and its two documents. */
internal object BaseMapTerrain {
    fun terrain(
        slug: String,
        light: String,
        dark: String,
    ): TileSchema.Terrain =
        Json.decodeFromString(
            TileSchema.Terrain.serializer(),
            """{"id": "$slug", "slug": "$slug", "locales": {}, "variants": {
              "light": {"style": {"url": "$light"}, "tiles": {"size": 512, "url": "https://t.test/l"}},
              "dark": {"style": {"url": "$dark"}, "tiles": {"size": 512, "url": "https://t.test/d"}}}}""",
        )

    val outdoors: TileSchema.Terrain =
        terrain("outdoors", "https://t.test/outdoors.json", "https://t.test/outdoors-dark.json")
    val satellite: TileSchema.Terrain =
        terrain("satellite", "https://t.test/satellite.json", "https://t.test/satellite.json")
}
