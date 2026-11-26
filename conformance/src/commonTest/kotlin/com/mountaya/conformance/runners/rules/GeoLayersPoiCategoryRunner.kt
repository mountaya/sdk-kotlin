package com.mountaya.conformance

import com.mountaya.geo.poiCategoryOf
import com.mountaya.geo.poiTileCategories
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.poi-category`: the Data API category the case's tile `category` reads as, or null. The table
 * is held to the one Geo publishes, entry by entry.
 */
object GeoLayersPoiCategoryRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val categories =
            FixtureValue.Object(
                poiTileCategories.entries.sortedBy { it.key }.map {
                    it.key to
                        FixtureValue.Text(it.value.value)
                },
            )
        holdDeclaredConstants(case, emptyMap(), values = mapOf("categories" to categories))
        val category = poiCategoryOf(case.fields.text("category"))
        return ConformanceOutcome.Value(category?.let { FixtureValue.Text(it.value) } ?: FixtureValue.Null)
    }
}
