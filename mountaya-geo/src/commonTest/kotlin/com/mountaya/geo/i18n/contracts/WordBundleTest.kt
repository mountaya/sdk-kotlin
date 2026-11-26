package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WordBundleTest {
    @Test
    fun twoBundlesAreEqualWhereTheyHoldTheSameWords() {
        val title = JsonObject(mapOf("title" to JsonPrimitive("Map")))
        assertEquals(WordBundle(title), WordBundle(JsonObject(mapOf("title" to JsonPrimitive("Map")))))
        assertEquals(
            WordBundle(title).hashCode(),
            WordBundle(JsonObject(mapOf("title" to JsonPrimitive("Map")))).hashCode(),
        )
        assertNotEquals(WordBundle(title), WordBundle(JsonObject(emptyMap())))
    }

    @Test
    fun theElementsWordsHoldEnglishAndEveryTranslationUnderTheSameKeys() {
        val bundle = WordBundle.elements
        val english = words(bundle.english)

        assertTrue(english.size >= 150, "${english.size} English words")
        assertTrue(bundle.translations.isNotEmpty())
        assertTrue(bundle.partial.isEmpty())
        for ((language, translation) in bundle.translations) {
            assertEquals(english.keys, words(translation).keys, language.value)
        }
        assertEquals("{count} results", english["search.resultCount.other"])
    }

    @Test
    fun theElementsWordsResolveForAReaderTheirOverridesFoldedOver() {
        val close = JsonObject(mapOf("close" to JsonPrimitive("Fermer la carte")))
        val french =
            resolveBundle(
                WordBundle.elements,
                RestSchema.Language.Fr,
                WordOverrides.BySection(JsonObject(mapOf("controls" to close))),
            )
        val german = resolveBundle(WordBundle.elements, RestSchema.Language.De)

        assertEquals(
            "Fermer la carte",
            french
                .getValue("controls")
                .jsonObject
                .getValue("close")
                .jsonPrimitive.content,
        )
        assertEquals(WordBundle.elements.english, german)
    }

    /** Every word of [tree] by its path, `controls.close`, a counted word's forms each a path of their own. */
    private fun words(
        tree: JsonObject,
        path: String = "",
    ): Map<String, String> =
        tree.entries.fold(emptyMap()) { words, (key, value: JsonElement) ->
            val place = if (path.isEmpty()) key else "$path.$key"
            words + if (value is JsonObject) words(value, place) else mapOf(place to value.jsonPrimitive.content)
        }
}
