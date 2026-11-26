package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * StreamedEvents is how one kind of stream reads its records: the stream's rules, and what each
 * record is and the part it plays.
 *
 * A record it cannot read, one naming an event it does not know or carrying data it cannot decode,
 * is passed over: it hands the reader nothing and moves no position, so a reopen has it sent again.
 */
internal class StreamedEvents<Event>(
    /** The stream's own rules: when it ends, when it reopens and what counts as progress. */
    val rules: EventStreamRules,
    /** Returns the event a record carries and the part it plays, or null where the reader passes it over. */
    val read: (EventRecord) -> Pair<Event, EventRole>?,
    /** Returns the user whose arrival an event announces, a collaboration feed's `presence.join`, or null for every other event. */
    val arrivingMember: (Event) -> String? = { null },
) {
    companion object {
        private val agentRunEvents =
            RestSchema.AgentRunEvent
                .serializer()
                .descriptor.serialName
        private val collaborationEvents =
            RestSchema.CollabStreamEvent
                .serializer()
                .descriptor.serialName

        /**
         * The records of a stream opened by its declaration, read as a feed: the client knows no
         * event of it, so none ends it and none is passed over, every record's id moves the
         * position, and only the `reconnect` the API writes on every stream is read for what it
         * says.
         */
        val records: StreamedEvents<EventRecord> =
            StreamedEvents(EventStreamRules.collaborationFeed, { record ->
                record to if (record.event == "reconnect") EventRole.reconnect else EventRole.ordinary
            })

        /**
         * Returns how the stream the operation named [operationId] opens, whose events [serializer]
         * reads, is read, or null where no stream of the contract carries them: an agent run's
         * events, or a collaboration feed's, read by an editor stream's rules or a list stream's as the
         * operation opens one or the other.
         */
        fun <Event> of(
            serializer: KSerializer<Event>,
            operationId: String,
        ): StreamedEvents<Event>? =
            when (serializer.descriptor.serialName) {
                agentRunEvents -> {
                    StreamedEvents(EventStreamRules.agentRun, { record ->
                        decoded(record, serializer)?.let { event ->
                            (event as? RestSchema.AgentRunEvent)?.let(AgentRunStream::role)?.let { event to it }
                        }
                    })
                }

                collaborationEvents -> {
                    StreamedEvents(
                        CollaborationFeed.rules(operationId),
                        { record ->
                            decoded(record, serializer)?.let { event ->
                                (event as? RestSchema.CollabStreamEvent)?.let(CollaborationFeed::role)?.let {
                                    event to
                                        it
                                }
                            }
                        },
                        { event -> (event as? RestSchema.CollabStreamEvent)?.let(CollaborationFeed::arrivingMember) },
                    )
                }

                else -> {
                    null
                }
            }

        /**
         * Returns a record's data read as JSON with its `event:` name folded in as `type`, whatever
         * the data says there, or null where it is no JSON object or does not decode.
         */
        fun <Event> decoded(
            record: EventRecord,
            serializer: KSerializer<Event>,
        ): Event? {
            val members =
                try {
                    wireJson.parseToJsonElement(record.data) as? JsonObject
                } catch (malformed: IllegalArgumentException) {
                    null
                } ?: return null
            val name = record.event
            val folded = if (name == null) members else JsonObject(members + ("type" to JsonPrimitive(name)))
            return try {
                wireJson.decodeFromJsonElement(serializer, folded)
            } catch (undecodable: IllegalArgumentException) {
                null
            }
        }
    }
}
