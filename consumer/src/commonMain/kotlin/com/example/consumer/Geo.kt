package com.example.consumer

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.DateLength
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.GuidancePlan
import com.mountaya.geo.GuidancePrompt
import com.mountaya.geo.GuidanceStep
import com.mountaya.geo.HexColor
import com.mountaya.geo.ItineraryLeg
import com.mountaya.geo.ItineraryPlace
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.ItineraryStroke
import com.mountaya.geo.LOCATE_MAX_ZOOM
import com.mountaya.geo.LengthUnit
import com.mountaya.geo.LineLayerStyle
import com.mountaya.geo.MapPaint
import com.mountaya.geo.MapViewport
import com.mountaya.geo.NavigationEngine
import com.mountaya.geo.NavigationError
import com.mountaya.geo.NavigationProgress
import com.mountaya.geo.NavigationRemainder
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.NavigationRouteSource
import com.mountaya.geo.NavigationState
import com.mountaya.geo.NavigationStatus
import com.mountaya.geo.OFF_ROUTE_METERS
import com.mountaya.geo.PROMPT_METERS
import com.mountaya.geo.PROMPT_SLACK_METERS
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.PoiStyle
import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.RouteFollowingEngine
import com.mountaya.geo.ScriptedNavigationEngine
import com.mountaya.geo.ShapePaint
import com.mountaya.geo.TimeLength
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.WaypointDetails
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.accuracyBounds
import com.mountaya.geo.activityMarkerGlyph
import com.mountaya.geo.bearingDegrees
import com.mountaya.geo.boundsOf
import com.mountaya.geo.buildElevationSeries
import com.mountaya.geo.cameraForBounds
import com.mountaya.geo.convertLength
import com.mountaya.geo.cumulativeMeters
import com.mountaya.geo.duePrompt
import com.mountaya.geo.followedRouteOf
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatDuration
import com.mountaya.geo.formatElevation
import com.mountaya.geo.formatLngLat
import com.mountaya.geo.formatNumber
import com.mountaya.geo.glyphPaint
import com.mountaya.geo.guidancePlanOf
import com.mountaya.geo.haversineMeters
import com.mountaya.geo.highlightParts
import com.mountaya.geo.isOffRoute
import com.mountaya.geo.isOffRouteAt
import com.mountaya.geo.isochroneColor
import com.mountaya.geo.itineraryBounds
import com.mountaya.geo.itineraryGeometry
import com.mountaya.geo.itinerarySegments
import com.mountaya.geo.itineraryStrokeStyle
import com.mountaya.geo.itineraryWaypoints
import com.mountaya.geo.navigationProgressAt
import com.mountaya.geo.parseGlyph
import com.mountaya.geo.poiCategoryOf
import com.mountaya.geo.poiCategoryStyles
import com.mountaya.geo.poiStyleOf
import com.mountaya.geo.poiTileCategories
import com.mountaya.geo.positionAtMeters
import com.mountaya.geo.profileTotals
import com.mountaya.geo.readDirectionsSteps
import com.mountaya.geo.readItineraryDirectionsSteps
import com.mountaya.geo.roadAccessColor
import com.mountaya.geo.roadAccessFeatures
import com.mountaya.geo.roadAccessGlyph
import com.mountaya.geo.routeLengthMeters
import com.mountaya.geo.searchQueryChangeOf
import com.mountaya.geo.segmentColorAt
import com.mountaya.geo.specificationBounds
import com.mountaya.geo.stepInstruction
import com.mountaya.geo.timeZoneViewport
import com.mountaya.geo.trimWhiteSpace
import com.mountaya.geo.zoomIntoRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Instant

/**
 * Geo reads the geo layer as an app does: a rule of every topic, the values a rule answers and a
 * host builds for its own screens, the navigation engine implemented from outside, and the
 * scripted engine an app's tests answer.
 */
