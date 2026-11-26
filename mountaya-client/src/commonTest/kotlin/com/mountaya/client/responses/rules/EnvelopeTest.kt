package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnvelopeTest {
    @Test
    fun theEnvelopeIsReadWholeANullDataAndAbsentMetadataIncluded() {
        val (data, metadata) =
            Envelope.rest(
                """{"data": null}""".encodeToByteArray(),
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
            )
        val (rows, paged) =
            Envelope.rest(
                """{"data": [1], "extensions": {"metadata": {"pagination": {"limit": 50, "next_cursor": "c1"}}}}"""
                    .encodeToByteArray(),
                ListSerializer(Int.serializer()),
                RestSchema.ListMetadata.serializer(),
            )

        assertEquals(JsonNull, data)
        assertNull(metadata)
        assertEquals(listOf(1), rows)
        assertEquals("c1", paged.pagination.next_cursor)
    }

    @Test
    fun metadataTheOperationDeclaresIsRefusedWhereTheAnswerCarriesNone() {
        assertFailsWith<SerializationException> {
            Envelope.rest(
                """{"data": []}""".encodeToByteArray(),
                ListSerializer(Int.serializer()),
                RestSchema.ListMetadata.serializer(),
            )
        }
        assertFailsWith<SerializationException> {
            Envelope.rest(
                """{"extensions": {}}""".encodeToByteArray(),
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
            )
        }
    }

    @Test
    fun aDataApiAnswerIsReadForItsOperationsFieldAndForItsEntries() {
        val routes = MapSerializer(String.serializer(), Int.serializer())
        val answer =
            Envelope.data(
                """{"data": {"directions": {"routes": 2}}}""".encodeToByteArray(),
                "directions",
                routes,
            )
        val missing = Envelope.data("""{"data": {}}""".encodeToByteArray(), "directions", routes)
        val failed =
            Envelope.data(
                """{"data": null, "errors": [{"message": "Limit must be at most 50."}]}""".encodeToByteArray(),
                "directions",
                routes,
            )

        assertEquals(mapOf("routes" to 2), answer.result)
        assertNull(missing.result)
        assertNull(failed.result)
        assertEquals(
            "Limit must be at most 50.",
            failed.envelope.errors
                .first()
                .message,
        )
    }

    /**
     * The body is read in one pass, so each level it reads skips what it does not know, as the
     * client's `Json` does, and keeps what a failure is read from however it arrives.
     */
    @Test
    fun aMemberTheEnvelopeDoesNotKnowIsSkippedAtEveryLevelItReads() {
        val (data, metadata) =
            Envelope.rest(
                (
                    """{"warnings": [1, {"a": null}], "data": {"id": "t1", "later": true},""" +
                        """ "extensions": {"trace": "x", "metadata": null}}"""
                ).encodeToByteArray(),
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
            )
        val routes = MapSerializer(String.serializer(), Int.serializer())
        val answer =
            Envelope.data(
                (
                    """{"extensions": {"cost": 3, "metadata": {"limit": 50}}, "trace": {},""" +
                        """ "data": {"later": [1], "directions": {"routes": 2}}, "errors": "none"}"""
                ).encodeToByteArray(),
                "directions",
                routes,
            )
        val failed =
            Envelope.data(
                """{"data": {"directions": null}, "errors": [{"message": "boom"}, 3]}""".encodeToByteArray(),
                "directions",
                routes,
            )

        assertEquals("""{"id":"t1","later":true}""", data.toString())
        assertNull(metadata)
        assertEquals(mapOf("routes" to 2), answer.result)
        assertTrue(answer.envelope.hasErrors, "`errors` that are no list still say the answer failed")
        assertEquals(emptyList(), answer.envelope.errors)
        assertEquals("""{"limit":50}""", answer.envelope.metadata.toString())
        assertNull(failed.result)
        assertEquals(listOf("boom"), failed.envelope.errors.map { it.message })
        assertFailsWith<SerializationException> {
            Envelope.rest("[]".encodeToByteArray(), JsonElement.serializer(), JsonObject.serializer().nullable)
        }
        assertFailsWith<SerializationException> { Envelope.data("null".encodeToByteArray(), "directions", routes) }
        assertFalse(Envelope.data("""{"data": null}""".encodeToByteArray(), "directions", routes).envelope.hasErrors)
    }

    @Test
    fun anExtensionsOrADataThatIsNoObjectDoesNotDecodeAndANullOneIsNone() {
        assertFailsWith<SerializationException> {
            Envelope.rest(
                """{"data": 1, "extensions": "x"}""".encodeToByteArray(),
                JsonElement.serializer(),
                JsonObject.serializer().nullable,
            )
        }
        assertFailsWith<SerializationException> {
            Envelope.data("""{"data": "x"}""".encodeToByteArray(), "directions", JsonElement.serializer())
        }
        assertNull(
            Envelope
                .rest(
                    """{"data": 1, "extensions": null}""".encodeToByteArray(),
                    JsonElement.serializer(),
                    JsonObject.serializer().nullable,
                ).second,
        )
    }
}
