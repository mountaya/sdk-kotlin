package com.mountaya.conformance

import com.mountaya.geo.SEARCH_LIMIT_MAX
import com.mountaya.geo.SearchQueryChange
import com.mountaya.geo.isSearchLimit
import com.mountaya.geo.searchQueryChangeOf
import kotlinx.coroutines.test.TestScope

/**
 * `geo.search.query`: the rounds a search asks as a reader types the case's `queries`, each settling
 * before the next, under its `limit`. Each typed query is read through `searchQueryChangeOf`, after
 * the query the last one settled: a round is asked, the search cleared, or nothing. The cap is held
 * to `isSearchLimit`, and a cap that is no whole number is refused as the `Int` a round's cap is
 * cannot hold one. The ceiling is held to the one Geo publishes.
 */
object GeoSearchQueryRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, rules = mapOf("searchLimitMax" to SEARCH_LIMIT_MAX.toDouble()))
        val input = case.fields
        val cap = input.numberOrNull("limit")
        val limit = cap?.takeIf { it == it.toInt().toDouble() }?.toInt()
        if (cap != null && (limit == null || !isSearchLimit(limit))) return answer(refused = true, rounds = emptyList())
        val rounds = mutableListOf<String>()
        var settled = ""
        for (typed in fixtureTexts(input.items("queries"), "`queries`")) {
            settled =
                when (val change = searchQueryChangeOf(settled, typed)) {
                    is SearchQueryChange.Ask -> change.query.also { rounds += it }
                    SearchQueryChange.Clear -> ""
                    SearchQueryChange.Unchanged -> settled
                }
        }
        return answer(
            refused = false,
            rounds =
                rounds.map { query ->
                    FixtureValue.Object(
                        listOf(
                            "limit" to (limit?.let { FixtureValue.Number(it.toDouble()) } ?: FixtureValue.Null),
                            "query" to FixtureValue.Text(query),
                        ),
                    )
                },
        )
    }

    private fun answer(
        refused: Boolean,
        rounds: List<FixtureValue>,
    ) = ConformanceOutcome.Value(
        FixtureValue.Object(
            listOf(
                "refused" to FixtureValue.Bool(refused),
                "rounds" to FixtureValue.Array(rounds),
            ),
        ),
    )
}
