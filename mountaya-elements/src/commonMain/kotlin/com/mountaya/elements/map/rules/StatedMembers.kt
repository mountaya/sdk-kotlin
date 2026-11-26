package com.mountaya.elements

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * statedMembers answers the members of a patch that state a value, which is how a patch is read
 * before it is merged onto what a map holds (`map.engine.patch`).
 *
 * A member mapped to Kotlin's `null` is unstated: it names nothing, which is what a caller forwards
 * where it may not have a value, and it leaves the member held as it is. Every other member is kept as
 * written, `JsonNull`, `false`, `0` and the empty string included, since each is a value a member may
 * give a meaning of its own: a paint property set to `JsonNull` goes back to the style's default.
 * Merged over what is held, the answer replaces each member it states and adds one the map did not
 * hold.
 */
internal fun statedMembers(patch: Map<String, JsonElement?>): Map<String, JsonElement> =
    buildMap {
        for ((name, member) in patch) {
            if (member != null) put(name, member)
        }
    }

/**
 * mergedMembers answers what a map holds once [patch] is merged onto [held] (`map.engine.patch`): each member the
 * patch states replaces the held one, and one the map did not hold is added, but a member stated as the value held
 * keeps the held one, so the merge changes nothing there. A map compares what it holds as JSON values compare, a
 * number as a number, so `4.0` restates `4` and `8e-1` restates `0.8`, while the same digits written as text, `"4"`,
 * are another value.
 */
internal fun mergedMembers(
    held: Map<String, JsonElement>,
    patch: Map<String, JsonElement?>,
): Map<String, JsonElement> =
    held +
        statedMembers(patch).filterNot { (name, member) ->
            held[name]?.let { isSameJsonValue(it, member) } == true
        }

/**
 * isSameJsonValue answers whether [first] and [second] are one JSON value: two numbers as numbers, whatever digits
 * write them, text as text, `true`, `false` and `null` as themselves, and an array or an object member by member,
 * an object's members in any order.
 */
internal fun isSameJsonValue(
    first: JsonElement,
    second: JsonElement,
): Boolean =
    when (first) {
        is JsonPrimitive -> {
            second is JsonPrimitive && first.isString == second.isString && isSamePrimitive(first, second)
        }

        is JsonArray -> {
            second is JsonArray && first.size == second.size &&
                first.indices.all { isSameJsonValue(first[it], second[it]) }
        }

        is JsonObject -> {
            second is JsonObject && first.keys == second.keys &&
                first.all { (name, member) -> second[name]?.let { isSameJsonValue(member, it) } == true }
        }
    }

/** Whether two primitives of one kind are one value: text by its characters, and a number by the double it reads as. */
private fun isSamePrimitive(
    first: JsonPrimitive,
    second: JsonPrimitive,
): Boolean {
    if (first.isString) return first.content == second.content
    val number = first.content.toDoubleOrNull()?.takeIf { isNumber(first.content) }
    val other = second.content.toDoubleOrNull()?.takeIf { isNumber(second.content) }
    return if (number != null && other != null) number == other else first.content == second.content
}

/** Whether [text] is a JSON number as written, so `NaN`, `Infinity` and a hexadecimal number read as no number. */
private fun isNumber(text: String): Boolean = jsonNumber.matches(text)

/** RFC 8259's grammar of a number. */
private val jsonNumber = Regex("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")
