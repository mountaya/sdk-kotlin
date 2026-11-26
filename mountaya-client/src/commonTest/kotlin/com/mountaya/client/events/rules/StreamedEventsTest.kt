package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class StreamedEventsTest {
    @Test
    fun aRecordsEventNameIsFoldedIntoItsTypeWhateverItsDataSays() {
        val event =
            StreamedEvents.decoded(
                EventRecord("message", """{"type":"done","content":"Hi","message_index":0}""", null),
                RestSchema.AgentRunEvent.serializer(),
            )

        assertEquals("Hi", assertIs<RestSchema.AgentMessageEvent>(event).content)
    }

    @Test
    fun dataThatIsNoJsonObjectIsPassedOver() {
        assertNull(StreamedEvents.decoded(EventRecord("message", "a", null), RestSchema.AgentRunEvent.serializer()))
        assertNull(StreamedEvents.decoded(EventRecord("message", "[1]", null), RestSchema.AgentRunEvent.serializer()))
    }

    @Test
    fun aStreamByDeclarationHandsEveryRecordOnAndReadsOnlyItsReconnect() {
        val record = EventRecord("brand-new", "not json", "1-0")

        assertEquals(record to EventRole.ordinary, StreamedEvents.records.read(record))
        assertSame(EventRole.reconnect, StreamedEvents.records.read(EventRecord("reconnect", "{}", null))?.second)
        assertNull(StreamedEvents.of(JsonElement.serializer(), "followThings"))
    }

    @Test
    fun aStreamByDeclarationIsReadOnACollaborationFeedsRules() {
        assertSame(
            com.mountaya.client.EventStreamRules.collaborationFeed,
            StreamedEvents.records.rules,
        )
    }
}
