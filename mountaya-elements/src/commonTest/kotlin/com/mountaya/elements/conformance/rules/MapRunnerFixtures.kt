package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.TileSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.MapPaint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * MapRunnerFixtures is what this module's map runners read a fixture with and write an answer in: JSON both
 * ways, a style layer as MapLibre's style specification writes it, a paint patch folded as a consumer restyles
 * a copy, a specification of the layers a case names, and a client over a scripted exchange. The conformance
 * module's own helpers read the public surface alone, and the rules of the entries this module runs are
 * internal, so its runners keep their own here.
 */
internal object MapRunnerFixtures {
    /** The largest magnitude below which every whole double is an exact `Long`, 2^53. */
    private const val EXACT_WHOLE_LIMIT = 9_007_199_254_740_992.0

    /** The JSON a value is written in: a member unset left out, as the client writes one. */
    val json: Json = Json { encodeDefaults = false }

    /**
     * A client for a map, over [exchange], with a session token so a Data API read is sent. Every answer
     * here arrives whole, so it is read and decoded where the call is, unconfined: a read on a thread of
     * the platform's would leave the test's virtual time free to spend an attempt's budget.
     */
    fun client(exchange: HttpExchange = Answering { 404 to "{}" }): Mountaya =
        Mountaya(
            Credential.Publishable("pk_test_abc", SessionToken.Value("sess_abc")),
            exchange = exchange,
            dispatcher = Dispatchers.Unconfined,
        )

    /** [value] as JSON, a whole number written as one so a member the wire types as an `Int` decodes. */
    fun json(value: FixtureValue): JsonElement =
        when (value) {
            is FixtureValue.Null -> {
                JsonNull
            }

            is FixtureValue.Bool -> {
                JsonPrimitive(value.value)
            }

            is FixtureValue.Text -> {
                JsonPrimitive(value.value)
            }

            is FixtureValue.Number -> {
                val number = value.value
                val whole =
                    number.isFinite() && number == kotlin.math.floor(number) &&
                        kotlin.math.abs(number) < EXACT_WHOLE_LIMIT
                if (whole) JsonPrimitive(number.toLong()) else JsonPrimitive(number)
            }

            is FixtureValue.Array -> {
                JsonArray(value.items.map(::json))
            }

            is FixtureValue.Object -> {
                JsonObject(value.entries.associate { (key, item) -> key to json(item) })
            }
        }

    /** [element] as a fixture value, an answer is compared as. */
    fun value(element: JsonElement): FixtureValue =
        when (element) {
            is JsonNull -> {
                FixtureValue.Null
            }

            is JsonPrimitive -> {
                when {
                    element.isString -> {
                        FixtureValue.Text(element.content)
                    }

                    element.booleanOrNull != null -> {
                        FixtureValue.Bool(element.content == "true")
                    }

                    else -> {
                        FixtureValue.Number(
                            element.doubleOrNull ?: throw AssertionError("`${element.content}` is no number"),
                        )
                    }
                }
            }

            is JsonArray -> {
                FixtureValue.Array(element.map(::value))
            }

            is JsonObject -> {
                FixtureValue.Object(element.entries.sortedBy { it.key }.map { it.key to value(it.value) })
            }
        }

    /** [texts] as a fixture's array of strings. */
    fun texts(texts: List<String>): FixtureValue = FixtureValue.Array(texts.map { FixtureValue.Text(it) })

    /** The strings [items] holds, failing with a sentence on any other value. */
    fun textsOf(items: List<FixtureValue>): List<String> =
        items.map {
            (it as? FixtureValue.Text)?.value ?: throw AssertionError(
                "a list holds a value that is no string",
            )
        }