object Geo {
    /** Calls the rules a screen reads a route and a reader's units through. */
    fun measurements() {
        val line = listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682))
        val meters = haversineMeters(line[0], line[1])
        val units = UnitPreferences(language = "fr-CH", length = RestSchema.UnitLength.Imperial)
        seen(
            convertLength(meters, LengthUnit.Meter, LengthUnit.Mile),
            formatDistance(meters, units),
            formatElevation(1_035.0, units),
            formatDuration(5_400.0, units),
            formatLngLat(line[0]),
            formatNumber(1_531.25, "de-CH"),
            bearingDegrees(line[0], line[1]),
            routeLengthMeters(line),
            cumulativeMeters(line),
            positionAtMeters(line, 100.0),
        )
        val arrival = Instant.parse("2026-03-14T00:00:00Z")
        seen(
            formatDateTime(arrival, "fr-CH", "Europe/Zurich"),
            formatDateTime(arrival, "fr-CH", "Europe/Zurich", timeLength = TimeLength.Short),
            formatDateTime(arrival, "fr-CH", "Europe/Zurich", DateLength.Medium, TimeLength.Medium),
        )
        val series =
            buildElevationSeries(GeoJsonLineString(listOf(listOf(6.86, 45.92, 1_035.0), listOf(6.93, 45.97, 1_210.0))))
        seen(profileTotals(series), series.hasElevation, series.points.firstOrNull()?.distance)
        seen(
            boundsOf(listOf(GeoJsonLineString(line))),
            timeZoneViewport("Europe/Zurich")?.zoom,
            segmentColorAt(3),
            highlightParts("Chamonix", "cham"),
            searchQueryChangeOf(settled = "", typed = "Cham"),
        )
        framed()
    }

    /** Calls the rules a map frames its content and a fix by, as a host that draws a map of its own does. */
    fun framed(segments: List<RestSchema.ItinerarySegment> = emptyList()): MapViewport? {
        val box = specificationBounds(listOf(179.8, -17.0, -179.9, -16.0)) ?: itineraryBounds(segments)
        val viewport: MapViewport? =
            box?.let {
                cameraForBounds(
                    it,
                    listOf(390.0, 844.0),
                    ZoomBounds(0.0, 25.5),
                    PixelPadding(48.0, 24.0, 120.0, 24.0),
                    maxZoom = 15.0,
                )
            }
        seen(
            viewport?.center,
            viewport?.zoom,
            zoomIntoRange(6.0, ZoomBounds(10.0, 14.0)),
            accuracyBounds(listOf(6.86, 45.83), radiusMeters = 25.0),
            LOCATE_MAX_ZOOM,
            trimWhiteSpace("  &copy; Mountaya "),
        )
        return viewport
    }

    /**
     * Reads a route's steps, follows it on the scripted engine and on the host's own, and reads
     * every value a navigation reports.
     */
    fun navigation() {
        val reading = readItineraryDirectionsSteps(null, coordinateCount = 2)
        val data = readDirectionsSteps(null, coordinateCount = 2)
        seen(reading.refused, reading.steps, data.steps)

        val route = NavigationRoute(line = listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)), duration = 3_600.0)
        followedRouteOf(route)?.let { followed ->
            val progress = navigationProgressAt(followed, listOf(6.9, 45.95), previous = 0.0)
            seen(
                followed.line,
                progress.along,
                progress.next,
                progress.toManeuver.duration,
                progress.toArrival.distance,
            )
            followed.steps.getOrNull(progress.step)?.let { step ->
                val instruction = stepInstruction(step)
                seen(instruction.key, instruction.name, instruction.exit)
            }
            // The plan an engine of the app's own that speaks announces the route from, and the prompt due.
            guidancePlanOf(followed)?.let { plan: GuidancePlan ->
                val due: GuidancePrompt? = duePrompt(plan, progress)
                seen(due?.id, due?.instruction, due?.meters, PROMPT_METERS, PROMPT_SLACK_METERS)
                plan.steps.firstOrNull()?.let { step: GuidanceStep ->
                    val own = GuidancePlan(listOf(GuidanceStep(0.0, step.instruction, emptyList())))
                    seen(step.meters, step.prompts.size, own == plan)
                }
            }
        }

        val scripted = ScriptedNavigationEngine<String>()
        follow(scripted) { scripted.sent.firstOrNull()?.answer(route) }
        scripted.sent.firstOrNull()?.let { request -> seen(request.request) }
        follow(StandingStill()) {}
    }

    /**
     * Follows a route on the published machine, over the host's own route source and in its scope, as
     * an app follows one on a platform with no navigation engine of its own, and reads the off-route rule
     * every engine reads a position by.
     */
    fun following(scope: CoroutineScope) {
        val source = NavigationRouteSource<List<List<Double>>> { waypoints -> NavigationRoute(line = waypoints) }
        val engine = RouteFollowingEngine(scope, source)
        val waypoints = listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682))
        engine.start(waypoints)
        engine.setPosition(listOf(6.9, 45.95))
        seen(
            engine.state.value.status,
            OFF_ROUTE_METERS,
            isOffRoute(60.0),
            isOffRouteAt(listOf(6.9, 45.95), listOf(GeoJsonLineString(waypoints))),
        )
        engine.close()
        follow(RouteFollowingEngine(scope) { NavigationRoute(line = waypoints) }) {}
        // An app's own engine over the machine, told each state before any collector of its flow.
        val told = mutableListOf<NavigationStatus>()
        val observed = RouteFollowingEngine(scope, source) { state -> told += state.status }
        observed.start(waypoints)
        observed.close()
        seen(told, RouteFollowingEngine(scope, source, onChange = null).state.value)
    }

    /** An app's own code, written against the interface whatever engine it is handed. */
    private fun follow(
        navigation: NavigationEngine<String>,
        answer: () -> Unit,
    ) {
        navigation.start("summit")
        answer()
        try {
            navigation.setPosition(listOf(Double.NaN, 45.9))
        } catch (refusal: IllegalArgumentException) {
            seen(refusal.message)
        }
        navigation.setPosition(listOf(6.9, 45.95))
        navigation.reroute("from here")
        val state = navigation.state.value
        seen(
            state.status.id,
            state.route?.steps,
            state.progress?.toArrival?.distance,
            NavigationStatus.entries.map { it.id },
        )
        state.error?.let(::said)
        navigation.stop()
        navigation.close()
    }

    /** Reads a navigation's failure, its kinds open to the ones a later release adds. */
    private fun said(error: NavigationError) {
        when (error) {
            is NavigationError.RouteFailed -> seen(error.cause)
            is NavigationError.EmptyRoute -> seen(error)
            else -> seen(error)
        }
    }

    /**
     * Reads the glyphs a screen draws: a set of the host's own, alone and chained over the bundled one, how
     * each shape of a glyph is painted, and the glyphs an activity and a restricted stretch are marked with.
     */
    fun icons() {
        val licensed =
            GlyphSet.of(
                name = "licensed",
                paint = GlyphSet.Paint.Fill,
                strokeWidth = 1.5,
                viewBox = 16.0,
                markup = mapOf("refuge" to "<rect width=\"16\" height=\"16\"/>"),
            )
        val partial = GlyphSet.of("partial", GlyphSet.Paint.Stroke, markup = emptyMap())
        val set = partial?.let { GlyphSet.chaining(listOf(it, GlyphSet.marker)) } ?: GlyphSet.marker
        for (shape in parseGlyph(set.markup["map-pin"].orEmpty())) {
            val paint: ShapePaint = glyphPaint(shape, set)
            seen(paint.isFilled, paint.isStroked, paint.strokeWidth, paint.lineCap.id, paint.lineJoin.id)
            seen(paint.miterLimit, paint.fillRule == ShapePaint.FillRule.EvenOdd)
        }
        seen(
            licensed?.name,
            set.name,
            set.paint.id,
            set.viewBox,
            set.strokeWidth,
            activityMarkerGlyph(RestSchema.ActivitySlug.HikingAndTrail, set),
            roadAccessGlyph(RoadAccessKind.Permit),
            roadAccessColor(RoadAccessKind.LocalAccess).hex,
            ShapePaint(
                isFilled = true,
                isStroked = false,
                strokeWidth = 2.0,
                lineCap = ShapePaint.LineCap.Square,
                lineJoin = ShapePaint.LineJoin.MiterClip,
                miterLimit = 4.0,
                fillRule = ShapePaint.FillRule.NonZero,
            ),
        )
    }

    /**
     * Reads the rules a map's content is painted by: the paint restyled on a copy, an itinerary's strokes, a
     * point of interest's style and category, an isochrone band's colour, and a route's restricted stretches as
     * features.
     */
    fun paint() {
        val paint =
            MapPaint.standard.copy {
                itinerary.casingColor = "#1b1b1b"
                itinerary.lineWidthStops = listOf(10.0, 3.0, 16.0, 6.0)
                segments.palette = listOf("#e64980", "#228be6")
                layerRanks += "acme_avalanche" to 40
            }
        seen(paint == MapPaint.standard, paint.itinerary.casingOpacity, paint.itinerary.casingWidthStops)
        seen(paint.itinerary.dashArray, paint.itinerary.dashedOpacity, paint.segments.defaultColor, paint.layerRanks)
        val strokes =
            listOf(
                ItineraryStroke.Line(JsonPrimitive("#e64980")),
                ItineraryStroke.Dashed(JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("color")))),
                ItineraryStroke.Casing,
            )
        for (stroke in strokes) {
            val style: LineLayerStyle = itineraryStrokeStyle(stroke, paint)
            seen(style.layout, style.paint)
        }
        poiStyleOf(DataSchema.POICategory.Hut)?.let { hut -> seen(hut.glyph, hut.color.hex) }
        seen(poiCategoryStyles.size, poiTileCategories.size, poiCategoryOf("Alpine_Hut"))
        seen(PoiStyle("hut", HexColor("#0b7285")), isochroneColor(-1))
        seen(LineLayerStyle(mapOf("line-cap" to JsonPrimitive("round")), mapOf("line-width" to JsonPrimitive(4))))
        val stretches = roadAccessFeatures(GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))), null)
        seen(stretches.map { it.properties["kind"] })
    }

    /**
     * Reads an itinerary by id as an element does: one cache per client, an engine over it, and the projections
     * a resolved itinerary holds, with the rules that read them off the wire.
     */
    suspend fun itineraries(
        mountaya: Mountaya,
        scope: CoroutineScope,
    ) {
        val cache =
            ItineraryResourceCache(scope) { id, surface ->
                mountaya.studio.itineraries
                    .getItinerary(id) { if (surface != null) this.surface = surface }
                    .data
            }
        seen(ItineraryResourceCache.capacity)
        val engine = ItineraryResourceEngine(cache, itineraryId = "itn_4e5f")
        when (
            val resource =
                engine.state.first {
                    it is ItineraryResource.Resolved || it is ItineraryResource.Failed
                }
        ) {
            is ItineraryResource.Resolved -> {
                val itinerary = resource.itinerary
                seen(itinerary.geometry, itinerary.characteristics, itinerary.legs.map { it.name })
                seen(itinerary.waypoints.mapNotNull { it.name })
            }

            is ItineraryResource.Failed -> {
                seen(resource.cause)
            }

            ItineraryResource.Idle, ItineraryResource.Resolving -> {
                seen(resource)
            }
        }
        engine.configure(cache, itineraryId = null)
        cache.invalidate("itn_4e5f")
        cache.invalidate()
        engine.close()
        seen(
            itineraryGeometry(emptyList()),
            itinerarySegments(emptyList(), emptyList()),
            itineraryWaypoints(emptyList()),
        )
        val leg = ItineraryLeg(id = "climb", name = "Montée", sectionDistance = 4_200.0, sectionAscent = 850)
        val place =
            ItineraryPlace(
                id = "w1",
                lngLat = listOf(6.86, 45.83),
                name = "Refuge",
                details =
                    WaypointDetails(
                        description = "Gardé de juin à septembre",
                        timing = WaypointDetails.Timing(stopDuration = 900),
                    ),
            )
        seen(leg.memberIds, leg.geometry, place.glyph, place.details?.contact)
    }

    /** Returns the states a host's own screen is tested against. */
    fun state(): NavigationState =
        NavigationState(
            status = NavigationStatus.Navigating,
            progress =
                NavigationProgress(
                    along = 120.0,
                    step = 0,
                    next = 1,
                    toManeuver = NavigationRemainder(distance = 380.0, duration = 240.0),
                    toArrival = NavigationRemainder(distance = 5_080.0, duration = null),
                ),
            error = NavigationError.EmptyRoute(),
        )
}

/**
 * StandingStill is a host's own engine, implemented from outside: it follows nothing, and reports
 * the one state it holds.
 */
class StandingStill : NavigationEngine<String> {
    private val held = MutableStateFlow(NavigationState.idle)

    override val state: StateFlow<NavigationState> = held.asStateFlow()

    override fun start(request: String) {
        held.value = NavigationState(NavigationStatus.Routing)
    }

    override fun reroute(request: String) = Unit

    override fun setPosition(position: List<Double>) {
        require(position.size >= 2 && position[0].isFinite() && position[1].isFinite()) { "A position names a place." }
    }

    override fun stop() {
        held.value = NavigationState.idle
    }

    override fun close() = Unit
}
