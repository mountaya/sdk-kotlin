package com.mountaya.conformance

import com.mountaya.geo.GlyphSet
import kotlinx.coroutines.test.TestScope

/**
 * `geo.icons.glyph-set`: whether the set the case declares is refused and, where it is not, the paint,
 * the box and the width it is drawn at. A paint the closed `GlyphSet.Paint` does not hold is refused by
 * the type, before any set is built. The defaults are held to those of a set that states neither box nor
 * width.
 */
object GeoIconsGlyphSetRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val bare =
            GlyphSet.of("bare", GlyphSet.Paint.Stroke, markup = emptyMap())
                ?: throw AssertionError("a set that states neither box nor width is refused")
        holdDeclaredConstants(case, rules = mapOf("strokeWidth" to bare.strokeWidth, "viewBox" to bare.viewBox))
        val set = fixtureGlyphSet(case.fields)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "paint" to (set?.let { FixtureValue.Text(it.paint.id) } ?: FixtureValue.Null),
                    "refused" to FixtureValue.Bool(set == null),
                    "strokeWidth" to (set?.let { FixtureValue.Number(it.strokeWidth) } ?: FixtureValue.Null),
                    "viewBox" to (set?.let { FixtureValue.Number(it.viewBox) } ?: FixtureValue.Null),
                ),
            ),
        )
    }
}
