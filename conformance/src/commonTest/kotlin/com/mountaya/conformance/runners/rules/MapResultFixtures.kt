package com.mountaya.conformance

import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.generated.DataSchema
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapSource
import com.mountaya.geo.FEATURE_KIND_PROPERTY
import com.mountaya.geo.GeoJsonFeature
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * MapResultFixtures plays the Data API result scripts of the map's fixtures: the results a step hands over or
 * asks for, the patches an edit applies, and what each result the map draws reads as, every value through the
 * published surface.
 */
internal object MapResultFixtures {
    /** The JSON a Data API input is written in, as the client writes it: a member unset left out. */
    private val json = Json { encodeDefaults = false }

    /** Draws a result of [kind] handed over as [result] writes it, and answers its id. */
    suspend fun add(
        kind: String,
        result: FixtureValue,
        map: MapEngine,
    ): String =
        when (kind) {
            "directions" -> map.addDirections(decoded(result, DataSchema.DirectionsResult.serializer())).id
            "suggestions" -> map.addSuggestions(decoded(result, DataSchema.SuggestionsResult.serializer())).id
            "isochrones" -> map.addIsochrones(decoded(result, DataSchema.IsochronesResult.serializer())).id
            "places" -> map.addPlaces(decoded(result, DataSchema.GeocodeResult.serializer())).id
            else -> throw AssertionError("no result of kind `$kind` is drawn as handed over")
        }

    /** Asks for a result of [kind] with the [input] the fixture writes, and answers its id. */
    suspend fun ask(
        kind: String,
        input: FixtureValue,
        map: MapEngine,
    ): String =
        when (kind) {
            "isochrones" -> map.addIsochrones(decoded(input, DataSchema.IsochronesInput.serializer())).id
            "suggestions" -> map.addSuggestions(decoded(input, DataSchema.SuggestionsInput.serializer())).id
            "directions" -> map.addDirections(decoded(input, DataSchema.DirectionsInput.serializer())).id
            else -> throw AssertionError("no result of kind `$kind` is asked for")
        }

    /**
     * Edits the result [id] through the edit of [kind]: no patch where [stated] holds no member, else one writing
     * each member it states onto the input held, through the input's own encoding, and the draw [options] the
     * case states.
     */
    suspend fun edit(
        kind: String,
        id: String,
        stated: List<Pair<String, FixtureValue>>,
        options: FixtureValue.Object?,
        map: MapEngine,
    ) {
        val chosen = options?.let { wholeOrNull(it, "chosen") }
        when (kind) {
            "isochrones" -> map.editIsochrones(id, patched(stated, DataSchema.IsochronesInput.serializer()))
            "suggestions" -> map.editSuggestions(id, patched(stated, DataSchema.SuggestionsInput.serializer()), chosen)
            "directions" -> map.editDirections(id, patched(stated, DataSchema.DirectionsInput.serializer()))
            else -> throw AssertionError("no result of kind `$kind` is edited")
        }
    }

    /** Clears every result of [kind]. */
    fun clear(
        kind: String,
        map: MapEngine,
    ) {
        when (kind) {
            "directions" -> map.clearDirections()
            "suggestions" -> map.clearSuggestions()
            "isochrones" -> map.clearIsochrones()
            "places" -> map.clearPlaces()
            "matrix" -> map.clearMatrix()
            "pois" -> map.clearPointsOfInterest()
            else -> throw AssertionError("no kind `$kind` is cleared")
        }
    }

    /**
     * A patch writing each of [stated] onto an input, through the input's own encoding, or null where it states
     * no member.
     */
    fun <Input> patched(
        stated: List<Pair<String, FixtureValue>>,
        serializer: KSerializer<Input>,
    ): ((Input) -> Input)? {
        if (stated.isEmpty()) return null
        return { input ->
            val written = json.encodeToJsonElement(serializer, input) as JsonObject
            val members = written.toMutableMap()
            for ((key, value) in stated) members[key] = fixtureJson(value)
            json.decodeFromJsonElement(serializer, JsonObject(members))
        }
    }

    /** [written] read by the wire's own decoder through [serializer]. */
    fun <Value> decoded(
        written: FixtureValue,
        serializer: KSerializer<Value>,
    ): Value = json.decodeFromJsonElement(serializer, fixtureJson(written))

    /**
     * Each result drawn, by the name it was added under, as [write] reads the features its source draws; a source
     * no step drew fails the case.
     */
    fun drawn(
        ids: List<Pair<String, String>>,
        map: MapEngine,
        write: (List<GeoJsonFeature>) -> FixtureValue,
    ): FixtureValue =
        FixtureValue.Object(
            map.state.value.sources.map { source ->
                val name =
                    ids.lastOrNull { "mountaya-${it.second}" == source.id }?.first
                        ?: throw AssertionError("the map draws `${source.id}`, which no step drew")
                val features =
                    (source.kind as? MapSource.Kind.GeoJson)?.features?.features
                        ?: throw AssertionError("`${source.id}` is no GeoJSON")
                name to write(features)
            },
        )

