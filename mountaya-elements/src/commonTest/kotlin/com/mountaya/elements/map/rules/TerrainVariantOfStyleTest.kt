package com.mountaya.elements

import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TerrainVariantOfStyleTest {
    @Test
    fun aDocumentIsTheVariantItIsAndNoneWhereTheTwoVariantsShareItOrNeitherIs() {
        assertEquals(
            TileSchema.TerrainVariant.Light,
            terrainVariantOfStyle(BaseMapTerrain.outdoors, Url("https://t.test/outdoors.json")),
        )
        assertEquals(
            TileSchema.TerrainVariant.Dark,
            terrainVariantOfStyle(BaseMapTerrain.outdoors, Url("https://t.test/outdoors-dark.json")),
        )
        assertNull(terrainVariantOfStyle(BaseMapTerrain.satellite, Url("https://t.test/satellite.json")))
        assertNull(terrainVariantOfStyle(BaseMapTerrain.outdoors, Url("https://t.test/other.json")))
        assertNull(terrainVariantOfStyle(BaseMapTerrain.outdoors, null))
    }
}
