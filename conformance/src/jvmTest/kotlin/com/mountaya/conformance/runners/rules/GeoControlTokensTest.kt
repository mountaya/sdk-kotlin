package com.mountaya.conformance

import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The theme-tokens runner's table against the contract's tokens table, `sdk/elements/tokens.json`: it names every
 * token the table names and no other, each entry reads the value the table states for its name, and each entry
 * writes the member it reads. A runner that reached a token by another's member would read a value the fixture
 * expects of another token, in no test's sight.
 */
class GeoControlTokensTest {
    private val table: JsonObject? =
        System
            .getProperty("mountaya.tokens")
            ?.let(::File)
            ?.takeIf { it.isFile }
            ?.let { Json.parseToJsonElement(it.readText()).jsonObject }

    private fun color(element: JsonElement?): HexColor? =
        element?.jsonObject?.let {
            HexColor(
                it.getValue("hex").jsonPrimitive.content,
                it["opacity"]?.jsonPrimitive?.double ?: 1.0,
            )
        }

    @Test
    fun theRunnersTokensAreTheTablesNameForNameEachReadingAndWritingItsOwnMember() {
        val tokens =
            table ?: return println("GeoControlTokensTest: skipped, the contract checkout's tokens table is absent.")
        val standard = ElementTokens.standard
        val colors = tokens.getValue("colors").jsonObject
        val shadows = tokens.getValue("shadows").jsonObject
        val dashes = tokens.getValue("dashes").jsonObject

        assertEquals(colors.keys, GeoControlFixtures.colorTokens.keys)
        assertEquals(shadows.keys, GeoControlFixtures.shadowTokens.keys)
        assertEquals(dashes.keys, GeoControlFixtures.dashTokens.keys)
        for ((name, token) in GeoControlFixtures.colorTokens) {
            val schemes = colors.getValue(name).jsonObject
            assertEquals(ThemeToken(color(schemes["light"]), color(schemes["dark"])), token.read.get(standard), name)
            val stated = ThemeToken(light = HexColor("#010203"))
            assertEquals(stated, token.read.get(standard.copy { token.write.set(this, stated) }), name)
        }
        for ((name, token) in GeoControlFixtures.shadowTokens) {
            val schemes = shadows.getValue(name).jsonObject
            assertEquals(schemes["light"]?.jsonArray?.map(::color), token.read.get(standard).light, name)
            assertEquals(schemes["dark"]?.jsonArray?.map(::color), token.read.get(standard).dark, name)
            val stated = ThemeToken(light = listOf(HexColor("#010203")))
            assertEquals(stated, token.read.get(standard.copy { token.write.set(this, stated) }), name)
        }
        for ((name, token) in GeoControlFixtures.dashTokens) {
            val schemes = dashes.getValue(name).jsonObject
            assertEquals(
                schemes["light"]?.jsonArray?.map { it.jsonPrimitive.double },
                token.read.get(standard).light,
                name,
            )
            assertEquals(
                schemes["dark"]?.jsonArray?.map { it.jsonPrimitive.double },
                token.read.get(standard).dark,
                name,
            )
            val stated = ThemeToken(light = listOf(9.0, 1.0))
            assertEquals(stated, token.read.get(standard.copy { token.write.set(this, stated) }), name)
        }
    }
}
