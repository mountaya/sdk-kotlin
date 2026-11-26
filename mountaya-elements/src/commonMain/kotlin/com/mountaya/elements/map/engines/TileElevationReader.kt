package com.mountaya.elements

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlin.coroutines.CoroutineContext

/**
 * TileElevationReader reads a readout's elevation through [map]'s client's tile transport (`map.readout.open`), under
 * the credential the map draws its base map with, a publishable key carrying `tiles` or a share token, as every route a
 * map fetches for itself declares it: the source the layers catalogue names, its TileJSON document read at the address
 * the catalogue states, `elevation.url`, for its encoding, its deepest zoom and its tile template, and its tile size off
 * the catalogue's `elevation.tile_size`, read once for each catalogue the map holds; and each tile at the address the
 * template fills, its bytes, a WebP or a PNG image, read into pixels by [decode], the platform's own image codec, in
 * [decoding], a context off the thread the map is driven from, so a tile hundreds of pixels a side is never decoded on
 * the main thread and only the pixels land there. An address off the client's Tile API is read from nowhere, so the
 * map's credential never leaves for another host. It asks nothing of the Data API.
 */
internal class TileElevationReader(
    private val map: MapEngine,
    private val decoding: CoroutineContext,
    private val decode: (ByteArray, ElevationTile.Address, Int) -> ElevationTile?,
) : ElevationReader {
    /** A source read off a catalogue's document: the catalogue it was read for, the source and its tile template. */
    private class Held(
        val catalogue: TileSchema.Layers,
        val source: ElevationSource,
        val template: String,
    )

    /** The source read last, with the catalogue it was read for, or null before its document answered. */
    private var held: Held? = null

    @OptIn(InternalMountayaApi::class)
    override suspend fun source(): ElevationSource? {
        val catalogue = map.loadLayers()
        held?.takeIf { it.catalogue === catalogue }?.let { return it.source }
        val elevation = catalogue.elevation
        val document =
            try {
                Json.parseToJsonElement(
                    map.client
                        .mapFile(elevation.url, "getElevationTileJson")
                        .bytes
                        .decodeToString(),
                )
            } catch (malformed: IllegalArgumentException) {
                return null
            } as? JsonObject ?: return null
        val template =
            ((document["tiles"] as? JsonArray)?.firstOrNull() as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
                ?: return null
        val encoding =
            (document["encoding"] as? JsonPrimitive)?.takeIf { it.isString }?.content?.let(
                ::ElevationEncoding,
            )
        val zoom = (document["maxzoom"] as? JsonPrimitive)?.intOrNull ?: return null
        val source = ElevationSource(encoding, elevation.tile_size.value, zoom)
        held = Held(catalogue, source, template)
        return source
    }

    @OptIn(InternalMountayaApi::class)
    override suspend fun tile(address: ElevationTile.Address): ElevationTile {
        val read = held ?: error("a tile is read before its source")
        val filled =
            read.template
                .replace("{z}", "${address.z}")
                .replace("{x}", "${address.x}")
                .replace("{y}", "${address.y}")
        val answer = map.client.mapFile(Url(filled), "getElevationTile")
        return withContext(decoding) { decode(answer.bytes, address, read.source.tileSize) }
            ?: error("the elevation tile $address holds no image the device reads")
    }
}
