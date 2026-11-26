package com.mountaya.elements

import com.mountaya.client.Url
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.Collections

/**
 * StubTileApi is the Tile API and another tile host, as MapLibre's own loader reaches them in these
 * tests: an interceptor of the app's own `OkHttpClient`, which the map's request hook runs ahead of, so
 * it reads each request as it would leave the device, records it, and answers it, and no test reaches
 * the network.
 *
 * `https://tiles.example.test` is the Tile API the tests' clients name, and
 * `https://elsewhere.example.test` a host a style names that is not Mountaya's. The base map's style is
 * served at [styleUrl], and at any other address ending in `/style`, its GeoJSON source `style-huts` holding two
 * features no layer of its own draws, one of id 5, a number, and one of id "6", a text; a tile under `/refused/` is refused a
 * 402, as the Tile API refuses one for want of an entitlement; anything under `/missing/`, a style included,
 * is answered 404, a document that does not arrive; every other request to either is answered with no
 * content, and one to any other host fails.
 */
internal object StubTileApi : Interceptor {
    /** The Tile API's origin the tests' clients reach. */
    val tiles = Url("https://tiles.example.test")

    /** The base map's style document, on the Tile API. */
    val styleUrl = Url("https://tiles.example.test/v1/terrains/outdoor/light/style")

    /** The base map: a background, a fill and a label of its own vector tiles on the Tile API, and a raster of another host's tiles. */
    const val STYLE = """{"version": 8, "name": "stub",
     "sources": {
       "base": {"type": "vector", "tiles": ["https://tiles.example.test/v1/terrains/outdoor/light/{z}/{x}/{y}"], "minzoom": 0, "maxzoom": 14},
       "elsewhere": {"type": "raster", "tiles": ["https://elsewhere.example.test/{z}/{x}/{y}.png"], "tileSize": 256, "attribution": "<a href=\"https://elsewhere.example.test/\" target=\"_blank\">&copy; Elsewhere</a>"},
       "style-huts": {"type": "geojson", "data": {"type": "FeatureCollection", "features": [
         {"type": "Feature", "id": 5, "geometry": {"type": "Point", "coordinates": [6.86, 45.83]}, "properties": {}},
         {"type": "Feature", "id": "6", "geometry": {"type": "Point", "coordinates": [6.87, 45.83]}, "properties": {}}
       ]}}
     },
     "layers": [
       {"id": "base-background", "type": "background", "paint": {"background-color": "#ffffff"}},
       {"id": "base-elsewhere", "type": "raster", "source": "elsewhere"},
       {"id": "base-water", "type": "fill", "source": "base", "source-layer": "water", "paint": {"fill-color": "#0000ff"}},
       {"id": "base-labels", "type": "symbol", "source": "base", "source-layer": "place", "layout": {"text-field": ["get", "name"]}}
     ]}"""

    private val seen: MutableList<Request> = Collections.synchronizedList(mutableListOf())

    /** The app's own client, whose one interceptor is the stub. */
    val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(this).build()

    /** Every request the stub was sent, in order. */
    val requests: List<Request> get() = synchronized(seen) { seen.toList() }

    /** Every request the stub was sent to [host]. */
    fun requests(host: String): List<Request> = requests.filter { it.url.host == host }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        seen += request
        if (!request.url.host.endsWith("example.test")) throw IOException("the stub serves example.test alone")
        val path = request.url.encodedPath
        val isMissing = "/missing/" in path
        val isStyle = !isMissing && path.endsWith("/style")
        val isRefused = "/refused/" in path
        val (code, message) =
            when {
                isMissing -> 404 to "Not Found"
                isRefused -> 402 to "Payment Required"
                isStyle -> 200 to "OK"
                else -> 204 to "No Content"
            }
        return Response
            .Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(
                (if (isStyle) STYLE else "").toResponseBody(
                    (if (isStyle) "application/json" else "application/x-protobuf").toMediaType(),
                ),
            ).build()
    }
}
