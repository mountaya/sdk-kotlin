package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ScopeOfTest {
    /** The `controls` words [words] states, by key. */
    private fun controls(vararg words: Pair<String, String>): JsonObject =
        JsonObject(mapOf("controls" to JsonObject(words.associate { (key, word) -> key to JsonPrimitive(word) })))

    /** French in the locale `fr-CH`, its zoom-in word written `Plus`. */
    private val french =
        scopeOf(
            ElementsScope.standard,
            ElementsScopeStatement(
                language = RestSchema.Language.Fr,
                locale = "fr-CH",
                words = WordOverrides.BySection(controls("zoomIn" to "Plus")),
            ),
        )

    /** A scope's two zoom words. */
    private fun zoom(scope: ElementsScope): List<String> =
        listOf(scope.word("controls.zoomIn"), scope.word("controls.zoomOut"))

    /** A set of the host's own, named [name]. */
    private fun licensed(name: String): GlyphSet =
        assertNotNull(
            GlyphSet.of(name, GlyphSet.Paint.Stroke, markup = mapOf("zoom-in" to """<path d="M0 0"/>""")),
            "the host's set is a set",
        )

    @Test
    fun aScopeStatingNothingTakesEverySettingFromTheScopeAroundIt() {
        val scope = scopeOf(french, ElementsScopeStatement())

        assertEquals(french, scope)
        assertEquals(listOf("Plus", "Zoom arrière"), zoom(scope))
    }

    @Test
    fun aScopeStatingAnotherLanguageResolvesItsWordsAfreshItsLocaleFollowing() {
        val scope = scopeOf(french, ElementsScopeStatement(language = RestSchema.Language.En))

        assertEquals(RestSchema.Language.En, scope.language)
        assertEquals("en", scope.locale)
        assertEquals(listOf("Zoom in", "Zoom out"), zoom(scope))
    }

    @Test
    fun aScopeRestatingTheLanguageAroundItKeepsItsWordsAndItsLocale() {
        val scope = scopeOf(french, ElementsScopeStatement(language = RestSchema.Language("fr")))

        assertEquals("fr-CH", scope.locale)
        assertEquals(listOf("Plus", "Zoom arrière"), zoom(scope))
    }

    @Test
    fun aScopeStatingWordsResolvesThemFromItsLanguagesBaseItsLocaleKept() {
        val scope =
            scopeOf(french, ElementsScopeStatement(words = WordOverrides.BySection(controls("zoomOut" to "Moins"))))

        assertEquals(RestSchema.Language.Fr, scope.language)
        assertEquals("fr-CH", scope.locale)
        assertEquals(listOf("Zoom avant", "Moins"), zoom(scope))
    }

    @Test
    fun aStatedLocaleStandsBesideAStatedLanguageAndAloneKeepsLanguageAndWords() {
        val english =
            scopeOf(french, ElementsScopeStatement(language = RestSchema.Language.En, locale = "en-GB"))
        val belgian = scopeOf(french, ElementsScopeStatement(locale = "fr-BE"))

        assertEquals("en-GB", english.locale)
        assertEquals(listOf("Zoom in", "Zoom out"), zoom(english))
        assertEquals(RestSchema.Language.Fr, belgian.language)
        assertEquals("fr-BE", belgian.locale)
        assertEquals(listOf("Plus", "Zoom arrière"), zoom(belgian))
    }

    @Test
    fun wordsWrittenForALanguageStayWithTheScopeThatWroteThem() {
        val outer =
            scopeOf(
                ElementsScope.standard,
                ElementsScopeStatement(
                    words =
                        WordOverrides.ByLanguage(
                            mapOf(RestSchema.Language.Fr to controls("zoomIn" to "Plus")),
                        ),
                ),
            )
        val inner = scopeOf(outer, ElementsScopeStatement(language = RestSchema.Language.Fr))
        val german =
            scopeOf(
                ElementsScope.standard,
                ElementsScopeStatement(
                    language = RestSchema.Language.De,
                    words =
                        WordOverrides.ByLanguage(
                            mapOf(RestSchema.Language.De to controls("zoomIn" to "Vergrößern")),
                        ),
                ),
            )

        assertEquals(listOf("Zoom in", "Zoom out"), zoom(outer))
        assertEquals("fr", inner.locale)
        assertEquals(listOf("Zoom avant", "Zoom arrière"), zoom(inner))
        assertEquals("de", german.locale)
        assertEquals(listOf("Vergrößern", "Zoom out"), zoom(german))
    }

    @Test
    fun aControlSetStatedNeverStandsInForTheMarkerSetAndEachIsInheritedAlone() {
        val controls = licensed("licensed-controls")
        val markers = licensed("licensed-markers")
        val stated = scopeOf(ElementsScope.standard, ElementsScopeStatement(controlGlyphs = controls))
        val both =
            scopeOf(ElementsScope.standard, ElementsScopeStatement(controlGlyphs = controls, markerGlyphs = markers))
        val inner = scopeOf(both, ElementsScopeStatement(controlGlyphs = GlyphSet.control))

        assertEquals(controls, stated.controlGlyphs)
        assertEquals(GlyphSet.marker, stated.markerGlyphs)
        assertEquals(GlyphSet.control, inner.controlGlyphs)
        assertEquals(markers, inner.markerGlyphs)
    }

    @Test
    fun threeScopesDeepEachTakesWhatItDoesNotStateFromTheOneAroundIt() {
        val first =
            scopeOf(
                ElementsScope.standard,
                ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH"),
            )
        val second =
            scopeOf(first, ElementsScopeStatement(words = WordOverrides.BySection(controls("zoomIn" to "Plus"))))
        val third = scopeOf(second, ElementsScopeStatement())

        assertEquals(RestSchema.Language.Fr, third.language)
        assertEquals("fr-CH", third.locale)
        assertEquals(listOf("Plus", "Zoom arrière"), zoom(third))
    }

    /**
     * A composable's own strings around a nested scope stay where its words are the ones around it, and stay with
     * that scope where they resolve afresh, a language or words of its own, as the words around it do.
     */
    @OptIn(InternalMountayaApi::class)
    @Test
    fun aComposablesOwnStringsAreKeptWhereTheWordsAreInheritedAndLeftWhereTheyResolveAfresh() {
        val own = ElementsScope.standard.withStrings(mapOf("controls.zoomIn" to "Plus"))

        assertEquals("Plus", scopeOf(own, ElementsScopeStatement()).word("controls.zoomIn"))
        assertEquals("Plus", scopeOf(own, ElementsScopeStatement(locale = "en-GB")).word("controls.zoomIn"))
        assertEquals(
            "Plus",
            scopeOf(own, ElementsScopeStatement(language = RestSchema.Language.En)).word("controls.zoomIn"),
        )
        assertEquals(
            "Zoom avant",
            scopeOf(own, ElementsScopeStatement(language = RestSchema.Language.Fr)).word("controls.zoomIn"),
        )
        assertEquals(
            "In",
            scopeOf(own, ElementsScopeStatement(words = WordOverrides.BySection(controls("zoomIn" to "In"))))
                .word("controls.zoomIn"),
        )
    }

    @Test
    fun aLanguageTheElementsCarryNoWordsForReadsEnglishInItsOwnLocale() {
        val scope = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Ja))

        assertEquals(RestSchema.Language.Ja, scope.language)
        assertEquals("ja", scope.locale)
        assertEquals(listOf("Zoom in", "Zoom out"), zoom(scope))
    }
}
