package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Instant

/**
 * `transport.dates.reviver`: the case's answer decoded as a generated type reads it, answered with
 * whether the value at `key` became an instant, and which one, written back as a request writes it.
 *
 * What a value becomes is decided by the type its property is declared as, never by its key or its
 * look: a field the contract declares a date-time is read through `TimestampSerializer`, the one
 * serializer every such generated property reads through; any other string field is read as a
 * `String`; and an object the contract leaves open is read as a `JsonObject`, inside which a string
 * stays a string. The answer carries the field at `key` and, where the case names `within`, the
 * open object holding the same field, which is where the value is read.
 *
 * A declared value the anchor refuses fails the decode of the answer it arrived in, as the contract
 * has it, and is no instant. Every declared value that is no instant is answered with `decodes`,
 * whether the answer holding it decodes at all: false where the serializer refuses it, true for a
 * null and for a value an open object holds.
 */
object TransportDatesReviverRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val key = input.text("key")
        val value: JsonElement = input.textOrNull("value")?.let(::JsonPrimitive) ?: JsonNull
        val within = input.textOrNull("within")
        val answer =
            JsonObject(
                buildMap {
                    put(key, value)
                    if (within != null) put(within, JsonObject(mapOf(key to value)))
                },
            )
        val field = answer.getValue(key)
        val declared = input.bool("declared")
        // Whether the answer holding the value decodes at all, which a declared value that is no instant states.
        var decodes = true
        val instant: Instant? =
            when {
                within != null -> {
                    val open =
                        answer[within] as? JsonObject ?: throw AssertionError("the open object did not decode as one")
                    val inside = open[key] as? JsonPrimitive
                    if (inside == null ||
                        !inside.isString
                    ) {
                        throw AssertionError("the value inside the open object is not a string")
                    }
                    null
                }

                field is JsonNull -> {
                    null
                }

                declared -> {
                    try {
                        wireJson.decodeFromJsonElement(TimestampSerializer, field)
                    } catch (refused: SerializationException) {
                        decodes = false
                        null
                    }
                }

                else -> {
                    wireJson.decodeFromJsonElement(String.serializer(), field)
                    null
                }
            }
        val read =
            mutableListOf(
                "instant" to FixtureValue.Bool(instant != null),
                "iso" to (instant?.let { FixtureValue.Text(formatTimestamp(it)) } ?: FixtureValue.Null),
            )
        if (declared && instant == null) read += "decodes" to FixtureValue.Bool(decodes)
        return ConformanceOutcome.Value(FixtureValue.Object(read))
    }
}
