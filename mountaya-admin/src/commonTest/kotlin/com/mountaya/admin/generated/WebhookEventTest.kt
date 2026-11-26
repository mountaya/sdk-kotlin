package com.mountaya.admin.generated

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.Stated
import com.mountaya.client.wireJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** The printed `WebhookEvent` against the real model: each event read as its class, and written back as it arrived. */
@OptIn(InternalMountayaApi::class)
class WebhookEventTest {
    /** A webhook event of [type] about a collection, its `data` written by the caller. */
    private fun collectionEvent(
        type: String,
        data: String,
    ): String =
        """{"actor":{"type":"mountaya"},"api_version":"0.8.0","created_at":"2026-11-14T09:12:44.000Z",""" +
            """"data":$data,"id":"019a8eed-e1ad-7753-b049-b0e20d79577a","object":"webhook_event",""" +
            """"organization_id":"org","related_object":{"id":"col","type":"collection"},""" +
            """"request":null,"task":"collection.create","type":"$type"}"""

    /** A collection as a webhook event carries it. */
    private val collection =
        """{"created_at":"2026-11-14T09:12:44.000Z","created_by":{"id":"user"},"description":null,""" +
            """"id":"col","is_private":false,"name":"Alps","object":"collection",""" +
            """"organization_id":"org","updated_at":"2026-11-14T09:12:44.000Z","version":3}"""

    private fun read(text: String): WebhookEvent = wireJson.decodeFromString(WebhookEvent.serializer(), text)

    /** Returns [event] written as JSON, read back as a tree, so two writings compare whatever their key order. */
    private fun written(event: WebhookEvent): JsonObject = wireJson.parseToJsonElement(wireJson.encodeToString(WebhookEvent.serializer(), event)) as JsonObject

    private fun tree(text: String): JsonObject = wireJson.parseToJsonElement(text) as JsonObject

    @Test
    fun aWebhookEventOfAKnownTypeIsItsClassAndIsWrittenBackAsItArrived() {
        val text = collectionEvent("collection.created", """{"object":$collection}""")

        val event = assertIs<WebhookEvent.CollectionCreated>(read(text))

        assertEquals(AdminSchema.WebhookEventType.CollectionCreated, event.type)
        assertEquals("webhook_event", event.payload.`object`)
        assertEquals("Alps", event.data.`object`.name)
        assertEquals(3L, event.data.`object`.version)
        assertNull(event.payload.request)
        assertEquals(tree(text), written(event))
        // The envelope's members are written in the contract's order, `type` among them.
        assertEquals(listOf("id", "object", "type"), written(event).keys.take(3))
    }

    @Test
    fun aTypeWhoseChangeMovesNoFieldReadsNoneWhateverItsBodyCarries() {
        val text = collectionEvent("collection.deleted", """{"object":$collection,"previous_attributes":{"name":"Old"}}""")

        val event = assertIs<WebhookEvent.CollectionDeleted>(read(text))

        assertNull(event.data.previous_attributes)
    }

    @Test
    fun previousAttributesHoldTheNamedFieldsAloneEachInItsThreeStates() {
        val text =
            collectionEvent(
                "collection.updated",
                """{"object":$collection,"previous_attributes":{"description":null,"name":"Old","zz":1}}""",
            )

        val event = assertIs<WebhookEvent.CollectionUpdated>(read(text))

        val previous = assertNotNull(event.data.previous_attributes, "the update states the attributes it changed")
        assertEquals("Old", previous.name)
        assertEquals(Stated(null), previous.description)
        assertNull(previous.is_private)
        assertEquals(tree(text.replace(""","zz":1""", "")), written(event))
    }

    @Test
    fun aWebhookEventOfATypeThisBuildDoesNotKnowIsTheEnvelopeWrittenBackWhole() {
        val text = collectionEvent("collection.teleported", """{"object":{"object":"collection.teleport"}}""")

        val event = assertIs<WebhookEvent.Unknown>(read(text))

        assertEquals("collection.teleported", event.type.value)
        val held = assertIs<AdminSchema.WebhookEventObject.Unknown>(event.event.data.`object`)
        assertEquals(JsonObject(mapOf("object" to JsonPrimitive("collection.teleport"))), held.json)
        assertEquals(tree(text), written(event))
    }

    @Test
    fun aPayloadItsTypesDoNotReadIsTheEnvelopeReadOffAsTheClassesAreWrittenWhole() {
        // A collection carrying no version: the collection's own type reads none of it.
        val text = collectionEvent("collection.updated", """{"object":${collection.replace(""","version":3""", "")},"previous_attributes":{"name":"Old"}}""")

        val event = assertIs<WebhookEvent.Unknown>(read(text))

        val held = assertIs<AdminSchema.WebhookEventObject.Unknown>(event.event.data.`object`)
        assertEquals(JsonPrimitive("Alps"), held.json["name"])
        assertFalse("version" in held.json)
        assertEquals(AdminSchema.WebhookEventType.CollectionUpdated, event.type)
        assertEquals("019a8eed-e1ad-7753-b049-b0e20d79577a", event.id)
        assertEquals(Instant.fromEpochSeconds(1_794_647_564), event.created_at)
        assertEquals("org", event.organization_id)
        assertEquals("collection", event.related_object.type)
        assertNull(event.request)
        assertEquals(AdminSchema.WebhookEventActorType.Mountaya, event.actor.type)
        assertEquals(tree(text), written(event))
    }

    @Test
    fun aBodyThatIsNoEnvelopeFailsTheDecodeWhateverTypeItNames() {
        val missing = collectionEvent("collection.created", """{"object":$collection}""").replace(""""task":"collection.create",""", "")

        assertFailsWith<SerializationException> { read(missing) }
        assertFailsWith<SerializationException> { read("""{"id":"e"}""") }
    }
}
