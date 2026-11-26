package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ResolveBundleTest {
    private fun words(json: String): JsonObject = Json.parseToJsonElement(json).jsonObject

    private val english = words("""{"controls": {"zoomIn": "Zoom in", "zoomOut": "Zoom out"}, "title": "Map"}""")
    private val french =
        words("""{"controls": {"zoomIn": "Zoom avant", "zoomOut": "Zoom arrière"}, "title": "Carte"}""")
    private val fr = RestSchema.Language.Fr
    private val de = RestSchema.Language.De
    private val bundle = WordBundle(english, translations = mapOf(fr to french))

    @Test
    fun aLanguageReadsItsTranslationAndOneWithNoneReadsEnglish() {
        assertSame(english, resolveBundle(bundle))
        assertSame(french, resolveBundle(bundle, fr))
        assertSame(english, resolveBundle(bundle, de))
    }

    @Test
    fun aTranslationHandedOverStandsAheadOfTheBundlesOwn() {
        val plan = words("""{"title": "Plan"}""")
        assertSame(plan, resolveBundle(bundle, fr, handedOver = mapOf(fr to plan)))
        assertSame(english, resolveBundle(bundle, RestSchema.Language.It, handedOver = mapOf(de to plan)))
    }

    @Test
    fun aPartialLayerFoldsOverTheBaseAndAnOverrideOverThePartialLayer() {
        val partial = WordBundle(english, partial = mapOf(de to words("""{"controls": {"zoomIn": "Vergrößern"}}""")))
        assertEquals(
            words("""{"controls": {"zoomIn": "Vergrößern", "zoomOut": "Zoom out"}, "title": "Map"}"""),
            resolveBundle(partial, de),
        )
        assertSame(french, resolveBundle(WordBundle(english, mapOf(fr to french), partial.partial), fr))
        val overrides =
            WordOverrides.ByLanguage(
                mapOf(de to words("""{"controls": {"zoomOut": "Verkleinern"}, "title": "Karte"}""")),
            )
        assertEquals(
            words("""{"controls": {"zoomIn": "Vergrößern", "zoomOut": "Verkleinern"}, "title": "Karte"}"""),
            resolveBundle(partial, de, overrides),
        )
    }

    @Test
    fun anOverrideBySectionIsForTheSelectedLanguageAndOneByLanguageForItsOwn() {
        val bySection = WordOverrides.BySection(words("""{"controls": {"zoomIn": "Plus près"}}"""))
        assertEquals(
            words("""{"controls": {"zoomIn": "Plus près", "zoomOut": "Zoom arrière"}, "title": "Carte"}"""),
            resolveBundle(bundle, fr, bySection),
        )
        assertSame(
            french,
            resolveBundle(
                bundle,
                fr,
                WordOverrides.ByLanguage(
                    mapOf(RestSchema.Language.En to words("""{"title": "Chart"}""")),
                ),
            ),
        )
    }

    /** A word written null is left out, and a list replaces as a word does. */
    @Test
    fun aNullWordIsLeftOutAndAListReplaces() {
        val overrides =
            WordOverrides.BySection(
                words("""{"controls": {"zoomIn": null}, "title": "Plan", "steps": ["a"]}"""),
            )
        val listed = WordBundle(words("""{"controls": {"zoomIn": "Zoom in"}, "title": "Map", "steps": ["x", "y"]}"""))
        assertEquals(
            words("""{"controls": {"zoomIn": "Zoom in"}, "title": "Plan", "steps": ["a"]}"""),
            resolveBundle(listed, overrides = overrides),
        )
    }

    /** A key every JavaScript object has is folded on in no SDK. */
    @Test
    fun aKeyNamingAPrototypeMemberIsLeftOut() {
        val overrides = WordOverrides.BySection(words("""{"constructor": "x", "__proto__": {"title": "y"}}"""))
        assertSame(english, resolveBundle(bundle, overrides = overrides))
    }

    @Test
    fun anOverrideThatRestatesTheWordsChangesNothing() {
        assertSame(
            french,
            resolveBundle(bundle, fr, WordOverrides.BySection(words("""{"title": "Carte", "controls": {}}"""))),
        )
    }
}