    /** What each named result draws: an isochrone band's value, or whether a suggested candidate is the one chosen. */
    fun marks(
        ids: List<Pair<String, String>>,
        map: MapEngine,
    ): FixtureValue =
        drawn(ids, map) { features ->
            FixtureValue.Array(
                features.map { feature ->
                    val kind = feature.properties[FEATURE_KIND_PROPERTY]
                    val mark =
                        if (kind ==
                            JsonPrimitive("suggestion")
                        ) {
                            feature.properties["chosen"]
                        } else {
                            feature.properties["value"]
                        }
                    mark?.let(::fixtureValueOf) ?: FixtureValue.Null
                },
            )
        }

    /**
     * Queries is an exchange answering the Data API's queries: a query sent with its persisted digest alone is
     * refused at once, as the protocol asks, and one sending its full document is recorded, then answered at once
     * or, [holding], held until a step answers it: isochrones with a band per value of the query's `range`, each
     * band's value that value, suggestions with three candidates and directions with one route. A query held
     * fails with the cancellation of the coroutine that sent it the moment that coroutine is cancelled, which it
     * records.
     */
    class Queries(
        private val holding: Boolean,
    ) : HttpExchange {
        /** One query sent with its full document. */
        class Query(
            val kind: String,
            val input: JsonElement,
        ) {
            internal val answer = CompletableDeferred<ExchangeResponse>()
            var cancelled: Boolean = false
                internal set

            /** The query as the fixtures write one, `{kind, input}`. */
            val written: FixtureValue
                get() = MapFixtures.objectOf("input" to fixtureValueOf(input), "kind" to FixtureValue.Text(kind))
        }

        /** Every query sent with its full document, in order. */
        val queries = mutableListOf<Query>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val body = request.body?.decodeToString()?.let { Json.parseToJsonElement(it) as? JsonObject }
            val document =
                (body?.get("query") as? JsonPrimitive)?.content
                    ?: return MapContentFixtures.response(
                        200,
                        """{"errors": [{"message": "PersistedQueryNotFound", "extensions": {"code": "PERSISTED_QUERY_NOT_FOUND"}}]}""",
                    )
            val input = (body["variables"] as? JsonObject)?.get("input") ?: JsonNull
            val query = Query(kindOf(document), input)
            queries += query
            if (!holding) return MapContentFixtures.response(200, answered(query.kind, input))
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
            query.answer.complete(MapContentFixtures.response(200, answered(query.kind, query.input)))
        }

        /** Fails every query still held, as a script ends, so no call waits for ever. */
        fun release() {
            for (query in queries) query.answer.completeExceptionally(IllegalStateException("the script ended"))
        }

        private companion object {
            /** The kind a document asks for, read off its operation's name, `Geocode` reading as `places`. */
            fun kindOf(document: String): String {
                val words = document.split(Regex("[^A-Za-z]+")).filter { it.isNotEmpty() }
                val index = words.indexOf("query")
                if (index < 0 || index + 1 >= words.size) return ""
                val operation = words[index + 1].lowercase()
                return if (operation == "geocode") "places" else operation
            }

            /** The body a query of [kind] is answered with. */
            fun answered(
                kind: String,
                input: JsonElement,
            ): String {
                fun route(seed: Int) =
                    """{"distance": 1000, "duration": 600, "geometry": {"coordinates": [[6.86, 45.83], [6.9, 45.9]], "type": "LineString"}, "summary": "Route $seed"}"""
                val center =
                    ((input as? JsonObject)?.get("locations") as? JsonArray)?.firstOrNull()?.toString()
                        ?: "[6.86, 45.83]"
                return when (kind) {
                    "isochrones" -> {
                        val range = ((input as? JsonObject)?.get("range") as? JsonArray).orEmpty()
                        val bands =
                            range.map { value ->
                                """{"area": 1, "center": $center, "geometry": {"coordinates": [[[6.85, 45.82], [6.87, 45.82], [6.87, 45.84], [6.85, 45.82]]], "type": "Polygon"}, "reachFactor": 1, "value": ${value.jsonPrimitive.content}}"""
                            }
                        """{"data": {"isochrones": {"isochrones": [${bands.joinToString(",")}]}}}"""
                    }

                    "suggestions" -> {
                        """{"data": {"suggestions": {"routes": [${route(0)}, ${route(1)}, ${route(2)}]}}}"""
                    }

                    else -> {
                        """{"data": {"directions": {"routes": [${route(0)}]}}}"""
                    }
                }
            }
        }
    }
}
