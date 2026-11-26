package com.mountaya.conformance

import com.mountaya.geo.ElementsScope
import com.mountaya.geo.scopeOf
import kotlinx.coroutines.test.TestScope

/**
 * `geo.i18n.nested-scope`: the innermost of the case's `scopes`, each nested in the one before it and the outermost
 * in the standard scope, which holds the elements' words in every language the table holds: its language, its
 * locale, its two zoom words and the names of its two glyph sets (`GeoControlFixtures.glyphSet`).
 */
object GeoI18nNestedScopeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val nested =
            case.fields.items("scopes").fold(ElementsScope.standard) { outer, written ->
                scopeOf(
                    outer,
                    GeoControlFixtures.statement(
                        written as? FixtureValue.Object ?: throw AssertionError("a scope is not an object"),
                    ),
                )
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "glyphs" to
                        FixtureValue.Object(
                            listOf(
                                "controls" to FixtureValue.Text(GeoControlFixtures.nameOf(nested.controlGlyphs)),
                                "markers" to FixtureValue.Text(GeoControlFixtures.nameOf(nested.markerGlyphs)),
                            ),
                        ),
                    "language" to FixtureValue.Text(nested.language.value),
                    "locale" to FixtureValue.Text(nested.locale),
                    "words" to
                        FixtureValue.Object(
                            listOf(
                                "controls" to
                                    FixtureValue.Object(
                                        listOf(
                                            "zoomIn" to FixtureValue.Text(nested.word("controls.zoomIn")),
                                            "zoomOut" to FixtureValue.Text(nested.word("controls.zoomOut")),
                                        ),
                                    ),
                            ),
                        ),
                ),
            ),
        )
    }
}
