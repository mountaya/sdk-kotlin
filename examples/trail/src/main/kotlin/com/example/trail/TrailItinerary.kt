package com.example.trail

import android.content.res.AssetManager
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.CameraFit
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapMarker
import com.mountaya.elements.MapSource
import com.mountaya.geo.GeoJsonFeature
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.ItinerarySource
import com.mountaya.geo.LayerRank
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.itineraryBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * TrailItinerary is where Trail's itinerary comes from, the source its one `ItineraryResourceCache` reads through: the
 * itinerary `MOUNTAYA_ITINERARY_ID` names, read with `getItinerary` through [client], or, for its own id, the [demo]
 * itinerary, which needs no network. It holds the last itinerary it answered, which the map draws and the navigation
 * follows.
 */
class TrailItinerary(
    private val client: Mountaya?,
    /** The demo itinerary, the app's copy of the contract's `sdk/trail/itinerary.json`. */
    val demo: RestSchema.ItineraryWithDetails,
) : ItinerarySource {
    private val answered = MutableStateFlow<RestSchema.ItineraryWithDetails?>(null)

    /** The itinerary last answered, or null before the first read. */
    val held: StateFlow<RestSchema.ItineraryWithDetails?> = answered.asStateFlow()

    override suspend fun read(
        itineraryId: String,
        surface: RestSchema.ReadSurface?,
    ): RestSchema.ItineraryWithDetails {
        // trail: trail.itinerary
        val itinerary =
            if (client == null || itineraryId == demo.id) {
                demo
            } else {
                client.studio.itineraries
                    .getItinerary(itineraryId) { if (surface != null) this.surface = surface }
                    .data
            }
        answered.value = itinerary
        return itinerary
    }

    /** Returns the itinerary of id [itineraryId]: the one held where it is that one, read again otherwise. */
    suspend fun itinerary(itineraryId: String): RestSchema.ItineraryWithDetails =
        held.value?.takeIf { it.id == itineraryId } ?: read(itineraryId, null)

    /** The demo itinerary. */
    companion object {
        /** The demo itinerary's file among the app's assets, a byte-for-byte copy of the contract's. */
        const val ASSET = "itinerary.json"

        /** The id of the source and the layer the itinerary is drawn with. */
        private const val DRAWN = "trail-itinerary"

        /** Returns the demo itinerary, read from [assets] and decoded as the `ItineraryWithDetails` it is. */
        fun demo(assets: AssetManager): RestSchema.ItineraryWithDetails =
            trailJson.decodeFromString(
                RestSchema.ItineraryWithDetails.serializer(),
                assets.open(ASSET).use { it.readBytes().decodeToString() },
            )

        /**
         * Draws [itinerary] on [map]: its segments as one line, each in its own colour, ranked where the layer
         * registry ranks an itinerary's line, and a marker on each of its places, named by its name.
         */
        fun draw(
            map: MapEngine,
            itinerary: RestSchema.ItineraryWithDetails,
        ) {
            val segments =
                itinerary.segments.mapNotNull { segment ->
                    val geometry = segment.geojson ?: return@mapNotNull null
                    GeoJsonFeature(geometry, JsonObject(mapOf("color" to JsonPrimitive(segment.color ?: "#2f80ed"))))
                }
            map.addSource(MapSource(DRAWN, MapSource.Kind.GeoJson(GeoJsonFeatureCollection(segments))))
            map.addLayer(
                MapLayer(
                    id = DRAWN,
                    type = TileSchema.TileLayerType.Line,
                    source = DRAWN,
                    rank = LayerRank.ItineraryLine.rank,
                    paint =
                        mapOf(
                            "line-color" to JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("color"))),
                            "line-width" to JsonPrimitive(4),
                        ),
                    layout = mapOf("line-cap" to JsonPrimitive("round"), "line-join" to JsonPrimitive("round")),
                ),
            )
            for (waypoint in itinerary.waypoints) {
                val point = waypoint.geojson as? GeoJsonPoint ?: continue
                if (point.coordinates.size < 2) continue
                map.addMarker(MapMarker("$DRAWN-${waypoint.id}", point.coordinates.take(2), label = waypoint.name))
            }
        }

        /**
         * Frames [itinerary] on [map], clear of the search box above it and the sheet below. A map no view draws frames
         * nothing, so the app calls it once the map is ready.
         */
        fun frame(
            map: MapEngine,
            itinerary: RestSchema.ItineraryWithDetails,
        ) {
            val bounds = itineraryBounds(itinerary.segments) ?: return
            map.fitBounds(bounds, CameraFit(padding = FRAMING))
        }

        /** The room the search box takes above the itinerary and the sheet below it, in pixels. */
        private val FRAMING = PixelPadding(top = 120.0, right = 48.0, bottom = 370.0, left = 48.0)
    }
}
