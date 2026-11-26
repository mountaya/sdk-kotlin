package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WordOverridesTest {
    @Test
    fun twoOverridesAreEqualWhereTheyAreOneShapeHoldingTheSameWords() {
        val none = JsonObject(emptyMap())
        assertEquals(WordOverrides.BySection(none), WordOverrides.BySection(JsonObject(emptyMap())))
        assertEquals(
            WordOverrides.ByLanguage(mapOf(RestSchema.Language.Fr to none)),
            WordOverrides.ByLanguage(mapOf(RestSchema.Language.Fr to none)),
        )
        assertNotEquals<WordOverrides>(WordOverrides.BySection(none), WordOverrides.ByLanguage(emptyMap()))
    }
}
