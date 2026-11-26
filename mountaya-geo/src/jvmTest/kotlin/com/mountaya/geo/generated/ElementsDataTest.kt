package com.mountaya.geo.generated

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.WordBundle
import com.mountaya.geo.GlyphDrawing
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.labelLanguages
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The elements' data `mountaya-geo` publishes against the tables in the contract checkout, table by
 * table: every word, endonym, token, glyph and drawing the table holds is printed, as the table writes
 * it, and nothing else is. A printer that dropped a section, a dark value or a glyph would compile and
 * draw the elements a little wrong in no test's sight.
 */
class ElementsDataTest {
    private val directory: File? =
        System
            .getProperty("mountaya.elements")
            ?.let(::File)
            ?.takeIf { it.isDirectory }

    private fun table(file: String): JsonObject? =
        directory?.let { Json.parseToJsonElement(File(it, file).readText()).jsonObject }

    @Test
    fun theWordsAreTheTablesWordForWordInEveryLanguageItCarries() {
        val table = table("words.json") ?: return skipped()
        val languages = table.getValue("languages").jsonObject
        val bundle = WordBundle.elements

        assertEquals(languages.size, bundle.translations.size + 1)
        assertTrue(bundle.partial.isEmpty())
        for ((code, words) in languages) {
            val printed = if (code == "en") bundle.english else bundle.translations[RestSchema.Language(code)]
            val expected = described(words.jsonObject)

            assertEquals(expected, printed, code)
            assertTrue(words(expected).size >= 150, "$code holds ${words(expected).size} words")
        }
    }

    @Test
    fun everyWordStandsInEveryLanguageTheWordsPrintUnderTheSameKeys() {
        val english = words(WordBundle.elements.english).keys

        assertTrue(WordBundle.elements.translations.isNotEmpty())
        for ((language, words) in WordBundle.elements.translations) assertEquals(english, words(words).keys, language.value)
    }

    @Test
    fun eachLabelLanguagesEndonymIsTheTables() {
        val table = table("words.json") ?: return skipped()
        val endonyms = table.getValue("endonyms").jsonObject

        assertEquals(endonyms.size, languageEndonyms.size)
        for ((code, name) in endonyms) assertEquals(name.jsonPrimitive.content, languageEndonyms[RestSchema.Language(code)], code)
        assertTrue(labelLanguages.all { it in languageEndonyms })
    }

    @Test
    fun everyTokenIsTheTablesLightAndDarkAndNoOtherIsPrinted() {
        val table = table("tokens.json") ?: return skipped()
        val standard = ElementTokens.standard
        val colors = table.getValue("colors").jsonObject
        val shadows = table.getValue("shadows").jsonObject
        val dashes = table.getValue("dashes").jsonObject

        assertEquals(colors.keys, ElementTokens.colorTokens.keys)
        assertEquals(shadows.keys, ElementTokens.shadowTokens.keys)
        assertEquals(dashes.keys, ElementTokens.dashTokens.keys)
        for ((name, token) in ElementTokens.colorTokens) {
            val schemes = colors.getValue(name).jsonObject
            assertEquals(color(schemes["light"]), token.get(standard).light, name)
            assertEquals(color(schemes["dark"]), token.get(standard).dark, name)
        }
        for ((name, token) in ElementTokens.shadowTokens) {
            val schemes = shadows.getValue(name).jsonObject
            assertEquals(schemes["light"]?.jsonArray?.map(::color), token.get(standard).light, name)
            assertEquals(schemes["dark"]?.jsonArray?.map(::color), token.get(standard).dark, name)
        }
        for ((name, token) in ElementTokens.dashTokens) {
            val schemes = dashes.getValue(name).jsonObject
            assertEquals(schemes["light"]?.jsonArray?.map { it.jsonPrimitive.double }, token.get(standard).light, name)
            assertEquals(schemes["dark"]?.jsonArray?.map { it.jsonPrimitive.double }, token.get(standard).dark, name)
        }
    }

    @Test
    fun everyLayoutMeasureIsTheTablesAndNoOtherIsPrinted() {
        val table = table("tokens.json") ?: return skipped()
        val layout = table.getValue("layout").jsonObject
        val printed =
            ElementLayout::class
                .java.declaredMethods
                .filter { it.name.startsWith("get") && it.parameterCount == 0 }
                .associate { method ->
                    val member = method.name.removePrefix("get").replaceFirstChar { it.lowercase() }
                    member to (method.invoke(ElementLayout) as Number).toDouble()
                }

        assertEquals(layout.size, printed.size)
        for ((name, value) in layout) {
            val member = name.split('-').let { words -> words.first() + words.drop(1).joinToString("") { it.replaceFirstChar(Char::uppercase) } }
            assertEquals(value.jsonPrimitive.double, printed[member], name)
        }
    }

    @Test
    fun eachGlyphSetIsTheTablesGlyphForGlyph() {
        val table = table("glyphs.json") ?: return skipped()
        for ((key, printed) in listOf("marker" to GlyphSet.marker, "control" to GlyphSet.control)) {
            val expected = table.getValue(key).jsonObject
            val glyphs = expected.getValue("glyphs").jsonArray.map { it.jsonObject }

            assertEquals(expected.getValue("paint").jsonPrimitive.content, printed.paint.id, key)
            assertEquals(expected.getValue("strokeWidth").jsonPrimitive.double, printed.strokeWidth, key)
            assertEquals(expected.getValue("viewBox").jsonPrimitive.double, printed.viewBox, key)
            assertEquals(glyphs.size, printed.markup.size, key)
            for (glyph in glyphs) {
                val name = glyph.getValue("name").jsonPrimitive.content
                assertEquals(glyph.getValue("markup").jsonPrimitive.content, printed.markup[name], "$key $name")
            }
        }
    }

