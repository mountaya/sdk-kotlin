package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.icons.glyph-fallback`: what stands where a glyph the case's set, holding its `held` glyphs and no
 * other, cannot draw would be, by the case's `surface`: a place's glyph through `placeGlyph`, a badge's
 * glyph where the set draws it and otherwise the text `badgeFallback` writes, and a control's glyph where
 * the set draws it and nothing otherwise, which no rule of Geo's states: a control draws a glyph it holds,
 * as the Compose `Glyph` does. The fallback pin is held to the one Geo publishes, exactly, as the fixture
 * states no tolerance, and a constant it adds or drops fails the entry. The rules are the views' own,
 * which a consumer only observes, so they are reached as internals.
 */
object GeoIconsGlyphFallbackRunner : ConformanceRunner {
    private val badges =
        mapOf(
            "badge-cell" to BadgeSurface.Cell,
            "badge-stack" to BadgeSurface.Stack,
            "badge-labelled" to BadgeSurface.Labelled,
        )

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstants(case)
        val input = case.fields
        val held =
            input.items("held").map {
                (it as? FixtureValue.Text)?.value ?: throw AssertionError("`held` holds a value that is no string")
            }
        val set =
            GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = held.associateWith { "<path d=\"M0 0h1\"/>" })
                ?: throw AssertionError("a set holding the case's glyphs is refused")
        val answer =
            when (val surface = input.text("surface")) {
                "place" -> {
                    glyph(placeGlyph(input.textOrNull("glyph"), set))
                }

                "control" -> {
                    glyph(input.text("glyph").takeIf { it in set.markup })
                }

                else -> {
                    val badge = badges[surface] ?: throw AssertionError("`$surface` is no surface a glyph stands on")
                    val name = amenityGlyph(RestSchema.WaypointAmenity(input.text("amenity")))
                    if (name in set.markup) {
                        glyph(name)
                    } else {
                        badgeFallback(input.text("name"), badge)?.let {
                            FixtureValue.Object(listOf("text" to FixtureValue.Text(it)))
                        } ?: FixtureValue.Null
                    }
                }
            }
        return ConformanceOutcome.Value(answer)
    }

    /** The glyph drawn, as the fixture writes one, or null for nothing. */
    private fun glyph(name: String?): FixtureValue =
        name?.let { FixtureValue.Object(listOf("glyph" to FixtureValue.Text(it))) } ?: FixtureValue.Null

    /** Holds the fixture's constants to the rule's, the one it declares and no other. */
    private fun holdConstants(case: ConformanceCase) {
        val declared = case.root("constants")
        if (declared.keys != listOf("fallbackGlyph")) {
            throw AssertionError("the fixture declares ${declared.keys}, and the rule states fallbackGlyph alone")
        }
        val fallback = declared.text("fallbackGlyph")
        if (fallback != WAYPOINT_FALLBACK_GLYPH) {
            throw AssertionError(
                "the fixture declares fallbackGlyph as $fallback, and the rule states $WAYPOINT_FALLBACK_GLYPH",
            )
        }
    }
}
