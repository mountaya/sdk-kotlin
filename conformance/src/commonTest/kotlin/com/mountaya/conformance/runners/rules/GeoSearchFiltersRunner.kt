package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.SearchFilter
import com.mountaya.geo.sectionsForFilter
import kotlinx.coroutines.test.TestScope

/**
 * `geo.search.filters`: the sections the case's `filter` shows of its `sections`. The fixture names a
 * section as the Data API's search answer names its field and `SearchTargetsInput` its flag, `places`,
 * and a rule holding the search target reads the name in upper case, `PLACES`, as the fixture's
 * description says: each name is read as the target it upper-cases to and written back the fixture's way.
 */
object GeoSearchFiltersRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val filter =
            input.text("filter").let { id ->
                SearchFilter.entries.firstOrNull { it.id == id }
                    ?: throw AssertionError("`$id` is no filter")
            }
        val sections =
            fixtureTexts(
                input.items("sections"),
                "`sections`",
            ).map { DataSchema.SearchTarget(it.uppercase()) }
        return ConformanceOutcome.Value(textArray(sectionsForFilter(sections, filter).map { it.value.lowercase() }))
    }
}
