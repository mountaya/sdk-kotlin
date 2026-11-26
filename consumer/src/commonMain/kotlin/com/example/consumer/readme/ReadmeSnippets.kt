// Printed from README.md by the build. Never edit it: edit the document, then run
//   mise run readme
// and the diff is the review.
//
// 29 snippets compiled, 20 skipped.
// Skipped: a build script, which consumer/build.gradle.kts declares the same way.
// Skipped: a build script, which consumer/build.gradle.kts declares the same way.
// Skipped: a build script, which examples/trail/build.gradle.kts declares the same way.
// Skipped: JVM code, which consumer/src/jvmMain/kotlin/com/example/consumer/Webhooks.kt compiles in receive().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Navigation.kt compiles in guided().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in TrailMap().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in Selecting().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in Glyphs().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in TrailControls().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in Panels().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in Panels().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in Glyphs().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in OwnStack().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/SearchBox.kt compiles in TrailSearch().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/SearchBox.kt compiles in AppEngineSearch().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in ItineraryScreen().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in ItineraryScreen().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in OverTheAppsEngine().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in PlaceCard().
// Skipped: Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in LinkedPlaceCard().

package com.example.consumer.readme

import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.BaseUrls
import com.mountaya.client.Url
import com.mountaya.client.generated.MODEL_DIGEST
import com.mountaya.client.generated.SCHEMA_VERSION
import com.mountaya.client.SessionTokenEngine
import com.mountaya.admin.admin
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.RequestOptions
import kotlin.time.Duration.Companion.seconds
import com.mountaya.client.ApiException
import com.mountaya.client.ConfigurationException
import com.mountaya.client.ConnectionException
import com.mountaya.client.NotFoundException
import com.mountaya.client.SelectionException
import com.mountaya.client.generated.ApiErrorCode
import com.mountaya.client.RetryPolicy
import com.mountaya.client.isApiException
import com.mountaya.client.isMountayaException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.mountaya.client.FieldSelection
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.readingAsUser
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.DataOperation
import com.mountaya.client.OperationDeclaration
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.SystemExchange
import com.mountaya.client.parseTimestamp
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.ScriptedNavigationEngine
import com.mountaya.geo.TimeLength
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatLngLat
import com.mountaya.geo.routeLengthMeters
import kotlin.time.Instant
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.WordBundle
import com.mountaya.geo.WordOverrides
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.glyphPaint
import com.mountaya.geo.parseGlyph
import com.mountaya.geo.resolveBundle
import kotlinx.serialization.json.putJsonObject
import com.mountaya.geo.LayerRank
import com.mountaya.geo.MapPaint
import com.mountaya.geo.poiStyleOf
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import kotlinx.coroutines.flow.first
import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import com.mountaya.geo.aspectCardinal
import com.mountaya.geo.elevationTilesAt
import com.mountaya.geo.terrainFromElevationTiles
import com.mountaya.geo.NavigationInstruction
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.CameraFit
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapSource
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.PixelPadding
import kotlinx.serialization.json.JsonPrimitive
import com.mountaya.geo.isWatermarkKeptClear
import com.mountaya.geo.watermarkClearance
import com.mountaya.geo.watermarkInset
import com.mountaya.geo.watermarkWidth
import com.mountaya.geo.timeSliderReadout
import com.mountaya.geo.timeSliderRelease
import com.mountaya.geo.timeSliderWindow
import kotlin.time.Clock
import com.mountaya.client.GeoJsonPoint
import com.mountaya.elements.GeometrySlot
import com.mountaya.elements.MapMarker
import com.mountaya.elements.ZoomLimit
import com.mountaya.elements.ScreenProjection
import com.mountaya.elements.WaypointEdit
import com.mountaya.elements.CameraTarget
import com.mountaya.elements.MapEvent
import kotlinx.coroutines.launch
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.countedText
import com.mountaya.geo.scopeOf
import com.mountaya.geo.themeTokenValue
import com.mountaya.geo.SearchConfiguration
import com.mountaya.geo.SearchEngine
import com.mountaya.geo.SearchFetch
import com.mountaya.geo.SearchFilter
import com.mountaya.geo.dataLanguageFor
import com.mountaya.geo.pickTarget
import com.mountaya.geo.searchListState
import com.mountaya.geo.DateLength
import com.mountaya.geo.ResolvedItinerary
import com.mountaya.geo.buildElevationSeries
import com.mountaya.geo.formatAltitudeRange
import com.mountaya.geo.itinerarySegmentRows

