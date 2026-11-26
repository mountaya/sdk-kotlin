package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WordOverridesOfTest {
    private fun words(json: String): JsonObject = Json.parseToJsonElement(json).jsonObject

    @Test
    fun anOverrideIsKeyedBySectionOrByLanguage() {
        assertEquals(
            WordOverrides.BySection(words("""{"title": "Plan"}""")),
            wordOverridesOf(words("""{"title": "Plan"}""")),
        )
        assertEquals(
            WordOverrides.ByLanguage(mapOf(RestSchema.Language.De to words("""{"title": "Karte"}"""))),
            wordOverridesOf(words("""{"de": {"title": "Karte"}}""")),
        )
        assertEquals(WordOverrides.BySection(JsonObject(emptyMap())), wordOverridesOf(JsonObject(emptyMap())))
    }

    @Test
    fun anOverrideNamingALanguageAndASectionTogetherIsRefused() {
        assertNull(wordOverridesOf(words("""{"fr": {"title": "Plan"}, "title": "Chart"}""")))
    }

    /** Only the wire's own spelling of a language is a language key. */
    @Test
    fun aKeyIsALanguageOnlyAsTheWireSpellsIt() {
        assertEquals(
            WordOverrides.BySection(words("""{"FR": {"title": "Plan"}}""")),
            wordOverridesOf(words("""{"FR": {"title": "Plan"}}""")),
        )
        assertEquals(
            WordOverrides.ByLanguage(mapOf(RestSchema.Language.Fr to words("""{"title": "Plan"}"""))),
            wordOverridesOf(words("""{"fr": {"title": "Plan"}, "de": null, "it": "word"}""")),
        )
    }
}
