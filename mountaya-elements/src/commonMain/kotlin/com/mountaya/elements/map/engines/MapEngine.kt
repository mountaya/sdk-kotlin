package com.mountaya.elements

import com.mountaya.client.ApiException
import com.mountaya.client.FieldSelection
import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeature
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.GeolocateControlTarget
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.LOCATE_MAX_ZOOM
import com.mountaya.geo.LayersSelectionTarget
import com.mountaya.geo.MapPaint
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.OverlayKind
import com.mountaya.geo.ReadoutPoint
import com.mountaya.geo.SearchHighlight
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.accuracyBounds
import com.mountaya.geo.boundsOf
import com.mountaya.geo.directionsGeoJson
import com.mountaya.geo.findOverlay
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.isochronesGeoJson
import com.mountaya.geo.itineraryBounds
import com.mountaya.geo.labelLanguage
import com.mountaya.geo.matrixGeoJson
import com.mountaya.geo.maxBoundsLongitudes
import com.mountaya.geo.normalizeBoundingBox
import com.mountaya.geo.paintZoomRange
import com.mountaya.geo.placesGeoJson
import com.mountaya.geo.poisGeoJson
import com.mountaya.geo.refusesMaxBounds
import com.mountaya.geo.specificationBounds
import com.mountaya.geo.suggestionsGeoJson
import com.mountaya.geo.themeTokenValue
import com.mountaya.geo.timestampedSources
import com.mountaya.geo.wrapLongitude
import com.mountaya.geo.wrapLongitudePastRange
import com.mountaya.geo.zoomIntoRange
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.reflect.KClass
import kotlin.reflect.safeCast
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** A turn, in degrees: a pan lock at least this wide covers every longitude. */
private const val DEGREES_PER_TURN: Double = 360.0

/** How far from a marker's point, in density-independent pixels, a click is on it. */
private const val MARKER_RADIUS: Double = 22.0

/** The id the points of interest a query answers are drawn under, `MapEngine.poisAnalysisId`. */
private const val POIS_ANALYSIS_ID: String = "analysis:pois"

/**
 * MapEngine holds a map's state: the base map, the camera and the pan lock and zoom range it is held to,
 * the language the labels speak, every source and layer drawn above the base map, the markers shown above
 * them, the waypoint being placed, and whether a refusal of access stands, which `MapView` renders on
 * Android and a consumer who draws a map of their own reads as [state] on any target.
 *
 * It takes the app's `Mountaya`, never a client of its own: the map reads the Tile API's catalogue and the
 * content it draws by id through it, and the renderer sends its own requests for tiles, styles, sprites and
 * glyphs with its credential and its language (`Mountaya.mapRequestHeaders`). A map given no label language
 * labels in the client's, the one it sends as `Accept-Language`, before the device's
 * (`geo.style.label-language`).
 *
 * ```
 * val map = MapEngine(mountaya)
 * map.setTerrain()
 * map.setItinerary(itineraryId, fit = CameraFit(padding = PixelPadding(48.0)))
 * map.setZones(true)
 * ```
 *
 * It holds content in slots, each drawing its latest request (`map.slots.latest-request`): the itinerary and
 * the collection drawn by id, the itineraries and zones switched on, the classic overlay and the enrichments
 * the catalogue names, a specification an app draws under a name of its own, each Data API result drawn
 * under its id, and the three geometry slots. Every layer goes through the one layer registry, which ranks it
 * by `geo.layers.order`, keeps the caller's word on its visibility by its id (`map.layers.visibility`), and is
 * drawn again on every base map a switch brings in (`map.basemap.replay`). The classic overlay the server stamps is
 * drawn for the instant pinned last, [setInstant], and from the manifest's own addresses with none pinned
 * (`map.overlays.instant`). Beside each itinerary line it draws
 * the route's restricted stretches, shown until the app hides them (`map.road-access.layers`), and above everything
 * else the result a reader picked from a search, [setSearchHighlight] (`map.search.result-highlight`). A layer is
 * changed by a patch (`map.engine.patch`): a member it states replaces the one held, `JsonNull`, `false`, `0`
 * and the empty string included, and a member it maps to `null` leaves the one held as it is.
 *
 * The camera moves by [jump], [ease] and [fly], each taking the members a [CameraTarget] states and keeping
 * the rest (`map.camera.pose`), frames a box by [fitBounds], a fit this SDK computes rather than asks of its
 * renderer (`geo.camera.fit`), steps by [zoomIn], [zoomOut] and [resetNorth] (`map.camera.control-moves`),
 * and locates the device by [locate] (`map.camera.locate`). A point query, [featuresAt] and its siblings,
 * asks only about the layers the map placed (`map.query.layers`), and what happens on the map is told by
 * [events]: a click, a long click, the features a click lands on and a hovering pointer is over, a refusal
 * of access and its end, a failed request, and a change of the layers catalogue.
 *
 * A waypoint is placed by an edit, [beginWaypointEdit] to [endWaypointEdit]: each click on the map places
 * it, [placeWaypoint], and where it falls within 40 pixels of one of the active segments the edit's magnet
 * places it on the segment (`map.edit.snap`).
 *
 * Its marker sprites, the images a waypoint's and a point of interest's symbol layers name, are drawn from
 * [glyphs], the bundled marker set unless the app hands in a set of its own: where a style names
 * `section-icon-` or `poi-icon-` then a glyph, a hyphen and six hexadecimal digits, the map draws that glyph
 * in white on a disc of that colour (`map.icons.missing-image`).
 *
 * It launches nothing, so it takes no scope: every read runs in the coroutine of the call that asked for it,
 * and a call is withdrawn by cancelling that coroutine. It is driven from one thread, the one its commands are
 * called on, as Compose's state is; each command has its effect before it returns, and [state] reports a state
 * only where it differs from the one held. A closed map is inert (`map.engine.closed`).
 *
 * @param client The app's own client.
 * @param style A base map's style document of the app's own, null until [setTerrain] or [setStyle] chooses
 * one.
 * @param terrainVariant The variant every terrain of the catalogue is drawn in until [setTerrainVariant] asks
 * for another, light unless the app names dark.
 * @param camera Where the camera stands.
 * @param labelLanguage Where given, the language the labels speak, read as a label language is, by the
 * languages the base map's tiles carry.
 * @param paint How the map paints what it draws, `MapPaint.standard` unless the app restyles a copy of it:
 * the itinerary stroke and the band each tile source layer draws at.
 * @param glyphs The set the map's marker sprites and its markers' pins are drawn from, a licensed set of the
 * app's own, alone or chained over the bundled one, in place of `GlyphSet.marker`.
 * @param geolocation Where [locate] asks for the device's position: the app's own, `InertGeolocation`, or,
 * where it is null, the device's location services as the view drawing the map reaches them, which
 * `MapView` hands the engine on Android; with no view to reach them through, a locate answers that the
 * platform has none.
 * @param transformRequest The app's own hook for every request the renderer makes for itself, handed each
 * as a [MapRequest] and called first: the URL it answers is the one the client's credential is claimed by,
 * and a header it sets stands over the client's. It is called on a thread of the renderer's own, and does
 * not suspend.
 */
