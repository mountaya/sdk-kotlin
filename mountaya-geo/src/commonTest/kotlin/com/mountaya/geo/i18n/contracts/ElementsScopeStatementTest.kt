package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ElementsScopeStatementTest {
    @Test
    fun aStatementStatesNothingUntilItIsGivenASetting() {
        val statement = ElementsScopeStatement()

        assertNull(statement.language)
        assertNull(statement.locale)
        assertNull(statement.words)
        assertNull(statement.controlGlyphs)
        assertNull(statement.markerGlyphs)
    }

    @Test
    fun twoStatementsAreEqualWhereTheyStateTheSame() {
        val words =
            WordOverrides.BySection(
                JsonObject(
                    mapOf("controls" to JsonObject(mapOf("zoomIn" to JsonPrimitive("Plus")))),
                ),
            )
        val a = ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH", words = words)
        val b = ElementsScopeStatement(language = RestSchema.Language("fr"), locale = "fr-CH", words = words)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH"))
        assertNotEquals(
            a,
            ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-ch", words = words),
        )
        assertNotEquals(ElementsScopeStatement(locale = "é"), ElementsScopeStatement(locale = "é"))
        assertNotEquals(
            ElementsScopeStatement(controlGlyphs = GlyphSet.control),
            ElementsScopeStatement(markerGlyphs = GlyphSet.control),
        )
    }
}
