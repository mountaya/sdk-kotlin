package com.example.consumer

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.AnalysisHandle
import com.mountaya.elements.CameraFit
import com.mountaya.elements.CameraMotion
import com.mountaya.elements.CameraTarget
import com.mountaya.elements.Geolocation
import com.mountaya.elements.GeolocationFix
import com.mountaya.elements.GeolocationPermission
import com.mountaya.elements.GeolocationRefusal
import com.mountaya.elements.GeometrySlot
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.MapException
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapMarker
import com.mountaya.elements.MapRequest
import com.mountaya.elements.MapSource
import com.mountaya.elements.MapState
import com.mountaya.elements.MapTap
import com.mountaya.elements.QueriedFeature
import com.mountaya.elements.ScreenProjection
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.elements.SlotGeometry
import com.mountaya.elements.WaypointEdit
import com.mountaya.elements.ZoomLimit
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.LayerRank
import com.mountaya.geo.MapPaint
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.refusesMaxBounds
import com.mountaya.geo.tileSourceLayerRanks
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.milliseconds

/**
 * Elements reads the map's state and engine as an app's shared code does, on every target: every
 * command, every value the state holds and every refusal, every event and every feature a query answers,
 * the base map by its terrain, content drawn by id and by name, overlays and enrichments, layers and
 * concepts shown and hidden, restricted stretches, Data API results and geometry slots, markers with their
 * glyph, the hook an app hands the renderer's own requests through, a geolocation of the app's own, where the
 * map's read of its organization stands, and the scripted surface an app's tests draw a map on. None of it names a
 * type of the renderer underneath.
 */
object Elements {
    /** Held is a geolocation of the app's own: a fix its own location stack already holds. */
    class Held(
        private val fix: GeolocationFix,
    ) : Geolocation {
        override suspend fun locate(): GeolocationFix = fix

        override suspend fun permission(): GeolocationPermission = GeolocationPermission.Granted
    }

