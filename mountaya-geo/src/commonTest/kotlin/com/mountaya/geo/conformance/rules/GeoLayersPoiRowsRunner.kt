package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.poi-rows`: the rows a points-of-interest picker offers over the case's `drawn` keys, its `hidden` ones
 * hidden, curated by its `keys` or its `groups`, each group labelled by its id, the master row, and whether the picker
 * notes that the overlay draws nothing at the case's `zoom` below its `minZoom`.
 */
object GeoLayersPoiRowsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val hidden = texts(input.items("hidden"))
        val groups =
            (input["groups"] as? FixtureValue.Array)?.items?.map { item ->
                val group = item as? FixtureValue.Object ?: throw AssertionError("a group is not an object")
                val id = group.text("id")
                PoiPanelGroup(id, id, texts(group.items("keys")))
            }
        val keys = (input["keys"] as? FixtureValue.Array)?.let { texts(it.items) }
        val offer = poiPanelRows(texts(input.items("drawn")), { it !in hidden }, keys = keys, groups = groups)
        val isNoted = !(input.number("zoom") >= poiPanelNoteZoom(input.numberOrNull("minZoom")))
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "master" to FixtureValue.Bool(offer.isAnyVisible),
                    "note" to FixtureValue.Bool(isNoted),
                    "rows" to
                        FixtureValue.Array(
                            offer.rows.map { row ->
                                FixtureValue.Object(
                                    listOf(
                                        "id" to FixtureValue.Text(row.id),
                                        "keys" to FixtureValue.Array(row.keys.map { FixtureValue.Text(it) }),
                                        "on" to FixtureValue.Bool(row.isVisible),
                                    ),
                                )
                            },
                        ),
                ),
            ),
        )
    }

    /** The texts [items] holds. */
    private fun texts(items: List<FixtureValue>): List<String> =
        items.map {
            (it as? FixtureValue.Text)?.value ?: throw AssertionError(
                "a list holds a value that is no string",
            )
        }
}
