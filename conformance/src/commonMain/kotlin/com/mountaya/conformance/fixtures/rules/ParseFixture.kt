package com.mountaya.conformance

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Parses a fixture's text, keeping each object's key order.
 *
 * A number JSON cannot carry is written `{ "$number": "NaN" }`, `{ "$number": "Infinity" }` or
 * `{ "$number": "-Infinity" }`, and is decoded here into the [FixtureValue.Number] it stands for,
 * so an input and an expectation carry it alike (`sdk/SPEC.md` section 10). An object that is not
 * exactly one of those three spellings stays an object.
 */
internal fun parseFixture(text: String): FixtureValue = Json.parseToJsonElement(text).toFixtureValue()

private const val NUMBER_KEY = "\$number"

private val NON_FINITE =
    mapOf(
        "NaN" to Double.NaN,
        "Infinity" to Double.POSITIVE_INFINITY,
        "-Infinity" to Double.NEGATIVE_INFINITY,
    )

private fun JsonElement.toFixtureValue(): FixtureValue =
    when (this) {
        is JsonNull -> {
            FixtureValue.Null
        }

        is JsonObject -> {
            nonFinite()?.let(FixtureValue::Number)
                ?: FixtureValue.Object(entries.map { (key, value) -> key to value.toFixtureValue() })
        }

        is JsonArray -> {
            FixtureValue.Array(map { it.toFixtureValue() })
        }

        is JsonPrimitive -> {
            when {
                isString -> FixtureValue.Text(content)
                booleanOrNull != null -> FixtureValue.Bool(content.toBoolean())
                else -> FixtureValue.Number(content.toDouble())
            }
        }
    }

/** The number this object spells, when it is exactly `{ "$number": <one of the three> }`. */
private fun JsonObject.nonFinite(): Double? {
    if (keys != setOf(NUMBER_KEY)) return null
    val spelling = get(NUMBER_KEY) as? JsonPrimitive ?: return null
    return if (spelling.isString) NON_FINITE[spelling.content] else null
}
