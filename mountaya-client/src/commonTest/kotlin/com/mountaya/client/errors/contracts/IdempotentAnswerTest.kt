package com.mountaya.client

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class IdempotentAnswerTest {
    @Test
    fun eachPartIsReadWhereTheMetadataStatesOneOfItsTypeAndNoneOtherwise() {
        val stated =
            JsonObject(
                mapOf(
                    "status" to JsonPrimitive(201),
                    "location" to JsonPrimitive("/v1/things/t1"),
                    "etag" to JsonPrimitive("\"d1\""),
                ),
            )
        val wrong =
            JsonObject(
                mapOf(
                    "status" to JsonPrimitive("201"),
                    "location" to JsonPrimitive(7),
                    "etag" to JsonPrimitive(201.5),
                ),
            )

        assertEquals(IdempotentAnswer(201, "/v1/things/t1", "\"d1\""), IdempotentAnswer.of(stated))
        assertEquals(IdempotentAnswer(null, null, null), IdempotentAnswer.of(wrong))
        assertEquals(
            IdempotentAnswer(null, null, null),
            IdempotentAnswer.of(
                JsonObject(
                    mapOf("status" to JsonPrimitive(201.5)),
                ),
            ),
        )
        assertEquals(IdempotentAnswer(null, null, null), IdempotentAnswer.of(null))
    }
}
