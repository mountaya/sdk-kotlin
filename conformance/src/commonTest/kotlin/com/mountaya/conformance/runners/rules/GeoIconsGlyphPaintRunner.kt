package com.mountaya.conformance

import com.mountaya.geo.GlyphShape
import com.mountaya.geo.glyphPaint
import kotlinx.coroutines.test.TestScope

/**
 * `geo.icons.glyph-paint`: how each of the case's `shapes` is drawn in its `set`, built from the set's
 * paint and width and named for the case. The set's own paint, a shape that states nothing, is held to
 * the cap, the join, the miter limit and the fill rule the fixture declares.
 */
object GeoIconsGlyphPaintRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val written = input.objectOrNull("set") ?: throw AssertionError("the input states no `set`")
        val set = declaredGlyphSet(FixtureValue.Object(listOf("name" to FixtureValue.Text("case")) + written.entries))
        val own = glyphPaint(GlyphShape("path", emptyMap()), set)
        holdDeclaredConstants(
            case,
            rules = mapOf("miterLimit" to own.miterLimit),
            values =
                mapOf(
                    "fillRule" to FixtureValue.Text(own.fillRule.id),
                    "lineCap" to FixtureValue.Text(own.lineCap.id),
                    "lineJoin" to FixtureValue.Text(own.lineJoin.id),
                ),
        )
        return ConformanceOutcome.Value(
            FixtureValue.Array(input.items("shapes").map { writtenShapePaint(glyphPaint(fixtureGlyphShape(it), set)) }),
        )
    }
}
