package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlin.test.assertNotNull
import kotlin.time.Duration

/**
 * `geo.search.focus`: what the one request of a round of one branch sends, `focus` and `bbox`, each null where it
 * sends none, for a search reading the case's `camera`, `{center, zoom}` or null, and confined where its `scoped` says
 * so, through the engine's own round in the case's background scope. The point a round is ranked around is held to
 * the internal rule besides. The entry is one a consumer only observes, as a request's members, so it runs from
 * Geo's own tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object GeoSearchFocusRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val camera =
            input
                .objectOrNull(
                    "camera",
                )?.let { MapViewport(fixturePosition(it["center"], "center"), it.number("zoom")) }
        val sent = mutableListOf<DataSchema.SearchInput>()
        val held = CompletableDeferred<DataSchema.SearchResult>()
        val engine =
            SearchEngine(
                scope.backgroundScope,
                SearchConfiguration(
                    fetch =
                        SearchFetch { request ->
                            sent += request
                            held.await()
                        },
                    targets = listOf(DataSchema.SearchTarget.Places),
                ),
                query = "chamonix",
                camera = camera,
                isConfined = (input["scoped"] as? FixtureValue.Bool)?.value ?: false,
                pause = assertNotNull(SearchPause.of(Duration.ZERO), "a pause of nothing is a pause"),
            )
        try {
            scope.runCurrent()
        } finally {
            engine.close()
            scope.runCurrent()
        }
        val request = sent.singleOrNull() ?: throw AssertionError("a round of one branch sent ${sent.size}")
        val ruled = camera?.let { searchFocus(it.center) }
        if (request.focus != ruled) throw AssertionError("the round sent ${request.focus}, and the rule answers $ruled")
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "bbox" to numbersOrNull(request.bbox),
                    "focus" to numbersOrNull(request.focus),
                ),
            ),
        )
    }

    /** [numbers] as a fixture's array of numbers, or its null. */
    private fun numbersOrNull(numbers: List<Double>?): FixtureValue =
        numbers?.let { list -> FixtureValue.Array(list.map { FixtureValue.Number(it) }) } ?: FixtureValue.Null
}
