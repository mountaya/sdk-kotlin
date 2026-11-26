package com.mountaya.conformance

import com.mountaya.geo.GlyphShape
import com.mountaya.geo.glyphAttributes
import com.mountaya.geo.glyphShapeTags
import com.mountaya.geo.parseGlyph
import kotlinx.coroutines.test.TestScope

/**
 * `geo.icons.glyph-parse`: the shapes the case's `markup` reads into, each its `tag` and its `attributes`.
 * The lists the fixture declares are the parse's: the attributes and the shapes Geo publishes, sorted as
 * the fixture lists them, each shape read as a shape where it is written alone.
 */
object GeoIconsGlyphParseRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "attributes" to textArray(glyphAttributes.sorted()),
                    "tags" to textArray(glyphShapeTags.sorted()),
                ),
        )
        for (tag in glyphShapeTags) {
            if (parseGlyph("<$tag/>") != listOf(GlyphShape(tag, emptyMap()))) {
                throw AssertionError("`<$tag/>` reads as no shape of its own")
            }
        }
        return ConformanceOutcome.Value(
            FixtureValue.Array(parseGlyph(case.fields.text("markup")).map(::writtenGlyphShape)),
        )
    }
}
