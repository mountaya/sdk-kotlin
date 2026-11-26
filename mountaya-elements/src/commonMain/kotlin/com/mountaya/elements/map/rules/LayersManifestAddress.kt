package com.mountaya.elements

/**
 * The layers manifest's path on the Tile API, the `manifestPath` `map/events-access-refused.json` declares:
 * the catalogue `MapEngine.loadLayers` and `MapEngine.setTerrain` read through the client.
 */
internal const val LAYERS_MANIFEST_PATH: String = "/v1/tile-layers"

/**
 * layersManifestAddress is the address the layers manifest is read at, which a refusal of it is told at
 * (`map.events.access-refused`): the Tile API's address as the client reaches it, [tilesBaseUrl], with the
 * manifest's path joined on and no slash doubled, every slash ending the address dropped first. The read goes
 * through the client, whose refusal carries a status and no address.
 */
internal fun layersManifestAddress(tilesBaseUrl: String): String = tilesBaseUrl.trimEnd('/') + LAYERS_MANIFEST_PATH
