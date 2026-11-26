package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RelabelTextFieldTest {
    private val fr = RestSchema.Language.Fr
    private val name = """["coalesce", ["get", "name:fr"], ["get", "name"]]"""

    private fun json(text: String): JsonElement = Json.parseToJsonElement(text)

    private fun relabel(textField: String): JsonElement? = relabelTextField(json(textField), fr)

    @Test
    fun aReadOfANameFieldReadsTheLanguagesNameFallingBackToTheDefault() {
        assertEquals(json(name), relabel("""["get", "name"]"""))
        assertEquals(json(name), relabel("""["get", "name:latin"]"""))
        assertEquals(json(name), relabel("""["get", "name_en"]"""))
        assertEquals(json(name), relabel("\"{name}\""))
        assertEquals(json(name), relabel("""["coalesce", ["get", "name:en"], ["get", "name"]]"""))
        assertEquals(
            json("""["coalesce", ["get", "name:ja"], ["get", "name"]]"""),
            relabelTextField(json("\"{name}\""), RestSchema.Language.Ja),
        )
    }

    @Test
    fun theRestOfTheLabelStays() {
        assertEquals(json("""["concat", $name, "\n", ["get", "ele"], " m"]"""), relabel("\"{name}\\n{ele} m\""))
        assertEquals(
            json("""["step", ["zoom"], "", 10, $name]"""),
            relabel("""["step", ["zoom"], "", 10, ["get", "name"]]"""),
        )
        assertEquals(
            json("""["format", $name, {"font-scale": 1.2}, "\n", {}, ["get", "ref"], {"font-scale": 0.8}]"""),
            relabel(
                """["format", ["get", "name"], {"font-scale": 1.2}, "\n", {}, ["get", "ref"], {"font-scale": 0.8}]""",
            ),
        )
        // A match's input and labels decide, and stay; each output says the name, and an output
        // standing where the label does is the name alone rather than a row of one.
        assertEquals(
            json("""["match", ["get", "class"], "peak", $name, ["get", "ref"]]"""),
            relabel("""["match", ["get", "class"], "peak", ["concat", ["get", "name"]], ["get", "ref"]]"""),
        )
        assertEquals(json("""["upcase", $name]"""), relabel("""["upcase", ["get", "name"]]"""))
    }

    @Test
    fun namesJoinedByWhiteSpaceReadAsOneName() {
        assertEquals(json(name), relabel("\"{name:latin}\\n{name:nonlatin}\""))
        assertEquals(json(name), relabel("""["concat", ["get", "name:latin"], " ", ["get", "name:nonlatin"]]"""))
        // A row of names and white space keeps the first name's options in a format.
        assertEquals(
            json("""["format", $name, {"font-scale": 1.2}]"""),
            relabel("""["format", ["get", "name:latin"], {"font-scale": 1.2}, "\n", ["get", "name:nonlatin"]]"""),
        )
    }

    /** Where nothing and a string differ, the name standing for a row is a concat of one. */
    @Test
    fun aRowThatIsTheNameAloneUnderAnOperandIsAConcatOfOne() {
        assertEquals(
            json("""["upcase", ["concat", $name]]"""),
            relabel("""["upcase", ["concat", ["get", "name:latin"], "\n", ["get", "name:nonlatin"]]]"""),
        )
        assertEquals(json("""["concat", "\n", $name, " "]"""), relabel("""["concat", "\n", ["get", "name"], " "]"""))
    }

    @Test
    fun aLabelThatReadsNoNameOrThatAlreadyReadsTheLanguageIsLeftAlone() {
        assertNull(relabel("""["get", "ref"]"""))
        assertNull(relabel("""["get", "place_name"]"""))
        assertNull(relabel("\"{ref}\""))
        assertNull(relabel("\"Summit\""))
        assertNull(relabel("12"))
        assertNull(relabel(name))
        assertNull(relabelTextField(null, fr))
        assertNull(relabelTextField(JsonNull, fr))
    }

    @Test
    fun aNameTheWalkCannotRewriteWithoutSayingItTwiceOrDroppingTextIsLeftAlone() {
        assertNull(relabel("\"{name} ({name:en})\""))
        assertNull(relabel("\"{name} {ref} {name:en}\""))
        assertNull(relabel("""["slice", ["get", "name"], 0, 3]"""))
        assertNull(relabel("""["let", "n", ["get", "name"], ["var", "n"]]"""))
        // A separator written as a literal is text, and a name on each side of it is twice.
        assertNull(relabel("""["concat", ["get", "name"], ["literal", " / "], ["get", "name:en"]]"""))
    }

    @Test
    fun aLegacyFunctionIsTheNameAloneOnlyWhereEveryStopIs() {
        assertEquals(json(name), relabel("""{"stops": [[8, "{name}"], [12, "{name:latin}"]]}"""))
        assertNull(relabel("""{"stops": [[8, "{name}"], [12, "{ref}"]]}"""))
        assertNull(relabel("""{"base": 1}"""))
    }

    @Test
    fun aLetsResultIsWalkedAndItsBindingsStay() {
        assertEquals(json("""["let", "size", 2, $name]"""), relabel("""["let", "size", 2, ["get", "name"]]"""))
    }

    @Test
    fun aLabelNestedPastTheLimitIsLeftAlone() {
        fun nested(depth: Int): String =
            "[\"upcase\", ".repeat(depth - 1) + """["get", "name"]""" + "]".repeat(depth - 1)
        assertEquals(
            json(nested(MAX_LABEL_DEPTH).replace("""["get", "name"]""", name)),
            relabel(nested(MAX_LABEL_DEPTH)),
        )
        assertNull(relabel(nested(MAX_LABEL_DEPTH + 1)))
        // Built without a parser, a label ten thousand arrays deep is read with no recursion at all.
        var deep: JsonElement = json("""["get", "name"]""")
        repeat(10_000) { deep = JsonArray(listOf(JsonPrimitive("upcase"), deep)) }
        assertNull(relabelTextField(deep, fr))
    }
}
