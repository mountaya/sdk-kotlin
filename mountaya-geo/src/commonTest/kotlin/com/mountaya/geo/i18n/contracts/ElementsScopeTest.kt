package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

@OptIn(InternalMountayaApi::class)
class ElementsScopeTest {
    @Test
    fun theStandardScopeIsEnglishInTheLocaleEnTheElementsWordsAndTheBundledSets() {
        val scope = ElementsScope.standard

        assertEquals(RestSchema.Language.En, scope.language)
        assertEquals("en", scope.locale)
        assertEquals(WordBundle.elements.english, scope.words)
        assertEquals(GlyphSet.control, scope.controlGlyphs)
        assertEquals(GlyphSet.marker, scope.markerGlyphs)
    }

    @Test
    fun aWordIsReadByItsPathTheEnglishWordStandingWhereAnOverrideLeftNoText() {
        val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Fr))
        val numbered =
            scopeOf(
                ElementsScope.standard,
                ElementsScopeStatement(
                    words =
                        WordOverrides.BySection(
                            JsonObject(mapOf("controls" to JsonObject(mapOf("zoomIn" to JsonPrimitive(3))))),
                        ),
                ),
            )

        assertEquals("Zoom in", ElementsScope.standard.word("controls.zoomIn"))
        assertEquals("Zoom avant", french.word("controls.zoomIn"))
        assertEquals(
            "Location is blocked. Allow it in your settings.",
            ElementsScope.standard.word("tooltips.geolocate.deniedDescription"),
        )
        assertEquals(
            "Localisation bloquée\u00A0: autorisez-la dans vos réglages.",
            french.word("tooltips.geolocate.deniedDescription"),
        )
        assertEquals("Zoom in", numbered.word("controls.zoomIn"))
        assertEquals("Zoom out", numbered.word("controls.zoomOut"))
    }

    @Test
    fun aPathThatNamesNoTextIsDrawnAsItself() {
        val scope = ElementsScope.standard

        assertEquals("controls.nothing", scope.word("controls.nothing"))
        assertEquals("controls", scope.word("controls"))
        assertEquals("search.resultCount", scope.word("search.resultCount"))
        assertEquals("", scope.word(""))
        assertEquals("controls..zoomIn", scope.word("controls..zoomIn"))
    }

    @Test
    fun twoScopesAreEqualWhereTheyHoldTheSame() {
        val a = scopeOf(ElementsScope.standard, ElementsScopeStatement(locale = "fr-CH"))
        val b = scopeOf(ElementsScope.standard, ElementsScopeStatement(locale = "fr-CH"))
        val other = scopeOf(ElementsScope.standard, ElementsScopeStatement(locale = "fr-BE"))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, other)
        assertNotEquals(a, ElementsScope.standard)
    }

    /**
     * A composable's own strings stand over the scope's words for it alone: a word they name by its path is read
     * there, before the scope's own and the English, by `word` and by every rule reading the scope's text, and every
     * other word is the scope's; strings layered on strings stand over them, and none hand back the very scope.
     */
    @Test
    fun aComposablesOwnStringsStandOverTheScopesWordsByPath() {
        val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Fr))
        val own = french.withStrings(mapOf("controls.zoomIn" to "Plus", "amenities.water" to "Eau potable"))

        assertEquals("Plus", own.word("controls.zoomIn"))
        assertEquals("Zoom arrière", own.word("controls.zoomOut"), "a word they do not name is the scope's")
        assertEquals("Eau potable", own.text("amenities.water"))
        assertEquals("Zoom avant", french.word("controls.zoomIn"), "the scope around it keeps its own")
        assertEquals("Moins", own.withStrings(mapOf("controls.zoomOut" to "Moins")).word("controls.zoomOut"))
        assertEquals("Plus", own.withStrings(mapOf("controls.zoomOut" to "Moins")).word("controls.zoomIn"))
        assertEquals("Encore", own.withStrings(mapOf("controls.zoomIn" to "Encore")).word("controls.zoomIn"))
        assertSame(french, french.withStrings(null))
        assertSame(french, french.withStrings(emptyMap()))
        assertEquals(french.words, own.words, "the words themselves are the scope's")
        assertNotEquals(french, own)
        assertEquals(own, french.withStrings(mapOf("controls.zoomIn" to "Plus", "amenities.water" to "Eau potable")))
    }

    @Test
    fun aPathsTextIsTheWordItNamesAndNoneWhereNoWordIsText() {
        assertEquals("Water", ElementsScope.standard.text("amenities.water"))
        assertEquals(null, ElementsScope.standard.text("amenities.sauna"))
        assertEquals(null, ElementsScope.standard.text("waypointCard.summary"))
        assertEquals("amenities.sauna", ElementsScope.standard.word("amenities.sauna"))
    }
}
