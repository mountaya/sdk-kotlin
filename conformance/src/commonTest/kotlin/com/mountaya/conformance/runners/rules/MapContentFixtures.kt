package com.mountaya.conformance

import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json

/**
 * MapContentFixtures builds what the map's content fixtures write, the Tile API's specifications, catalogue
 * and terrains, as the client receives them, and writes the sources and layers a map draws back as MapLibre's
 * style specification writes them, every value through the published surface.
 */
internal object MapContentFixtures {
    private val json = Json

    /**
     * [source] as MapLibre's style specification writes it: `{type, tiles, minzoom, maxzoom}`, with `tileSize`
     * for a raster one that states it and its `attribution` where it states one.
     */
    fun styleSource(source: MapSource): FixtureValue {
        val members = mutableListOf<Pair<String, FixtureValue>>()
        source.attribution?.let { members += "attribution" to FixtureValue.Text(it) }

        fun tiled(
            type: String,
            tiles: List<String>,
            size: Int?,
            zoom: com.mountaya.geo.ZoomBounds,
        ) {
            members += "maxzoom" to FixtureValue.Number(zoom.maxZoom)
            members += "minzoom" to FixtureValue.Number(zoom.minZoom)
            size?.let { members += "tileSize" to FixtureValue.Number(it.toDouble()) }
            members += "tiles" to textArray(tiles)
            members += "type" to FixtureValue.Text(type)
        }
        when (val kind = source.kind) {
            is MapSource.Kind.Vector -> tiled("vector", kind.tiles, null, kind.zoom)
            is MapSource.Kind.Raster -> tiled("raster", kind.tiles, kind.tileSize, kind.zoom)
            is MapSource.Kind.RasterDem -> tiled("raster-dem", kind.tiles, kind.tileSize, kind.zoom)
            is MapSource.Kind.GeoJson -> members += "type" to FixtureValue.Text("geojson")
        }
        return FixtureValue.Object(members)
    }

    /**
     * [layer] as MapLibre's style specification writes it: `{id, type, source, source-layer, filter, layout,
     * paint, minzoom, maxzoom}`, each member it does not state left out, an empty paint and an empty layout
     * among them.
     */
    fun styleLayer(layer: MapLayer): FixtureValue {
        val members = mutableListOf<Pair<String, FixtureValue>>()
        layer.filter?.let { members += "filter" to fixtureValueOf(it) }
        members += "id" to FixtureValue.Text(layer.id)
        if (layer.layout.isNotEmpty()) members += "layout" to properties(layer.layout)
        layer.zoom?.let {
            members += "maxzoom" to FixtureValue.Number(it.maxZoom)
            members += "minzoom" to FixtureValue.Number(it.minZoom)
        }
        if (layer.paint.isNotEmpty()) members += "paint" to properties(layer.paint)
        members += "source" to FixtureValue.Text(layer.source)
        layer.sourceLayer?.let { members += "source-layer" to FixtureValue.Text(it) }
        members += "type" to FixtureValue.Text(layer.type.value)
        return FixtureValue.Object(members)
    }

    /**
     * One vector source's specification of the [layers] a case names, as the Tile API answers one: each with its
     * `id`, its `key`, its id where the case names none, its `source_layer` and its `type`, at every zoom.
     */
    fun specification(
        name: String,
        layers: List<FixtureValue>,
    ): TileSchema.TileSpecification =
        TileSchema.TileSpecification(
            listOf(
                TileSchema.TileSource(
                    id = name,
                    layers =
                        layers.map { written ->
                            val layer =
                                written as? FixtureValue.Object ?: throw AssertionError("a layer is not an object")
                            val id = layer.text("id")
                            TileSchema.TileLayer(
                                id,
                                key = layer.textOrNull("key") ?: id,
                                layout = null,
                                paint = null,
                                source_layer = layer.text("source_layer"),
                                type = TileSchema.TileLayerType(layer.text("type")),
                                zoom_max = 22,
                                zoom_min = 0,
                            )
                        },
                    tiles = listOf("https://tiles.test/$name/{z}/{x}/{y}"),
                    type = TileSchema.TileSourceType.Vector,
                    zoom_max = 22,
                    zoom_min = 0,
                ),
            ),
        )

    /** The body the Tile API answers [specification] with, `{"data": ...}`. */
    fun specificationBody(specification: TileSchema.TileSpecification): String =
        "{\"data\": " + json.encodeToString(TileSchema.TileSpecification.serializer(), specification) + "}"

    /** The specification [written] states, read by the Tile API's own decoder. */
    fun decodedSpecification(written: FixtureValue): TileSchema.TileSpecification =
        json.decodeFromJsonElement(TileSchema.TileSpecification.serializer(), fixtureJson(written))

    /** The source [written] states, read by the Tile API's own decoder. */
    fun decodedSource(written: FixtureValue): TileSchema.TileSource =
        json.decodeFromJsonElement(TileSchema.TileSource.serializer(), fixtureJson(written))

