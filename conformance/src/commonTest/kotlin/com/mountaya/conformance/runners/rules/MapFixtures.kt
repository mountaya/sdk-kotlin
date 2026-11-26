package com.mountaya.conformance

import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.CameraFit
import com.mountaya.elements.CameraTarget
import com.mountaya.elements.Geolocation
import com.mountaya.elements.GeolocationFix
import com.mountaya.elements.GeolocationPermission
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.MapException
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapTap
import com.mountaya.elements.QueriedFeature
import com.mountaya.elements.ZoomLimit
import com.mountaya.geo.CameraPose
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.ZoomBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * MapFixtures builds what the map fixtures write into the engine's own values, and writes the engine's
 * answers back as the fixtures expect them, every value through the published surface: the engine, the
 * scripted surface it draws on, the client and the geolocation seam.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal object MapFixtures {
    /**
     * A client for a map, sending through [exchange], an exchange that answers nothing at all unless one is
     * given, repeating nothing, with a session token so a Data API read is sent.
     *
     * Every answer here arrives whole, so it is read and decoded where the call is, unconfined: a read on
     * a thread of the platform's would leave the test's virtual time free to spend an attempt's budget.
     */
    fun client(
        baseUrls: BaseUrls = BaseUrls.standard,
        exchange: HttpExchange = Recording(Recording.Answer.Failing),
    ): Mountaya =
        Mountaya(
            credential = Credential.Publishable("pk_test_abc", SessionToken.Value("sess_abc")),
            baseUrls = baseUrls,
            retry = Scripted.noRetries,
            exchange = exchange,
            dispatcher = Dispatchers.Unconfined,
        )

    /** The contract's spelling of the kind of a map engine's refusal. */
    fun kind(error: MapException): String =
        when (error.kind) {
            MapException.Kind.PositionRefused -> "position-refused"

            MapException.Kind.NumberRefused -> "number-refused"

            MapException.Kind.ZoomBoundsRefused -> "window-refused"

            MapException.Kind.FixRefused -> "fix-refused"

            MapException.Kind.Closed -> "closed"

            MapException.Kind.LayerNotFound,
            MapException.Kind.MarkerNotFound,
            MapException.Kind.TerrainNotFound,
            MapException.Kind.OverlayNotFound,
            MapException.Kind.AnalysisNotFound,
            -> "not-found"

            MapException.Kind.TypeRefused -> "type-refused"

            MapException.Kind.KindMismatch -> "kind-mismatch"

            MapException.Kind.NoInput -> "no-input"

            MapException.Kind.MaxBoundsRefused -> "bounds-refused"

            else -> error.kind.value
        }

    /** The answer of a camera move refused as [kind]. */
    fun refusedPose(kind: String): FixtureValue =
        objectOf(
            "pose" to FixtureValue.Null,
            "refused" to FixtureValue.Text(kind),
        )

    /** The members a camera move states, and no other. */
    fun target(target: CameraTarget): FixtureValue =
        FixtureValue.Object(
            listOfNotNull(
                target.bearing?.let { "bearing" to FixtureValue.Number(it) },
                target.center?.let { "center" to numbers(it) },
                target.pitch?.let { "pitch" to FixtureValue.Number(it) },
                target.zoom?.let { "zoom" to FixtureValue.Number(it) },
            ),
        )

    /** A camera, `{ bearing, center, pitch, zoom }`. */
    fun camera(pose: CameraPose): FixtureValue =
        objectOf(
            "bearing" to FixtureValue.Number(pose.bearing),
            "center" to numbers(pose.center),
            "pitch" to FixtureValue.Number(pose.pitch),
            "zoom" to FixtureValue.Number(pose.zoom),
        )

    /** FitReading is a drawing call's `fit` as a Kotlin caller can hand it: none, a [CameraFit], or a value no `CameraFit` holds. */
    sealed interface FitReading {
        /** A fit a caller hands, or null for none. */
        class Read(
            val fit: CameraFit?,
        ) : FitReading

        /** A value no `CameraFit` holds, refused by the type before any call as the kind named. */
        class Refused(
            val kind: String,
        ) : FitReading
    }

    /**
     * A drawing call's `fit` as [written]: left out or `false` is none, `true` a fit stating nothing, options a
     * fit of the members they state, each of [unstated] left null. A fit that is neither a boolean nor options
     * is refused as `kind-mismatch`, and a member no `CameraFit` holds, text or a duration that is no number,
     * as `number-refused`. A duration of no finite length is a Kotlin `Duration`, `Duration.INFINITE`, which the
     * engine itself refuses.
     */
    fun readFit(
        written: FixtureValue?,
        unstated: Set<String>,
    ): FitReading {
        if (written == null) return FitReading.Read(null)
        if (written is FixtureValue.Bool) return FitReading.Read(if (written.value) CameraFit() else null)
        val options = written as? FixtureValue.Object ?: return FitReading.Refused("kind-mismatch")
        var padding: PixelPadding? = null
        var maxZoom: Double? = null
        var duration: Duration? = null
        val writtenPadding = options["padding"]
        if (writtenPadding != null && "padding" !in unstated) {
            padding =
                when (writtenPadding) {
                    is FixtureValue.Number -> {
                        PixelPadding(writtenPadding.value)
                    }

                    is FixtureValue.Object -> {
                        val edges =
                            listOf(
                                "top",
                                "right",
                                "bottom",
                                "left",
                            ).map { (writtenPadding[it] as? FixtureValue.Number)?.value }
                        if (edges.any { it == null }) return FitReading.Refused("number-refused")
                        PixelPadding(edges[0] ?: 0.0, edges[1] ?: 0.0, edges[2] ?: 0.0, edges[3] ?: 0.0)
                    }

                    else -> {
                        return FitReading.Refused("number-refused")
                    }
                }
        }
        val writtenCeiling = options["maxZoom"]
        if (writtenCeiling != null && "maxZoom" !in unstated) {
            maxZoom = (writtenCeiling as? FixtureValue.Number)?.value ?: return FitReading.Refused("number-refused")
        }
        val writtenDuration = options["duration"]
        if (writtenDuration != null && "duration" !in unstated) {
            val milliseconds = (writtenDuration as? FixtureValue.Number)?.value
            if (milliseconds == null || milliseconds.isNaN()) return FitReading.Refused("number-refused")
            duration = milliseconds.milliseconds
        }
        return FitReading.Read(CameraFit(padding, maxZoom, duration))
    }

    /** The members a fit states: its padding as one number where the case wrote one, else per edge, its duration in milliseconds. */
    fun fitOptions(
        fit: CameraFit,
        paddingAsOne: Boolean,
    ): FixtureValue {
        val padding = fit.padding
        return FixtureValue.Object(
            listOfNotNull(
                fit.duration?.let { "duration" to FixtureValue.Number(it.toDouble(DurationUnit.MILLISECONDS)) },
                fit.maxZoom?.let { "maxZoom" to FixtureValue.Number(it) },
                padding?.let {
                    "padding" to
                        if (paddingAsOne) {
                            FixtureValue.Number(it.top)
                        } else {
                            objectOf(
                                "bottom" to FixtureValue.Number(it.bottom),
                                "left" to FixtureValue.Number(it.left),
                                "right" to FixtureValue.Number(it.right),
                                "top" to FixtureValue.Number(it.top),
                            )
                        }
                },
            ),
        )
    }

    /** The answer of a drawing call handed a fit. */
    fun fitAnswer(
        drawn: Boolean,
        fit: FixtureValue?,
        refused: String?,
    ): FixtureValue =
        objectOf(
            "drawn" to FixtureValue.Bool(drawn),
            "fit" to (fit ?: FixtureValue.Null),
            "refused" to (refused?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
        )

    /** A collection's specification, stating the box it is framed on, with one source of one layer. */
    val collection: TileSchema.TileSpecification =
        TileSchema.TileSpecification(
            listOf(
                TileSchema.TileSource(
                    id = "collection",
                    layers =
                        listOf(
                            TileSchema.TileLayer(
                                "collection-lines",
                                key = "lines",
                                layout = null,
                                paint = null,
                                source_layer = "itinerary_lines",
                                type = TileSchema.TileLayerType.Line,
                                zoom_max = 22,
                                zoom_min = 0,
                            ),
                        ),
                    tiles = listOf("https://tiles.test/v1/collections/c/{z}/{x}/{y}"),
                    type = TileSchema.TileSourceType.Vector,
                    zoom_max = 14,
                    zoom_min = 0,
                ),
            ),
        ) { bbox = listOf(6.8, 45.8, 6.95, 45.9) }

    /** A zoom limit a call states: left out is kept, null released, a number moved to, and anything else null, a limit no `ZoomLimit` holds. */
    fun limit(written: FixtureValue?): ZoomLimit? =
        when (written) {
            null -> ZoomLimit.Kept
            is FixtureValue.Null -> ZoomLimit.Released
            is FixtureValue.Number -> ZoomLimit.Zoom(written.value)
            else -> null
        }

    /** The zoom window the case writes under [key], `{ max, min }`. */
    fun window(
        input: FixtureValue.Object,
        key: String,
    ): ZoomBounds {
        val window = input.objectOrNull(key) ?: throw AssertionError("the input states no `$key`")
        return ZoomBounds(window.number("min"), window.number("max"))
    }

    /** A zoom window as the fixtures write one, `{ max, min }`. */
    fun windowAnswer(window: ZoomBounds): FixtureValue =
        objectOf("max" to FixtureValue.Number(window.maxZoom), "min" to FixtureValue.Number(window.minZoom))

    /** A feature a renderer found, as the fixtures write one, built through `QueriedFeature`'s published constructor. */
    fun feature(written: FixtureValue): QueriedFeature {
        val fields = written as? FixtureValue.Object ?: throw AssertionError("a feature is not an object")
        val id =
            when (val writtenId = fields["id"]) {
                is FixtureValue.Number -> QueriedFeature.Id.Number(writtenId.value)
                is FixtureValue.Text -> QueriedFeature.Id.Text(writtenId.value)
                else -> null
            }
        val properties =
            when (val writtenProperties = fields["properties"]) {
                null, is FixtureValue.Null -> {
                    emptyMap()
                }

                is FixtureValue.Object -> {
                    writtenProperties.entries.associate { (name, value) ->
                        name to
                            fixtureJson(value)
                    }
                }

                else -> {
                    throw AssertionError("a feature's `properties` is not an object")
                }
            }
        val geometry = fields["geometry"] ?: throw AssertionError("a feature states no geometry")
        return QueriedFeature(
            properties = properties,
            geometry = fixtureGeometry(geometry),
            layer = fields.text("layer"),
            sourceLayer = fields.textOrNull("sourceLayer"),
            id = id,
        )
    }

    /** The features a renderer found under [key] of [input], topmost first, none where it states none. */
    fun features(
        input: FixtureValue.Object,
        key: String,
    ): List<QueriedFeature> = if (input[key] == null) emptyList() else input.items(key).map(::feature)

    /** A feature a query answers, as the fixtures expect one: a GeoJSON feature, with its id where it has one, its layer and its source layer where it has one. */
    fun answer(feature: QueriedFeature): FixtureValue {
        val geometry = fixtureValueOf(Json.encodeToJsonElement(GeoJsonGeometry.serializer(), feature.geometry))
        val id =
            when (val held = feature.id) {
                is QueriedFeature.Id.Number -> FixtureValue.Number(held.value)
                is QueriedFeature.Id.Text -> FixtureValue.Text(held.value)
                null -> null
            }
        return FixtureValue.Object(
            listOfNotNull(
                "geometry" to geometry,
                id?.let { "id" to it },
                "layer" to FixtureValue.Text(feature.layer),
                "properties" to fixtureValueOf(JsonObject(feature.properties)),
                feature.sourceLayer?.let { "sourceLayer" to FixtureValue.Text(it) },
                "type" to FixtureValue.Text("Feature"),
            ),
        )
    }

    /** Places each layer the case names under `placed` at its rank, as a map places its own. */
    fun place(
        input: FixtureValue.Object,
        map: MapEngine,
    ) {
        for (written in input.items("placed")) {
            val layer = written as? FixtureValue.Object ?: throw AssertionError("a placed layer is not an object")
            map.addLayer(
                MapLayer(
                    id = layer.text("id"),
                    type = TileSchema.TileLayerType.Line,
                    source = "placed",
                    rank = layer.number("rank").toInt(),
                ),
            )
        }
    }

    /**
     * Whether the renderer's request [failure] names left under a credential the client no longer sends, its
     * `sentUnder`: `superseded`, or `current`, the credential the client sends as the answer arrives, which a
     * failure stating none left with too. The scripted surface hands it to the engine as the live renderer does,
     * whose rule tells a refusal of the credential of such a request to nobody.
     */
    fun isSuperseded(failure: FixtureValue.Object): Boolean =
        when (val sentUnder = failure.textOrNull("sentUnder") ?: "current") {
            "current" -> false
            "superseded" -> true
            else -> throw AssertionError("no credential is sent under as `$sentUnder`")
        }

    /** A tap or a long tap as the fixtures expect the event, `{ event, lngLat, point }`. */
    fun gesture(
        name: String,
        tap: MapTap,
    ): FixtureValue =
        objectOf(
            "event" to FixtureValue.Text(name),
            "lngLat" to numbers(tap.position),
            "point" to numbers(tap.point),
        )

    /**
     * Every event [map] tells while [play] runs, in order, read by a collector that begins before it, the map
     * closed once [play] is done so the collector's flow ends.
     */
    suspend fun told(
        scope: TestScope,
        map: MapEngine,
        play: suspend () -> Unit,
    ): List<MapEvent> {
        val told = mutableListOf<MapEvent>()
        val reading =
            scope.launch(
                UnconfinedTestDispatcher(scope.testScheduler),
            ) { map.events().collect { told += it } }
        play()
        map.close()
        reading.join()
        return told
    }

    /** [values] as a fixture's array of numbers. */
    fun numbers(values: List<Double>): FixtureValue = FixtureValue.Array(values.map { FixtureValue.Number(it) })

    /** An object of the members given, in order. */
    fun objectOf(vararg members: Pair<String, FixtureValue>): FixtureValue = FixtureValue.Object(members.toList())

    /** The numbers [written] holds as a position, or null where it is no array of numbers, a value no `List<Double>` holds. */
    fun numbersOrNull(written: FixtureValue): List<Double>? =
        try {
            fixturePosition(written, "position")
        } catch (notNumbers: AssertionError) {
            null
        }

    /**
     * The fields a Data API document selects under its operation's [field], as the fixtures write a selection: an
     * object per level, `true` for a field selected whole. A field's arguments are read past.
     */
    fun selection(
        document: String,
        field: String,
    ): FixtureValue {
        val tokens = mutableListOf<String>()
        val word = StringBuilder()
        var arguments = 0
        for (character in document) {
            if (character == '(') arguments += 1
            if (character == ')') {
                arguments -= 1
                continue
            }
            if (arguments != 0) continue
            if (character.isLetterOrDigit() || character == '_') {
                word.append(character)
                continue
            }
            if (word.isNotEmpty()) {
                tokens += word.toString()
                word.clear()
            }
            if (character == '{' || character == '}') tokens += character.toString()
        }
        var index = tokens.indexOf("{")
        if (index < 0) throw AssertionError("the document selects nothing")
        index += 1

        fun level(): FixtureValue.Object {
            val members = mutableListOf<Pair<String, FixtureValue>>()
            while (index < tokens.size) {
                val token = tokens[index]
                index += 1
                when (token) {
                    "}" -> {
                        return FixtureValue.Object(members)
                    }

                    "{" -> {
                        val last = members.removeLastOrNull() ?: throw AssertionError("a selection opens with no field")
                        members += last.first to level()
                    }

                    else -> {
                        members += token to FixtureValue.Bool(true)
                    }
                }
            }
            throw AssertionError("a selection is never closed")
        }
        return level()[field] ?: throw AssertionError("the document selects nothing under `$field`")
    }

    /** Fix is a geolocation answering one fix, which records whether it was asked. */
    class Fix(
        private val answer: GeolocationFix,
    ) : Geolocation {
        var asked: Boolean = false
            private set

        override suspend fun locate(): GeolocationFix {
            asked = true
            return answer
        }

        override suspend fun permission(): GeolocationPermission = GeolocationPermission.Granted
    }

    /**
     * Recording is an exchange that records every request and answers each as it is told: a Data API query that
     * sends no document first asked for it, as the persisted-query protocol does, then answered with its result;
     * any other request with a status alone, or failing with no answer at all.
     */
    class Recording(
        private val answer: Answer,
    ) : HttpExchange {
        /** Answer is how a request is answered. */
        sealed interface Answer {
            /** A Data API query answered with [result], under the operation's name [operation]. */
            class Data(
                val operation: String,
                val result: FixtureValue,
            ) : Answer

            /** An answer of [status] and an error envelope. */
            class Status(
                val status: Int,
            ) : Answer

            /** No answer, the connection failing. */
            object Failing : Answer
        }

        /** Every request sent, in order. */
        val sent = mutableListOf<ExchangeRequest>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += request
            return when (val answering = answer) {
                is Answer.Failing -> {
                    throw Scripted.Dropped()
                }

                is Answer.Status -> {
                    ExchangeResponse(
                        answering.status,
                        JSON_HEADERS,
                        ExchangeBody(Scripted.errorBody("FORBIDDEN", "refused")),
                    )
                }

                is Answer.Data -> {
                    val read = request.body?.decodeToString()?.let { Json.parseToJsonElement(it) as? JsonObject }
                    val text =
                        if (read?.get("query") == null) {
                            """{"errors":[{"message":"PersistedQueryNotFound","extensions":{"code":"PERSISTED_QUERY_NOT_FOUND"}}]}"""
                        } else {
                            JsonObject(
                                mapOf(
                                    "data" to JsonObject(mapOf(answering.operation to fixtureJson(answering.result))),
                                ),
                            ).toString()
                        }
                    ExchangeResponse(200, JSON_HEADERS, ExchangeBody(text.encodeToByteArray()))
                }
            }
        }

        private companion object {
            val JSON_HEADERS: Map<String, String> = mapOf("Content-Type" to "application/json")
        }
    }
}