    /**
     * [layer] as MapLibre's style specification writes it: `{id, type, source, source-layer, filter, layout,
     * paint, minzoom, maxzoom}`, each member it does not state left out, an empty paint and an empty layout
     * among them.
     */
    fun styleLayer(layer: MapLayer): FixtureValue {
        val members = mutableListOf<Pair<String, FixtureValue>>()
        layer.filter?.let { members += "filter" to value(it) }
        members += "id" to FixtureValue.Text(layer.id)
        if (layer.layout.isNotEmpty()) members += "layout" to value(JsonObject(layer.layout))
        layer.zoom?.let {
            members += "maxzoom" to FixtureValue.Number(it.maxZoom)
            members += "minzoom" to FixtureValue.Number(it.minZoom)
        }
        if (layer.paint.isNotEmpty()) members += "paint" to value(JsonObject(layer.paint))
        members += "source" to FixtureValue.Text(layer.source)
        layer.sourceLayer?.let { members += "source-layer" to FixtureValue.Text(it) }
        members += "type" to FixtureValue.Text(layer.type.value)
        return FixtureValue.Object(members)
    }

    /**
     * The paint a case's [patch] resolves to: `MapPaint.standard` with each member it states set on a copy, a
     * member written null left as shipped, and a member the paint holds no place for failing the case.
     */
    fun paint(patch: FixtureValue?): MapPaint {
        if (patch == null || patch is FixtureValue.Null) return MapPaint.standard
        val members = (patch as? FixtureValue.Object)?.entries ?: throw AssertionError("a patch is not an object")
        return MapPaint.standard.copy {
            for ((key, written) in members) {
                if (written is FixtureValue.Null) continue
                if (key !=
                    "itinerary"
                ) {
                    throw AssertionError("a patch here sets the itinerary's paint alone, not `$key`")
                }
                val group =
                    (written as? FixtureValue.Object)?.entries ?: throw AssertionError("`itinerary` is not an object")
                for ((member, stated) in group) {
                    if (stated is FixtureValue.Null) continue
                    when (member) {
                        "casingColor" -> {
                            itinerary.casingColor =
                                (stated as? FixtureValue.Text)?.value ?: notOfItsShape(member)
                        }

                        "casingOpacity" -> {
                            itinerary.casingOpacity =
                                (stated as? FixtureValue.Number)?.value ?: notOfItsShape(member)
                        }

                        "casingWidthStops" -> {
                            itinerary.casingWidthStops = numbers(stated, member)
                        }

                        "dashArray" -> {
                            itinerary.dashArray = numbers(stated, member)
                        }

                        "dashedOpacity" -> {
                            itinerary.dashedOpacity =
                                (stated as? FixtureValue.Number)?.value ?: notOfItsShape(member)
                        }

                        "lineWidthStops" -> {
                            itinerary.lineWidthStops = numbers(stated, member)
                        }

                        else -> {
                            throw AssertionError("an itinerary's paint holds no member `$member`")
                        }
                    }
                }
            }
        }
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

    /**
     * Holds every constant the case's fixture declares to [stated], the value the rule states for it, a number
     * as the same number and every other value exactly; [inputs] names the constants the cases read as their
     * input, known and held to nothing. A constant declared and not stated fails, and so does one stated the
     * fixture no longer declares.
     */
    fun holdConstants(
        case: ConformanceCase,
        stated: Map<String, FixtureValue>,
        inputs: Set<String> = emptySet(),
    ) {
        val declared = case.root("constants")
        for ((name, written) in declared.entries) {
            if (name in inputs) continue
            val rule = stated[name] ?: throw AssertionError("the fixture declares `$name`, which no rule here states")
            if (!same(
                    written,
                    rule,
                )
            ) {
                throw AssertionError("the fixture declares `$name`, and the rule states another value")
            }
        }
        val undeclared = stated.keys + inputs - declared.keys.toSet()
        if (undeclared.isNotEmpty()) {
            throw AssertionError(
                "the fixture no longer declares ${undeclared.sorted().joinToString(", ")}",
            )
        }
    }

    /** Whether [a] and [b] are the same value, a number as the same number and an object whatever its key order. */
    private fun same(
        a: FixtureValue,
        b: FixtureValue,
    ): Boolean =
        when (a) {
            is FixtureValue.Null -> {
                b is FixtureValue.Null
            }

            is FixtureValue.Bool -> {
                b is FixtureValue.Bool && b.value == a.value
            }

            is FixtureValue.Number -> {
                b is FixtureValue.Number && b.value == a.value
            }

            is FixtureValue.Text -> {
                b is FixtureValue.Text && b.value == a.value
            }

            is FixtureValue.Array -> {
                b is FixtureValue.Array && b.items.size == a.items.size &&
                    a.items.indices.all { same(a.items[it], b.items[it]) }
            }

            is FixtureValue.Object -> {
                b is FixtureValue.Object && b.keys.toSet() == a.keys.toSet() &&
                    a.entries.all { (key, item) -> same(item, b[key] ?: FixtureValue.Null) }
            }
        }

    private fun numbers(
        value: FixtureValue,
        member: String,
    ): List<Double> =
        (value as? FixtureValue.Array)?.items?.map { (it as? FixtureValue.Number)?.value ?: notOfItsShape(member) }
            ?: notOfItsShape(member)

    private fun notOfItsShape(member: String): Nothing =
        throw AssertionError("`$member` is not of the shape the paint holds")

    /** A response of [status] carrying [body], JSON. */
    fun response(
        status: Int,
        body: String,
    ): ExchangeResponse =
        ExchangeResponse(status, mapOf("Content-Type" to "application/json"), ExchangeBody(body.encodeToByteArray()))

    /** Answering is an exchange answering every request at once as [answer] says. */
    class Answering(
        private val answer: (ExchangeRequest) -> Pair<Int, String>,
    ) : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val (status, body) = answer(request)
            return response(status, body)
        }
    }

    /**
     * IsochroneQueries is an exchange answering the Data API's isochrone queries: a query sent with its persisted
     * digest alone is refused at once, as the protocol asks, and one sending its full document is recorded and
     * held until a step answers it, with a band per value of its `range`, each band's value that value. A query
     * held fails with the cancellation of the coroutine that sent it the moment that coroutine is cancelled,
     * which it records.
     */
    class IsochroneQueries : HttpExchange {
        /** One query sent with its full document. */
        class Query(
            val input: JsonElement,
        ) {
            internal val answer = CompletableDeferred<ExchangeResponse>()
            var cancelled: Boolean = false
                internal set
        }

        /** Every query sent with its full document, in order. */
        val queries = mutableListOf<Query>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val body = request.body?.decodeToString()?.let { Json.parseToJsonElement(it) as? JsonObject }
            if (body?.get("query") == null) {
                return response(
                    200,
                    """{"errors": [{"message": "PersistedQueryNotFound", "extensions": {"code": "PERSISTED_QUERY_NOT_FOUND"}}]}""",
                )
            }
            val query = Query((body["variables"] as? JsonObject)?.get("input") ?: JsonNull)
            queries += query
            try {
                return query.answer.await()
            } catch (cancelled: CancellationException) {
                if (!currentCoroutineContext().isActive) query.cancelled = true
                throw cancelled
            }
        }

        /** Lets the held query [index] answer, where it is still held. */
        fun answer(index: Int) {
            val query = queries.getOrNull(index) ?: return
            val range = ((query.input as? JsonObject)?.get("range") as? JsonArray).orEmpty()
            val bands =
                range.map { value ->
                    """{"area": 1, "center": [6.86, 45.83], "geometry": {"coordinates": [[[6.85, 45.82], [6.87, 45.82], [6.87, 45.84], [6.85, 45.82]]], "type": "Polygon"}, "reachFactor": 1, "value": $value}"""
                }
            query.answer.complete(
                response(200, """{"data": {"isochrones": {"isochrones": [${bands.joinToString(",")}]}}}"""),
            )
        }

        /** Fails every query still held, as a script ends, so no call waits for ever. */
        fun release() {
            for (query in queries) query.answer.completeExceptionally(IllegalStateException("the script ended"))
        }
    }
}
