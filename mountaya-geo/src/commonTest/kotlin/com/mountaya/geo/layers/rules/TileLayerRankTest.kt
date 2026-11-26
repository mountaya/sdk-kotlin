package com.mountaya.geo

import com.mountaya.client.generated.TileSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class TileLayerRankTest {
    @Test
    fun aLayerDrawsAtTheRankItsSourceLayerTakes() {
        assertEquals(50, tileLayerRank(TileFixture.layer("itinerary_lines")))
        assertEquals(90, tileLayerRank(TileFixture.layer("itinerary_points")))
        assertEquals(55, tileLayerRank(TileFixture.layer("itinerary_restrictions")))
        assertEquals(40, tileLayerRank(TileFixture.layer("fills", TileSchema.TileLayerType.Fill)))
        for (layer in poiSourceLayers) assertEquals(80, tileLayerRank(TileFixture.layer(layer)), layer)
    }

    @Test
    fun aHillshadeIsTheTerrainRankWhateverItsSourceLayer() {
        val shade = TileFixture.layer("itinerary_lines", TileSchema.TileLayerType.Hillshade)
        assertEquals(LayerRank.Hillshade.rank, tileLayerRank(shade))
    }

    /** Matched exactly: another case is another source layer, and one the table does not know is an overlay. */
    @Test
    fun aSourceLayerTheTableDoesNotKnowIsAnOverlay() {
        assertEquals(20, tileLayerRank(TileFixture.layer("not_a_layer")))
        assertEquals(20, tileLayerRank(TileFixture.layer("Itinerary_Lines")))
    }

    @Test
    fun aConsumerPatchesTheTable() {
        val patched = tileSourceLayerRanks + ("my_overlay" to LayerRank.Poi.rank + 2)
        assertEquals(82, tileLayerRank(TileFixture.layer("my_overlay"), patched))
        assertEquals(80, tileLayerRank(TileFixture.layer("poi_food"), patched))
        assertEquals(90, tileLayerRank(TileFixture.layer("itinerary_points"), patched))
    }
}
