package com.mountaya.elements

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatedMembersTest {
    @Test
    fun aMemberMappedToNullIsUnstatedAndEveryOtherIsKeptAsWritten() {
        val patch = mapOf("line-color" to JsonPrimitive("#228be6"), "line-opacity" to null)

        assertEquals(mapOf("line-color" to JsonPrimitive("#228be6")), statedMembers(patch))
    }

    @Test
    fun jsonNullFalseZeroAndTheEmptyStringAreValues() {
        val patch =
            mapOf(
                "color" to JsonNull,
                "visible" to JsonPrimitive(false),
                "opacity" to JsonPrimitive(0),
                "label" to JsonPrimitive(""),
            )

        assertEquals(patch, statedMembers(patch))
    }

    @Test
    fun aPatchOfNothingButUnstatedMembersStatesNothingAndAnEmptyOneNeither() {
        assertTrue(statedMembers(mapOf("color" to null, "label" to null)).isEmpty())
        assertTrue(statedMembers(emptyMap()).isEmpty())
    }

    @Test
    fun mergedOverWhatIsHeldAStatedMemberReplacesTheHeldOneAndAddsOneNotHeld() {
        val held = mapOf("color" to JsonPrimitive("#e64980"), "label" to JsonPrimitive("Hut"))
        val merged =
            held +
                statedMembers(mapOf("label" to JsonPrimitive("Refuge"), "zIndex" to JsonPrimitive(2), "color" to null))

        assertEquals(
            mapOf(
                "color" to JsonPrimitive("#e64980"),
                "label" to JsonPrimitive("Refuge"),
                "zIndex" to JsonPrimitive(2),
            ),
            merged,
        )
    }

    /**
     * A map compares what it holds as JSON values compare, a number as a number: a member restated in another
     * spelling keeps the held one, so the merge changes nothing, and the same digits written as text are another value.
     */
    @Test
    fun aMemberStatedAsTheValueHeldKeepsTheHeldOneANumberInAnySpelling() {
        val held = mapOf("width" to JsonPrimitive(4), "opacity" to JsonPrimitive(0.8), "label" to JsonPrimitive("Hut"))

        assertEquals(held, mergedMembers(held, mapOf("width" to Json.parseToJsonElement("4.0"))))
        assertEquals(held, mergedMembers(held, mapOf("opacity" to Json.parseToJsonElement("8e-1"))))
        assertEquals(
            held + ("width" to JsonPrimitive("4")),
            mergedMembers(held, mapOf("width" to Json.parseToJsonElement("\"4\""))),
        )
        assertEquals(
            held + ("label" to JsonPrimitive("Refuge")),
            mergedMembers(held, mapOf("label" to JsonPrimitive("Refuge"), "width" to Json.parseToJsonElement("4.0"))),
        )
    }

    @Test
    fun twoJsonValuesAreOneWhereTheyHoldTheSameNumbersTextAndMembers() {
        fun same(
            first: String,
            second: String,
        ) = isSameJsonValue(Json.parseToJsonElement(first), Json.parseToJsonElement(second))

        assertTrue(same("4", "4.0"))
        assertTrue(same("0.8", "8e-1"))
        assertTrue(same("[\"interpolate\", [\"linear\"], 7, 2]", "[\"interpolate\", [\"linear\"], 7.0, 2e0]"))
        assertTrue(same("{\"a\": 1, \"b\": [true, null]}", "{\"b\": [true, null], \"a\": 1.0}"))
        assertTrue(!same("4", "\"4\""))
        assertTrue(!same("true", "1"))
        assertTrue(!same("[1, 2]", "[2, 1]"))
        assertTrue(!same("{\"a\": 1}", "{\"a\": 1, \"b\": 2}"))
        assertTrue(!same("null", "0"))
    }
}
