package com.mountaya.conformance

import com.mountaya.geo.GlyphSet
import kotlinx.coroutines.test.TestScope

/**
 * `geo.icons.glyph-set-chain`: the chain of the case's `sets`, each declared as `geo.icons.glyph-set`
 * declares one: whether it is refused and, where it is not, the markup it answers for each name `asked`,
 * every name it holds, sorted by code unit as the fixture lists them, and the paint, the box and the width
 * it draws in.
 */
object GeoIconsGlyphSetChainRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val sets =
            input.items("sets").map {
                declaredGlyphSet(it as? FixtureValue.Object ?: throw AssertionError("a set is not an object"))
            }
        val asked = fixtureTexts(input.items("asked"), "`asked`")
        val chain =
            GlyphSet.chaining(sets)
                ?: return ConformanceOutcome.Value(
                    FixtureValue.Object(
                        listOf(
                            "glyphs" to FixtureValue.Array(emptyList()),
                            "names" to FixtureValue.Array(emptyList()),
                            "paint" to FixtureValue.Null,
                            "refused" to FixtureValue.Bool(true),
                            "strokeWidth" to FixtureValue.Null,
                            "viewBox" to FixtureValue.Null,
                        ),
                    ),
                )
        val glyphs =
            asked.map { glyph ->
                FixtureValue.Object(
                    listOf(
                        "glyph" to FixtureValue.Text(glyph),
                        "markup" to (chain.markup[glyph]?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    ),
                )
            }
        // A Kotlin string compares by UTF-16 code unit, the order the fixture lists the names in.
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "glyphs" to FixtureValue.Array(glyphs),
                    "names" to textArray(chain.markup.keys.sorted()),
                    "paint" to FixtureValue.Text(chain.paint.id),
                    "refused" to FixtureValue.Bool(false),
                    "strokeWidth" to FixtureValue.Number(chain.strokeWidth),
                    "viewBox" to FixtureValue.Number(chain.viewBox),
                ),
            ),
        )
    }
}
