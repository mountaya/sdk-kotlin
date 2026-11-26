package com.mountaya.elements

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.GlyphSet
import kotlinx.coroutines.test.TestScope

/**
 * `map.icons.missing-image`: the marker sprite the case's image `id` names, its glyph and its colour, or
 * null for an image left missing, read against a set holding the glyphs the fixture declares and no other.
 * The prefixes are held to the ones the rule reads; the held glyphs are the input every case reads. The rule
 * is the map's own, so it is reached as an internal.
 */
object MapIconsMissingImageRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val declared = case.root("constants")
        if (declared.keys.toSet() != setOf("heldGlyphs", "prefixes")) {
            throw AssertionError("the fixture declares ${declared.keys}, and the rule reads heldGlyphs and prefixes")
        }
        val prefixes = texts(declared.items("prefixes"))
        if (prefixes != markerImagePrefixes) {
            throw AssertionError("the fixture declares the prefixes $prefixes, and the rule reads $markerImagePrefixes")
        }
        val set =
            GlyphSet.of(
                "held",
                GlyphSet.Paint.Stroke,
                markup = texts(declared.items("heldGlyphs")).associateWith { "<path d=\"M0 0h1\"/>" },
            ) ?: throw AssertionError("a set holding the fixture's glyphs is refused")
        val image = readMarkerImageId(case.fields.text("id"), set) ?: return ConformanceOutcome.Value(FixtureValue.Null)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "color" to FixtureValue.Text(image.color),
                    "glyph" to FixtureValue.Text(image.glyph),
                ),
            ),
        )
    }

    private fun texts(items: List<FixtureValue>): List<String> =
        items.map {
            (it as? FixtureValue.Text)?.value ?: throw AssertionError(
                "a list of the constants holds no string",
            )
        }
}
