package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class LayersManifestAddressTest {
    @Test
    fun theManifestsPathIsJoinedOntoTheTileApisAddressNoSlashDoubled() {
        assertEquals("https://tiles.test/v1/tile-layers", layersManifestAddress("https://tiles.test"))
        assertEquals("https://tiles.test/v1/tile-layers", layersManifestAddress("https://tiles.test/"))
        assertEquals("https://tiles.test/v1/tile-layers", layersManifestAddress("https://tiles.test//"))
        assertEquals("/tiles/v1/tile-layers", layersManifestAddress("/tiles"))
        assertEquals("/v1/tile-layers", layersManifestAddress(""))
        assertEquals("/v1/tile-layers", LAYERS_MANIFEST_PATH)
    }
}
