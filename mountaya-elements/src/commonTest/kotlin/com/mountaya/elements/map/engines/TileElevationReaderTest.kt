package com.mountaya.elements

import com.mountaya.client.ConfigurationException
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class TileElevationReaderTest {
    /** Answers the catalogue, the elevation source's document and each tile, recording every request. */
    private class Elevation(
        private val document: String,
    ) : HttpExchange {
        val sent = mutableListOf<ExchangeRequest>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += request
            val path = request.url.toString().substringAfter("mountaya.com")
            val body =
                when {
                    path == "/v1/elevation/tiles.json" -> document
                    path.startsWith("/v1/elevation/") -> "tile $path"
                    else -> CATALOGUE
                }
            return ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(body.encodeToByteArray()),
            )
        }
    }

    private fun map(exchange: HttpExchange): MapEngine =
        MapEngine(
            Mountaya(
                Credential.Publishable("pk_test_tiles", SessionToken.Value("sess_tiles")),
                exchange = exchange,
                dispatcher = Dispatchers.Unconfined,
            ),
            geolocation = InertGeolocation(),
        )

    @Test
    fun theSourceIsReadOffTheDocumentAtTheCataloguesAddressAndItsTileSizeOncePerCatalogue() =
        runTest {
            val exchange =
                Elevation("""{"tilejson": "3.0.0", "encoding": "terrarium", "maxzoom": 15, "tiles": ["$TEMPLATE"]}""")
            val map = map(exchange)
            val reader = TileElevationReader(map, Dispatchers.Unconfined) { _, _, _ -> null }

            assertEquals(ElevationSource(ElevationEncoding.Terrarium, 512, 15), reader.source())
            assertEquals(ElevationSource(ElevationEncoding.Terrarium, 512, 15), reader.source())
            assertEquals(
                listOf(
                    "https://tiles.mountaya.com/v1/tile-layers",
                    "https://tiles.mountaya.com/v1/elevation/tiles.json",
                ),
                exchange.sent.map { it.url.toString() },
            )

            // A catalogue read again, for another studio scope, is a source read again.
            map.setLayersOrganization("00000000-0000-4000-8000-000000000001")
            reader.source()
            assertEquals(
                "https://tiles.mountaya.com/v1/elevation/tiles.json",
                exchange.sent
                    .last()
                    .url
                    .toString(),
            )
            assertEquals(4, exchange.sent.size)
        }

    @Test
    fun aDocumentNamingNoZoomOrNoTileTemplateGivesNoSource() =
        runTest {
            val zoomless =
                TileElevationReader(
                    map(Elevation("""{"tilejson": "3.0.0", "tiles": ["$TEMPLATE"]}""")),
                    Dispatchers.Unconfined,
                ) { _, _, _ -> null }
            val templateless =
                TileElevationReader(
                    map(Elevation("""{"tilejson": "3.0.0", "maxzoom": 15}""")),
                    Dispatchers.Unconfined,
                ) { _, _, _ -> null }

            assertNull(zoomless.source())
            assertNull(templateless.source())
        }

    @Test
    fun aTileIsReadAtTheAddressTheTemplateFillsUnderTheMapsCredentialAndDecodedByThePlatform() =
        runTest {
            val exchange = Elevation("""{"maxzoom": 16, "tiles": ["$TEMPLATE"]}""")
            val address = ElevationTile.Address(16, 34017, 23335)
            var decoded: String? = null
            val reader =
                TileElevationReader(map(exchange), Dispatchers.Unconfined) { bytes, at, size ->
                    decoded = bytes.decodeToString()
                    ElevationTile(at, size, IntArray(size * size))
                }
            reader.source()
            val tile = reader.tile(address)

            assertEquals(address, tile.address)
            assertEquals("tile /v1/elevation/16/34017/23335.webp", decoded)
            val request = exchange.sent.last()
            assertEquals("https://tiles.mountaya.com/v1/elevation/16/34017/23335.webp", request.url.toString())
            assertEquals("pk_test_tiles", request.headers["X-API-Key"])
            assertEquals("GET", request.method)
        }

    @Test
    fun aTemplateOffTheTileApiIsReadFromNowhere() =
        runTest {
            val exchange = Elevation("""{"maxzoom": 16, "tiles": ["https://elevation.example.com/{z}/{x}/{y}.webp"]}""")
            val reader =
                TileElevationReader(map(exchange), Dispatchers.Unconfined) { _, at, size ->
                    ElevationTile(at, size, IntArray(size * size))
                }
            reader.source()
            val sent = exchange.sent.size

            assertFailsWith<ConfigurationException> { reader.tile(ElevationTile.Address(16, 34017, 23335)) }
            assertEquals(sent, exchange.sent.size)
        }

    /** Runs each block it is handed at once, and tells whether one is running, as a dispatcher of its own thread does. */
    private class Decoding : CoroutineDispatcher() {
        var isRunning = false

        override fun dispatch(
            context: CoroutineContext,
            block: Runnable,
        ) {
            isRunning = true
            try {
                block.run()
            } finally {
                isRunning = false
            }
        }
    }

    @Test
    fun aTileIsDecodedInTheContextTheReaderIsHandedOffTheThreadTheMapIsDrivenFrom() =
        runTest {
            val decoding = Decoding()
            var isDecodedThere: Boolean? = null
            val reader =
                TileElevationReader(
                    map(Elevation("""{"maxzoom": 16, "tiles": ["$TEMPLATE"]}""")),
                    decoding,
                ) { _, at, size ->
                    isDecodedThere = decoding.isRunning
                    ElevationTile(at, size, IntArray(size * size))
                }
            reader.source()
            reader.tile(ElevationTile.Address(16, 34017, 23335))

            assertEquals(true, isDecodedThere, "the image is decoded in the context handed for it")
            assertFalse(decoding.isRunning, "the pixels land back on the caller's thread")
        }

    private companion object {
        /** The tile template the elevation source's document states, on the Tile API. */
        const val TEMPLATE = "https://tiles.mountaya.com/v1/elevation/{z}/{x}/{y}.webp"

        const val CATALOGUE = """{"data": {
            "elevation": {"id": "55763749-32b4-42d5-9f62-b6c51ef54d99", "tile_size": 512, "url": "https://tiles.mountaya.com/v1/elevation/tiles.json"},
            "itineraries": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
              "content_type": "application/x-protobuf", "id": "i", "locales": {}, "slug": "itineraries", "sources": []},
            "zones": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
              "content_type": "application/x-protobuf", "id": "z", "locales": {}, "slug": "zones", "sources": []},
            "overlays": [], "terrains": []}}"""
    }
}