    /** An overlay of the layers catalogue drawing one source of one fill layer named after its [slug], at every zoom. */
    private fun overlay(slug: String): String =
        """{"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
          "content_type": "application/x-protobuf", "id": "$slug", "locales": {}, "slug": "$slug",
          "sources": [{"id": "$slug-source", "layers": [{"id": "$slug-layer", "key": "$slug",
            "layout": null, "paint": null, "source_layer": "$slug", "type": "fill", "zoom_max": 22,
            "zoom_min": 0}], "tiles": ["https://tiles.test/$slug/{z}/{x}/{y}"], "type": "vector",
            "zoom_max": 22, "zoom_min": 0}]}"""

    /** A terrain of the layers catalogue, by its [slug] and the address of each variant's style. */
    fun terrain(
        slug: String,
        light: String,
        dark: String,
    ): String =
        """{"id": "$slug", "slug": "$slug", "locales": {}, "variants": {
          "light": {"style": {"url": "$light"},
            "tiles": {"size": 512, "url": "https://tiles.test/$slug/light/tiles.json"}},
          "dark": {"style": {"url": "$dark"},
            "tiles": {"size": 512, "url": "https://tiles.test/$slug/dark/tiles.json"}}}}"""

    /**
     * The body the Tile API answers the layers catalogue with: [terrains] and the [classic] and [enrichment]
     * overlays given, its elevation named [mark], which tells one read's catalogue from another's.
     */
    fun catalogueBody(
        mark: String = "elevation",
        terrains: List<String> = emptyList(),
        classic: List<String> = emptyList(),
        enrichment: List<String> = emptyList(),
    ): String {
        val empty =
            """{"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
              "content_type": "application/x-protobuf", "id": "none", "locales": {}, "slug": "none", "sources": []}"""
        val overlays =
            """[{"children": [${classic.joinToString(",", transform = ::overlay)}], "id": "classic",
              "locales": {}, "slug": "conditions"},
             {"children": [${enrichment.joinToString(",", transform = ::overlay)}], "id": "stacked",
              "locales": {}, "slug": "enrichment"}]"""
        return """{"data": {
          "elevation": {"id": "$mark", "tile_size": 512, "url": "https://tiles.test/elevation.json"},
          "itineraries": $empty, "zones": $empty, "overlays": $overlays,
          "terrains": [${terrains.joinToString(",")}]}}"""
    }

    private fun properties(members: Map<String, kotlinx.serialization.json.JsonElement>): FixtureValue =
        FixtureValue.Object(members.entries.sortedBy { it.key }.map { it.key to fixtureValueOf(it.value) })

    /** A response of [status] carrying [body], JSON. */
    fun response(
        status: Int,
        body: String,
    ): ExchangeResponse =
        ExchangeResponse(status, mapOf("Content-Type" to "application/json"), ExchangeBody(body.encodeToByteArray()))

    /** Serving is an exchange that answers every request at once, with the status and the body [answer] gives for it, and records each one. */
    class Serving(
        private val answer: (ExchangeRequest) -> Pair<Int, String>,
    ) : HttpExchange {
        /** Every request sent, in order. */
        val sent = mutableListOf<ExchangeRequest>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += request
            val (status, body) = answer(request)
            return response(status, body)
        }
    }

    /**
     * Held is an exchange that holds every request until a step of a script answers it, in the order the
     * requests were sent, and fails one with the cancellation of the coroutine that sent it the moment that
     * coroutine is cancelled, which it records.
     */
    class Held : HttpExchange {
        /** One request sent, and the answer it waits on while it is held. */
        class Read(
            val request: ExchangeRequest,
        ) {
            internal val answer = CompletableDeferred<ExchangeResponse>()
            var cancelled: Boolean = false
                internal set
        }

        /** Every request sent, in order. */
        val reads = mutableListOf<Read>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val read = Read(request)
            reads += read
            try {
                return read.answer.await()
            } catch (cancelled: CancellationException) {
                read.cancelled = true
                throw cancelled
            }
        }

        /** Lets the read [index] answer with [body], a success. */
        fun answer(
            index: Int,
            body: String,
        ) {
            settle(index).complete(response(200, body))
        }

        /** Lets the read [index] answer a 404, which no attempt repeats. */
        fun fail(index: Int) {
            settle(index).complete(response(404, """{"errors": [{"code": "NOT_FOUND", "message": "not found"}]}"""))
        }

        /** Fails every read still held, as a script ends, so no read waits for ever. */
        fun release() {
            for (index in reads.indices) if (isHeld(index)) fail(index)
        }

        /** Whether the read [index] waits on an answer. */
        fun isHeld(index: Int): Boolean =
            index in reads.indices && !reads[index].answer.isCompleted && !reads[index].cancelled

        private fun settle(index: Int): CompletableDeferred<ExchangeResponse> {
            if (!isHeld(index)) throw AssertionError("read $index was never sent, or answered already")
            return reads[index].answer
        }
    }
}
