package com.mountaya.conformance

import com.mountaya.geo.GlyphSet
import kotlinx.coroutines.test.TestScope

/**
 * `geo.icons.bundled-sets`: the markup the case's `set`, `marker` or `control`, holds for its `glyph`, or
 * null. Each set's paint, width and box are held to the ones the fixture declares.
 */
object GeoIconsBundledSetsRunner : ConformanceRunner {
    private val sets = mapOf("control" to GlyphSet.control, "marker" to GlyphSet.marker)

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, rules = emptyMap(), values = sets.mapValues { (_, set) -> drawn(set) })
        val input = case.fields
        val named = input.text("set")
        val set = sets[named] ?: throw AssertionError("`$named` names no bundled set")
        return ConformanceOutcome.Value(
            set.markup[input.text("glyph")]?.let { FixtureValue.Text(it) } ?: FixtureValue.Null,
        )
    }

    /** How [set] is drawn, as the fixture declares each set. */
    private fun drawn(set: GlyphSet): FixtureValue =
        FixtureValue.Object(
            listOf(
                "paint" to FixtureValue.Text(set.paint.id),
                "strokeWidth" to FixtureValue.Number(set.strokeWidth),
                "viewBox" to FixtureValue.Number(set.viewBox),
            ),
        )
}
