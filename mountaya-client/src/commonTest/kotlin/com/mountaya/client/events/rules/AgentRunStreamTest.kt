package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class AgentRunStreamTest {
    private val reading = StreamedEvents.of(RestSchema.AgentRunEvent.serializer(), "getAgentRunEvents")

    private fun role(
        event: String,
        data: String,
    ): EventRole? = reading?.read?.invoke(EventRecord(event, data, "1-0"))?.second

    @Test
    fun anAgentRunsStreamIsReadByItsRulesWhateverTheOperationAndAnnouncesNoArrival() {
        for (operation in listOf("getAgentRunEvents", "getItineraryEvents", "aStreamALaterContractAdds")) {
            assertSame(
                EventStreamRules.agentRun,
                StreamedEvents.of(RestSchema.AgentRunEvent.serializer(), operation)?.rules,
            )
        }
        val event = reading?.read?.invoke(EventRecord("message-delta", """{"text":"The"}""", null))?.first
        assertNull(event?.let { reading.arrivingMember(it) })
    }

    @Test
    fun doneEndsTheStreamAnErrorAndAwaitingInputDoNotAndAnUnknownEventIsPassedOver() {
        assertSame(EventRole.end, role("done", """{"reason":"completed"}"""))
        assertSame(EventRole.ordinary, role("error", """{"code":"rate-limited","message":"Slow."}"""))
        assertSame(EventRole.ordinary, role("awaiting-input", "{}"))
        assertSame(EventRole.reconnect, role("reconnect", """{"reason":"credential-expired"}"""))
        assertNull(role("an-event-no-reader-knows", """{"detail":"x"}"""))
        assertNull(role("message", """{"content":7}"""))
    }

    @Test
    fun everyMemberPrintedTodayHasItsPartDecided() {
        val ordinary =
            mapOf(
                "message-delta" to """{"text":"The"}""",
                "message" to """{"content":"Hi","message_index":0}""",
                "budget-exhausted" to """{"reason":"cost"}""",
                "awaiting-input" to "{}",
            )
        for ((event, data) in ordinary) assertEquals(EventRole.ordinary, role(event, data), event)
        assertSame(EventStreamRules.agentRun, reading?.rules)
    }
}
