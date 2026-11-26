package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CountedTextTest {
    private val english = forms("one" to "{count} result", "other" to "{count} results")
    private val french = forms("one" to "{count} résultat", "other" to "{count} résultats")

    /** A counted word of the forms given, each a string. */
    private fun forms(vararg forms: Pair<String, String>): JsonObject =
        JsonObject(
            forms.associate { (form, text) ->
                form to
                    JsonPrimitive(text)
            },
        )

    @Test
    fun englishTakesTheOneFormForOneAloneFrenchForZeroAndOne() {
        val en = RestSchema.Language.En
        val fr = RestSchema.Language.Fr

        assertEquals("1 result", countedText(english, 1, en))
        assertEquals("0 results", countedText(english, 0, en))
        assertEquals("2 results", countedText(english, 2, en))
        assertEquals("-1 results", countedText(english, -1, en))
        assertEquals("0 résultat", countedText(french, 0, fr))
        assertEquals("1 résultat", countedText(french, 1, fr))
        assertEquals("2 résultats", countedText(french, 2, fr))
        assertEquals("-1 résultats", countedText(french, -1, fr))
    }

    /**
     * The bytes are held, on the jvm, the iOS simulator and the emulator: French groups a count with a narrow
     * no-break space, U+202F, which neither a no-break space nor a plain one stands in for.
     */
    @Test
    fun theCountIsWrittenAsTheWordsLanguageWritesANumber() {
        assertEquals("1,234 results", countedText(english, 1234, RestSchema.Language.En))
        assertEquals("1 234 résultats", countedText(french, 1234, RestSchema.Language.Fr))
        assertEquals("1,234,567 results", countedText(english, 1_234_567, RestSchema.Language.En))
    }

    @Test
    fun aLanguageTheTableDoesNotHoldTakesTheOneFormForOneAsEnglishDoes() {
        val german = forms("one" to "{count} Ergebnis", "other" to "{count} Ergebnisse")
        val de = RestSchema.Language.De

        assertEquals("1 Ergebnis", countedText(german, 1, de))
        assertEquals("0 Ergebnisse", countedText(german, 0, de))
        assertEquals("1.234 Ergebnisse", countedText(german, 1234, de))
        assertEquals("1件", countedText(forms("other" to "{count}件"), 1, RestSchema.Language.Ja))
    }

    @Test
    fun aFormTheWordDoesNotStateIsItsOtherFormAndTheFirstCountPlaceholderFills() {
        val en = RestSchema.Language.En

        assertEquals("1 items", countedText(forms("other" to "{count} items"), 1, en))
        val numbered: JsonElement =
            JsonObject(
                mapOf(
                    "one" to JsonPrimitive(1),
                    "other" to JsonPrimitive("{count} items"),
                ),
            )
        assertEquals("1 items", countedText(numbered, 1, en))
        assertEquals("none", countedText(forms("other" to "none"), 3, en))
        assertEquals("3 of {count}", countedText(forms("other" to "{count} of {count}"), 3, en))
        assertEquals("{Count} 3", countedText(forms("other" to "{Count} {count}"), 3, en))
        assertEquals("{total} 3", countedText(forms("other" to "{total} {count}"), 3, en))
    }

    @Test
    fun aWordThatIsNoCountedWordIsTheRefusal() {
        val en = RestSchema.Language.En

        assertNull(countedText(JsonPrimitive("{count} results"), 1, en))
        assertNull(countedText(forms("one" to "{count} result"), 1, en))
        assertNull(countedText(JsonObject(mapOf("other" to JsonPrimitive(3))), 1, en))
        assertNull(countedText(JsonNull, 1, en))
    }
}
