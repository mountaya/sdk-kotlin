package com.mountaya.conformance

import com.mountaya.geo.SearchFilter
import com.mountaya.geo.searchSectionOrder
import kotlinx.coroutines.test.TestScope

/**
 * `geo.search.section-order`: plays the case's script (`GeoSearchScripts`). The order the sections render in is held
 * to `searchSectionOrder`, which is also the branches a search asks where none are listed, and the filters a list
 * offers to `SearchFilter`'s entries, in order.
 */
object GeoSearchSectionOrderRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "sectionOrder" to textArray(searchSectionOrder.map { it.value.lowercase() }),
                    "filters" to textArray(SearchFilter.entries.map { it.id }),
                ),
        )
        return ConformanceOutcome.Value(GeoSearchScripts.play(scope, GeoSearchScripts.script(case.fields)).value)
    }
}
