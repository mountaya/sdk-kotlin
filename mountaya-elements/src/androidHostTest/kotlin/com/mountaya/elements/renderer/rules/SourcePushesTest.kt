package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeature
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.LayerRank
import com.mountaya.geo.ZoomBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * The sources MapLibre may be sent for a change to what a map holds: one, the source it changed, and for a scrub frame
 * of a profile's cursor the emphasis slot's alone, where the frame lights another leg. MapLibre parses and tiles again
 * every source it is sent (`source-pushes` in the contract's `sdk/hot-paths.yaml`).
 */
private const val SOURCE_PUSHES_BUDGET = 1

/** What the renderer sends MapLibre of a map's sources, read with no renderer: the rule its reconciliation runs. */
class SourcePushesTest {
    private val zoom = ZoomBounds(0.0, 14.0)

    /** A source's features: one line north of the map's centre, [offset] hundredths east. */
    private fun trail(offset: Int): GeoJsonFeatureCollection {
        val east = 6.85 + offset / 100.0
        val line = GeoJsonLineString(listOf(listOf(east, 45.83), listOf(east, 45.85)))
        return GeoJsonFeatureCollection(listOf(GeoJsonFeature(line, JsonObject(emptyMap()))))
    }

    private fun shapes(
        id: String,
        offset: Int,
    ): MapSource = MapSource(id, MapSource.Kind.GeoJson(trail(offset)))

    private fun tiles(
        id: String,
        template: String,
    ): MapSource = MapSource(id, MapSource.Kind.Vector(listOf(template), zoom))

    @Test
    fun aSourceEqualToTheOneDrawnIsSentNothingAndGeoJsonIsGivenItsNewFeaturesInPlace() {
        val drawn = mapOf("a" to shapes("a", 0), "b" to shapes("b", 1), "zones" to tiles("zones", "https://t/{z}"))
        val pushes = sourcePushes(drawn, listOf(shapes("a", 0), shapes("b", 5), tiles("zones", "https://t/{z}")))

        assertEquals(listOf(shapes("b", 5)), pushes.refreshed)
        assertTrue(pushes.removed.isEmpty())
        assertTrue(pushes.added.isEmpty())
        assertEquals(listOf("b"), pushes.sent)
    }

    @Test
    fun aSourceGoneOrServingAnotherKindIsTakenOffAndOneNotDrawnIsAddedInTheStatesOrder() {
        val drawn =
            mapOf("a" to shapes("a", 0), "zones" to tiles("zones", "https://t/{z}"), "gone" to shapes("gone", 2))
        val wanted = listOf(shapes("new", 3), tiles("a", "https://a/{z}"), shapes("zones", 4))

        val pushes = sourcePushes(drawn, wanted)

        assertTrue(pushes.refreshed.isEmpty())
        assertEquals(setOf("a", "zones", "gone"), pushes.removed)
        assertEquals(wanted, pushes.added)
        assertEquals(listOf("new", "a", "zones"), pushes.sent)
    }

    /**
     * MapLibre Native's tile sources take their addresses as they are built, so a pin moved is sent as the stamped
     * overlay's source taken off and added anew, its layers made again in place, and no other source (`map.overlays.instant`).
     */
    @Test
    fun aPinMovedSendsTheStampedOverlaysSourceAloneTakenOffAndAddedAgain() {
        val catalogue =
            """{"data": {"elevation": {"id": "e", "tile_size": 512, "url": "https://tiles.test/elevation.json"},
              "itineraries": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "i", "locales": {}, "slug": "itineraries", "sources": []},
              "zones": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "z", "locales": {}, "slug": "zones", "sources": []},
              "overlays": [{"id": "c", "locales": {}, "slug": "conditions", "children": [
                {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": true}},
                  "content_type": "application/x-protobuf", "id": "snow", "locales": {}, "slug": "snow",
                  "sources": [{"id": "snow", "layers": [{"id": "snow-layer", "key": "snow", "layout": null,
                    "paint": null, "source_layer": "snow", "type": "fill", "zoom_max": 22, "zoom_min": 0}],
                    "tiles": ["https://tiles.test/snow/{z}/{x}/{y}"], "type": "vector", "zoom_max": 14, "zoom_min": 0}]}]}],
              "terrains": []}}"""
        val map =
            MapEngine(
                Mountaya(
                    Credential.Publishable("pk_test_sources"),
                    exchange =
                        object : HttpExchange {
                            override suspend fun send(request: ExchangeRequest): ExchangeResponse =
                                ExchangeResponse(
                                    200,
                                    mapOf("Content-Type" to "application/json"),
                                    ExchangeBody(catalogue.encodeToByteArray()),
                                )
                        },
                    dispatcher = Dispatchers.Unconfined,
                ),
            )
        val drawn = mutableMapOf<String, MapSource>()
        map.addSource(shapes("route", 0))
        runBlocking { map.setOverlay("snow") }
        for (source in sourcePushes(drawn, map.state.value.sources).added) drawn[source.id] = source
        val layers = map.state.value.layers

        runBlocking { map.setInstant(Instant.parse("2026-03-14T09:00:00Z")) }
        val pushes = sourcePushes(drawn, map.state.value.sources)

        assertEquals(setOf("snow"), pushes.removed)
        assertEquals(
            listOf(tiles("snow", "https://tiles.test/snow/{z}/{x}/{y}?timestamp=2026-03-14T09%3A00%3A00.000Z").kind),
            pushes.added.map { it.kind },
        )
        assertEquals(listOf("snow"), pushes.sent)
        assertEquals(layers, map.state.value.layers, "the overlay's layers stand where they stood")
    }

    @Test
    fun hotPathBudgetSourcePushesAChangeSendsItsSourceAloneAndAScrubFrameTheLegItLightsAlone() {
        val map =
            MapEngine(
                Mountaya(
                    Credential.Publishable("pk_test_sources"),
                    exchange =
                        object : HttpExchange {
                            override suspend fun send(request: ExchangeRequest): ExchangeResponse =
                                error("a map drawing sources asks the network nothing")
                        },
                    dispatcher = Dispatchers.Unconfined,
                ),
                camera = CameraPose(listOf(6.86, 45.83), 12.0, 0.0, 0.0),
            )
        val drawn = mutableMapOf<String, MapSource>()
        val sent = mutableListOf<String>()

        // What the renderer does with every state the engine reports: it brings the sources it drew to the state's, and
        // sends MapLibre what that takes.
        fun draw() {
            val pushes = sourcePushes(drawn, map.state.value.sources)
            for (source in pushes.refreshed) drawn[source.id] = source
            for (id in pushes.removed) drawn.remove(id)
            for (source in pushes.added) drawn[source.id] = source
            sent += pushes.sent
        }

        // Five sources of GeoJSON the map draws, each a line of its own.
        val ids = listOf("a", "b", "c", "d", "e")
        for ((index, id) in ids.withIndex()) {
            map.addSource(shapes(id, index))
            map.addLayer(MapLayer(id, TileSchema.TileLayerType.Line, id, rank = LayerRank.ItineraryLine.rank))
            draw()
        }
        assertEquals(ids, sent)

        // Thirty changes to one source: each sends that source alone.
        sent.clear()
        for (change in 1..30) {
            map.addSource(shapes("c", 10 + change))
            draw()
        }
        assertEquals(30, sent.size, "every change was sent: $sent")
        assertTrue(sent.all { it == "c" }, "$sent")
        assertTrue(sent.size <= SOURCE_PUSHES_BUDGET * 30, "${sent.size} sources sent for 30 changes")

        // Sixty frames of a profile's cursor over an itinerary of three legs, four samples each: its marker moved on
        // each, and the leg it points at lit as it moves from one to the next, as a profile's link writes them.
        sent.clear()
        val legs =
            (0 until 3).map { leg ->
                GeoJsonLineString((0..4).map { listOf(6.80 + (leg * 4 + it) / 100.0, 45.9) })
            }
        val samples = (0 until 12).map { listOf(6.80 + it / 100.0, 45.9) }
        val bounce = (0 until 12) + (10 downTo 1)
        var pinned: CursorMarkerPin? = null
        var isLit = false
        val lit = mutableListOf<GeoJsonLineString?>(null)
        for (frame in 0 until 60) {
            val sample = bounce[frame % bounce.size]
            pinned = showCursorMarker(map, pinned, CursorMarkerPoint(PROFILE_CURSOR_MARKER_ID, samples[sample]))
            isLit = writeLegEmphasis(map, isLit, legs[sample / 4])
            draw()
            lit += legs[sample / 4]
        }
        val litAnew = lit.zipWithNext().count { (before, after) -> before != after }

        // The work happened: the marker stands on the last sample, and legs were lit anew as the cursor crossed them.
        val last = samples[bounce[59 % bounce.size]]
        assertEquals(
            last,
            map.state.value.markers
                .single { it.id == PROFILE_CURSOR_MARKER_ID }
                .position,
        )
        assertTrue(litAnew > 1, "the cursor crossed $litAnew legs")
        // Each frame sent the emphasis slot's source at most, and only where it lit another leg.
        assertEquals(setOf(GeometrySlot.Emphasis.sourceId), sent.toSet(), "$sent")
        assertTrue(
            sent.size <= SOURCE_PUSHES_BUDGET * litAnew,
            "${sent.size} sources sent over 60 frames, a leg lit anew $litAnew times",
        )
    }
}
