package com.mountaya.conformance

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.OverlayKind
import com.mountaya.geo.findOverlay
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.overlay-lookup`: the overlay of the case's `layers` manifest its `slug` names, among the
 * categories of its `kind` where it names one. Each overlay is built as the Tile API's with an id of
 * its own, so the one found is answered as the entry the fixture wrote for it, and an overlay two
 * categories hold is told apart by which it is.
 */
object GeoLayersOverlayLookupRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val written = mutableMapOf<String, FixtureValue>()
        val manifest = input.objectOrNull("layers") ?: throw AssertionError("the case has no `layers`")
        val categories =
            manifest.items("overlays").mapIndexed { at, item ->
                val category = item as? FixtureValue.Object ?: throw AssertionError("a category is not an object")
                val children =
                    category.items("children").mapIndexed { place, child ->
                        val entry = child as? FixtureValue.Object ?: throw AssertionError("an overlay is not an object")
                        val id = "$at-$place"
                        written[id] = entry
                        fixtureOverlay(entry.text("slug"), id)
                    }
                fixtureCategory(category.text("slug"), children)
            }
        val kind =
            when (val named = input.textOrNull("kind")) {
                null -> null
                "classic" -> OverlayKind.Classic
                "enrichment" -> OverlayKind.Enrichment
                else -> throw AssertionError("`$named` is no kind of overlay")
            }
        val found: TileSchema.Overlay? = findOverlay(fixtureLayers(categories), input.text("slug"), kind)
        return ConformanceOutcome.Value(found?.let { written.getValue(it.id) } ?: FixtureValue.Null)
    }
}
