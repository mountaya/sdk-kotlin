package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.search.list-state`: what a list shows of the case's `snapshot` under its `filter`, each section of its `rows`,
 * titled as named, standing as its `state` says, a failed one holding the failure the API reports. A branch is named
 * as the search input's member, `places`, and read as the target it is written in capitals. The entry is one a
 * consumer only observes, as a list, so it runs from Geo's own tests.
 */
object GeoSearchListStateRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val filterId = input.text("filter")
        val filter =
            SearchFilter.entries.firstOrNull { it.id == filterId } ?: throw AssertionError("`$filterId` is no filter")
        val written = input.objectOrNull("snapshot") ?: throw AssertionError("the case has no `snapshot`")
        val sections =
            written.items("sections").map { item ->
                val section = item as? FixtureValue.Object ?: throw AssertionError("a section is not an object")
                val branch = DataSchema.SearchTarget(section.text("id").uppercase())
                val state =
                    when (val name = section.text("state")) {
                        "loading" -> SearchSectionState.Loading
                        "settled" -> SearchSectionState.Settled
                        "error" -> SearchSectionState.Failed(SearchBranchException(branch))
                        else -> throw AssertionError("`$name` is no state of a section")
                    }
                val rows =
                    section.items("rows").mapIndexed { index, title ->
                        val text = (title as? FixtureValue.Text)?.value ?: throw AssertionError("a row is no title")
                        SearchRow(
                            "${branch.value}:$index",
                            text,
                            emptyList(),
                            null,
                            emptyList(),
                            SearchRowPayload.Itinerary("${branch.value}-$index", null, null),
                        )
                    }
                SearchSection(branch, state, rows)
            }
        val snapshot =
            SearchSnapshot(
                "chamonix",
                isSearching = written.bool("searching"),
                isSettled = written.bool("settled"),
                hasFallenBack = written.bool("fallback"),
                sections = sections,
            )
        val shown = searchListState(snapshot, filter)
        val status =
            when (val announced = shown.status) {
                null -> {
                    FixtureValue.Null
                }

                is SearchListStatus.ResultCount -> {
                    FixtureValue.Object(
                        listOf(
                            "count" to FixtureValue.Number(announced.count.toDouble()),
                            "key" to FixtureValue.Text(announced.key),
                        ),
                    )
                }

                else -> {
                    FixtureValue.Object(listOf("key" to FixtureValue.Text(announced.key)))
                }
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "failed" to FixtureValue.Array(shown.failed.map { FixtureValue.Text(it.id.value.lowercase()) }),
                    "fallbackNote" to FixtureValue.Bool(shown.hasFallbackNote),
                    "nothingFound" to FixtureValue.Bool(shown.isNothingFound),
                    "rows" to FixtureValue.Array(shown.rows.map { FixtureValue.Text(it.title) }),
                    "status" to status,
                ),
            ),
        )
    }
}