public class MapEngine(
    client: Mountaya,
    style: Url? = null,
    terrainVariant: TileSchema.TerrainVariant = TileSchema.TerrainVariant.Light,
    camera: CameraPose = CameraPose(listOf(0.0, 0.0), 0.0, 0.0, 0.0),
    labelLanguage: RestSchema.Language? = null,
    paint: MapPaint = MapPaint.standard,
    glyphs: GlyphSet = GlyphSet.marker,
    geolocation: Geolocation? = null,
    transformRequest: ((MapRequest) -> MapRequest)? = null,
) : AutoCloseable,
    LayersSelectionTarget,
    GeolocateControlTarget,
    CursorMarkerTarget,
    LegEmphasisTarget {
    /** The glyph set the map's marker sprites and its markers' pins are drawn from, fixed for the engine's life. */
    public val glyphs: GlyphSet = glyphs

    /**
     * How the map paints what it draws above its base map, fixed for the engine's life: the itinerary stroke
     * the lines it draws from data take, and the rank each tile source layer draws at (`geo.style.map-paint`).
     */
    public val paint: MapPaint = paint

    /** The app's client, whose credential and language the renderer's own requests carry. */
    internal val client: Mountaya = client

    /** The app's own hook for the renderer's requests, called before the client's credential is attached. */
    internal val transformRequest: ((MapRequest) -> MapRequest)? = transformRequest

    /** Where a locate's fix comes from, as the app handed it, or null for the device's. */
    private val geolocation: Geolocation? = geolocation

    /**
     * The device's location services, as the view that drew the map last reached them: what a locate asks
     * where the app handed no geolocation of its own.
     */
    internal var deviceGeolocation: Geolocation? = null

    private val held =
        MutableStateFlow(
            MapState(
                style = style,
                camera = camera,
                labelLanguage = labelLanguageOf(labelLanguage, client),
                terrainVariant = terrainVariant,
            ),
        )

    /** The one layer registry, which ranks every layer the map draws. */
    private val registry = MapLayerRegistry()
    private var closed = false

    /** What draws the map, which the engine asks what only a renderer knows, or null where nothing draws it. */
    private var surface: MapSurface? = null

    /**
     * The channel of every collector of [events] that has not ended, or null once the map is closed. It is
     * swapped whole, so a collector may begin on any thread while the engine tells on its own.
     */
    private val listeners = MutableStateFlow<List<SendChannel<MapEvent>>?>(emptyList())

    /** The refusals of access that stand. */
    private var standing = StandingRefusals()

    /** Where a hovering pointer last moved, the point of the canvas and the place under it, waiting on the next frame. */
    private var hovering: Pair<List<Double>, List<Double>>? = null

    /** The id of every symbol layer of the base map whose label the engine rewrote. */
    private val relabelled = mutableListOf<String>()

    /** The requests made of the map's slots, which a load that lands late draws nothing by. */
    private val slots = SlotRequests()

    /** What each slot draws, in the order each slot was first asked for. */
    private val contents = mutableListOf<MapContent>()

    /**
     * The id of every layer the app hid, kept by its id before the layer is drawn, across a base-map switch
     * and through a redraw of the content holding it.
     */
    private val hiddenLayerIds = mutableListOf<String>()

    /** The studio scope the catalogue is read for, or null for none. */
    private var layersOrganization: String? = null

    /** The number of the catalogue read out for the scope named, or null for none. */
    private var manifestRead: Int? = null

    /** How many catalogue reads the map sent. */
    private var manifestReads = 0

    /** Every load waiting on a catalogue read another load sent. */
    private val manifestWaiters = mutableListOf<CompletableDeferred<TileSchema.Layers>>()

    /** The variant the app asked for last, light until it asks for dark. */
    private var terrainVariantAsked: TileSchema.TerrainVariant = terrainVariant

    /** How many base maps the app asked for, of which the latest is shown. */
    private var baseMapRequests = 0

    /** The terrain a [setTerrain] still reading the catalogue asks for. */
    private var terrainReading: TerrainAsk? = null

    /** The terrain asked for last that the catalogue did not refuse. */
    private var terrainLastAsked: TerrainAsk? = null

    /** The base map's style the renderer last loaded, which a base map that does not arrive gives the map back to. */
    private var shownStyle: Url? = style

    /** The terrain the renderer last loaded, which a base map that does not arrive gives the map back to. */
    private var shownTerrain: String? = null

    /**
     * The base map, its style and its terrain, the map was given back where the document of the one asked for did
     * not arrive, until it loads again, or null where none was given back: it answers nothing as it loads.
     */
    private var givenBack: Pair<Url?, String?>? = null

    /** Every Data API result the map draws, by the id it is drawn under. */
    private val analyses = mutableListOf<Pair<String, HeldAnalysis>>()

    /** The abort of each query out for a result, by the result's id. */
    private val queryAborts = mutableListOf<Pair<String, QueryAbort>>()

    /** How many results the map was asked to add, which numbers their ids. */
    private var analysisCount = 0

    /** What each geometry slot written holds. */
    private val geometrySlots = mutableListOf<Pair<GeometrySlot, SlotGeometry>>()

    /** What the map shows. */
    public val state: StateFlow<MapState> = held.asStateFlow()

    /**
     * Whether the map shows the restricted stretches of the itineraries it draws: the stripes over each stretch
     * of a route that follows a way restricting who may pass, and the warning icon of its kind. They are shown
     * until the app hides them, [setAccessRestrictionsVisibility].
     */
    public var isAccessRestrictionsVisible: Boolean = true
        private set

    /**
     * The layers catalogue the map holds, read for the studio scope it names, or null before a read lands and
     * once the scope moved (`map.layers.manifest`). It never reads: [loadLayers] does, and each change is told
     * as `MapEvent.LayersChange`.
     */
    public var layersManifest: TileSchema.Layers? = null
        private set

    /**
     * The instant the classic overlay the server stamps is drawn for, or null with none pinned, the manifest's own
     * addresses then drawn (`map.overlays.instant`). [setInstant] pins it, and it outlives the overlay it was asked for.
     */
    public var instant: Instant? = null
        private set

    /** The instant [instant] holds, which [pinnedInstant] reports. */
    private val instantStanding = MutableStateFlow<Instant?>(null)

    /**
     * The instant [instant] holds, as a flow told each pin [setInstant] moves, which a readout open on the map follows
     * live, so its moment moves with the pin while the band it read at the press stays (`geo.readout.rows`).
     */
    internal val pinnedInstant: StateFlow<Instant?> = instantStanding.asStateFlow()

    /**
     * The organization the client's publishable key belongs to, as the read of it answered, or null before it answers,
     * where it failed, and where the client holds no publishable key (`map.organization.read`). It never reads:
     * [loadOrganization] does. Its answer serves the map's watermark and nothing else the map draws: no control, unit,
     * language, base map or colour is read from it.
     */
    public var organization: RestSchema.Organization? = null
        private set

    /** Where the map's read of its organization stands, which [organizationRead] reports. */
    @OptIn(InternalMountayaApi::class)
    private val organizationStanding =
        MutableStateFlow<OrganizationRead>(
            if (client.publishableKey == null) OrganizationRead.NoKey else OrganizationRead.Waiting,
        )

    /**
     * Where the map's read of its organization stands, in the five states the contract names in every SDK's public
     * type for it (`map.organization.read`), which the watermark of every view drawing the map follows
     * (`geo.organization.watermark`): waiting from the map's making until its read answers or has gone one attempt's
     * budget without an answer, the client's budget for `postOrganizationLookup`, timed out from then on while the
     * read's further attempts go on, failed once a read failed, each drawing the mark while a later read goes on,
     * answered once a read answers, with whether the organization holds white label, the organization itself held by
     * [organization], and no key where the client holds no publishable key, which reads nothing. Each
     * [loadOrganization] moves it, the host's and a view's alike, and a closed map leaves it where it stood: `value` is
     * where it stands, and a collector is told each move. A host reads it to keep its own views clear of the mark's
     * frame until the read answers white label, as the map's own controls, panels and readout do.
     */
    public val organizationRead: StateFlow<OrganizationRead> = organizationStanding.asStateFlow()

    /** Where the view drawing the map laid it out last, which [shownMap] reports. */
    private val shownMapStanding = MutableStateFlow<ShownMap?>(null)

    /**
     * Where the view drawing the map laid it out last, its rect and its safe area in its window, or null while no view
     * draws it: a control stack, its panels and every other part of the SDK's own interface laid over the map read the
     * map's real frame here, and the watermark's frame measured from its safe area, never from a box of their own
     * (`geo.organization.watermark`). The view laid out last decides.
     */
    internal val shownMap: StateFlow<ShownMap?> = shownMapStanding.asStateFlow()

    /** Records where the view drawing the map laid it out, [map]. */
    internal fun shown(map: ShownMap) {
        shownMapStanding.value = map
    }

    /** Forgets where a view laid the map out, where [map] is still the place recorded: that view draws it no more. */
    internal fun unshown(map: ShownMap) {
        shownMapStanding.compareAndSet(map, null)
    }

    /** Completed when the map closes, which a load of its organization waiting on a read is refused at. */
    private val closing = CompletableDeferred<Unit>()

    /**
     * The readout of the view drawing the map (`map.readout.open`): a long press the map reads opens it where it falls,
     * and a tap on the map and the start of every camera move, the reader's gesture as it begins and a move the app or a
     * control asks for as it is asked, close it. Null where no view holding one draws the map.
     */
    internal var readout: ReadoutEngine? = null

    /** The classic overlay drawn, and the catalogue it was found in, whose addresses a pin moved stamps again. */
    private var drawnOverlay: Pair<TileSchema.Overlay, TileSchema.Layers>? = null

    /** The classic overlay the map draws, which the readout states the band of (`geo.readout.rows`), or null. */
    internal val classicOverlay: TileSchema.Overlay? get() = drawnOverlay?.first

    /**
     * Answers the band of the classic overlay the map draws at [point] of the canvas (`geo.readout.rows`): the first
     * feature one of its layers drew there, topmost first, stating a `minValue` or a `maxValue`, or null where the map
     * draws no classic overlay or none of its features there states either. A feature another layer drew, an
     * enrichment's or the app's own, is no band, whatever it states.
     */
    internal fun classicOverlayBandAt(point: List<Double>): ReadoutPoint.Band? {
        if (drawnOverlay == null) return null
        val layers = contents.firstOrNull { it.slot == ContentSlot.Overlay }?.layerIds?.toSet() ?: return null

        fun number(
            feature: QueriedFeature,
            key: String,
        ): Double? = (feature.properties[key] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull
        return featuresAt(point).firstNotNullOfOrNull { feature ->
            if (feature.layer !in layers) return@firstNotNullOfOrNull null
            val min = number(feature, "minValue")
            val max = number(feature, "maxValue")
            if (min == null && max == null) null else ReadoutPoint.Band(min, max)
        }
    }

    /** Whether a hovering pointer's move waits on the next frame, which a renderer counts frames for. */
    internal val isHoverWaiting: Boolean get() = hovering != null

    /**
     * Returns every event the map tells from the moment a collector begins, in order, for as long as it collects: each
     * click and long click no edit takes, the features a click lands on and those a hovering pointer is over, each
     * refusal of access and the end of the last one standing, each failure of one of the map's own requests, and each
     * change of the layers catalogue it holds. Each collector is told every event, none dropped however slowly it
     * reads. The flow ends at [close], after which nothing is told, and one collected after it ends at once.
     */
    public fun events(): Flow<MapEvent> =
        flow {
            val channel = Channel<MapEvent>(Channel.UNLIMITED)
            if (!listen(channel)) return@flow
            try {
                for (event in channel) emit(event)
            } finally {
                stopListening(channel)
            }
        }

    // The base map.

    /**
     * Shows the base map of a style document of the app's own at [style], or none, in place of any terrain of
     * the catalogue. Everything the map draws above its base map is drawn again on it, each layer at its rank
     * and a hidden one hidden, a layer added with [addLayer] included (`map.basemap.replay`). A document that
     * does not arrive leaves the base map shown and its content as they were.
     */
    public fun setStyle(style: Url?) {
        if (closed) return
        baseMapRequests += 1
        terrainReading = null
        terrainLastAsked = null
        report { it.copy(style = style, terrain = null, terrainVariant = terrainVariantAsked) }
    }

    /**
     * Shows a terrain of the Tile API's catalogue as the base map (`map.basemap.terrain`): the one of slug
     * [slug], as the catalogue spells it, or the first it lists where [slug] is null, in the variant asked for
     * last, light until [setTerrainVariant] asks for dark. It reads the catalogue through [loadLayers], once for
     * the studio scope the map names, and every layer the map draws is drawn again on the base map it brings in.
     *
     * The latest base map asked for wins: a [setTerrain] or a [setStyle] asked for while this one reads the
     * catalogue is the one shown, and this one returns without switching. `state.terrain` names the terrain
     * from the moment the catalogue names it, and a document that does not arrive gives the map back the base
     * map still shown.
     *
     * A catalogue read the API refuses access, a 401, a 402 or a 403, is told as `MapEvent.AccessRefused` at
     * the catalogue's address and stands until the next style loads; one failing for any other reason is told
     * as a `MapEvent.Failure` (`map.events.access-refused`). Either way it throws what the client throws, and a
     * [MapException] of kind `TerrainNotFound` where the catalogue lists no such terrain, or none at all; the
     * base map is then left as it was. A closed map reads nothing.
     */
    override suspend fun setTerrain(slug: String?) {
        if (closed) return
        baseMapRequests += 1
        val request = baseMapRequests
        val asked = TerrainAsk(request, slug)
        terrainReading = asked
        terrainLastAsked = asked
        val catalogue =
            try {
                loadLayers()
            } catch (failure: Throwable) {
                if (request == baseMapRequests) {
                    terrainReading = null
                    // A read the map gave up on, or one a later base map superseded, is told to nobody.
                    if (failure !is CancellationException && failure !is MapException) {
                        requestFailed(
                            layersManifestAddress(client.baseUrls.tiles.toString()),
                            (failure as? ApiException)?.status,
                            null,
                        )
                    }
                }
                throw failure
            }
        if (request == baseMapRequests) terrainReading = null
        // The variant asked for while the catalogue was read is the one drawn.
        val found = baseMapOf(catalogue.terrains, slug, terrainVariantAsked)
        if (found == null) {
            // A refused slug is never the terrain a later variant asks for again.
            if (terrainLastAsked?.request == request) terrainLastAsked = null
            throw MapException(MapException.Kind.TerrainNotFound, slug.orEmpty())
        }
        if (request != baseMapRequests || closed) return
        val (terrain, style) = found
        report {
            it.copy(
                style = style,
                terrain = terrain.slug,
                terrainVariant = terrainVariantOfStyle(terrain, style) ?: terrainVariantAsked,
            )
        }
    }

    /**
     * Draws the catalogue's terrains in [variant], light or dark, the app's word apart from any colour scheme
     * around the map, from here on (`map.basemap.terrain`). The terrain the map shows or is switching to, or,
     * on a map that shows none because the one asked for last did not arrive, that one, is asked for again in
     * it through [setTerrain]; a terrain whose two variants are one document, and the variant already drawn,
     * ask the renderer for nothing. Under a style of the app's own, and before any terrain, nothing is switched,
     * and the next terrain is drawn in [variant]. It throws what [setTerrain] throws for the terrain asked for
     * again, and the variant stands either way.
     */
    public suspend fun setTerrainVariant(variant: TileSchema.TerrainVariant) {
        if (closed) return
        terrainVariantAsked = variant
        val shown = held.value
        val again =
            terrainReading
                ?: shown.terrain?.let { TerrainAsk(baseMapRequests, it) }
                ?: if (shown.style == null) terrainLastAsked else null
        if (again == null) {
            report { it.copy(terrainVariant = variant) }
            return
        }
        setTerrain(again.slug)
    }

    // The layers catalogue.

    /**
     * Answers the layers catalogue, `getTileLayers`, read through the client for the studio scope
     * [setLayersOrganization] names, and held: every terrain, overlay and enrichment the credential draws
     * (`map.layers.manifest`). [setTerrain], [setOverlay] and [addEnrichment] resolve their slugs through it, so
     * a map that switches base maps reads it once.
     *
     * It is read once for the scope and shared by every load until the scope moves: a load made while a read
     * is out waits on that read. Each read that lands is told as `MapEvent.LayersChange`. A read that fails is
     * kept by nobody, so the next load reads again, and every load waiting on it throws what it failed with. A
     * read that lands, or fails, after the scope moved is another scope's: it is kept by nobody and told to
     * nobody, and every load waiting on it is answered the current scope's catalogue instead, read again where
     * no read is out for it. Every read runs in the coroutine of a load waiting on it. A closed map reads
     * nothing more: it answers the catalogue it held, and throws a [MapException] of kind `Closed` where it held
     * none.
     */
    override suspend fun loadLayers(): TileSchema.Layers {
        layersManifest?.let { return it }
        if (closed) throw MapException(MapException.Kind.Closed, "loadLayers")
        if (manifestRead != null) return waitForLayers()
        return readLayers()
    }

    /**
     * Names the studio scope the layers catalogue is read for: an organization, sent as `organization_id`, or
     * null, or the empty text, for the read that names none (`map.layers.manifest`). Moving the scope drops the
     * catalogue held, which [layersManifest] then reads as null and `MapEvent.LayersChange` tells, and a read
     * still out for the scope before is another scope's as it lands. Naming the scope the map already reads for
     * changes nothing.
     *
     * What the map already draws stays drawn: an overlay, the itineraries and the zones keep the tile addresses
     * of the scope they were read in, so an app redraws what it still wants once the new catalogue lands.
     */
    public fun setLayersOrganization(organizationId: String?) {
        if (closed) return
        val named = organizationId?.ifEmpty { null }
        if (named == layersOrganization) return
        layersOrganization = named
        manifestRead = null
        if (layersManifest == null) return
        layersManifest = null
        tell(MapEvent.LayersChange(null))
    }

    // The camera.

    /**
     * Moves the camera to [target] at once: the members it states, every other kept where the camera has it,
     * the zoom held within the zoom range (`map.camera.pose`).
     *
     * It throws a [MapException] of kind `PositionRefused` where the centre is not two finite numbers, the
     * latitude from -90 to 90, and of kind `NumberRefused` where the zoom, the bearing or the pitch is not a
     * finite number; nothing moves.
     */
    public fun jump(target: CameraTarget) {
        move(target, CameraMotion.Jump)
    }

    /** Moves the camera to [target] along a straight ease, the default for a move the app makes, as [jump] reads it and refuses what it refuses. */
    public fun ease(target: CameraTarget) {
        move(target, CameraMotion.Ease())
    }

    /** Moves the camera to [target] along the curved flight that pulls out and back in, as [jump] reads it and refuses what it refuses. */
    public fun fly(target: CameraTarget) {
        move(target, CameraMotion.Fly)
    }

    /**
     * Moves the camera to frame [bounds], `[west, south, east, north]` in degrees, with the padding, the ceiling
     * and the duration [fit] states (`map.camera.fit-options`): facing north at the highest zoom at which the
     * box fits the canvas less its padding, within the zoom range and under the fit's ceiling, keeping its
     * pitch (`geo.camera.fit`).
     *
     * The box is read as `normalizeBoundingBox` writes it, so one drawn from its east corner is framed the
     * right way round and one across the antimeridian, its east edge past 180, is framed across it. The fit is
     * computed on the canvas the map is drawn on, so a map no view draws frames nothing.
     *
     * It throws a [MapException] of kind `NumberRefused` where the fit states a padding or a duration that is
     * not a finite number from 0 up, or a ceiling that is not a finite number, and of kind `PositionRefused`
     * where the box is not four finite numbers; nothing moves.
     */
    public fun fitBounds(
        bounds: List<Double>,
        fit: CameraFit = CameraFit(),
    ) {
        if (closed) return
        val read = readCameraFit(fit)
        val box = normalizeBoundingBox(bounds)
        if (!box.all { it.isFinite() }) throw MapException(MapException.Kind.PositionRefused, bounds.toString())
        frame(box, read)
    }

    /**
     * Eases the camera one zoom in from the zoom it stands at, unrounded and held within the zoom range, its
     * centre, bearing and pitch kept (`map.camera.control-moves`).
     */
    public fun zoomIn() {
        controlMove(ControlMove.ZoomIn)
    }

    /**
     * Eases the camera one zoom out from the zoom it stands at, unrounded and held within the zoom range, its
     * centre, bearing and pitch kept (`map.camera.control-moves`).
     */
    public fun zoomOut() {
        controlMove(ControlMove.ZoomOut)
    }

    /**
     * Eases the camera back to facing north and looking straight down, its bearing and its pitch turned to 0,
     * its centre and zoom kept, which is what a compass means by reset (`map.camera.control-moves`).
     */
    public fun resetNorth() {
        controlMove(ControlMove.ResetNorth)
    }

    /**
     * Eases the camera into [window], the zooms a specification paints across (`paintZoomRange`): a camera
     * below it moves up to its floor and one above it down to its ceiling, the nearer edge, and one inside it,
     * or before a window no move satisfies, stays (`geo.camera.zoom-into-range`). A layer outside its window
     * paints nothing and reports nothing, so a switch that turns one on from too far out would otherwise read
     * as a dead control.
     */
    public fun easeIntoZoomRange(window: ZoomBounds) {
        if (closed) return
        val zoom = zoomIntoRange(held.value.camera.zoom, window) ?: return
        move(CameraTarget(zoom = zoom), CameraMotion.Ease(zoomEase))
    }

    /**
     * Asks the device for its position, one fix, through the map's geolocation, and frames it: the camera fits
     * the box its accuracy spans (`accuracyBounds`) at `LOCATE_MAX_ZOOM` at most, so a coarse fix frames the
     * area it may be anywhere in and a precise one a street. A fix that states no accuracy, or one that is no
     * finite number from 0 up, is framed on its position alone. It answers the position,
     * `[longitude, latitude]` (`map.camera.locate`). A fix that arrives once the map is closed is answered and
     * frames nothing.
     *
     * It throws a [MapException] of kind `FixRefused` where the geolocation answers no fix, naming its reason,
     * and nothing moves; and of kind `Closed` on a closed map, which asks the device nothing, since asking may
     * prompt its reader and nothing would follow.
     */
    override suspend fun locate(): List<Double> = locate(jumps = false)

    /**
     * Locates as [locate] does, the fix framed at once where [jumps], as a locate control frames it for a reader
     * who turned animations off, and eased otherwise.
     */
    internal suspend fun locate(jumps: Boolean): List<Double> {
        if (closed) throw MapException(MapException.Kind.Closed, "locate")
        val seam = geolocation ?: deviceGeolocation ?: InertGeolocation()
        return when (val fix = seam.locate()) {
            is GeolocationFix.Refused -> {
                throw MapException(MapException.Kind.FixRefused, "locate", fix.reason)
            }

            is GeolocationFix.Located -> {
                val accuracy = fix.accuracyMeters
                val radius = if (accuracy.isFinite() && accuracy > 0.0) accuracy else 0.0
                val box = normalizeBoundingBox(accuracyBounds(fix.position, radius))
                val fit = CameraFit(maxZoom = LOCATE_MAX_ZOOM, duration = Duration.ZERO.takeIf { jumps })
                if (box.all { it.isFinite() }) frame(box, fit)
                fix.position
            }
        }
    }

    /**
     * Reads, through the map's geolocation and without asking anyone, whether the standing permission refuses a fix:
     * true where it answers `GeolocationPermission.Denied`, the reader's refusal, the platform's restriction or a
     * reader allowing only an approximate fix, and false where it is granted, never asked for, or cannot be known. A
     * locate control says location is blocked on this word alone (`geo.controls.geolocate-state`), and a closed map
     * still answers it. It reads the geolocation [locate] asks, the device's where the app handed none, and an inert
     * one, which reads `Denied`, with no view to reach the device's through.
     */
    override suspend fun isLocationRefused(): Boolean =
        (geolocation ?: deviceGeolocation ?: InertGeolocation()).permission() == GeolocationPermission.Denied

    /**
     * The slug of the catalogue terrain the base map is, as `state.value.terrain` holds it, or null under a style of
     * the app's own and before any terrain: the terrain a layers selection states once the catalogue lands
     * (`geo.layers.selection`).
     */
    override val terrain: String?
        get() = held.value.terrain

    /**
     * Holds the camera within a pan lock, `[west, south, east, north]` in degrees, or lifts the lock where
     * [bounds] is null.
     *
     * The lock is read as a box is: each axis ordered low to high, so a west edge greater than the east one is
     * a box drawn from its east corner, and one crossing the antimeridian is written with its east edge past
     * 180; each latitude held within the latitudes a map draws; and a box a turn or more wide is the whole
     * world a hair under a turn, which a renderer measures a width in ([maxBoundsLongitudes]). The renderer
     * then keeps what the map shows within it.
     *
     * It throws a [MapException] of kind `MaxBoundsRefused` where the lock so read encloses no area
     * ([refusesMaxBounds]), and the map is left as it was.
     */
    public fun setMaxBounds(bounds: List<Double>?) {
        if (closed) return
        if (bounds == null) {
            report { it.copy(maxBounds = null) }
            return
        }
        val box = normalizeBoundingBox(bounds)
        val lock =
            if (box[2] - box[0] >= DEGREES_PER_TURN) {
                val longitudes = maxBoundsLongitudes(box[0], box[2])
                listOf(longitudes[0], box[1], longitudes[1], box[3])
            } else {
                box
            }
        if (refusesMaxBounds(lock)) throw MapException(MapException.Kind.MaxBoundsRefused, bounds.toString())
        report { it.copy(maxBounds = lock) }
    }

    /**
     * Moves the zoom range the camera is held to, each limit as [min] and [max] state it
     * (`map.camera.zoom-window`): a limit left `ZoomLimit.Kept` stays where the map holds it, one
     * `ZoomLimit.Released` goes back to the renderer's own, MapLibre Native's floor of 0 and ceiling of 25.5,
     * and one given a zoom moves to it. A range of one zoom holds the camera at it, and a camera outside the
     * range is brought to its nearest end.
     *
     * ```
     * map.setZoomBounds(min = ZoomLimit.Zoom(8.0), max = ZoomLimit.Zoom(12.0))
     * map.setZoomBounds(max = ZoomLimit.Released)
     * ```
     *
     * A call that moves the range reports one state; one that leaves it as it is reports none. It throws, with
     * nothing applied, a [MapException] of kind `NumberRefused` where a limit is not a finite number, and of
     * kind `ZoomBoundsRefused` where the least zoom lies under the renderer's floor, or above the greatest, the
     * one stated beside it or the one held where it is stated alone, or where a greatest zoom stated alone lies
     * under the least one held.
     */
    public fun setZoomBounds(
        min: ZoomLimit = ZoomLimit.Kept,
        max: ZoomLimit = ZoomLimit.Kept,
    ) {
        if (closed) return
        val window = zoomWindowAfter(held.value.zoomBounds, min, max, rendererZoomLimits)
        report { it.copy(zoomBounds = window, camera = held(it.camera, window)) }
    }

    // Labels and credits.

    /**
     * Labels the map in [language], read as a label language is, or, where it is null, in the client's
     * language, then the device's. It relabels the base map's labels alone, never a layer the map placed, and
     * again on every base map a switch brings in (`map.style.label-scope`).
     */
    public fun setLabelLanguage(language: RestSchema.Language?) {
        if (closed) return
        val resolved = labelLanguageOf(language, client)
        report { it.copy(labelLanguage = resolved) }
        relabelBaseMap()
    }

    /**
     * What the map credits (`map.style.attribution`): Mountaya, always and first, then each source it draws by
     * the credit that source states, the base map's in the order its renderer reads them and then the map's own
     * in the order they were added, each credit once by its exact text and a blank one left out. A credit is
     * markup, which a renderer shows as it shows its own.
     */
    public val credits: List<String>
        get() {
            val shown = held.value
            val own =
                shown.sources.map { source ->
                    CreditedSource(
                        source.attribution,
                        shown.layers.any { it.source == source.id && draws(it, shown.camera.zoom) },
                    )
                }
            return mapCredits(surface?.baseSources.orEmpty() + own)
        }

    // Sources and layers.

    /** Adds a source the map's layers may draw from, replacing the one of its id in its place. */
    public fun addSource(source: MapSource) {
        report { it.copy(sources = withSource(it.sources, source)) }
    }

    /**
     * Takes off the source of id [id] and every layer drawing from it. A source the map does not hold is
     * nothing to take off.
     */
    public fun removeSource(id: String) {
        if (closed) return
        registry.removeLayers(id)
        val layers = registry.layers
        report { state -> state.copy(sources = state.sources.filterNot { it.id == id }, layers = layers) }
    }

    /**
     * Draws [layer] at its rank, through the one layer registry (`map.layers.rank-insertion`): beneath the
     * lowest layer of a greater rank and above every other, so layers of one rank stack in the order they were
     * placed. A layer added again under an id in use at the rank it holds keeps its place, and one added at
     * another rank moves to the top of its new band. A layer the app hid by its id is drawn hidden.
     *
     * The map holds it as it holds every layer, so it is drawn again on every base map a switch brings in,
     * until [removeLayer] takes it off.
     */
    public fun addLayer(layer: MapLayer) {
        if (closed) return
        registry.add(if (isHidden(layer.id)) layer.withVisibility(HIDDEN) else layer)
        val layers = registry.layers
        report { it.copy(layers = layers) }
    }

    /**
     * Merges a patch onto the layer drawn under [id], in its place (`map.engine.patch`).
     *
     * A member [paint] or [layout] states replaces the one held, or is added where none was, and `JsonNull`,
     * `false`, `0` and the empty string are values, a paint property set to `JsonNull` going back to the
     * style's default. A member mapped to `null` is unstated, which is what a caller forwards where it may not
     * have a value: it leaves the member held as it is. A member stated as the value held leaves it as it is too,
     * as the map compares what it holds, a number as a number: `4.0` restates `4` and changes nothing, while the
     * text `"4"` is another value.
     *
     * ```
     * map.editLayer("route", paint = mapOf("line-color" to JsonPrimitive("#228be6"), "line-opacity" to null))
     * ```
     *
     * It throws a [MapException] of kind `LayerNotFound` where no layer is drawn under [id], the map left as it
     * was, and does nothing once the engine is closed.
     */
    public fun editLayer(
        id: String,
        paint: Map<String, JsonElement?> = emptyMap(),
        layout: Map<String, JsonElement?> = emptyMap(),
    ) {
        if (closed) return
        val layer = registry.layer(id) ?: throw MapException(MapException.Kind.LayerNotFound, id)
        registry.replace(id, layer.with(mergedMembers(layer.paint, paint), mergedMembers(layer.layout, layout)))
        val layers = registry.layers
        report { it.copy(layers = layers) }
    }

    /**
     * Takes off the layer drawn under [id], and forgets whether the app hid it. A layer the map does not draw is
     * nothing to take off.
     */
    public fun removeLayer(id: String) {
        if (closed) return
        hiddenLayerIds.remove(id)
        if (!registry.remove(id)) return
        val layers = registry.layers
        report { it.copy(layers = layers) }
    }

    // Content.

    /**
     * Draws the itinerary of id [id], its specification read through the client, `getItinerarySpecification`,
     * in place of the itinerary drawn before, and answers whether it drew (`map.slots.latest-request`).
     *
     * The latest call draws, whatever order the reads answer in: a read that answers after a later call for the
     * itinerary, or after [clearItinerary], draws nothing, and the call answers false. A call is withdrawn by
     * cancelling its coroutine: withdrawn before it drew, it throws the `CancellationException` and gives the
     * itinerary back to the call it superseded, which draws as its own read answers; withdrawn once it drew, it
     * changes nothing. A call a later one superseded waits for that one to draw, end or be withdrawn before it
     * answers.
     *
     * Where [fit] is given, the itinerary's geometry is read beside it, `getItinerary`, and the camera frames
     * the box its segments span as [fitBounds] frames one, once the call drew and never for a call that drew
     * nothing (`geo.camera.content-bounds`). The fit is read before anything is read or drawn: it throws a
     * [MapException] of kind `NumberRefused` where it states a padding or a duration that is not a finite number
     * from 0 up, or a ceiling that is not a finite number (`map.camera.fit-options`). It throws what the client
     * throws, and a [MapException] of kind `TypeRefused` where the specification holds a source of a type the
     * contract does not declare, nothing of it drawn. A closed map reads and draws nothing, and answers false.
     */
    public suspend fun setItinerary(
        id: String,
        fit: CameraFit? = null,
    ): Boolean {
        if (closed) return false
        val framing = fit?.let(::readCameraFit)
        val loaded =
            load(ContentSlot.Itinerary) {
                if (framing == null) {
                    SlotLoad(client.tiles.getItinerarySpecification(id).data)
                } else {
                    coroutineScope {
                        val specification = async { client.tiles.getItinerarySpecification(id).data }
                        val itinerary =
                            async {
                                client.studio.itineraries
                                    .getItinerary(id)
                                    .data
                            }
                        SlotLoad(specification.await(), itineraryBounds(itinerary.await().segments))
                    }
                }
            } ?: return false
        if (framing != null) loaded.bounds?.let { frameBox(it, framing) }
        return true
    }

    /** Takes off the itinerary drawn, and a read still out for one draws nothing. */
    public fun clearItinerary() {
        unmount(ContentSlot.Itinerary)
    }

    /**
     * Draws the collection of id [id], every itinerary and zone it holds, its specification read through the
     * client, `getCollectionSpecification`, in place of the collection drawn before, and answers whether it
     * drew, as [setItinerary] reads, draws, is withdrawn and refuses (`map.slots.latest-request`).
     *
     * Where [fit] is given, the camera frames the box the specification states, `bbox`, the extent of the
     * itineraries its tiles draw (`specificationBounds`), once the call drew; a specification stating no box
     * frames nothing (`geo.camera.content-bounds`).
     */
    public suspend fun setCollection(
        id: String,
        fit: CameraFit? = null,
    ): Boolean {
        if (closed) return false
        val framing = fit?.let(::readCameraFit)
        val loaded =
            load(ContentSlot.Collection) {
                val specification = client.tiles.getCollectionSpecification(id).data
                SlotLoad(specification, specificationBounds(specification.bbox))
            } ?: return false
        if (framing != null) loaded.bounds?.let { frameBox(it, framing) }
        return true
    }

    /** Takes off the collection drawn, and a read still out for one draws nothing. */
    public fun clearCollection() {
        unmount(ContentSlot.Collection)
    }

    /**
     * Switches on or off the itineraries of the credential's scope, their specification read through the
     * client, `getItinerariesSpecification` (`map.slots.latest-request`). Switched on while they are drawn, it
     * reads nothing; switched off, a read still out draws nothing. It throws what the client throws.
     */
    public suspend fun setItineraries(shown: Boolean) {
        toggle(ContentSlot.Itineraries, shown) { client.tiles.getItinerariesSpecification().data }
    }

    /**
     * Switches on or off the zones of the credential's scope, their specification read through the client,
     * `getZonesSpecification`, as [setItineraries] switches the itineraries.
     */
    public suspend fun setZones(shown: Boolean) {
        toggle(ContentSlot.Zones, shown) { client.tiles.getZonesSpecification().data }
    }

    /**
     * Draws a specification the app already holds, under a [name] of its own, in place of what that name drew
     * before: each of its sources, and each of their layers at the rank its source layer declares in the map's
     * paint, in the order it lists them (`map.specification.style`), with the restricted stretches of each
     * source that draws an itinerary line. A layer the app hid by its id is drawn hidden. The name is the app's,
     * apart from every slot the map draws for itself, and a specification drawn again under it keeps the place
     * its concepts are named in (`map.layers.keys`).
     *
     * It throws a [MapException] of kind `TypeRefused` where a source is of a type the contract does not
     * declare, and nothing of the specification is drawn.
     */
    public fun setSpecification(
        specification: TileSchema.TileSpecification,
        name: String,
    ) {
        if (closed) return
        val slot = ContentSlot.Specification(name)
        // The latest request for the name, so a read of the app's own that lands late draws nothing.
        slots.take(slot)
        mount(specification, slot)
    }

    /**
     * Takes off the specification drawn under [name], and forgets whether the app hid any of its layers. A name
     * holding nothing is nothing to take off.
     */
    public fun removeSpecification(name: String) {
        unmount(ContentSlot.Specification(name))
    }

    /** Takes off every specification drawn under a name of the app's own, leaving every slot the map draws for itself. */
    public fun clearSpecifications() {
        for (name in specifications) unmount(ContentSlot.Specification(name))
    }

    /** The name of every specification drawn under a name of the app's own, in the order each was first drawn. */
    public val specifications: List<String>
        get() = contents.mapNotNull { (it.slot as? ContentSlot.Specification)?.name }

    /**
     * Draws the classic overlay of slug [slug], as the layers catalogue lists it, in place of the one drawn
     * before, and eases the camera into the zooms it paints across (`map.overlays.composition`). Classic
     * overlays repaint one ground, so a map draws one at a time; enrichments stack, through [addEnrichment].
     *
     * It reads the catalogue through [loadLayers], and throws what that throws. It throws a [MapException] of
     * kind `KindMismatch` where the catalogue holds the slug as an enrichment, and of kind `OverlayNotFound`
     * where it holds it under neither kind, the overlay drawn left as it was. A later call, or [clearOverlay],
     * made while the catalogue is read has this one draw nothing.
     */
    override suspend fun setOverlay(slug: String) {
        if (closed) return
        val (overlay, catalogue) = overlay(slug, OverlayKind.Classic, ContentSlot.Overlay) ?: return
        draw(overlay, ContentSlot.Overlay, stampedSources(overlay, catalogue))
        drawnOverlay = overlay to catalogue
    }

    /** Takes off the classic overlay drawn. It suspends as every command of a layers selection's map does, and waits on nothing. */
    override suspend fun clearOverlay() {
        drawnOverlay = null
        unmount(ContentSlot.Overlay)
    }

    /**
     * Draws the classic overlay the server stamps, `capabilities.server.timestamp`, for [instant], or, where it is null,
     * from the manifest's own addresses (`map.overlays.instant`), which the Tile API answers for the moment each tile
     * arrives, so a device whose clock runs fast or slow still draws the present.
     *
     * Each address of the overlay's sources is stamped as `timestampedSources` stamps it, and the overlay drawn is asked
     * again in place: its sources' addresses change and nothing else does, its layers standing where they stood, and
     * `MapView`'s renderer gives MapLibre the source anew under them, since MapLibre Native's tile sources take their
     * addresses as they are built. An overlay drawn later is stamped as it is drawn, a cleared overlay leaves the pin
     * held, and a base-map switch keeps the addresses asked for. An enrichment and a classic overlay the server does not
     * stamp are drawn as the manifest writes them whatever is pinned, and a refused stamped tile is told at the address
     * it was asked at, as any refusal is. It is the command a layers selection pins through
     * (`geo.layers.selection-instant`), and suspends as every one of them does, waiting on nothing. A closed map does
     * nothing.
     */
    override suspend fun setInstant(instant: Instant?) {
        if (closed) return
        this.instant = instant
        instantStanding.value = instant
        val (overlay, catalogue) = drawnOverlay ?: return
        val sources = stampedSources(overlay, catalogue).map(::MapSource)
        report { state ->
            state.copy(sources = sources.fold(state.sources) { held, source -> withSource(held, source) })
        }
    }

    /**
     * Draws the enrichment of slug [slug], as the layers catalogue lists it, on top of whatever is drawn, and
     * eases the camera into the zooms it paints across (`map.overlays.composition`). Enrichments stack, each
     * under its own slug in the order first added, so one added again keeps its place.
     *
     * It reads the catalogue through [loadLayers], and throws what that throws. It throws a [MapException] of
     * kind `KindMismatch` where the catalogue holds the slug as a classic overlay, and of kind `OverlayNotFound`
     * where it holds it under neither kind, what is drawn left as it was.
     */
    override suspend fun addEnrichment(slug: String) {
        if (closed) return
        val (overlay, _) = overlay(slug, OverlayKind.Enrichment, ContentSlot.Enrichment(slug)) ?: return
        draw(overlay, ContentSlot.Enrichment(overlay.slug), overlay.sources)
    }

    /**
     * Takes off the enrichment of slug [slug], and one still being found in the catalogue draws nothing. A slug
     * not drawn is nothing to take off. It suspends as every command of a layers selection's map does, and waits
     * on nothing.
     */
    override suspend fun removeEnrichment(slug: String) {
        unmount(ContentSlot.Enrichment(slug))
    }

    /** Takes off every enrichment, those still being found in the catalogue included. */
    public fun clearEnrichments() {
        val cleared = slots.requestedSlots().toMutableList()
        for (content in contents) if (content.slot !in cleared) cleared += content.slot
        for (slot in cleared) if (slot is ContentSlot.Enrichment) unmount(slot)
    }

    /** The slug of every enrichment drawn, in the order each was first added. */
    public val enrichments: List<String>
        get() = contents.mapNotNull { (it.slot as? ContentSlot.Enrichment)?.slug }

    // Visibility.

    /**
     * Shows or hides the layer of id [id], the app's word kept by that id (`map.layers.visibility`): before the
     * layer is drawn, so it is born hidden, across a base-map switch, and through a redraw of the content
     * holding it, until that content is cleared, which forgets it. A layer hidden keeps its place and its tiles,
     * so showing it again fetches nothing.
     */
    public fun setLayerVisibility(
        id: String,
        visible: Boolean,
    ) {
        if (closed) return
        writeVisibility(id, visible)
        val layers = registry.layers
        report { it.copy(layers = layers) }
    }

    /**
     * Answers whether the app's word on the layer of id [id] is shown, which it is until the app hides it: the app's
     * intent, the same before the layer is drawn and through a base-map switch (`map.layers.visibility`).
     */
    public fun isLayerVisible(id: String): Boolean = !isHidden(id)

    /**
     * Answers the concepts the map draws, named by the layers catalogue's keys (`map.layers.keys`): each listed
     * once, in the order its content was first asked for and its layers listed, and narrowed, where [rank] names
     * a band, to those with a layer drawn at that rank. A layer of no named concept, the empty key on the wire,
     * names none.
     */
    public fun drawnLayerKeys(rank: Int? = null): List<String> {
        val keys = mutableListOf<String>()
        for (content in contents) {
            for (id in content.layerIds) {
                val layer = registry.layer(id) ?: continue
                val key = layer.key ?: continue
                if ((rank == null || layer.rank == rank) && key !in keys) keys += key
            }
        }
        return keys
    }

    /**
     * Answers the id of every drawn layer carrying the concept [key], in the order its content was first asked
     * for and its layers listed, none for a key nothing draws (`map.layers.keys`).
     */
    public fun layerIds(key: String): List<String> {
        val ids = mutableListOf<String>()
        for (content in contents) {
            for (id in content.layerIds) {
                if (registry.layer(id)?.key == key && id !in ids) ids += id
            }
        }
        return ids
    }

    /** Shows or hides every layer drawing the concept [key], as [setLayerVisibility] shows or hides one. */
    public fun setKeyVisibility(
        key: String,
        visible: Boolean,
    ) {
        if (closed) return
        for (id in layerIds(key)) writeVisibility(id, visible)
        val layers = registry.layers
        report { it.copy(layers = layers) }
    }

    /**
     * Answers whether the concept [key] is shown: while none of its layers is hidden, so a key that draws nothing reads
     * as shown (`map.layers.keys`).
     */
    public fun isKeyVisible(key: String): Boolean = layerIds(key).none(::isHidden)

    /**
     * Shows or hides the restricted stretches of every itinerary the map draws, from here on and across a
     * base-map switch (`map.road-access.layers`). Their layers stay drawn while hidden, so showing them again
     * fetches nothing, and a stretch layer the app hid by its id stays hidden either way.
     */
    public fun setAccessRestrictionsVisibility(visible: Boolean) {
        if (closed || visible == isAccessRestrictionsVisible) return
        isAccessRestrictionsVisible = visible
        for (content in contents) {
            for (id in content.restrictionLayerIds) {
                val layer = registry.layer(id) ?: continue
                registry.replace(id, layer.withVisibility(if (visible && !isHidden(id)) VISIBLE else HIDDEN))
            }
        }
        val layers = registry.layers
        report { it.copy(layers = layers) }
    }

    // Point queries.

    /**
     * Answers the features the map drew at [point] of the canvas, `[x, y]` in density-independent pixels from
     * its top left corner, topmost first: those of the layers it placed, in every band, never the base map's,
     * each as a [QueriedFeature] once for each layer that drew it (`map.query.layers`,
     * `map.query.feature-shape`). A map no view draws, or a closed one, answers none.
     */
    public fun featuresAt(point: List<Double>): List<QueriedFeature> = query(PointQuery.Features, point)

    /** Answers the points of interest the map drew at [point], as [featuresAt] answers them. */
    public fun poisAt(point: List<Double>): List<QueriedFeature> = query(PointQuery.Pois, point)

    /** Answers the zones the map drew at [point], as [featuresAt] answers them. */
    public fun zonesAt(point: List<Double>): List<QueriedFeature> = query(PointQuery.Zones, point)

    /** Answers an itinerary's routed lines and waypoints the map drew at [point], as [featuresAt] answers them. */
    public fun itinerariesAt(point: List<Double>): List<QueriedFeature> = query(PointQuery.Itineraries, point)

    /**
     * Answers the point of the canvas [position], `[longitude, latitude]`, is drawn at, `[x, y]` in
     * density-independent pixels from its top left corner. A map no view draws, or a closed one, has no
     * projection, and answers `[NaN, NaN]`.
     */
    public fun project(position: List<Double>): List<Double> {
        if (closed) return noPoint
        return surface?.projection?.point?.invoke(position) ?: noPoint
    }

    /**
     * Answers the position, `[longitude, latitude]`, drawn at [point] of the canvas. A map no view draws, or a
     * closed one, has no projection, and answers `[NaN, NaN]`.
     */
    public fun unproject(point: List<Double>): List<Double> {
        if (closed) return noPoint
        return surface?.projection?.position?.invoke(point) ?: noPoint
    }

    /**
     * Answers the organization the client's publishable key belongs to, read through `postOrganizationLookup` with the
     * key in the body and no credential sent, once per key and REST API origin for as long as the process lives, and
     * shared with every map whose client holds that key and sends to that origin, a map made later included
     * (`map.organization.read`): a read in flight is waited on rather than sent again, and an answer is kept, which
     * [organization] then holds. The origin is part of what the read is kept under, so a client of a staging origin
     * never shares an answer with one of production's, even under the same key.
     *
     * A read that fails is kept by nobody, so every load waiting on it throws what it threw and the next load reads
     * again; a read reaching the transport's own time budget fails as any read does, so no load waits past one call's
     * budget. It answers null where the client holds no publishable key, a share token or a secret key alone, reads
     * nothing and never throws, a closed map's load among them, as every SDK answers it. A closed map reads nothing more:
     * it answers the organization it held, and throws a [MapException] of kind `Closed` where it held none, a load still
     * waiting when the map closes among them.
     *
     * The answer serves the map's watermark and nothing else it draws: the map's labels, its base map, its controls
     * and its units are the app's, else the SDK's own, whatever the organization prefers. The watermark follows the
     * read as this load moves it, whoever asked: it is drawn once the read fails, answers without white label, or has
     * gone one attempt's budget without an answer while its further attempts go on, and only a later answer of white
     * label takes it away (`geo.organization.watermark`).
     */
    @OptIn(InternalMountayaApi::class)
    public suspend fun loadOrganization(): RestSchema.Organization? {
        organization?.let { return it }
        val key = client.publishableKey ?: return null
        if (closed) throw MapException(MapException.Kind.Closed, "loadOrganization")
        val read = OrganizationReads.request(client, key)
        var isWindowOpen = true
        while (true) {
            val step =
                try {
                    select<OrganizationStep> {
                        read.outcome.onAwait { OrganizationStep.Answered(it) }
                        closing.onAwait { OrganizationStep.Closed }
                        if (isWindowOpen) read.overdue.onAwait { OrganizationStep.Overdue }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failed: Exception) {
                    if (!closed && organization == null) organizationStanding.value = OrganizationRead.Failed
                    throw failed
                }
            when (step) {
                is OrganizationStep.Answered -> {
                    val answered = step.organization
                    if (closed) return answered
                    organization = answered
                    organizationStanding.value = OrganizationRead.Answered(answered.white_label)
                    return answered
                }

                OrganizationStep.Closed -> {
                    throw MapException(MapException.Kind.Closed, "loadOrganization")
                }

                OrganizationStep.Overdue -> {
                    // One attempt's budget passed with no answer: the mark is drawn while the read goes on.
                    isWindowOpen = false
                    if (!closed && organizationStanding.value == OrganizationRead.Waiting) {
                        organizationStanding.value = OrganizationRead.TimedOut
                    }
                }
            }
        }
    }

    /** What a load of the organization waits on next: the read's answer, the map's closing, or the read gone overdue. */
    private sealed interface OrganizationStep {
        /** The read answered [organization]. */
        class Answered(
            val organization: RestSchema.Organization,
        ) : OrganizationStep

        /** The map closed while the load waited. */
        object Closed : OrganizationStep

        /** One attempt's budget passed with no answer, the read's further attempts going on. */
        object Overdue : OrganizationStep
    }

    /**
     * Answers the ground's elevation at [position], `[longitude, latitude]`, in meters: the Data API's terrain
     * sample there, asked for its elevation alone, and null where the answer holds no sample or the sample
     * states none, 0 being an elevation (`map.query.elevation-at`). It is never read off the drawn terrain, a
     * shaded image that is no height field, and a closed map still answers it, since it reads through the
     * client. It throws what the client throws.
     */
    public suspend fun elevationAt(position: List<Double>): Double? =
        client.data
            .terrainSamples(DataSchema.TerrainSamplesInput(locations = listOf(position)), select = elevationAlone)
            .samples
            ?.firstOrNull()
            ?.elevation

    // Markers.

    /**
     * Pins [marker] above everything the map draws, on top of every other, in place of the marker pinned under
     * its id where one is (`map.marker.lifecycle`). Its position is a longitude and a latitude, and an altitude
     * after them, which a marker drawn flat on the map ignores.
     *
     * It throws a [MapException] of kind `PositionRefused` where the position's first two numbers are not
     * finite, its latitude lies outside -90 to 90, or it holds fewer than two numbers or more than three, and
     * whatever is pinned is left as it was.
     */
    override fun addMarker(marker: MapMarker) {
        if (closed) return
        val shown = marker.copy(position = place(marker.position, marker.id))
        report { state -> state.copy(markers = state.markers.filterNot { it.id == shown.id } + shown) }
    }

    /**
     * Changes the marker pinned under [id], which keeps its place among the others: each member given replaces
     * the one held, and each left null leaves it as it is (`map.marker.lifecycle`). A colour, a glyph or a label
     * is taken off by adding the marker again without it.
     *
     * It throws a [MapException] of kind `MarkerNotFound` where no marker is pinned under [id], and of kind
     * `PositionRefused` where [position] is refused as [addMarker] refuses one; the map is then left as it was.
     */
    override fun editMarker(
        id: String,
        position: List<Double>?,
        color: String?,
        glyph: String?,
        label: String?,
    ) {
        if (closed) return
        val markers = held.value.markers
        val index = markers.indexOfFirst { it.id == id }
        if (index < 0) throw MapException(MapException.Kind.MarkerNotFound, id)
        val marker = markers[index]
        val edited =
            marker.copy(
                position = position?.let { place(it, id) } ?: marker.position,
                color = color ?: marker.color,
                glyph = glyph ?: marker.glyph,
                label = label ?: marker.label,
            )
        report { state -> state.copy(markers = state.markers.toMutableList().also { it[index] = edited }) }
    }

    /** Takes off the marker pinned under [id]. A marker the map does not pin is nothing to take off. */
    override fun removeMarker(id: String) {
        report { state -> state.copy(markers = state.markers.filterNot { it.id == id }) }
    }

    /** Takes off every marker. */
    public fun clearMarkers() {
        report { it.copy(markers = emptyList()) }
    }

    // Geometry slots.

    /**
     * Draws [geometry] in [slot], the preview, the highlight or the emphasis, in [color], the slot's own where
     * it is null, or takes the slot down where [geometry] is null (`map.geometry.slots`). Writing one slot never
     * clears another. Each slot draws from a source of its own as the itinerary's cased line in the map's paint,
     * the highlight adding a circle for a point, at its band: the highlight above every content band, the
     * preview and the emphasis just above the itinerary lines (`map.geometry.layers`).
     */
    override fun setGeometry(
        geometry: GeoJsonGeometry?,
        slot: GeometrySlot,
        color: String?,
    ) {
        write(SlotGeometry(geometry, color ?: slot.color), slot)
    }

    /**
     * Changes what [slot] holds: [geometry] where it is given, and [color] where it is given, keeping the one it
     * does not name (`map.geometry.slots`).
     */
    public fun editGeometry(
        slot: GeometrySlot,
        geometry: GeoJsonGeometry? = null,
        color: String? = null,
    ) {
        val holding = geometry(slot)
        write(SlotGeometry(geometry ?: holding.geometry, color ?: holding.color), slot)
    }

    /** Takes [slot] down, and gives it back its own colour (`map.geometry.slots`). */
    override fun clearGeometry(slot: GeometrySlot) {
        write(SlotGeometry(null, slot.color), slot)
    }

    /** Answers what [slot] holds: its geometry, null where it draws none, and its colour. */
    public fun geometry(slot: GeometrySlot): SlotGeometry =
        geometrySlots.firstOrNull { it.first == slot }?.second ?: SlotGeometry(null, slot.color)

    // The search highlight.

    /**
     * Draws [highlight], the result a reader picked from a search, above everything else on the map in [accent], in
     * place of the one drawn before, or takes it down at once where it is null (`map.search.result-highlight`);
     * `pickTarget` answers what a pick marks.
     *
     * A place is the point-of-interest pin carrying a point, or the map pin where the map's glyph set holds no point; a
     * point of interest is its category's own pin in its category's colour, the pin the tiles draw it with; and a way
     * is its line, haloed and cased, with its two ends, else its extent washed and outlined dashed, across the
     * antimeridian where it crosses it, else a pin at its anchor. It draws from a source of its own at the chrome band,
     * apart from the geometry slots, so lighting a feature leaves the picked result where it is, and is drawn again on
     * every base map a switch brings in. [accent] is the elements' `accent` token as the host's tokens read it under
     * the colour scheme in force, which `SearchControl` hands; the standard light one where a caller hands none. A
     * closed map draws nothing.
     */
    public fun setSearchHighlight(
        highlight: SearchHighlight?,
        accent: HexColor = themeTokenValue(ElementTokens.standard.accent, ThemeScheme.Light),
    ) {
        if (closed) return
        if (highlight == null) {
            unmount(ContentSlot.SearchHighlight)
            return
        }
        val (features, layers) = searchHighlightLayers(highlight, accent, SEARCH_HIGHLIGHT_PLACE_GLYPH in glyphs.markup)
        mountGeoJson(features, SEARCH_HIGHLIGHT_SOURCE_ID, layers, ContentSlot.SearchHighlight)
    }

    // Data API results.

    /**
     * Draws routed directions, asking the client for them, `directions`, under an id of their own, and answers
     * the id and the result (`map.analysis.slots`). Results of one kind accumulate, each removed alone by
     * [removeAnalysis]. Where [fit] is given, the camera then frames what the result draws.
     *
     * A [clearDirections] or a [removeAnalysis] made while the query is out cancels it, and the call throws a
     * `CancellationException` (`map.analysis.requests`). It throws a [MapException] of kind `NumberRefused` for a
     * fit [fitBounds] refuses, before anything is asked, and what the client throws.
     */
    public suspend fun addDirections(
        input: DataSchema.DirectionsInput,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.DirectionsResult> = addAnalysis(directions, null, input, null, 0, fit)

    /** Draws directions the app already holds, asking nothing, as the input form draws its answer. */
    public suspend fun addDirections(
        result: DataSchema.DirectionsResult,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.DirectionsResult> = addAnalysis(directions, null, null, result, 0, fit)

    /**
     * Asks again for the directions drawn under [id], with [patch] applied to the input they were asked with,
     * and redraws them, answering the new result (`map.analysis.edit`). With no patch it redraws the result held
     * under the draw options it states, asking nothing.
     *
     * It throws a [MapException] of kind `AnalysisNotFound` where nothing is drawn under [id], of kind
     * `KindMismatch` where a result of another kind is, and of kind `NoInput` where a patch is given for a
     * result drawn from one handed over; the result stays as it is drawn. A later edit, a removal or a clear
     * made while the query is out cancels it, the call throwing a `CancellationException`.
     */
    public suspend fun editDirections(
        id: String,
        patch: ((DataSchema.DirectionsInput) -> DataSchema.DirectionsInput)? = null,
        fit: CameraFit? = null,
    ): DataSchema.DirectionsResult = editAnalysis(directions, id, patch, null, fit)

    /** Takes off every directions result, and cancels every query of one still out. */
    public fun clearDirections() {
        clearAnalyses(AnalysisKind.Directions)
    }

    /**
     * Draws suggested itineraries, asking the client for them, `suggestions`: the one at [chosen] solid, the
     * rest dashed alternatives, as [addDirections] draws and answers.
     */
    public suspend fun addSuggestions(
        input: DataSchema.SuggestionsInput,
        chosen: Int = 0,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.SuggestionsResult> = addAnalysis(suggestions, null, input, null, chosen, fit)

    /** Draws suggested itineraries the app already holds, asking nothing. */
    public suspend fun addSuggestions(
        result: DataSchema.SuggestionsResult,
        chosen: Int = 0,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.SuggestionsResult> = addAnalysis(suggestions, null, null, result, chosen, fit)

    /**
     * Asks again for the suggestions drawn under [id] with [patch] applied to their input, as [editDirections]
     * does; with no patch it redraws the result held, the candidate at [chosen] solid where it is given, asking
     * nothing, since a seeded query asked again would answer other itineraries than the one the reader picked.
     */
    public suspend fun editSuggestions(
        id: String,
        patch: ((DataSchema.SuggestionsInput) -> DataSchema.SuggestionsInput)? = null,
        chosen: Int? = null,
        fit: CameraFit? = null,
    ): DataSchema.SuggestionsResult = editAnalysis(suggestions, id, patch, chosen, fit)

    /** Takes off every suggestion set, and cancels every query of one still out. */
    public fun clearSuggestions() {
        clearAnalyses(AnalysisKind.Suggestions)
    }

    /** Draws the areas reached within each range of an origin, asking the client for them, `isochrones`, as [addDirections] draws and answers. */
    public suspend fun addIsochrones(
        input: DataSchema.IsochronesInput,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.IsochronesResult> = addAnalysis(isochrones, null, input, null, 0, fit)

    /** Draws isochrones the app already holds, asking nothing. */
    public suspend fun addIsochrones(
        result: DataSchema.IsochronesResult,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.IsochronesResult> = addAnalysis(isochrones, null, null, result, 0, fit)

    /** Asks again for the isochrones drawn under [id] with [patch] applied to their input, as [editDirections] does. */
    public suspend fun editIsochrones(
        id: String,
        patch: ((DataSchema.IsochronesInput) -> DataSchema.IsochronesInput)? = null,
        fit: CameraFit? = null,
    ): DataSchema.IsochronesResult = editAnalysis(isochrones, id, patch, null, fit)

    /** Takes off every isochrone set, and cancels every query of one still out. */
    public fun clearIsochrones() {
        clearAnalyses(AnalysisKind.Isochrones)
    }

    /**
     * Draws the places a search answers, asking the client for them, `geocode`, each a point, a place that names
     * no position drawing none, as [addDirections] draws and answers.
     */
    public suspend fun addPlaces(
        input: DataSchema.GeocodeInput,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.GeocodeResult> = addAnalysis(places, null, input, null, 0, fit)

    /** Draws places the app already holds, asking nothing. */
    public suspend fun addPlaces(
        result: DataSchema.GeocodeResult,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.GeocodeResult> = addAnalysis(places, null, null, result, 0, fit)

    /** Asks again for the places drawn under [id] with [patch] applied to their input, as [editDirections] does. */
    public suspend fun editPlaces(
        id: String,
        patch: ((DataSchema.GeocodeInput) -> DataSchema.GeocodeInput)? = null,
        fit: CameraFit? = null,
    ): DataSchema.GeocodeResult = editAnalysis(places, id, patch, null, fit)

    /** Takes off every place search result, and cancels every query of one still out. */
    public fun clearPlaces() {
        clearAnalyses(AnalysisKind.Places)
    }

    /**
     * Draws the costs between the locations of [input] as connectors between them, a straight line per solved
     * pair weighted by its cost, asking the client for them, `matrix`, or drawing [answered], a result the app
     * already holds for that input, asking nothing, as [addDirections] draws and answers. A matrix always holds
     * its input, since its connectors run between the input's locations and a result holds none.
     */
    public suspend fun addMatrix(
        input: DataSchema.MatrixInput,
        answered: DataSchema.MatrixResult? = null,
        fit: CameraFit? = null,
    ): AnalysisHandle<DataSchema.MatrixResult> = addAnalysis(matrix, null, input, answered, 0, fit)

    /** Asks again for the matrix drawn under [id] with [patch] applied to its input, as [editDirections] does. */
    public suspend fun editMatrix(
        id: String,
        patch: ((DataSchema.MatrixInput) -> DataSchema.MatrixInput)? = null,
        fit: CameraFit? = null,
    ): DataSchema.MatrixResult = editAnalysis(matrix, id, patch, null, fit)

    /** Takes off every matrix, and cancels every query of one still out. */
    public fun clearMatrix() {
        clearAnalyses(AnalysisKind.Matrix)
    }

    /**
     * Draws the points of interest a query answers, asking the client for them, `pois`, in place of the ones
     * drawn before: the map draws one such result at a time, under the id [poisAnalysisId]. It answers the
     * result, as [addDirections] draws it.
     */
    public suspend fun setPointsOfInterest(
        input: DataSchema.PoisInput,
        fit: CameraFit? = null,
    ): DataSchema.PoisResult = addAnalysis(pois, POIS_ANALYSIS_ID, input, null, 0, fit).result

    /** Draws points of interest the app already holds, asking nothing. */
    public suspend fun setPointsOfInterest(
        result: DataSchema.PoisResult,
        fit: CameraFit? = null,
    ): DataSchema.PoisResult = addAnalysis(pois, POIS_ANALYSIS_ID, null, result, 0, fit).result

    /** Asks again for the points of interest drawn with [patch] applied to their input, as [editDirections] does. */
    public suspend fun editPointsOfInterest(
        patch: ((DataSchema.PoisInput) -> DataSchema.PoisInput)? = null,
        fit: CameraFit? = null,
    ): DataSchema.PoisResult = editAnalysis(pois, POIS_ANALYSIS_ID, patch, null, fit)

    /** Takes off the points of interest drawn, and cancels their query while it is out. */
    public fun clearPointsOfInterest() {
        removeAnalysis(POIS_ANALYSIS_ID)
    }

    /**
     * Takes off the Data API result drawn under [id], whatever its kind, and cancels its query while it is out,
     * the call waiting on it throwing a `CancellationException` (`map.analysis.requests`). An id nothing is drawn
     * under is nothing to take off.
     */
    public fun removeAnalysis(id: String) {
        if (closed) return
        abortQuery(id)
        analyses.removeAll { it.first == id }
        unmount(ContentSlot.Analysis(id))
    }

    // Editing.

    /**
     * Sets the lines an edit's magnet places a waypoint on, each a list of positions, such as the segments of
     * the itinerary being edited; none turns the magnet off. A waypoint placed before is left where it was.
     */
    public fun setActiveSegments(lines: List<List<List<Double>>>) {
        report { it.copy(activeSegments = lines) }
    }

    /**
     * Begins placing the waypoint [waypointId], already at [position] or not yet. An edit of that waypoint
     * already begun goes on as it is; an edit of another is ended, and this one begins in its place. While it
     * lasts, a click on the map places the waypoint and a long click is taken too, each told to nobody.
     *
     * It throws a [MapException] of kind `PositionRefused` where [position] names no place a map draws, and the
     * map is left as it was.
     */
    public fun beginWaypointEdit(
        waypointId: String,
        position: List<Double>? = null,
    ) {
        if (closed) return
        if (held.value.waypointEdit?.waypointId == waypointId) return
        val placed = position?.let { place(it, waypointId) }
        report { it.copy(waypointEdit = WaypointEdit(waypointId, placed)) }
    }

    /**
     * Places the waypoint being edited where a click fell, [position], its longitude read within [-180, 180],
     * as [projection] draws the map: on the nearest point of an active segment where that point is drawn within
     * 40 pixels of the click, and at the click otherwise (`map.edit.snap`). The renderer's projection is read at
     * that moment, so the magnet reads alike at every zoom. Where no waypoint is being edited, it does nothing.
     *
     * It throws a [MapException] of kind `PositionRefused` where [position] names no place a map draws, and the
     * waypoint is left where it was.
     */
    public fun placeWaypoint(
        position: List<Double>,
        projection: ScreenProjection,
    ) {
        if (closed) return
        val editing = held.value.waypointEdit ?: return
        val placed = place(position, editing.waypointId)
        val clicked = listOf(wrapLongitude(placed[0]), placed[1])
        val snap = snapToActiveSegments(clicked, held.value.activeSegments, projection)
        report { it.copy(waypointEdit = WaypointEdit(editing.waypointId, snap.position, snap.isSnapped)) }
    }

    /**
     * Ends the edit of the waypoint being placed, and answers it as it stands, where it was placed and whether
     * the magnet placed it, or null where none was being edited. A save and a cancel alike end it; what the app
     * does with the answer is its own.
     */
    public fun endWaypointEdit(): WaypointEdit? {
        if (closed) return null
        val edit = held.value.waypointEdit ?: return null
        report { it.copy(waypointEdit = null) }
        return edit
    }

    /**
     * Makes the map inert (`map.engine.closed`): every later command does nothing, a drawing call draws
     * nothing and answers false, a point query answers no feature, a projection no number, [locate] is
     * refused, and every flow of [events] ends, so nothing is told; [state] and [credits] read as the map
     * ended, [loadLayers] answers the catalogue held and [loadOrganization] the organization held, and
     * [elevationAt], which reads through the client, still answers. Closing a closed map does nothing.
     */
    override fun close() {
        if (closed) return
        closed = true
        hovering = null
        readout?.close()
        closing.complete(Unit)
        listeners.getAndUpdate { null }?.forEach { it.close() }
    }

    // What a surface tells.

    /**
     * Makes [surface] the one the map is drawn on, which the engine asks what only a renderer knows and hands
     * its camera moves to.
     */
    internal fun attach(surface: MapSurface) {
        this.surface = surface
    }

    /** Lets go of [surface] where it is the one the map is drawn on, so a map that outlives its view holds no view. */
    internal fun detach(surface: MapSurface) {
        if (this.surface === surface) this.surface = null
    }

    /**
     * Records where the renderer's camera came to rest after the reader moved it, so the state is the map as
     * it is shown.
     */
    internal fun cameraMoved(camera: CameraPose) {
        report { it.copy(camera = camera) }
    }

    /**
     * Reads the start of a camera move on the renderer, the reader's gesture as it begins or a move the renderer
     * starts for anyone else, which closes the readout open (`map.readout.open`).
     */
    internal fun cameraMoveStarted() {
        if (closed) return
        readout?.close()
    }

    /**
     * Reads a click at [point] of the canvas, where the renderer read [position]: it closes the readout open,
     * whatever it lands on (`map.readout.open`); while a waypoint is being placed it places it, through the
     * surface's projection, and is told to nobody; otherwise it is told as a `MapEvent.Click`, and again as a
     * `MapEvent.FeatureClick` where it landed on features the map drew (`map.events.pointer`).
     */
    internal fun clicked(
        point: List<Double>,
        position: List<Double>,
    ) {
        if (closed) return
        readout?.close()
        if (held.value.waypointEdit != null) {
            val projection = surface?.projection ?: return
            try {
                placeWaypoint(position, projection)
            } catch (refused: MapException) {
                // A click that names no place leaves the waypoint where it was.
            }
            return
        }
        val place = within(position) ?: return
        tell(MapEvent.Click(MapTap(place, point, markerNear(point))))
        val found = query(PointQuery.Features, point)
        if (found.isNotEmpty()) tell(MapEvent.FeatureClick(place, found))
    }

    /**
     * Reads a long press, or a secondary click, at [point] of the canvas, where the renderer read [position]
     * with the camera at [zoom]: it is told as a `MapEvent.LongClick`, and to nobody while a waypoint is being
     * placed, whose edit takes it (`map.events.pointer`), and then opens the readout at the place it names, in
     * place of one open, where the view holds one and the host leaves it on (`map.readout.open`).
     */
    internal fun longClicked(
        point: List<Double>,
        position: List<Double>,
        zoom: Double = held.value.camera.zoom,
    ) {
        if (closed || held.value.waypointEdit != null) return
        val place = within(position) ?: return
        tell(MapEvent.LongClick(MapTap(place, point, markerNear(point))))
        if (place.all { it.isFinite() }) readout?.press(place, point, zoom)
    }

    /** Records where a hovering pointer moved, [point] of the canvas, where the renderer read [position]; the next frame tells it (`map.events.feature-hover`). */
    internal fun pointerMoved(
        point: List<Double>,
        position: List<Double>,
    ) {
        if (closed) return
        hovering = point to position
    }

    /**
     * Tells where a hovering pointer ended the frame, with the features of an itinerary's line and points under
     * it, topmost first, once a frame at most. Nothing is asked of the renderer while nobody listens.
     */
    internal fun frameDrawn() {
        if (closed) return
        val (point, position) = hovering ?: return
        hovering = null
        if (listeners.value.isNullOrEmpty()) return
        val place = within(position) ?: return
        tell(MapEvent.FeatureHover(place, query(PointQuery.Itineraries, point)))
    }

    /**
     * Tells that a hovering pointer left the canvas at [point], where the renderer read [position], with no
     * feature, dropping a hover still waiting on its frame.
     */
    internal fun pointerLeft(
        point: List<Double>,
        position: List<Double>,
    ) {
        if (closed) return
        hovering = null
        val place = within(position) ?: return
        tell(MapEvent.FeatureHover(place, emptyList()))
    }

    /**
     * Tells a failure of one of the map's own requests, to [url] where the renderer reported one, carrying
     * [status] where an answer carried one, from the source [source] where it was a tile's: a refusal of
     * access, which stands until that source loads a tile or, naming no source, until the next style loads; or
     * any other failure (`map.events.access-refused`).
     *
     * [superseded] says the request left under a credential the client no longer sends, a session token since
     * replaced or minted after the request left under the key alone, or a visitor's cookie since changed, which
     * the renderer reads off each request. A refusal of the credential of such a request, a 401 or a 403
     * ([isCredentialRefusal]), refuses one the map has already given up: it is told to nobody, neither as a
     * refusal nor as a failure, and starts no standing and ends none (`map.events.access-standing`). It answers
     * whether it gave the failure up so, which has a renderer ask again for a style refused so; a 402 and every
     * other failure are told as they are, whatever credential the request left with, and answer false.
     *
     * [cause] is what failed where the renderer knows it, a read of the client's credential that threw for a request
     * that never left for want of one: it is told as a [MapEvent.Failure] carrying it, with no status whatever
     * [status] the request would have met, and starts no standing.
     */
    internal fun requestFailed(
        url: String?,
        status: Int?,
        source: String?,
        superseded: Boolean = false,
        cause: Throwable? = null,
    ): Boolean {
        if (closed) return false
        if (cause != null) {
            tell(MapEvent.Failure(url.orEmpty(), null, cause))
            return false
        }
        if (superseded && isCredentialRefusal(status)) return true
        val refused = accessRefusalStatus(status)
        if (refused == null) {
            tell(MapEvent.Failure(url.orEmpty(), status))
            return false
        }
        standing = accessStandingAfter(standing, AccessAnswer.Refused(source), ::holdsSource).first
        report { it.copy(isAccessRequired = true) }
        tell(MapEvent.AccessRefused(refused, url.orEmpty()))
        return false
    }

    /** Ends the refusal of access of [source], and of every source the style no longer holds (`map.events.access-standing`). */
    internal fun tileLoaded(source: String) {
        if (closed) return
        answerAccess(AccessAnswer.Tile(source))
    }

    /**
     * Relabels the base map a style brought in, and ends every refusal of access naming no source, and those of
     * the sources the style no longer holds. The base map asked for is the one shown from then on.
     *
     * A base map given back where the one asked for did not arrive answers nothing as it loads again: it ends no
     * refusal, the refusal of the style that did not arrive among them, which stands until a style the map asks
     * for loads (`map.events.access-standing`).
     */
    internal fun styleLoaded() {
        if (closed) return
        val loaded = held.value.style to held.value.terrain
        val isGivenBack = givenBack == loaded
        givenBack = null
        shownStyle = loaded.first
        shownTerrain = loaded.second
        relabelled.clear()
        relabelBaseMap()
        if (!isGivenBack) answerAccess(AccessAnswer.Style)
    }

    /**
     * Gives the map back the base map shown, where the document of the one asked for did not arrive, its
     * content drawn on it as it was (`map.basemap.replay`), which answers nothing as it loads again.
     */
    internal fun styleFailed() {
        if (closed) return
        val shown = held.value
        if (shown.style == shownStyle && shown.terrain == shownTerrain) return
        val style = shownStyle
        val terrain = shownTerrain
        val variant = drawnVariant(style, terrain)
        givenBack = style to terrain
        report { it.copy(style = style, terrain = terrain, terrainVariant = variant) }
    }

    // Applying the rules.

    /** A terrain a [setTerrain] asked for: the base-map [request] it made, and the [slug], null for the catalogue's first. */
    private class TerrainAsk(
        val request: Int,
        val slug: String?,
    )

    /** What a slot's read answered: the [specification] it draws, and the box a fit frames once it drew, where one was read. */
    private class SlotLoad(
        val specification: TileSchema.TileSpecification,
        val bounds: List<Double>? = null,
    )

    /**
     * Reads what [slot] draws through [read], as its latest request, and draws it where the request still owns
     * the slot as the read answers, or regains it once the call that superseded it settles, answering what it
     * read, or null where it drew nothing (`map.slots.latest-request`). A call whose coroutine is cancelled
     * before it drew gives the slot back and throws the `CancellationException`.
     */
    private suspend fun load(
        slot: ContentSlot,
        read: suspend () -> SlotLoad,
    ): SlotLoad? {
        // A call withdrawn before it starts reads nothing.
        currentCoroutineContext().ensureActive()
        val token = slots.claim(slot)
        val loaded =
            try {
                read().also {
                    // An answer that lands all the same, after the call was withdrawn, is withdrawn with it.
                    currentCoroutineContext().ensureActive()
                }
            } catch (failure: Throwable) {
                if (currentCoroutineContext().isActive) slots.settle(token) else slots.giveBack(slot, token)
                throw failure
            }
        if (!slots.owns(slot, token)) {
            val regained =
                try {
                    slots.regains(slot, token)
                } catch (cancelled: CancellationException) {
                    slots.giveBack(slot, token)
                    throw cancelled
                }
            if (!regained) {
                slots.settle(token)
                return null
            }
        }
        try {
            if (closed) return null
            mount(loaded.specification, slot)
            return loaded
        } finally {
            slots.settle(token)
        }
    }

    /**
     * Draws or clears [slot], whose content needs no id: switched on while it is drawn it reads nothing, and a
     * read switched off, or superseded, while it was out draws nothing.
     */
    private suspend fun toggle(
        slot: ContentSlot,
        on: Boolean,
        read: suspend () -> TileSchema.TileSpecification,
    ) {
        if (closed) return
        if (!on) {
            unmount(slot)
            return
        }
        if (contents.any { it.slot == slot }) return
        val token = slots.take(slot)
        val specification = read()
        if (!slots.owns(slot, token) || closed) return
        mount(specification, slot)
    }

    /**
     * Finds the overlay of slug [slug] and of [kind] in the layers catalogue, read where the map holds none,
     * holding [slot] meanwhile as a request a later one supersedes, and answers it with the catalogue it was found in:
     * null where a later request for the slot, or a clear, was made while the catalogue was read. A refusal gives the
     * slot back first, so it depends on the slug alone.
     */
    private suspend fun overlay(
        slug: String,
        kind: OverlayKind,
        slot: ContentSlot,
    ): Pair<TileSchema.Overlay, TileSchema.Layers>? {
        layersManifest?.let { return requireOverlay(slug, kind, it) to it }
        val token = slots.claim(slot)
        val found =
            try {
                val catalogue = loadLayers()
                requireOverlay(slug, kind, catalogue) to catalogue
            } catch (failure: Throwable) {
                slots.giveBack(slot, token)
                throw failure
            }
        val owns = slots.owns(slot, token) && !closed
        slots.settle(token)
        return if (owns) found else null
    }

    /**
     * The sources of the classic [overlay] of [catalogue] as the map draws them for the instant pinned: each source the
     * server stamps with its addresses stamped (`timestampedSources`), and every one as the manifest writes it with none
     * pinned or for an overlay the server does not stamp.
     */
    private fun stampedSources(
        overlay: TileSchema.Overlay,
        catalogue: TileSchema.Layers,
    ): List<TileSchema.TileSource> {
        val stamped = instant?.let { timestampedSources(catalogue, overlay.slug, it) }.orEmpty()
        return overlay.sources.map { source ->
            val tiles = stamped.firstOrNull { it.sourceId == source.id }?.tiles ?: return@map source
            TileSchema.TileSource(source.id, source.layers, tiles, source.type, source.zoom_max, source.zoom_min) {
                source.attribution?.let { attribution = it }
                source.size?.let { size = it }
            }
        }
    }

    /**
     * The overlay of slug [slug] and of [kind] in [catalogue], refusing a slug the catalogue holds only as the
     * other kind as `KindMismatch`, and one it does not hold as `OverlayNotFound`.
     */
    private fun requireOverlay(
        slug: String,
        kind: OverlayKind,
        catalogue: TileSchema.Layers,
    ): TileSchema.Overlay {
        findOverlay(catalogue, slug, kind)?.let { return it }
        val other = if (kind == OverlayKind.Classic) OverlayKind.Enrichment else OverlayKind.Classic
        if (findOverlay(catalogue, slug, other) != null) throw MapException(MapException.Kind.KindMismatch, slug)
        throw MapException(MapException.Kind.OverlayNotFound, slug)
    }

    /**
     * Draws [overlay] from [sources], its own as the map draws them, as what [slot] holds, taking the slot for good, and
     * eases the camera into the zooms it paints across, so switching it on from too far out reads as no dead control.
     */
    private fun draw(
        overlay: TileSchema.Overlay,
        slot: ContentSlot,
        sources: List<TileSchema.TileSource>,
    ) {
        slots.take(slot)
        mount(TileSchema.TileSpecification(sources = sources), slot)
        paintZoomRange(overlay.sources)?.let(::easeIntoZoomRange)
    }

    /**
     * Draws [specification] as what [slot] holds, in place of what it held, which keeps its place among the
     * map's contents and the app's word on its layers: every source, then every layer at the rank its source
     * layer declares, a layer the app hid born hidden, then the restricted stretches of each source that draws
     * an itinerary line. Every source is read before anything is drawn, so a specification refused for one of
     * them draws none of the others.
     */
    private fun mount(
        specification: TileSchema.TileSpecification,
        slot: ContentSlot,
    ) {
        val sources = specification.sources.map(::MapSource)
        val before = contents.firstOrNull { it.slot == slot }
        if (before != null) for (id in before.layerIds + before.restrictionLayerIds) registry.remove(id)
        val layerIds = mutableListOf<String>()
        for (source in specification.sources) {
            for (layer in source.layers) {
                val drawn = MapLayer(layer, source.id, paint.layerRanks)
                registry.add(if (isHidden(drawn.id)) drawn.withVisibility(HIDDEN) else drawn)
                layerIds += drawn.id
            }
        }
        val restrictionLayerIds = mutableListOf<String>()
        for (source in specification.sources) {
            for (drawn in roadAccessLayersOf(source, isAccessRestrictionsVisible)) {
                registry.add(if (isHidden(drawn.id)) drawn.withVisibility(HIDDEN) else drawn)
                restrictionLayerIds += drawn.id
            }
        }
        place(MapContent(slot, sources.map { it.id }, layerIds, restrictionLayerIds))
        val gone = before?.sourceIds.orEmpty().filter { id -> sources.none { it.id == id } }
        val layers = registry.layers
        report { state ->
            val kept = state.sources.filterNot { it.id in gone }
            state.copy(sources = sources.fold(kept) { held, source -> withSource(held, source) }, layers = layers)
        }
    }

    /**
     * Takes off what [slot] holds, and forgets the app's word on its layers, as the slot is cleared for good: a
     * read still out for it draws nothing once it lands. A source another slot still draws stays.
     */
    private fun unmount(slot: ContentSlot) {
        if (closed) return
        slots.release(slot)
        val index = contents.indexOfFirst { it.slot == slot }
        if (index < 0) return
        val content = contents.removeAt(index)
        val ids = content.layerIds + content.restrictionLayerIds
        for (id in ids) registry.remove(id)
        hiddenLayerIds.removeAll { it in ids }
        val kept = contents.flatMap { it.sourceIds }
        val gone = content.sourceIds.filter { it !in kept }
        val layers = registry.layers
        report { state -> state.copy(sources = state.sources.filterNot { it.id in gone }, layers = layers) }
    }

    /** Records [content] as what its slot draws, in the slot's place among the contents, or after them. */
    private fun place(content: MapContent) {
        val index = contents.indexOfFirst { it.slot == content.slot }
        if (index < 0) contents += content else contents[index] = content
    }

    /**
     * Records the app's word on the layer of id [id] and writes it onto the layer where it is drawn, a
     * restricted stretch drawn only while the stretches are shown too.
     */
    private fun writeVisibility(
        id: String,
        visible: Boolean,
    ) {
        hiddenLayerIds.remove(id)
        if (!visible) hiddenLayerIds += id
        val layer = registry.layer(id) ?: return
        val isStretch = contents.any { id in it.restrictionLayerIds }
        val shown = visible && (!isStretch || isAccessRestrictionsVisible)
        registry.replace(id, layer.withVisibility(if (shown) VISIBLE else HIDDEN))
    }

    /** Whether the app hid the layer of id [id]. */
    private fun isHidden(id: String): Boolean = id in hiddenLayerIds

    /** Waits on the catalogue read another load sent. */
    private suspend fun waitForLayers(): TileSchema.Layers {
        val waiter = CompletableDeferred<TileSchema.Layers>()
        manifestWaiters += waiter
        return waiter.await()
    }

    /**
     * Reads the layers catalogue for the studio scope named, in the coroutine of the load asking, and answers
     * it to that load and every load waiting on the read; a read that lands after the scope moved is another
     * scope's, and every load waiting on it is answered the current scope's catalogue instead.
     */
    private suspend fun readLayers(): TileSchema.Layers {
        while (true) {
            val read = manifestReads
            manifestReads += 1
            manifestRead = read
            val scope = layersOrganization
            val answer =
                try {
                    Result.success(client.tiles.getTileLayers { scope?.let { organization_id = it } }.data)
                } catch (failure: Throwable) {
                    Result.failure(failure)
                }
            if (manifestRead == read) {
                manifestRead = null
                val waiting = manifestWaiters.toList()
                manifestWaiters.clear()
                val catalogue =
                    answer.getOrElse { failure ->
                        for (waiter in waiting) waiter.completeExceptionally(failure)
                        throw failure
                    }
                layersManifest = catalogue
                tell(MapEvent.LayersChange(catalogue))
                for (waiter in waiting) waiter.complete(catalogue)
                return catalogue
            }
            // The scope moved while this read was out: the current scope's catalogue answers, held, still being
            // read, or read now.
            layersManifest?.let { return it }
            if (manifestRead != null) return waitForLayers()
        }
    }

    /**
     * AnalysisSpec is how a kind of Data API result is asked for and drawn: the [kind], the types of its
     * [input] and its [answer], the client's [query] that answers it, and the [features] an answer draws.
     */
    private class AnalysisSpec<Input : Any, Answer : Any>(
        val kind: AnalysisKind,
        val input: KClass<Input>,
        val answer: KClass<Answer>,
        val query: suspend (Mountaya, Input) -> Answer,
        val features: (Answer, Input?, Int) -> GeoJsonFeatureCollection,
    )

    /**
     * Draws a Data API result of [spec]'s kind under an id of its own, or [fixed], the result [answered] where
     * one is handed over, else the answer of its query asked with [input], and answers the id and the result. The
     * fit is read before anything is asked. A request a removal, a clear or a later request superseded draws and
     * records nothing, and a closed map still answers the query, drawing and recording nothing
     * (`map.analysis.slots`, `map.analysis.requests`).
     */
    private suspend fun <Input : Any, Answer : Any> addAnalysis(
        spec: AnalysisSpec<Input, Answer>,
        fixed: String?,
        input: Input?,
        answered: Answer?,
        chosen: Int,
        fit: CameraFit?,
    ): AnalysisHandle<Answer> {
        val framing = if (closed) null else fit?.let(::readCameraFit)
        val id = fixed ?: "analysis:${spec.kind.id}:${analysisCount++}"
        val slot = ContentSlot.Analysis(id)
        val token = if (closed) null else slots.take(slot)
        val answer =
            try {
                when {
                    answered != null -> answered
                    input != null -> runQuery(id) { spec.query(client, input) }
                    else -> throw MapException(MapException.Kind.NoInput, id)
                }
            } catch (failure: Throwable) {
                // A request that draws nothing gives its slot up, unless a result is drawn under the id.
                if (token != null && slots.owns(slot, token) && !closed && analyses.none { it.first == id }) {
                    slots.release(slot)
                }
                throw failure
            }
        if (token == null || !slots.owns(slot, token) || closed) return AnalysisHandle(id, answer)
        hold(id, HeldAnalysis(spec.kind, input, answer, chosen, fit))
        drawAnalysis(spec.kind, id, spec.features(answer, input, chosen), framing)
        return AnalysisHandle(id, answer)
    }

    /**
     * Asks again for the result of [spec]'s kind drawn under [id], with [patch] applied to the input it was
     * asked with, or, with no patch, redraws the result held under the options the call states merged onto the
     * held ones, asking nothing (`map.analysis.edit`). The options are read again as an answer lands, so a
     * redraw made while the query was out stands under what the call itself stated.
     */
    private suspend fun <Input : Any, Answer : Any> editAnalysis(
        spec: AnalysisSpec<Input, Answer>,
        id: String,
        patch: ((Input) -> Input)?,
        chosen: Int?,
        fit: CameraFit?,
    ): Answer {
        val holding =
            analyses.firstOrNull { it.first == id }?.second
                ?: throw MapException(MapException.Kind.AnalysisNotFound, id)
        val result =
            holding.result.takeIf { holding.kind == spec.kind }?.let(spec.answer::safeCast)
                ?: throw MapException(MapException.Kind.KindMismatch, id)
        if (!closed) fit?.let(::readCameraFit)
        val heldInput = holding.input?.let(spec.input::safeCast)
        if (patch == null) {
            if (closed) return result
            val restyled = holding.copy(chosen = chosen ?: holding.chosen, fit = fit ?: holding.fit)
            hold(id, restyled)
            drawAnalysis(spec.kind, id, spec.features(result, heldInput, restyled.chosen), restyled.fit)
            return result
        }
        val asked = patch(heldInput ?: throw MapException(MapException.Kind.NoInput, id))
        val slot = ContentSlot.Analysis(id)
        val token = if (closed) null else slots.take(slot)
        val answer = runQuery(id) { spec.query(client, asked) }
        val current = analyses.firstOrNull { it.first == id }?.second
        if (token == null || !slots.owns(slot, token) || closed || current == null) return answer
        val next = HeldAnalysis(spec.kind, asked, answer, chosen ?: current.chosen, fit ?: current.fit)
        hold(id, next)
        drawAnalysis(spec.kind, id, spec.features(answer, asked, next.chosen), next.fit)
        return answer
    }

    /**
     * Runs [query] in the calling coroutine for the result [id], cancelling the query that result had out, and
     * throws a `CancellationException` where a later run, a removal or a clear cancels it in turn.
     */
    private suspend fun <Answer> runQuery(
        id: String,
        query: suspend () -> Answer,
    ): Answer {
        if (closed) return query()
        abortQuery(id)
        val abort = QueryAbort()
        queryAborts += id to abort
        try {
            return abort.race(query)
        } finally {
            queryAborts.removeAll { it.second === abort }
        }
    }

    /** Cancels the query out for the result [id], where one is. */
    private fun abortQuery(id: String) {
        for ((held, abort) in queryAborts) if (held == id) abort.pull()
        queryAborts.removeAll { it.first == id }
    }

    /**
     * Takes off every result of [kind], those whose first query is still out included, and cancels every query
     * of them still out.
     */
    private fun clearAnalyses(kind: AnalysisKind) {
        if (closed) return
        val prefix = "analysis:${kind.id}:"
        val ids = analyses.filter { it.second.kind == kind }.map { it.first }.toMutableList()
        for (slot in slots.requestedSlots()) {
            if (slot is ContentSlot.Analysis && slot.id.startsWith(prefix) && slot.id !in ids) ids += slot.id
        }
        for (id in ids) removeAnalysis(id)
    }

    /** Records [held] as the result drawn under [id]. */
    private fun hold(
        id: String,
        held: HeldAnalysis,
    ) {
        val index = analyses.indexOfFirst { it.first == id }
        if (index < 0) analyses += id to held else analyses[index] = id to held
    }

    /**
     * Draws [features], a result of [kind], from the source `mountaya-` then [id], at its kind's rank, and frames
     * what it draws where [fit] is given.
     */
    private fun drawAnalysis(
        kind: AnalysisKind,
        id: String,
        features: GeoJsonFeatureCollection,
        fit: CameraFit?,
    ) {
        val sourceId = "mountaya-$id"
        mountGeoJson(features, sourceId, analysisLayers(kind, sourceId, paint), ContentSlot.Analysis(id))
        if (fit == null) return
        val framing =
            try {
                readCameraFit(fit)
            } catch (refused: MapException) {
                return
            }
        boundsOf(features.features.mapNotNull { it.geometry })?.let { frameBox(it, framing) }
    }

    /**
     * Records what a geometry slot holds and draws it, or takes the slot down where it holds no geometry
     * (`map.geometry.slots`, `map.geometry.layers`).
     */
    private fun write(
        holding: SlotGeometry,
        slot: GeometrySlot,
    ) {
        if (closed) return
        val index = geometrySlots.indexOfFirst { it.first == slot }
        if (index < 0) geometrySlots += slot to holding else geometrySlots[index] = slot to holding
        val geometry = holding.geometry
        if (geometry == null) {
            unmount(ContentSlot.Geometry(slot))
            return
        }
        mountGeoJson(
            GeoJsonFeatureCollection(listOf(GeoJsonFeature(geometry, JsonObject(emptyMap())))),
            slot.sourceId,
            geometrySlotLayers(slot, holding.color, paint),
            ContentSlot.Geometry(slot),
        )
    }

    /**
     * Draws [features] from the GeoJSON source [sourceId] through [layers], each at its rank, as what [slot]
     * holds: a slot drawn already takes the new features in place, and a layer it no longer lists comes off.
     */
    private fun mountGeoJson(
        features: GeoJsonFeatureCollection,
        sourceId: String,
        layers: List<MapLayer>,
        slot: ContentSlot,
    ) {
        contents.firstOrNull { it.slot == slot }?.let { before ->
            for (id in before.layerIds) if (layers.none { it.id == id }) registry.remove(id)
        }
        for (layer in layers) registry.add(if (isHidden(layer.id)) layer.withVisibility(HIDDEN) else layer)
        place(MapContent(slot, listOf(sourceId), layers.map { it.id }, emptyList()))
        val source = MapSource(sourceId, MapSource.Kind.GeoJson(features))
        val drawn = registry.layers
        report { it.copy(sources = withSource(it.sources, source), layers = drawn) }
    }

    /**
     * The variant the map draws terrains in, with the base map at [style] and the catalogue's terrain
     * [terrain]: the one the document is where the terrain's two variants are two documents, and the one the
     * app asked for last otherwise.
     */
    private fun drawnVariant(
        style: Url?,
        terrain: String?,
    ): TileSchema.TerrainVariant {
        val listed = terrain?.let { slug -> layersManifest?.terrains?.firstOrNull { it.slug == slug } }
        return listed?.let { terrainVariantOfStyle(it, style) } ?: terrainVariantAsked
    }

    /**
     * Hands the surface a camera move once it is read, the state holding where it lands, and closes the readout open
     * as the move is asked (`map.readout.open`).
     */
    private fun move(
        target: CameraTarget,
        motion: CameraMotion,
    ) {
        if (closed) return
        val read = readCameraTarget(target)
        readout?.close()
        report { it.copy(camera = held(read.applied(it.camera), it.zoomBounds)) }
        surface?.move(read, motion)
    }

    /** Fits the camera to [bounds] as a box is read, where it is four finite numbers, on the surface's canvas. */
    private fun frameBox(
        bounds: List<Double>,
        fit: CameraFit,
    ) {
        val box = normalizeBoundingBox(bounds)
        if (box.all { it.isFinite() }) frame(box, fit)
    }

    /**
     * Fits the camera to [box], a box in canonical form, on the surface's canvas, closing the readout open as the fit
     * moves the camera (`map.readout.open`).
     */
    private fun frame(
        box: List<Double>,
        fit: CameraFit,
    ) {
        if (closed) return
        val camera = surface?.frame(box, fit, held.value.zoomBounds) ?: return
        readout?.close()
        report { it.copy(camera = camera) }
    }

    /**
     * Moves the camera as a control asks, to the target `controlMoveTarget` answers, handing the renderer the members
     * that move so it keeps every other: at once where [jumps], as a control moves it for a reader who turned
     * animations off, and along its ease otherwise. The readout open closes as the move is asked (`map.readout.open`).
     */
    internal fun controlMove(
        move: ControlMove,
        jumps: Boolean = false,
    ) {
        if (closed) return
        val target = controlMoveTarget(held.value.camera, held.value.zoomBounds, move)
        readout?.close()
        report { it.copy(camera = target) }
        when (move) {
            ControlMove.ResetNorth -> {
                surface?.move(
                    CameraTarget(bearing = 0.0, pitch = 0.0),
                    if (jumps) CameraMotion.Jump else CameraMotion.Ease(northReset),
                )
            }

            ControlMove.ZoomIn, ControlMove.ZoomOut -> {
                surface?.move(
                    CameraTarget(zoom = target.zoom),
                    if (jumps) CameraMotion.Jump else CameraMotion.Ease(zoomEase),
                )
            }
        }
    }

    /** Asks the surface what a point [query] reads at [point], over the layers `queryLayerIds` names. */
    private fun query(
        query: PointQuery,
        point: List<Double>,
    ): List<QueriedFeature> {
        if (closed) return emptyList()
        val surface = surface ?: return emptyList()
        val layers = queryLayerIds(query, registry.placement) { surface.holdsLayer(it) } ?: return emptyList()
        return surface.renderedFeatures(point, layers)
    }

    /** The id of the topmost marker drawn within [MARKER_RADIUS] of [point], through the surface's projection, or null. */
    private fun markerNear(point: List<Double>): String? {
        val projection = surface?.projection ?: return null
        if (point.size < 2) return null
        val reach = MARKER_RADIUS * MARKER_RADIUS
        return held.value.markers
            .lastOrNull { marker ->
                val drawn = projection.point(marker.position)
                if (drawn.size < 2) return@lastOrNull false
                val across = drawn[0] - point[0]
                val down = drawn[1] - point[1]
                across * across + down * down <= reach
            }?.id
    }

    /**
     * Labels every symbol layer of the base map the surface draws in the state's language, from the label its
     * style wrote, and puts back a label it rewrote before where the language leaves it as written; a layer
     * the map placed is never relabelled.
     */
    private fun relabelBaseMap() {
        val surface = surface ?: return
        val labels =
            relabelledBaseLabels(surface.symbolLabels, { registry.layer(it) != null }, held.value.labelLanguage)
        for ((id, label) in labels) {
            if (label != null) {
                surface.relabel(id, label)
                if (id !in relabelled) relabelled += id
            } else if (id in relabelled) {
                surface.relabel(id, null)
                relabelled.remove(id)
            }
        }
    }

    /** Hears an answer a refusal of access ends on, and tells the end of the last one. */
    private fun answerAccess(answer: AccessAnswer) {
        val (after, restored) = accessStandingAfter(standing, answer, ::holdsSource)
        standing = after
        report { it.copy(isAccessRequired = !after.isEmpty) }
        if (restored) tell(MapEvent.AccessRestored)
    }

    /** Whether the surface's style holds the source [id], which none does without a surface. */
    private fun holdsSource(id: String): Boolean = surface?.holdsSource(id) ?: false

    /** The floor and the default ceiling of the renderer that draws the map: the surface's, else MapLibre Native's, the renderer `MapView` draws on. */
    private val rendererZoomLimits: ZoomBounds
        get() = surface?.zoomLimits ?: ZoomBounds(NATIVE_ZOOM_FLOOR, NATIVE_ZOOM_CEILING)

    /** Adds [channel] to the collectors told each event, and tells whether the map is open to tell it any. */
    private fun listen(channel: SendChannel<MapEvent>): Boolean {
        while (true) {
            val current = listeners.value ?: return false
            if (listeners.compareAndSet(current, current + channel)) return true
        }
    }

    /** Takes [channel] off the collectors told each event. */
    private fun stopListening(channel: SendChannel<MapEvent>) {
        while (true) {
            val current = listeners.value ?: return
            if (listeners.compareAndSet(current, current - channel)) return
        }
    }

    /** Hands [event] to every collector of [events], in order. */
    private fun tell(event: MapEvent) {
        if (closed) return
        listeners.value?.forEach { it.trySend(event) }
    }

    /** Applies [change] to the state held and reports the new state, where the engine is not closed. */
    private inline fun report(change: (MapState) -> MapState) {
        if (closed) return
        held.value = change(held.value)
    }

    /** The id every map engine draws the points of interest a query answers under. */
    public companion object {
        /** The id the points of interest a query answers are drawn under, one result at a time. */
        public val poisAnalysisId: String = POIS_ANALYSIS_ID

        /** A layout's `visibility` for a layer drawn. */
        private const val VISIBLE: String = "visible"

        /** A layout's `visibility` for a layer hidden. */
        private const val HIDDEN: String = "none"

        /** How long a zoom control's step eases, the platform's motion and no contract's. */
        private val zoomEase: Duration = 250.milliseconds

        /** How long a reset of north eases, as MapLibre's own reset takes. */
        private val northReset: Duration = 1.seconds

        /** What a map with no projection answers a projection with. */
        private val noPoint: List<Double> = listOf(Double.NaN, Double.NaN)

        /** The terrain sample's elevation, the one field a map's elevation reads. */
        private val elevationAlone: FieldSelection = FieldSelection { field("samples") { field("elevation") } }

        /** Directions, asked for by `directions`. */
        private val directions =
            AnalysisSpec(
                AnalysisKind.Directions,
                DataSchema.DirectionsInput::class,
                DataSchema.DirectionsResult::class,
                { client, input -> client.data.directions(input) },
                { result, _, _ -> directionsGeoJson(result) },
            )

        /** Suggestions, asked for by `suggestions`, the candidate at the chosen place solid. */
        private val suggestions =
            AnalysisSpec(
                AnalysisKind.Suggestions,
                DataSchema.SuggestionsInput::class,
                DataSchema.SuggestionsResult::class,
                { client, input -> client.data.suggestions(input) },
                { result, _, chosen -> suggestionsGeoJson(result, chosen) },
            )

        /** Isochrones, asked for by `isochrones`. */
        private val isochrones =
            AnalysisSpec(
                AnalysisKind.Isochrones,
                DataSchema.IsochronesInput::class,
                DataSchema.IsochronesResult::class,
                { client, input -> client.data.isochrones(input) },
                { result, _, _ -> isochronesGeoJson(result) },
            )

        /** Places, asked for by `geocode`, a place that names no position drawing none. */
        private val places =
            AnalysisSpec(
                AnalysisKind.Places,
                DataSchema.GeocodeInput::class,
                DataSchema.GeocodeResult::class,
                { client, input -> client.data.geocode(input) },
                { result, _, _ -> drawable(placesGeoJson(result)) },
            )

        /** A matrix, asked for by `matrix`, drawn between its input's locations. */
        private val matrix =
            AnalysisSpec(
                AnalysisKind.Matrix,
                DataSchema.MatrixInput::class,
                DataSchema.MatrixResult::class,
                { client, input -> client.data.matrix(input) },
                { result, input, _ ->
                    if (input == null) GeoJsonFeatureCollection(emptyList()) else drawable(matrixGeoJson(input, result))
                },
            )

        /** Points of interest, asked for by `pois`, one that names no position drawing none. */
        private val pois =
            AnalysisSpec(
                AnalysisKind.Pois,
                DataSchema.PoisInput::class,
                DataSchema.PoisResult::class,
                { client, input -> client.data.pois(input) },
                { result, _, _ -> drawable(poisGeoJson(result)) },
            )

        /** The features of [collection] that have a geometry to draw. */
        private fun drawable(collection: GeoJsonFeatureCollection): GeoJsonFeatureCollection =
            GeoJsonFeatureCollection(collection.features.filter { it.geometry != null })

        /** [camera] with its zoom brought within [range], a zoom that is no number left as it is. */
        private fun held(
            camera: CameraPose,
            range: ZoomBounds,
        ): CameraPose =
            when {
                camera.zoom < range.minZoom -> CameraPose(camera.center, range.minZoom, camera.pitch, camera.bearing)
                camera.zoom > range.maxZoom -> CameraPose(camera.center, range.maxZoom, camera.pitch, camera.bearing)
                else -> camera
            }

        /** A position a gesture was read at, its longitude brought within [-180, 180] only where it lies past that range, or null where it is not two numbers. */
        private fun within(position: List<Double>): List<Double>? =
            if (position.size < 2) null else listOf(wrapLongitudePastRange(position[0]), position[1])

        /** Whether [layer] draws at [zoom]: within its zoom window, where it has one, and not hidden. */
        private fun draws(
            layer: MapLayer,
            zoom: Double,
        ): Boolean {
            if (layer.isHidden) return false
            val window = layer.zoom ?: return true
            return zoom >= window.minZoom && zoom < window.maxZoom
        }

        /**
         * [position] read as `[longitude, latitude]`, a marker's or a waypoint's of id [id], an altitude after
         * them ignored, or a refusal where it names no place a map draws: fewer than two numbers or more than
         * three, or a longitude or a latitude that is not finite, the latitude from -90 to 90
         * (`map.marker.lifecycle`).
         */
        private fun place(
            position: List<Double>,
            id: String,
        ): List<Double> {
            if (position.size !in 2..3 || !position[0].isFinite() || !position[1].isFinite() ||
                position[1] !in -90.0..90.0
            ) {
                throw MapException(MapException.Kind.PositionRefused, id)
            }
            return listOf(position[0], position[1])
        }

        /** [sources] with [source] in the place of the one of its id, or added after them. */
        private fun withSource(
            sources: List<MapSource>,
            source: MapSource,
        ): List<MapSource> {
            val place = sources.indexOfFirst { it.id == source.id }
            return if (place < 0) sources + source else sources.toMutableList().also { it[place] = source }
        }

        /**
         * The language a map labels in: the one the app named, else the client's, else the device's, each read as
         * a label language is.
         */
        private fun labelLanguageOf(
            given: RestSchema.Language?,
            client: Mountaya,
        ): RestSchema.Language = labelLanguage(given?.value ?: client.language ?: preferredLanguage())
    }
}