    /** Returns every command of the engine over the app's client, and what its state reads. */
    suspend fun mapped(mountaya: Mountaya): MapState {
        val signed: (MapRequest) -> MapRequest = { request ->
            MapRequest(
                request.url,
                request.headers + ("X-App" to "consumer"),
            )
        }
        val paint =
            MapPaint.standard.copy {
                itinerary.casingColor = "#1b1b1b"
                layerRanks += "acme_avalanche" to LayerRank.Poi.rank + 2
            }
        val map =
            MapEngine(
                client = mountaya,
                style = null,
                terrainVariant = TileSchema.TerrainVariant.Light,
                camera = CameraPose(listOf(6.8652, 45.9237), 12.0, 30.0, 0.0),
                labelLanguage = RestSchema.Language.Fr,
                paint = paint,
                glyphs = GlyphSet.marker,
                geolocation = Held(GeolocationFix.Located(listOf(6.8652, 45.9237), accuracyMeters = 25.0)),
                transformRequest = signed,
            )
        seen(map.glyphs.name, map.paint.itinerary.casingColor)
        try {
            map.setTerrainVariant(TileSchema.TerrainVariant.Dark)
            map.setTerrain("outdoor")
        } catch (missing: MapException) {
            seen(missing.kind == MapException.Kind.TerrainNotFound, missing.name, missing.message)
        }
        seen(map.state.value.terrain, map.state.value.terrainVariant)
        map.setStyle(Url("https://tiles.mountaya.com/v1/terrains/outdoor/light/style"))
        moved(map)
        map.setLabelLanguage(null)
        content(map, mountaya)
        results(map)
        seen(map.credits)
        seen(map.organization, keepsClear(map.organizationRead.value), map.organizationRead.first())
        map.addSource(MapSource("trail", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
        map.addSource(
            MapSource(
                "shade",
                MapSource.Kind.RasterDem(
                    listOf("https://tiles.example.com/{z}/{x}/{y}.png"),
                    512,
                    ZoomBounds(0.0, 14.0),
                ),
            ),
        )
        map.addLayer(
            MapLayer(
                id = "trail",
                type = TileSchema.TileLayerType.Line,
                source = "trail",
                rank = LayerRank.ItineraryLine.rank,
                paint = mapOf("line-color" to JsonPrimitive("#e64980"), "line-width" to JsonPrimitive(4)),
            ),
        )
        try {
            map.editLayer(
                "trail",
                paint = mapOf("line-color" to JsonNull, "line-width" to null),
                layout =
                    mapOf("visibility" to JsonPrimitive("none")),
            )
        } catch (missing: MapException) {
            seen(missing.kind, missing.name)
        }
        map.removeLayer("trail")
        map.removeSource("shade")
        marked(map)
        limited(map)
        edited(map)
        queried(map)
        try {
            seen(map.locate())
        } catch (refused: MapException) {
            seen(
                refused.kind == MapException.Kind.FixRefused,
                refused.reason?.id,
                refused.kind == MapException.Kind.Closed,
            )
        }
        seen(map.elevationAt(listOf(6.86, 45.83)), mountaya.baseUrls.tiles)
        val state = map.state.value
        seen(state.style, state.camera, state.labelLanguage, state.sources, state.layers)
        seen(state.markers, state.maxBounds, state.zoomBounds, state.activeSegments, state.waypointEdit)
        seen(state.isAccessRequired, state.terrain, state.terrainVariant)
        for (source in state.sources) {
            when (val kind = source.kind) {
                is MapSource.Kind.Vector -> seen(kind.tiles, kind.zoom)
                is MapSource.Kind.Raster -> seen(kind.tiles, kind.tileSize, kind.zoom)
                is MapSource.Kind.RasterDem -> seen(kind.tiles, kind.tileSize, kind.zoom)
                is MapSource.Kind.GeoJson -> seen(kind.features)
            }
            seen(source.id, source.attribution)
        }
        for (layer in state.layers) {
            seen(
                layer.id,
                layer.type,
                layer.source,
                layer.sourceLayer,
                layer.rank,
                layer.paint,
                layer.layout,
                layer.filter,
                layer.zoom,
                layer.key,
                layer.isHidden,
            )
        }
        seen(mountaya.language, mountaya.mapRequestHeaders(Url("https://tiles.mountaya.com/v1/zones/1/2/3")))
        map.close()
        return state
    }

    /**
     * Draws what a map shows by id and by name, as an app does: an itinerary and a collection framed, the
     * credential's itineraries and zones, a specification of the app's own, the catalogue's overlays and
     * enrichments, and the layers, concepts and restricted stretches shown and hidden.
     */
    suspend fun content(
        map: MapEngine,
        mountaya: Mountaya,
    ) {
        val framing = CameraFit(padding = PixelPadding(24.0))
        if (map.setItinerary("itn_4e5f", fit = framing)) map.clearItinerary()
        val drew: Boolean = map.setCollection("col_6a7b", fit = framing)
        seen(drew)
        map.clearCollection()
        map.setItineraries(true)
        map.setZones(false)
        try {
            map.setSpecification(mountaya.tiles.getZonesSpecification().data, "zones")
        } catch (refused: MapException) {
            seen(refused.kind == MapException.Kind.TypeRefused, refused.name)
        }
        seen(map.specifications)
        map.removeSpecification("zones")
        map.clearSpecifications()
        map.setLayersOrganization("org_8c9d")
        val catalogue: TileSchema.Layers = map.loadLayers()
        seen(catalogue.terrains.size, map.layersManifest?.overlays?.size)
        try {
            map.setOverlay("snow-depth")
            map.addEnrichment("avalanche-risk")
        } catch (refused: MapException) {
            val kind = refused.kind
            seen(kind == MapException.Kind.OverlayNotFound || kind == MapException.Kind.KindMismatch)
        }
        map.clearOverlay()
        seen(map.enrichments)
        map.removeEnrichment("avalanche-risk")
        map.clearEnrichments()
        map.setLayerVisibility("route", visible = false)
        seen(map.isLayerVisible("route"))
        for (key in map.drawnLayerKeys(rank = LayerRank.Zone.rank)) {
            seen(map.layerIds(key))
            map.setKeyVisibility(key, visible = true)
            seen(map.isKeyVisible(key))
        }
        map.setAccessRestrictionsVisibility(false)
        seen(map.isAccessRestrictionsVisible)
    }

    /**
     * Draws Data API results and the geometry slots over a map, as an app does: each asked for or handed over,
     * edited, removed and cleared by kind.
     */
    suspend fun results(map: MapEngine) {
        val reach: AnalysisHandle<DataSchema.IsochronesResult> =
            map.addIsochrones(
                DataSchema.IsochronesInput(
                    DataSchema.ActivitySlug.HikingAndTrail,
                    listOf(listOf(6.86, 45.83)),
                    listOf(600.0, 1_200.0),
                    DataSchema.RangeType.Time,
                ),
            )
        seen(reach.id, reach.result.isochrones?.size)
        map.editIsochrones(reach.id, patch = {
            DataSchema.IsochronesInput(it.activity, it.locations, listOf(900.0), it.rangeType)
        })
        map.editIsochrones(reach.id)
        map.removeAnalysis(reach.id)
        map.clearIsochrones()
        val suggested =
            map.addSuggestions(
                DataSchema.SuggestionsInput(DataSchema.ActivitySlug.HikingAndTrail, 3, 10_000.0, listOf(6.86, 45.83)),
                chosen = 1,
            )
        map.editSuggestions(suggested.id, chosen = 2)
        map.clearSuggestions()
        map.addDirections(DataSchema.DirectionsResult { routes = emptyList() })
        map.clearDirections()
        map.addPlaces(DataSchema.GeocodeResult { places = emptyList() })
        map.clearPlaces()
        val matrix =
            map.addMatrix(
                DataSchema.MatrixInput(
                    DataSchema.ActivitySlug.HikingAndTrail,
                    listOf(listOf(6.86, 45.83), listOf(6.9, 45.9)),
                    listOf(DataSchema.MatrixMetric.Duration),
                ),
            )
        map.editMatrix(matrix.id, patch = {
            DataSchema.MatrixInput(it.activity, it.locations, listOf(DataSchema.MatrixMetric.Distance))
        })
        map.clearMatrix()
        map.setPointsOfInterest(DataSchema.PoisInput(listOf(DataSchema.POICategory.Hut, DataSchema.POICategory.Peak)))
        map.editPointsOfInterest(patch = { DataSchema.PoisInput(it.categories) { limit = 20 } })
        map.clearPointsOfInterest()
        seen(MapEngine.poisAnalysisId)
        map.setGeometry(GeoJsonLineString(listOf(listOf(6.85, 45.83), listOf(6.87, 45.84))), GeometrySlot.Preview)
        map.editGeometry(GeometrySlot.Preview, color = "#2b8a3e")
        map.setGeometry(GeoJsonPoint(listOf(6.86, 45.83)), GeometrySlot.Highlight)
        val held: SlotGeometry = map.geometry(GeometrySlot.Highlight)
        seen(held.geometry, held.color, GeometrySlot.entries)
        map.clearGeometry(GeometrySlot.Preview)
    }

    /** Every marker command, and what a marker holds. */
    private fun marked(map: MapEngine) {
        val hut = MapMarker("hut", listOf(6.8652, 45.9237, 1_834.0), color = "#e64980", glyph = "hut", label = "Refuge")
        try {
            map.addMarker(hut)
            map.addMarker(MapMarker("summit", listOf(6.8652, 45.8326)))
            map.editMarker(
                "hut",
                position = listOf(6.87, 45.93),
                color = null,
                glyph = "mountain",
                label = "Refuge du Col",
            )
            map.editMarker("lake")
        } catch (refused: MapException) {
            val kind = refused.kind
            seen(kind == MapException.Kind.MarkerNotFound || kind == MapException.Kind.PositionRefused)
        }
        seen(hut.id, hut.position, hut.color, hut.glyph, hut.label)
        map.removeMarker("summit")
        map.clearMarkers()
    }

    /** Every camera move, and its refusals. */
    private fun moved(map: MapEngine) {
        map.jump(CameraTarget(center = listOf(6.8752, 45.9287), zoom = 14.0, bearing = 90.0, pitch = 0.0))
        map.ease(CameraTarget(zoom = 13.0))
        map.fly(CameraTarget(center = listOf(6.87, 45.93), bearing = 0.0))
        map.fitBounds(
            listOf(6.8, 45.8, 6.95, 45.9),
            CameraFit(PixelPadding(24.0), maxZoom = 15.0, duration = 300.milliseconds),
        )
        map.zoomIn()
        map.zoomOut()
        map.resetNorth()
        map.easeIntoZoomRange(ZoomBounds(10.0, 14.0))
        try {
            map.ease(CameraTarget(zoom = Double.NaN))
        } catch (refused: MapException) {
            seen(refused.kind == MapException.Kind.NumberRefused || refused.kind == MapException.Kind.PositionRefused)
        }
        val target = CameraTarget(center = listOf(6.86, 45.83))
        seen(target.center, target.zoom, target.bearing, target.pitch)
        val fit = CameraFit()
        seen(fit.padding, fit.maxZoom, fit.duration)
    }

    /** The camera's limits, and their refusals. */
    private fun limited(map: MapEngine) {
        val lock = listOf(6.7, 45.7, 7.0, 46.0)
        if (!refusesMaxBounds(lock)) map.setMaxBounds(lock)
        try {
            map.setMaxBounds(listOf(6.7, 45.7, 6.7, 46.0))
            map.setZoomBounds(min = ZoomLimit.Zoom(13.0), max = ZoomLimit.Zoom(12.0))
        } catch (refused: MapException) {
            val kind = refused.kind
            seen(kind == MapException.Kind.MaxBoundsRefused || kind == MapException.Kind.ZoomBoundsRefused)
        }
        map.setZoomBounds(ZoomLimit.Zoom(5.0), ZoomLimit.Zoom(16.0))
        map.setZoomBounds(max = ZoomLimit.Kept)
        map.setMaxBounds(null)
        map.setZoomBounds(ZoomLimit.Released, ZoomLimit.Released)
        val limit: ZoomLimit = ZoomLimit.Zoom(8.0)
        if (limit is ZoomLimit.Zoom) seen(limit.zoom)
    }

    /** Every point query and projection, and every member of a feature a query answers. */
    private fun queried(map: MapEngine) {
        val point = map.project(listOf(6.86, 45.83))
        seen(map.unproject(point))
        val found = map.featuresAt(point) + map.poisAt(point) + map.zonesAt(point) + map.itinerariesAt(point)
        for (feature in found) feature(feature)
    }

    /** Returns what a feature a query answers holds. */
    fun feature(feature: QueriedFeature): Any? {
        seen(feature.properties, feature.geometry, feature.layer, feature.sourceLayer)
        return when (val id = feature.id) {
            is QueriedFeature.Id.Number -> id.value
            is QueriedFeature.Id.Text -> id.value
            null -> null
        }
    }

    /** Returns what an event the map tells holds, each kind read. */
    fun told(event: MapEvent): Any? =
        when (event) {
            is MapEvent.Click -> clicked(event.tap)
            is MapEvent.LongClick -> clicked(event.tap)
            is MapEvent.FeatureClick -> seen(event.position, event.features)
            is MapEvent.FeatureHover -> seen(event.position, event.features)
            is MapEvent.AccessRefused -> seen(event.status, event.url)
            is MapEvent.AccessRestored -> seen(event)
            is MapEvent.Failure -> seen(event.url, event.status)
            is MapEvent.LayersChange -> seen(event.layers?.terrains?.size)
        }

    /**
     * Returns whether a host keeps its own views clear of the watermark's frame where the map's read of its
     * organization stands at [read], each of the five states read: only an answer of white label releases it.
     */
    fun keepsClear(read: OrganizationRead): Boolean =
        when (read) {
            is OrganizationRead.Answered -> !read.whiteLabel

            OrganizationRead.Waiting,
            OrganizationRead.TimedOut,
            OrganizationRead.Failed,
            OrganizationRead.NoKey,
            -> true
        }

    /** Returns the first event a map tells, as a host's own coroutine collects them. */
    suspend fun firstEvent(map: MapEngine): MapEvent = map.events().first()

    /**
     * Returns a map drawn on the scripted surface, as an app's test draws one: a feature drawn under every point, a
     * click and a hover played, and what the engine asked of the surface read back.
     */
    fun scripted(mountaya: Mountaya): List<ScriptedMapSurface.Move> {
        val map = MapEngine(mountaya, geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map, canvas = listOf(390.0, 844.0), zoomLimits = ZoomBounds(0.0, 25.5))
        surface.rendered =
            listOf(
                QueriedFeature(
                    mapOf("segment_id" to JsonPrimitive("climb")),
                    GeoJsonPoint(listOf(6.86, 45.83)),
                    layer = "route",
                    sourceLayer = "itinerary_lines",
                    id = QueriedFeature.Id.Number(7.0),
                ),
            )
        surface.liveLayers = null
        surface.heldSources = listOf("route")
        surface.baseLabels = listOf(ScriptedMapSurface.Label("place-label", JsonPrimitive("{name}")))
        surface.projection = ScreenProjection(point = { it }, position = { it })
        map.addLayer(
            MapLayer("route", TileSchema.TileLayerType.Line, "route", rank = LayerRank.ItineraryLine.rank),
        )
        surface.click(listOf(120.0, 80.0), listOf(6.86, 45.83))
        surface.longClick(listOf(120.0, 80.0), listOf(6.86, 45.83))
        surface.hover(listOf(120.0, 80.0), listOf(6.86, 45.83))
        surface.drawFrame()
        surface.leave(listOf(-1.0, 80.0), listOf(6.0, 45.83))
        surface.failRequest("https://tiles.mountaya.com/v1/zones/1/2/3", 402, "route")
        surface.loadTile("route")
        surface.loadStyle()
        map.ease(CameraTarget(zoom = 12.0))
        for (frame in surface.frames) seen(frame.bounds, frame.fit)
        for (move in surface.moves) {
            when (val motion = move.motion) {
                is CameraMotion.Jump, is CameraMotion.Fly -> seen(move.target)
                is CameraMotion.Ease -> seen(move.target, motion.duration)
            }
        }
        seen(surface.canvas, surface.zoomLimits, surface.queries, surface.relabelled)
        seen(
            GeolocationFix.Refused(GeolocationRefusal.Unavailable),
            GeolocationPermission.Prompt.id,
            GeolocationRefusal.NoApi.id,
        )
        map.close()
        return surface.moves
    }

    /** A waypoint placed by an edit, through a projection of the app's own renderer. */
    private fun edited(map: MapEngine) {
        val projection = ScreenProjection(point = { it }, position = { it })
        map.setActiveSegments(listOf(listOf(listOf(6.86, 45.92), listOf(6.88, 45.94))))
        map.beginWaypointEdit("w1", position = null)
        map.placeWaypoint(listOf(6.87, 45.93), projection)
        val edit: WaypointEdit? = map.endWaypointEdit()
        if (edit != null) seen(edit.waypointId, edit.position, edit.isSnapped)
        map.setActiveSegments(emptyList())
        seen(projection.point(listOf(0.0, 0.0)), projection.position(listOf(0.0, 0.0)))
    }

    /** Returns what a click on the map hands an app, as a consumer drawing a map of their own builds one. */
    fun clicked(tap: MapTap): MapTap {
        seen(tap.position, tap.point, tap.marker)
        return MapTap(tap.position, tap.point, marker = null)
    }

    /** Returns the values a consumer who draws a map of their own builds from the Tile API's answers. */
    fun drawnFrom(
        source: TileSchema.TileSource,
        layer: TileSchema.TileLayer,
    ): MapState =
        MapState(
            style = Url("https://tiles.mountaya.com/v1/terrains/outdoor/light/style"),
            camera = CameraPose(listOf(0.0, 0.0), 0.0, 0.0, 0.0),
            labelLanguage = RestSchema.Language.En,
            sources = listOf(MapSource(source)),
            layers = listOf(MapLayer(layer, source.id), MapLayer(layer, source.id, tileSourceLayerRanks)),
        )
}
