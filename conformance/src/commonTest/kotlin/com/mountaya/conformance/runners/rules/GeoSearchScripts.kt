package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.MapViewport
import com.mountaya.geo.SearchBranchException
import com.mountaya.geo.SearchConfiguration
import com.mountaya.geo.SearchEngine
import com.mountaya.geo.SearchFetch
import com.mountaya.geo.SearchLimit
import com.mountaya.geo.SearchPause
import com.mountaya.geo.SearchSectionState
import com.mountaya.geo.SearchSnapshot
import com.mountaya.geo.SearchWording
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.searchSectionOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.test.assertNotNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * GeoSearchScripts plays the search engine's fixtures (`geo/search-section-order.json` and the four beside it) on the
 * case's virtual time, over a transport that holds every request until a step settles it, and writes what the list
 * shows as the fixtures write it.
 *
 * The engine runs in the case's background scope, the transport answering a request as the engine sends it, so the
 * requests are numbered in the order they leave; each step is settled by running what it queued, and a `wait` moves
 * the case's clock, so the pauses are waited out on no real time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal object GeoSearchScripts {
    /** Script is a search an engine fixture builds and the steps it plays. */
    class Script(
        val targets: List<DataSchema.SearchTarget>?,
        val query: String?,
        val pause: Duration?,
        val limit: SearchLimit?,
        val language: DataSchema.Language?,
        val camera: MapViewport?,
        val isConfined: Boolean,
        val isOpen: Boolean,
        val steps: List<FixtureValue.Object>,
        /** Whether the search is built with a failure handler, whose failures the answer then lists. */
        val handler: Boolean = false,
    )

    /**
     * Played is what a script answers: every request sent, in order, as the fixtures write one, the requests abandoned
     * while still open, and what the list showed once the search was built and after each step.
     */
    class Played(
        val requests: List<FixtureValue>,
        val cancelled: List<Int>,
        val snapshots: List<FixtureValue>,
        /**
         * Where the search was built with a failure handler, each section in error once the last step played, its id
         * and the code its failure holds, and each failure the handler was handed, as the code it holds; else null.
         */
        val failures: Pair<FixtureValue, FixtureValue>? = null,
    ) {
        /** The answer as the fixtures expect it. */
        val value: FixtureValue
            get() =
                FixtureValue.Object(
                    listOfNotNull(
                        "cancelled" to FixtureValue.Array(cancelled.map { FixtureValue.Number(it.toDouble()) }),
                        failures?.let { "failed" to it.first },
                        failures?.let { "handed" to it.second },
                        "requests" to FixtureValue.Array(requests),
                        "snapshots" to FixtureValue.Array(snapshots),
                    ),
                )
    }

    /**
     * Reads an engine fixture's input: `targets`, `query`, `pause` in milliseconds, `limit`, `language`, `camera`, null
     * and absent both a camera with nothing to read, `scoped`, `open` and `steps`.
     */
    fun script(input: FixtureValue.Object): Script =
        Script(
            targets = (input["targets"] as? FixtureValue.Array)?.let { written -> branches(written.items) },
            query = input.textOrNull("query"),
            pause = input.numberOrNull("pause")?.milliseconds,
            limit = input.numberOrNull("limit")?.let(::limit),
            language = input.textOrNull("language")?.let { DataSchema.Language(it) },
            camera = camera(input["camera"]),
            isConfined = flag(input, "scoped"),
            isOpen = flag(input, "open"),
            steps =
                (input["steps"] as? FixtureValue.Array)?.items.orEmpty().map {
                    it as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
                },
            handler = flag(input, "handler"),
        )

    /** Reads branches as the fixtures name them, the search input's members, `places`, into the targets they are in capitals. */
    fun branches(items: List<FixtureValue>): List<DataSchema.SearchTarget> =
        fixtureTexts(items, "`targets`").map { DataSchema.SearchTarget(it.uppercase()) }

    /** Reads a camera a case writes, `{center, zoom}`, or none for null or no camera. */
    fun camera(written: FixtureValue?): MapViewport? {
        val camera = written as? FixtureValue.Object ?: return null
        return MapViewport(fixturePosition(camera["center"], "center"), camera.number("zoom"))
    }

    /**
     * Builds the search [script] states in [scope]'s background and plays its steps, answering every request it sent,
     * the requests it abandoned while they were open, and what the list showed once the search was built and after each
     * step; or `{refused: true}` alone where the search is refused, a pause below zero, which is no `SearchPause`, so no
     * search is built over it, having asked and shown nothing.
     */
    fun played(
        scope: TestScope,
        script: Script,
    ): FixtureValue {
        if (script.pause != null && SearchPause.of(script.pause) == null) {
            return FixtureValue.Object(listOf("refused" to FixtureValue.Bool(true)))
        }
        return play(scope, script).value
    }

    /**
     * Builds the search [script] states in [scope]'s background and plays its steps, answering every request it sent,
     * the requests it abandoned while they were open, and what the list showed once the search was built and after each
     * step.
     */
    fun play(
        scope: TestScope,
        script: Script,
    ): Played {
        val transport = Transport()
        val handed = mutableListOf<Throwable>()
        var configuration =
            SearchConfiguration(
                fetch = transport.fetch,
                language = script.language,
                limit = script.limit,
                targets = script.targets ?: searchSectionOrder,
                onError = if (script.handler) { failure -> handed += failure } else null,
            )
        val engine =
            SearchEngine(
                scope.backgroundScope,
                configuration,
                query = script.query ?: "",
                camera = script.camera,
                isOpen = script.isOpen,
                isConfined = script.isConfined,
                pause =
                    script.pause?.let { assertNotNull(SearchPause.of(it), "a pause below zero builds no search") }
                        ?: SearchPause.standard,
            )
        try {
            scope.runCurrent()
            val snapshots = mutableListOf(shown(engine.state.value, transport.sent.size))

            // Plays one step; `together` plays the steps it holds in order, letting nothing rest between them.
            fun playStep(step: FixtureValue.Object) {
                when (val name = step.text("step")) {
                    "together" -> {
                        for (held in step.items("steps")) {
                            playStep(held as? FixtureValue.Object ?: throw AssertionError("a step is not an object"))
                        }
                    }

                    "type" -> {
                        engine.setQuery(step.text("query"))
                    }

                    "wait" -> {
                        scope.advanceTimeBy(step.number("ms").milliseconds)
                    }

                    "answer" -> {
                        transport.answer(whole(step, "request"), fixtureTexts(step.items("names"), "`names`"))
                    }

                    "fail" -> {
                        transport.fail(whole(step, "request"))
                    }

                    "report" -> {
                        transport.report(whole(step, "request"), step.textOrNull("code"))
                    }

                    "configure" -> {
                        configuration = configured(configuration, step)
                        engine.configure(configuration)
                    }

                    "scope" -> {
                        engine.setConfined(flag(step, "scoped"))
                    }

                    "open" -> {
                        engine.setOpen(flag(step, "open"))
                    }

                    "retry" -> {
                        engine.retry()
                    }

                    "move" -> {
                        engine.setCamera(camera(step["camera"]))
                    }

                    else -> {
                        throw AssertionError("`$name` is no step")
                    }
                }
            }
            for (step in script.steps) {
                playStep(step)
                scope.runCurrent()
                snapshots += shown(engine.state.value, transport.sent.size)
            }
            // The answer is read before the search is closed, which abandons whatever it still holds open.
            val failures =
                if (!script.handler) {
                    null
                } else {
                    val failed =
                        engine.state.value.sections.mapNotNull { section ->
                            val state = section.state as? SearchSectionState.Failed ?: return@mapNotNull null
                            FixtureValue.Object(
                                listOf(
                                    "code" to codeOf(state.failure),
                                    "id" to FixtureValue.Text(section.id.value.lowercase()),
                                ),
                            )
                        }
                    FixtureValue.Array(failed) to FixtureValue.Array(handed.map(::codeOf))
                }
            return Played(transport.sent.map { written(it.input) }, transport.cancelled.sorted(), snapshots, failures)
        } finally {
            engine.close()
            transport.release()
            scope.runCurrent()
        }
    }

    /** The code a branch's failure holds: the API's where it reported the branch failed, none for a failed request. */
    private fun codeOf(failure: Throwable): FixtureValue =
        (failure as? SearchBranchException)?.code?.let { FixtureValue.Text(it.value) } ?: FixtureValue.Null

    /** A cap a case states as the one a search takes, failing a case that states one no search takes. */
    private fun limit(value: Double): SearchLimit {
        val whole = value.toInt()
        if (whole.toDouble() != value) throw AssertionError("the cap $value is no whole number")
        return SearchLimit.of(whole) ?: throw AssertionError("the cap $value is no cap a search takes")
    }

    /** The flag [written] states at [key], false where absent. */
    private fun flag(
        written: FixtureValue.Object,
        key: String,
    ): Boolean = (written[key] as? FixtureValue.Bool)?.value ?: false

    /** The whole number [step] writes at [key]. */
    private fun whole(
        step: FixtureValue.Object,
        key: String,
    ): Int {
        val number = step.number(key)
        if (number != kotlin.math.floor(number)) throw AssertionError("`$key` is no whole number")
        return number.toInt()
    }

    /**
     * [configuration] with the `language`, `limit`, `targets` or `units` a step states, every member it does not state
     * as it was; `units` are a reader's unit preferences, English and metric where they leave one out.
     */
    private fun configured(
        configuration: SearchConfiguration,
        step: FixtureValue.Object,
    ): SearchConfiguration {
        val language =
            if (step["language"] !=
                null
            ) {
                step.textOrNull("language")?.let { DataSchema.Language(it) }
            } else {
                configuration.language
            }
        val limit = if (step["limit"] != null) step.numberOrNull("limit")?.let(::limit) else configuration.limit
        val targets = (step["targets"] as? FixtureValue.Array)?.let { branches(it.items) } ?: configuration.targets
        val units = step.objectOrNull("units")
        val wording =
            if (units == null) {
                configuration.wording
            } else {
                SearchWording(
                    UnitPreferences(
                        language = units.textOrNull("language") ?: "en",
                        length = RestSchema.UnitLength(units.textOrNull("length") ?: "metric"),
                    ),
                    configuration.wording.resolveLabel,
                )
            }
        return SearchConfiguration(configuration.fetch, language, limit, targets, wording, configuration.onError)
    }

    /** What the list shows as the fixtures write a snapshot: the requests sent so far, the flags, and each section by its branch, its state and its rows by title. */
    fun shown(
        snapshot: SearchSnapshot,
        asked: Int,
    ): FixtureValue =
        FixtureValue.Object(
            listOf(
                "asked" to FixtureValue.Number(asked.toDouble()),
                "fallback" to FixtureValue.Bool(snapshot.hasFallenBack),
                "query" to FixtureValue.Text(snapshot.query),
                "searching" to FixtureValue.Bool(snapshot.isSearching),
                "sections" to
                    FixtureValue.Array(
                        snapshot.sections.map { section ->
                            FixtureValue.Object(
                                listOf(
                                    "id" to FixtureValue.Text(section.id.value.lowercase()),
                                    "rows" to textArray(section.rows.map { it.title }),
                                    "state" to FixtureValue.Text(stateName(section.state)),
                                ),
                            )
                        },
                    ),
                "settled" to FixtureValue.Bool(snapshot.isSettled),
            ),
        )

    /** A section's state as the fixtures name it. */
    private fun stateName(state: SearchSectionState): String =
        when (state) {
            SearchSectionState.Loading -> "loading"
            SearchSectionState.Settled -> "settled"
            is SearchSectionState.Failed -> "error"
        }

    /** A request as the fixtures write one, `{kind, input}`: the members it sends, through the Data API's own serializer, its `targets` holding the one branch it enables. */
    private fun written(input: DataSchema.SearchInput): FixtureValue {
        val encoded = Json.encodeToJsonElement(DataSchema.SearchInput.serializer(), input) as? JsonObject
        val members = encoded ?: throw AssertionError("a search input that encodes as no object")
        val sent =
            JsonObject(
                members.mapValues { (key, value) ->
                    if (key != "targets") {
                        value
                    } else {
                        JsonObject((value as JsonObject).filterValues { (it as? JsonPrimitive)?.booleanOrNull == true })
                    }
                },
            )
        return FixtureValue.Object(listOf("input" to fixtureValueOf(sent), "kind" to FixtureValue.Text("search")))
    }

    /**
     * What the Data API answers a request for [branch] with: one result per name of [names], each titled by its name,
     * or, where [reported], the API reporting the branch failed with [code], `BAD_GATEWAY` where it is null.
     */
    fun answer(
        branch: DataSchema.SearchTarget,
        names: List<String>,
        reported: Boolean,
        code: String? = null,
    ): DataSchema.SearchResult {
        fun osm(index: Int): DataSchema.OSMRef =
            DataSchema.OSMRef {
                id = "$index"
                key = "place"
                type = DataSchema.OSMType.Node
                value = "city"
            }
        val point = listOf(6.87, 45.92)
        return DataSchema.SearchResult {
            errors =
                if (reported) {
                    listOf(
                        DataSchema.SearchTargetError {
                            this.code = code?.let { DataSchema.ErrorCode(it) } ?: DataSchema.ErrorCode.BadGateway
                            message = "${branch.value.lowercase()} failed"
                            target = branch
                        },
                    )
                } else {
                    emptyList()
                }
            if (!reported) answered(branch, names, point, ::osm)
        }
    }

    /** Fills [this] answer's member for [branch] with one result per name of [names], each at [point]. */
    private fun DataSchema.SearchResult.Builder.answered(
        branch: DataSchema.SearchTarget,
        names: List<String>,
        point: List<Double>,
        osm: (Int) -> DataSchema.OSMRef,
    ) {
        when (branch) {
            DataSchema.SearchTarget.Places -> {
                places =
                    DataSchema.GeocodeResult {
                        places =
                            names.mapIndexed { index, name ->
                                DataSchema.Place {
                                    category = DataSchema.PlaceCategory.PopulatedPlace
                                    label = ""
                                    this.name = name
                                    this.osm = osm(index)
                                    this.point = point
                                }
                            }
                    }
            }

            DataSchema.SearchTarget.Pois -> {
                pois =
                    DataSchema.PoisResult {
                        pois =
                            names.mapIndexed { index, name ->
                                DataSchema.POI {
                                    category = DataSchema.POICategory.Hut
                                    this.name = name
                                    this.osm = osm(index)
                                    this.point = point
                                }
                            }
                    }
            }

            DataSchema.SearchTarget.Ways -> {
                ways =
                    DataSchema.WaysResult {
                        ways =
                            names.mapIndexed { index, name ->
                                DataSchema.Way {
                                    activityCompatible = emptyList()
                                    highway = "path"
                                    this.name = name
                                    this.osm = osm(index)
                                    this.point = point
                                }
                            }
                    }
            }

            DataSchema.SearchTarget.Itineraries -> {
                itineraries =
                    DataSchema.ItinerariesResult {
                        count = names.size
                        itineraries =
                            names.mapIndexed { index, name ->
                                DataSchema.ItinerarySearchResult {
                                    id = "itinerary-$index"
                                    this.name = name
                                }
                            }
                    }
            }

            DataSchema.SearchTarget.Collections -> {
                collections =
                    DataSchema.CollectionsResult {
                        count = names.size
                        collections =
                            names.mapIndexed { index, name ->
                                DataSchema.CollectionSearchResult {
                                    id = "collection-$index"
                                    this.name = name
                                }
                            }
                    }
            }

            DataSchema.SearchTarget.Zones -> {
                zones =
                    DataSchema.ZonesResult {
                        count = names.size
                        zones =
                            names.mapIndexed { index, name ->
                                DataSchema.ZoneSearchResult {
                                    id = "zone-$index"
                                    this.name = name
                                }
                            }
                    }
            }

            else -> {}
        }
    }

    /**
     * Transport is a search's transport that records every request and holds it open until a step answers it, fails
     * it or reports its branch failed, and records each request the engine abandoned while it was still open.
     */
    class Transport {
        /** One request sent: its input, the answer it waits for, and whether a step settled it. */
        class Held(
            val input: DataSchema.SearchInput,
            var answer: CompletableDeferred<DataSchema.SearchResult>?,
            var isDone: Boolean = false,
        )

        /** What a request a step fails throws. */
        class Failed : Exception("failed")

        /** What a request still open as a case ends throws. */
        class Released : Exception("released")

        val sent = mutableListOf<Held>()
        val cancelled = mutableSetOf<Int>()

        /** The transport as a search takes it. */
        val fetch: SearchFetch = SearchFetch { input -> request(input) }

        private suspend fun request(input: DataSchema.SearchInput): DataSchema.SearchResult {
            val index = sent.size
            val answer = CompletableDeferred<DataSchema.SearchResult>()
            val held = Held(input, answer)
            sent += held
            try {
                return answer.await()
            } catch (withdrawn: CancellationException) {
                if (!held.isDone && !currentCoroutineContext().isActive) cancelled += index
                throw withdrawn
            }
        }

        /** Answers the request [index] with one result per name of [names]. */
        fun answer(
            index: Int,
            names: List<String>,
        ) {
            val branch = branch(index)
            settle(index).complete(answer(branch, names, reported = false))
        }

        /** Answers the request [index] with the API reporting its branch failed with [code], or `BAD_GATEWAY`. */
        fun report(
            index: Int,
            code: String? = null,
        ) {
            val branch = branch(index)
            settle(index).complete(answer(branch, emptyList(), reported = true, code = code))
        }

        /** Fails the request [index]. */
        fun fail(index: Int) {
            settle(index).completeExceptionally(Failed())
        }

        /** Fails every request still open, so no coroutine waits for ever once a case ends. */
        fun release() {
            for (held in sent) {
                held.answer?.completeExceptionally(Released())
                held.answer = null
            }
        }

        private fun branch(index: Int): DataSchema.SearchTarget {
            val held = sent.getOrNull(index) ?: throw AssertionError("request $index was never sent")
            val targets = held.input.targets
            val enabled =
                listOf(
                    targets.places to DataSchema.SearchTarget.Places,
                    targets.pois to DataSchema.SearchTarget.Pois,
                    targets.ways to DataSchema.SearchTarget.Ways,
                    targets.itineraries to DataSchema.SearchTarget.Itineraries,
                    targets.collections to DataSchema.SearchTarget.Collections,
                    targets.zones to DataSchema.SearchTarget.Zones,
                ).filter { it.first == true }
            return enabled.singleOrNull()?.second
                ?: throw AssertionError("request $index enables ${enabled.size} branches")
        }

        private fun settle(index: Int): CompletableDeferred<DataSchema.SearchResult> {
            val held = sent.getOrNull(index) ?: throw AssertionError("request $index was never sent")
            val answer = held.answer ?: throw AssertionError("request $index was settled already")
            held.answer = null
            held.isDone = true
            return answer
        }
    }
}