/**
 * ReadmeSnippets is every Kotlin snippet of README.md, each in a function of its own, written
 * as a consumer pastes it.
 */
internal object ReadmeSnippets {
    suspend fun snippet1() {
        val client =
            Mountaya(
                credential = Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")),
                language = "fr",
            )
        val itinerary = client.studio.itineraries.getItinerary("itn_4e5f")
        println("${itinerary.data.name} ${itinerary.status} ${itinerary.requestId}")
        client.close()
    }

    suspend fun snippet2() {
        // The contract tag this build speaks, and the digest of the exact files it was printed from.
        println("$SCHEMA_VERSION $MODEL_DIGEST")

        // A client pointed at another environment, its Map Studio API at a staging origin.
        val staging =
            Mountaya(
                Credential.Publishable("pk_test_0a1b"),
                baseUrls = BaseUrls(rest = Url("https://api.staging.example.com")),
            )
        println(staging.baseUrls)
        staging.close()
    }

    suspend fun snippet3() {
        // In an app: a publishable key, and a session token your own backend mints with its secret key.
        val engine = SessionTokenEngine(applicationScope, mint = { fetchSessionFromYourBackend() })
        val app = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Source(engine)))

        // For a visitor holding a share link: the share token replaces the key.
        val visitor = Mountaya(Credential.Share("share_6a7b"))

        // From parts held apart, a key from your configuration and a token from a link.
        val linked = Mountaya(Credential(publishableKey = "pk_live_0a1b", shareToken = "share_6a7b"))

        app.close()
        visitor.close()
        linked.close()
    }

    suspend fun snippet4() {
        /**
         * mintFor answers, on the app's backend under its secret key, the token the app's session engine asks
         * for, for one user, minted for the `studio-embedded` surface every write acts as a member on.
         */
        suspend fun mintFor(
            server: Mountaya,
            userId: String,
        ): SessionToken {
            val minted =
                server.admin.sessions.postSession(
                    AdminSchema.SessionCreateParams(publishable_key = "pk_live_0a1b") {
                        user_id = userId
                        surface = RestSchema.SessionSurface.StudioEmbedded
                    },
                )
            return SessionToken.Minted(minted.data)
        }
    }

    suspend fun snippet5() {
        val client = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))
        val zone = client.studio.zones.getZone("zon_8c9d")

        val renamed =
            client.studio.zones.patchZone(
                "zon_8c9d",
                body = RestSchema.ZoneUpdateParams { name = "Vallée Blanche" },
                options = RequestOptions(timeout = 10.seconds),
            ) {
                idempotencyKey = "rename-zon_8c9d-1"
                ifMatch = zone.data.etag
            }
        println("${renamed.status} ${renamed.data.name} ${renamed.etag}")
        client.close()
    }

    suspend fun snippet6() {
        val client = Mountaya(Credential.Publishable("pk_live_0a1b"))
        try {
            client.studio.itineraries.getItinerary("itn_4e5f")
        } catch (failure: NotFoundException) {
            println("gone ${failure.requestId}")
        } catch (failure: ApiException) {
            if (failure.hasCode(ApiErrorCode.Forbidden)) println("an entry names a scope the key does not reach")
            println("${failure.name} ${failure.code} ${failure.retryAfter}")
        } catch (failure: ConnectionException) {
            println("the request never arrived, after every retry it was owed: ${failure.cause}")
        } catch (failure: ConfigurationException) {
            println(failure.message)
        } catch (failure: SelectionException) {
            println(failure.path)
        }
        client.close()
    }

    suspend fun snippet7() {
        // The contract's schedule with a longer budget of waits for this client, and no retry for one call.
        val client = Mountaya(Credential.Publishable("pk_live_0a1b"), retry = RetryPolicy(waitBudget = 120.seconds))
        try {
            client.studio.activities.getActivities(
                options = RequestOptions(retry = RetryPolicy(maxRetries = 0), timeout = 10.seconds),
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            // Whether a failure is one of the family, and whether the API answered it.
            println("${isMountayaException(failure)} ${isApiException(failure)}")
        }

        // A call you withdraw throws its coroutine's `CancellationException`, never an error of the ladder.
        coroutineScope {
            val call = async { client.studio.activities.getActivities() }
            call.cancel()
            try {
                call.await()
            } catch (withdrawn: CancellationException) {
                println("withdrawn")
            }
        }
        client.close()
    }

    suspend fun snippet8() {
        val client = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))

        // A listing is read a page at a time, each page requested as the rows before it are read.
        client.studio.itineraries
            .walkGetItineraries(maxPages = 10) { sort = RestSchema.GetItinerariesSort.UpdatedAtDescending }
            .collect { itinerary -> println(itinerary.name) }

        // An event stream reopens, waits and resumes on the contract's schedule, and ends on `done`.
        client.agent.runs.getAgentRunEvents("run_0f1e").collect { frame ->
            val event = frame.event
            if (event is RestSchema.AgentMessageEvent) println(event.content)
        }

        // An editor stream is read as its member's user, which a session token given as a value does not
        // name, so that member's own arrival is not read as the stream moving on.
        client.studio.collab.getItineraryEvents("itn_8f14").readingAsUser("usr_7a4e").collect { frame ->
            val event = frame.event
            if (event is RestSchema.CollabPresenceJoinEvent) println(event.user_id)
        }

        // A Data API call narrows its document to the fields it selects.
        val places =
            client.data.geocode(
                DataSchema.GeocodeInput(query = "Chamonix"),
                select =
                    FieldSelection {
                        field("places") {
                            field("name")
                            field("point")
                        }
                    },
            )
        println(places.places?.firstOrNull()?.name)
        client.close()
    }

    suspend fun snippet9() {
        val client = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))

        // A read this build prints no method for, by what the contract declares of it.
        val getThing =
            OperationDeclaration(
                operationId = "getThing",
                method = "GET",
                idempotent = true,
                credentials = CredentialDeclaration(requiresSession = true),
            )
        val thing =
            client.send(getThing, "/v1/things/thg_1a2b", JsonElement.serializer(), JsonObject.serializer().nullable)
        println(thing.data)

        // A listing, walked a page at a time: the walk writes the page size and the cursor into the query.
        val getThings = OperationDeclaration("getThings", "GET", idempotent = true)
        client
            .walk(getThings, "/v1/things", JsonElement.serializer(), query = listOf("kind" to "hut"), maxPages = 5)
            .collect { row -> println(row) }

        // A Data API operation, by its name, its input's type and its field tree.
        val things = DataOperation("things", "ThingsInput!", FieldSelection { field("things") { field("name") } })
        println(client.query(things, buildJsonObject { put("query", "hut") }, JsonElement.serializer()))

        // A stream, read as a feed of records.
        val getThingEvents = OperationDeclaration("getThingEvents", "GET")
        client.events(getThingEvents, "/v1/things/thg_1a2b/events").collect { frame ->
            println("${frame.event.event} ${frame.event.data}")
        }
        client.close()
    }

    suspend fun snippet10() {
        // A test answers every attempt itself.
        val answered =
            object : HttpExchange {
                override suspend fun send(request: ExchangeRequest): ExchangeResponse =
                    ExchangeResponse(200, body = ExchangeBody("""{"data": []}""".encodeToByteArray()))
            }
        val client = Mountaya(Credential.Publishable("pk_test_0a1b"), exchange = answered)
        println(client.studio.activities.getActivities().data.size)

        // An app watches its traffic by wrapping the platform's own exchange.
        val platform = SystemExchange()
        val watched =
            object : HttpExchange {
                override suspend fun send(request: ExchangeRequest): ExchangeResponse {
                    val response = platform.send(request)
                    println("$request ${response.status} ${response.header("X-Request-Id")}")
                    return response
                }
            }
        val app = Mountaya(Credential.Publishable("pk_live_0a1b"), exchange = watched)
        app.close()
        client.close()
    }

    suspend fun snippet11() {
        val client =
            Mountaya(
                Credential.Publishable("pk_live_0a1b"),
                onResponse = { report ->
                    println("${report.operationId} ${report.status} attempt ${report.attempt} ${report.requestId}")
                    println(report.rateLimit?.policies)
                },
            )
        client.studio.activities.getActivities()
        client.close()
    }

    suspend fun snippet12() {
        // A rule is a function over the wire's own values: a position is [longitude, latitude].
        val line = listOf(listOf(6.8652, 45.9237), listOf(6.8752, 45.9287), listOf(6.9271, 45.9682))
        val meters = routeLengthMeters(line)

        // A value is written for its reader, in their language and their units, never the device's.
        val units = UnitPreferences(language = "fr-FR")
        println("${formatDistance(meters, units)} ${formatLngLat(line[0])}")

        // A time of day is the locale's own, on its own clock, in the zone you name: 오후 1:00 in Korean.
        val arrival = Instant.parse("2026-03-14T00:00:00Z")
        println(formatDateTime(arrival, "ko-KR", "Pacific/Auckland", timeLength = TimeLength.Short))
        // A timestamp held as text is read as the wire's are, the same instant here, and null where it names none.
        println(parseTimestamp("2026-03-14T13:00:00+13:00") == arrival)

        // In an app's own tests, the scripted engine is the navigation, and the test answers each request.
        val navigation = ScriptedNavigationEngine<String>()
        navigation.start("Refuge du Couvercle")
        navigation.sent[0].answer(NavigationRoute(line = listOf(line[0], line[2]), duration = 5_400.0))
        navigation.setPosition(line[2])
        println(navigation.state.value.status)
        navigation.close()
    }

    suspend fun snippet13() {
        val close = buildJsonObject { putJsonObject("controls") { put("close", "Fermer la carte") } }
        val words = resolveBundle(WordBundle.elements, RestSchema.Language.Fr, WordOverrides.BySection(close))
        val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
        // A glyph's markup, read into the shapes a view draws, each painted as its set paints it.
        val pin = parseGlyph(GlyphSet.marker.markup["map-pin"].orEmpty())
        val paints = pin.map { glyphPaint(it, GlyphSet.marker) }
        // A licensed set of your own, its missing glyphs drawn from the bundled marker set.
        val licensed = GlyphSet.of("licensed", GlyphSet.Paint.Stroke, markup = mapOf("refuge" to "<path d=\"M3 21h18\"/>"))
        val markers = licensed?.let { GlyphSet.chaining(listOf(it, GlyphSet.marker)) } ?: GlyphSet.marker
        println("${words["controls"]} ${tokens.accent.light.hex} ${paints.size} shapes, ${markers.markup.size} glyphs")
    }

    suspend fun snippet14() {
        // Every member you leave is the contract's; a band of your own sits between two of the map's.
        val paint =
            MapPaint.standard.copy {
                itinerary.casingColor = "#1b1b1b"
                layerRanks += "acme_avalanche" to LayerRank.Poi.rank + 2
            }
        val hut = poiStyleOf(DataSchema.POICategory.Hut)
        println("${paint.itinerary.lineWidthStops} ${hut?.glyph} ${hut?.color?.hex}")
    }

    suspend fun snippet15() {
        val client = Mountaya(Credential.Publishable("pk_live_0a1b"))
        // One cache per client: every element reading through it shares its reads.
        val itineraries =
            ItineraryResourceCache(applicationScope) { id, surface ->
                client.studio.itineraries.getItinerary(id) { if (surface != null) this.surface = surface }.data
            }
        // An element handed an id shows every projection at once, once its answer lands.
        val profile = ItineraryResourceEngine(itineraries, itineraryId = "itn_4e5f")
        val resource = profile.state.first { it is ItineraryResource.Resolved }
        if (resource is ItineraryResource.Resolved) {
            println("${resource.itinerary.legs.map { it.name }} ${resource.itinerary.waypoints.mapNotNull { it.name }}")
        }
        // Once your app has saved the itinerary, every element over it reads it again.
        itineraries.invalidate("itn_4e5f")
        // An engine you let go of is closed, as a composable's `DisposableEffect` closes it.
        profile.close()
    }

    suspend fun snippet16() {
        val source = ElevationSource(ElevationEncoding.Mapbox, tileSize = 512, zoom = 14)
        val here = listOf(6.8652, 45.9237)
        // Each tile's pixels, red, green and blue packed as an Android `Bitmap` hands them.
        val tiles = elevationTilesAt(here, source).map { address -> ElevationTile(address, 512, IntArray(512 * 512)) }
        val terrain = terrainFromElevationTiles(here, source, tiles)
        println("${terrain?.elevation} ${terrain?.slope} ${terrain?.aspect?.let(::aspectCardinal)?.code}")
    }

    suspend fun snippet17() {
        fun waypointSentence(instruction: NavigationInstruction): String? =
            when (instruction.key) {
                NavigationInstructionKey.Waypoint -> "Passez par ${instruction.name}"
                NavigationInstructionKey.WaypointPlain -> "Passez le point de passage"
                else -> null
            }
    }

    suspend fun snippet18() {
        val mountaya = Mountaya(Credential.Publishable("pk_live_0a1b"), language = "fr")
        val map = MapEngine(mountaya)
        map.setTerrain()
        // An itinerary by id, the camera framing it once it is drawn; a later call takes its place.
        map.setItinerary("itn_4e5f", fit = CameraFit(padding = PixelPadding(48.0)))
        // The zones of your credential's scope, each layer at the rank its source layer declares.
        map.setZones(true)

        map.addSource(MapSource("trail", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
        map.addLayer(
            MapLayer(
                id = "trail",
                type = TileSchema.TileLayerType.Line,
                source = "trail",
                rank = LayerRank.Poi.rank + 2,
                paint = mapOf("line-color" to JsonPrimitive("#e64980"), "line-width" to JsonPrimitive(4)),
            ),
        )
        // A member set to a value replaces the one held, JsonNull included; one mapped to null is kept.
        map.editLayer("trail", paint = mapOf("line-color" to JsonPrimitive("#228be6"), "line-width" to null))
        println(map.state.value.layers.map { it.id })
    }

    suspend fun snippet19() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
        // Null where the client holds no publishable key; a read that fails throws, and the next load reads again.
        val organization = map.loadOrganization()
        println("${organization?.name} ${organization?.white_label} ${map.organization?.white_label}")
        // Where the read stands now; collect the flow to be told each move. Only white label releases the frame.
        val keepsClear = isWatermarkKeptClear(map.organizationRead.value)
        // The frame's extent from the safe area's top-left corner, in dp: a view of yours starts right of it or below it.
        println("Keep clear: $keepsClear, right of ${watermarkInset + watermarkWidth} or below $watermarkClearance")
    }

    suspend fun snippet20() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
        val catalogue = map.loadLayers()
        println(catalogue.terrains.map { it.slug })
        map.setOverlay("snow-depth")
        map.addEnrichment("avalanche-risk")
        map.setLayerVisibility("avalanche-risk-fills", visible = false)
        for (key in map.drawnLayerKeys()) if (key == "labels") map.setKeyVisibility(key, visible = false)
        map.setAccessRestrictionsVisibility(false)
    }

    suspend fun snippet21() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
        map.setOverlay("snow-depth")
        val window = timeSliderWindow(Clock.System.now())
        val sixHoursAhead = window.nowValue + 6
        map.setInstant(timeSliderRelease(window, sixHoursAhead)?.pin)
        val readout = timeSliderReadout(window, sixHoursAhead)
        println("${readout.word} ${readout.count}")
        map.setInstant(null)
    }

    suspend fun snippet22() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
        val reach =
            map.addIsochrones(
                DataSchema.IsochronesInput(
                    DataSchema.ActivitySlug.HikingAndTrail,
                    listOf(listOf(6.8652, 45.9237)),
                    listOf(1_800.0, 3_600.0),
                    DataSchema.RangeType.Time,
                ),
            )
        // A patch answers the input to ask with, built from the one held.
        map.editIsochrones(reach.id, patch = { DataSchema.IsochronesInput(it.activity, it.locations, listOf(900.0), it.rangeType) })
        map.removeAnalysis(reach.id)

        map.setGeometry(GeoJsonPoint(listOf(6.8652, 45.9237)), GeometrySlot.Highlight)
        map.clearGeometry(GeometrySlot.Highlight)
    }

    suspend fun snippet23() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
        map.addMarker(MapMarker("hut", listOf(6.8652, 45.9237), glyph = "hut", label = "Refuge"))
        // A member left null keeps the one the marker holds, and the marker keeps its place.
        map.editMarker("hut", color = "#e64980")
        // [west, south, east, north]: what the map shows stays within it, and null lifts it.
        map.setMaxBounds(listOf(6.7, 45.8, 7.0, 46.0))
        // A limit left out is kept, and a released one goes back to the renderer's own, 0 and 25.5.
        map.setZoomBounds(min = ZoomLimit.Zoom(8.0), max = ZoomLimit.Zoom(17.0))
        map.setZoomBounds(max = ZoomLimit.Released)
    }

    suspend fun snippet24() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
        map.setActiveSegments(listOf(listOf(listOf(6.85, 45.83), listOf(6.87, 45.83))))
        map.beginWaypointEdit("w1")
        // MapView places it at each click; a map drawn some other way hands its own projection.
        map.placeWaypoint(listOf(6.86, 45.83), ScreenProjection(point = { it }, position = { it }))
        val placed: WaypointEdit? = map.endWaypointEdit()
        println(placed?.position)
    }

    suspend fun snippet25() {
        val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
        // A move states the members it changes; the bearing and the pitch here are kept.
        map.ease(CameraTarget(center = listOf(6.8652, 45.9237), zoom = 13.0))
        map.fitBounds(listOf(6.8, 45.8, 6.95, 45.9), CameraFit(padding = PixelPadding(48.0), maxZoom = 15.0))
        map.zoomIn()
        // What the map drew under a point of the view, in density-independent pixels, topmost first: every
        // feature with the layer that drew it, the points of interest alone, and the itineraries alone.
        val point = map.project(listOf(6.8652, 45.9237))
        println(map.featuresAt(point).map { it.layer })
        println(map.poisAt(point).size)
        println(map.itinerariesAt(point).map { it.properties["segment_id"] })
        println(map.elevationAt(listOf(6.8652, 45.9237)))
        // What a credit button lists: Mountaya first, then each source the map draws.
        println(map.credits)
        applicationScope.launch {
            map.events().collect { event ->
                when (event) {
                    is MapEvent.FeatureClick -> println(event.features.map { it.layer })
                    is MapEvent.AccessRefused -> println("${event.status} at ${event.url}")
                    is MapEvent.LayersChange -> println(event.layers?.terrains?.map { it.slug })
                    else -> Unit
                }
            }
        }
    }

    suspend fun snippet26() {
        // A scope nested in another takes every setting it does not state from it.
        val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH"))
        val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
        val results = JsonObject(mapOf("one" to JsonPrimitive("{count} résultat"), "other" to JsonPrimitive("{count} résultats")))
        println(french.word("controls.zoomIn"))
        println(themeTokenValue(tokens.controlBg, ThemeScheme.Dark).hex)
        println(countedText(results, 1_234, RestSchema.Language.Fr))
    }

    suspend fun snippet27() {
        val mountaya = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))
        // The engine waits for the typing to pause, asks one request per branch and keeps the latest.
        val search =
            SearchEngine(
                applicationScope,
                SearchConfiguration(
                    fetch = SearchFetch { input -> mountaya.data.search(input) },
                    language = dataLanguageFor("fr-CH"),
                    targets = listOf(DataSchema.SearchTarget.Pois, DataSchema.SearchTarget.Places),
                ),
            )
        search.setQuery("refuge")
        val settled = search.state.first { it.isSettled }
        search.close()
        // What a list shows under a filter, each title with what matched in brackets, and what picking a row
        // does to a map.
        for (row in searchListState(settled, SearchFilter.Places).rows) {
            val title = row.parts.joinToString("") { part -> if (part.match) "[${part.text}]" else part.text }
            println("$title: ${if (pickTarget(row.payload).mark == null) "unmarked" else "marked"}")
        }
    }

    suspend fun snippet28() {
        /**
         * cutoffWriter answers how a cut-off is written for a reader of [language], its date and its time of
         * day in [zone], or null where the platform knows no zone of that name, so no cut-off is written.
         */
        fun cutoffWriter(
            language: String,
            zone: String,
        ): ((Instant) -> String)? {
            // Whether the platform knows a zone does not hang on the instant, so one reading answers for every cut-off.
            formatDateTime(Instant.fromEpochSeconds(0), language, zone) ?: return null
            return { cutoff ->
                checkNotNull(formatDateTime(cutoff, language, zone, DateLength.Medium, TimeLength.Short)) {
                    "The platform knew the zone $zone when the writer was made."
                }
            }
        }
    }

    suspend fun snippet29() {
        val client = Mountaya(Credential.Publishable("pk_live_0a1b"))
        val itinerary = client.studio.itineraries.getItinerary("itn_4e5f").data
        // The projections every data element reads, and the rows a segment table states.
        val resolved = ResolvedItinerary(itinerary)
        val series = buildElevationSeries(resolved.geometry)
        val units = UnitPreferences("fr")
        for (row in itinerarySegmentRows(series, resolved.legs)) {
            val band = formatAltitudeRange(row.metrics.altitudeMin, row.metrics.altitudeMax, units)
            println("${row.name}: ${formatDistance(row.metrics.sectionDistance, units)}, $band")
        }
        client.close()
    }
}