    @Test
    fun eachDrawingIsTheTablesShapeForShapeAndPaintForPaint() {
        val table = table("glyphs.json") ?: return skipped()
        val drawings = table.getValue("artwork").jsonArray.map { it.jsonObject }

        assertEquals(drawings.size, GlyphDrawing.artwork.size)
        for (drawing in drawings) {
            val name = drawing.getValue("name").jsonPrimitive.content
            val printed = assertNotNull(GlyphDrawing.artwork[name], name)
            val shapes = drawing.getValue("shapes").jsonArray.map { it.jsonObject }

            assertEquals(drawing.getValue("viewBox").jsonArray.map { it.jsonPrimitive.double }, printed.viewBox, name)
            assertEquals(drawing["opacity"]?.jsonPrimitive?.double ?: 1.0, printed.opacity, name)
            val shadow = drawing["shadow"]?.jsonObject
            assertEquals(
                shadow?.let {
                    GlyphDrawing.Shadow(
                        it.getValue("blur").jsonPrimitive.double,
                        it.getValue("dx").jsonPrimitive.double,
                        it.getValue("dy").jsonPrimitive.double,
                        assertNotNull(color(it), "every token holds a colour"),
                    )
                },
                printed.shadow,
                name,
            )
            assertEquals(shapes.size, printed.shapes.size, name)
            for ((shape, expected) in printed.shapes.zip(shapes)) {
                assertEquals(expected.getValue("markup").jsonPrimitive.content, shape.markup, name)
                assertEquals(tablePaint(expected["fill"]), printedPaint(shape.fill), name)
                assertEquals(tablePaint(expected["stroke"]), printedPaint(shape.stroke), name)
                assertEquals(expected["stroke"]?.jsonObject?.get("width")?.jsonPrimitive?.double, shape.strokeWidth, name)
            }
        }
    }

    /** [tree] with every description, a `//` member, left out, as the printer leaves it out. */
    private fun described(tree: JsonObject): JsonObject =
        JsonObject(
            tree.filterKeys { it != "//" }.mapValues { (_, value) -> if (value is JsonObject) described(value) else value },
        )

    /** Every word of [tree] by its path, `controls.close`, a counted word's forms each a path of their own. */
    private fun words(
        tree: JsonObject,
        path: String = "",
    ): Map<String, String> =
        tree.entries.fold(emptyMap()) { words, (key, value: JsonElement) ->
            val place = if (path.isEmpty()) key else "$path.$key"
            words + if (value is JsonObject) words(value, place) else mapOf(place to value.jsonPrimitive.content)
        }

    /** A table's colour as the printed one is compared, or null where the table states none. */
    private fun color(json: JsonElement?): HexColor? {
        val colour = json as? JsonObject ?: return null
        return HexColor(colour.getValue("hex").jsonPrimitive.content, colour["opacity"]?.jsonPrimitive?.double ?: 1.0)
    }

    /** A printed paint read back as the table writes one. */
    private fun printedPaint(paint: GlyphDrawing.Paint?): Map<String, String> =
        when (paint) {
            null -> emptyMap()
            is GlyphDrawing.Paint.Color -> mapOf("hex" to paint.color.hex, "opacity" to "${paint.color.opacity}")
            is GlyphDrawing.Paint.Token -> {
                val name = ElementTokens.colorTokens.entries.firstOrNull { it.value == paint.token }?.key ?: "?"
                mapOf("token" to name, "opacity" to "${paint.opacity}")
            }

            is GlyphDrawing.Paint.LinearGradient -> {
                val stops = paint.stops.joinToString(";") { "${it.offset} ${it.color.hex} ${it.color.opacity}" }
                mapOf("linear" to "${paint.from} ${paint.to} $stops")
            }
        }

    /** A table's paint, its opacity 1 where it states none. */
    private fun tablePaint(json: JsonElement?): Map<String, String> {
        val paint = json as? JsonObject ?: return emptyMap()
        paint["linear"]?.jsonObject?.let { linear ->
            fun point(end: String) = linear.getValue(end).jsonArray.map { it.jsonPrimitive.double }
            val stops =
                linear.getValue("stops").jsonArray.joinToString(";") { stop ->
                    val written = stop.jsonObject
                    "${written.getValue("offset").jsonPrimitive.double} ${written.getValue("hex").jsonPrimitive.content} " +
                        "${written["opacity"]?.jsonPrimitive?.double ?: 1.0}"
                }
            return mapOf("linear" to "${point("from")} ${point("to")} $stops")
        }
        val read = mutableMapOf("opacity" to "${paint["opacity"]?.jsonPrimitive?.double ?: 1.0}")
        paint["hex"]?.let { read["hex"] = it.jsonPrimitive.content }
        paint["token"]?.let { read["token"] = it.jsonPrimitive.content }
        return read
    }

    private fun skipped() = println("ElementsDataTest: skipped, the contract checkout's sdk/elements/ is absent.")
}
