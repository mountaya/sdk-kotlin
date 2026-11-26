package com.mountaya.elements

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.GlyphSet
import kotlinx.coroutines.test.TestScope

/**
 * `map.icons.marker-sprite`: what the marker sprite of the case's `glyph` in its `color` draws, from a set
 * of the case's paint, width and box holding its `held` glyphs and no other, each a placeholder no rule
 * reads. The sizes and colours the fixture declares are held to a sprite the rule describes, read back
 * through it: its side, its border, its disc's radius, the square its glyph is fitted to and the colours of
 * both, each exactly, as the fixture states no tolerance, and a constant it adds or drops fails the entry.
 * The rule is the map's own, which a consumer observes as the images a map draws, so it is reached as an
 * internal.
 */
object MapIconsMarkerSpriteRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstants(case)
        val input = case.fields
        val written = input.objectOrNull("set") ?: throw AssertionError("the input states no `set`")
        val paint =
            GlyphSet.Paint.entries.firstOrNull { it.id == written.text("paint") }
                ?: throw AssertionError("`${written.text("paint")}` is no paint")
        val held =
            input.items("held").map {
                (it as? FixtureValue.Text)?.value
                    ?: throw AssertionError("`held` holds no string")
            }
        val set =
            GlyphSet.of(
                "held",
                paint,
                written.numberOrNull("strokeWidth") ?: 2.0,
                written.numberOrNull("viewBox") ?: 24.0,
                held.associateWith { PLACEHOLDER },
            ) ?: throw AssertionError("the case's set is refused")
        val sprite =
            describeMarkerSprite(input.text("glyph"), input.text("color"), set)
                ?: return ConformanceOutcome.Value(FixtureValue.Null)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "disc" to
                        FixtureValue.Object(
                            listOf(
                                "border" to
                                    FixtureValue.Object(
                                        listOf(
                                            "color" to FixtureValue.Text(sprite.disc.borderColor),
                                            "width" to FixtureValue.Number(sprite.disc.borderWidth),
                                        ),
                                    ),
                                "center" to FixtureValue.Array(sprite.disc.center.map { FixtureValue.Number(it) }),
                                "fill" to FixtureValue.Text(sprite.disc.fill),
                                "radius" to FixtureValue.Number(sprite.disc.radius),
                            ),
                        ),
                    "glyph" to
                        FixtureValue.Object(
                            listOf(
                                "color" to FixtureValue.Text(sprite.glyph.color),
                                "offset" to FixtureValue.Number(sprite.glyph.offset),
                                "scale" to FixtureValue.Number(sprite.glyph.scale),
                            ),
                        ),
                    "size" to FixtureValue.Number(sprite.size),
                ),
            ),
        )
    }

    /** The markup each held glyph stands for: a placeholder no rule reads. */
    private const val PLACEHOLDER = "<path d=\"M0 0h1\"/>"

    /** Holds the fixture's constants to a sprite the rule describes, read back through it. */
    private fun holdConstants(case: ConformanceCase) {
        val reference =
            GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = mapOf("x" to PLACEHOLDER))
                ?: throw AssertionError("a set holding one glyph is refused")
        val sprite =
            describeMarkerSprite("x", "#000000", reference)
                ?: throw AssertionError("a set holding the glyph draws no sprite of it")
        val stated =
            mapOf(
                "borderColor" to FixtureValue.Text(sprite.disc.borderColor),
                "borderPx" to FixtureValue.Number(sprite.disc.borderWidth),
                "discRadiusPx" to FixtureValue.Number(sprite.disc.radius),
                "glyphBoxPx" to FixtureValue.Number(sprite.glyph.scale * reference.viewBox),
                "glyphColor" to FixtureValue.Text(sprite.glyph.color),
                "spritePx" to FixtureValue.Number(sprite.size),
            )
        val declared = case.root("constants")
        if (declared.keys.toSet() != stated.keys) {
            throw AssertionError("the fixture declares ${declared.keys}, and the rule states ${stated.keys.sorted()}")
        }
        for ((name, value) in stated) {
            if (!same(
                    declared[name],
                    value,
                )
            ) {
                throw AssertionError("the fixture declares $name as ${declared[name]}, and the rule states $value")
            }
        }
    }

    /** Whether two constants are the same text, or the same number exactly. */
    private fun same(
        declared: FixtureValue?,
        stated: FixtureValue,
    ): Boolean =
        when (stated) {
            is FixtureValue.Text -> (declared as? FixtureValue.Text)?.value == stated.value
            is FixtureValue.Number -> (declared as? FixtureValue.Number)?.value?.compareTo(stated.value) == 0
            else -> false
        }
}
