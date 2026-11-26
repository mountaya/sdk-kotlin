package com.mountaya.conformance

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/** The largest magnitude below which every whole double is an exact `Long`, 2^53. */
private const val EXACT_WHOLE_LIMIT = 9_007_199_254_740_992.0

/**
 * [value] as the JSON a fixture writes it, for a rule whose input is JSON or a wire type the wire's
 * own serializer decodes. A whole number is written as one, `1450` and never `1450.0`, so a member
 * the wire types as an `Int` decodes; a number JSON cannot carry is written as the double it is.
 */
internal fun fixtureJson(value: FixtureValue): JsonElement =
    when (value) {
        is FixtureValue.Null -> {
            JsonNull
        }

        is FixtureValue.Bool -> {
            JsonPrimitive(value.value)
        }

        is FixtureValue.Text -> {
            JsonPrimitive(value.value)
        }

        is FixtureValue.Number -> {
            val number = value.value
            if (number.isFinite() && number == kotlin.math.floor(number) &&
                kotlin.math.abs(number) < EXACT_WHOLE_LIMIT
            ) {
                JsonPrimitive(number.toLong())
            } else {
                JsonPrimitive(number)
            }
        }

        is FixtureValue.Array -> {
            JsonArray(value.items.map(::fixtureJson))
        }

        is FixtureValue.Object -> {
            JsonObject(value.entries.associate { (key, item) -> key to fixtureJson(item) })
        }
    }

/** [element] as a fixture value, an answer is compared as. */
internal fun fixtureValueOf(element: JsonElement): FixtureValue =
    when (element) {
        is JsonNull -> {
            FixtureValue.Null
        }

        is JsonPrimitive -> {
            when {
                element.isString -> {
                    FixtureValue.Text(element.content)
                }

                element.booleanOrNull != null -> {
                    FixtureValue.Bool(element.content == "true")
                }

                else -> {
                    FixtureValue.Number(
                        element.doubleOrNull ?: throw AssertionError("`${element.content}` is no number"),
                    )
                }
            }
        }

        is JsonArray -> {
            FixtureValue.Array(element.map(::fixtureValueOf))
        }

        is JsonObject -> {
            FixtureValue.Object(element.entries.map { (key, item) -> key to fixtureValueOf(item) })
        }
    }

/** The strings [items] holds, failing with a sentence on any other value. */
internal fun fixtureTexts(
    items: List<FixtureValue>,
    what: String,
): List<String> =
    items.map {
        (it as? FixtureValue.Text)?.value
            ?: throw AssertionError("$what holds a value that is no string")
    }

/** [texts] as a fixture's array of strings. */
internal fun textArray(texts: List<String>): FixtureValue = FixtureValue.Array(texts.map { FixtureValue.Text(it) })
