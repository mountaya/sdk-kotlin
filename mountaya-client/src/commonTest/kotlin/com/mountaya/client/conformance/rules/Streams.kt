package com.mountaya.client

import com.mountaya.client.generated.OperationTable
import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration

/** Streams is what the internal stream runners share: a transport on a test's time, and a printed stream's reader. */
internal object Streams {
    /** The `data:` of a frame of each event the stream fixtures name, as the API writes it. */
    val frameData: Map<String, String> =
        mapOf(
            "message-delta" to """{"text":"The"}""",
            "message" to """{"content":"The second day is the long one.","message_index":0}""",
            "reconnect" to """{"reason":"credential-expired"}""",
            "presence.snapshot" to """{"members":[]}""",
            "lock.snapshot" to """{"locks":[]}""",
            "segment.changed" to
                """{"action":"updated","actor_id":"2b8d4f19-6a05-4e73-8c1f-9d3b7e05a24c",""" +
                """"id":"c1f0a3d2-5b7e-4a86-9f21-3e4d5c6b7a80",""" +
                """"itinerary_id":"8f14e45f-ceea-467a-9575-3f1b1a2d9c47",""" +
                """"kind":"segment","occurred_at":"2026-05-12T09:14:07Z",""" +
                """"organization_id":"0d9c2b7a-1e4f-4c3b-8a6d-5f7e9b1c2d3e",""" +
                """"revision":4}""",
        )

    /** The comment the API writes on an open stream every 15 seconds. */
    val heartbeat: ByteArray = ": heartbeat\n\n".encodeToByteArray()

    /**
     * One frame of the stream fixtures as the bytes of a record, a `presence.join` naming the member its
     * `userId` names, and a frame stating its own `data`, one naming an event no reader knows, carrying it.
     */
    fun record(frame: FixtureValue.Object): String {
        val event = frame.text("event")
        val stated = frame["data"]
        val data =
            when {
                stated != null -> {
                    json(stated).toString()
                }

                event == "presence.join" -> {
                    buildJsonObject {
                        put("user_id", frame.text("userId"))
                        put("connected_at", "2026-05-12T09:14:07Z")
                    }.toString()
                }

                else -> {
                    frameData[event] ?: throw AssertionError("no frame's data is written for the event `$event`")
                }
            }
        return buildString {
            append("event: ").append(event).append('\n')
            frame.textOrNull("id")?.let { append("id: ").append(it).append('\n') }
            append("data: ").append(data).append("\n\n")
        }
    }

    /** A fixture's value as the JSON it writes, a number written whole where it is whole. */
    fun json(value: FixtureValue): JsonElement =
        when (value) {
            is FixtureValue.Null -> {
                JsonNull
            }

            is FixtureValue.Bool -> {
                JsonPrimitive(value.value)
            }

            is FixtureValue.Number -> {
                val whole = value.value.toLong()
                if (whole.toDouble() == value.value) JsonPrimitive(whole) else JsonPrimitive(value.value)
            }

            is FixtureValue.Text -> {
                JsonPrimitive(value.value)
            }

            is FixtureValue.Array -> {
                JsonArray(value.items.map(::json))
            }

            is FixtureValue.Object -> {
                JsonObject(value.entries.associate { (key, item) -> key to json(item) })
            }
        }

    /**
     * A transport on the test's virtual time and wall clock, sending through [exchange], drawing [draw]
     * before every wait, and reading each answer on a dispatcher of the test's own scheduler.
     */
    fun transport(
        scope: TestScope,
        exchange: HttpExchange,
        credential: Credential,
        timeout: Duration = AttemptBudget.clientDefault,
        draw: Double = 0.0,
    ): Transport =
        Transport(
            Transport.Configuration(credential, null, BaseUrls.standard, RetryPolicy.standard, timeout, null),
            exchange,
            Scripted.clock(scope),
            Scripted.Random(draw),
            StandardTestDispatcher(scope.testScheduler),
        )

    /** The reader of the case's printed stream, `agent` or `collaboration`, told of each open's end through [observer]. */
    fun frames(
        stream: String,
        transport: Transport,
        observer: ((MountayaException?) -> Unit)? = null,
    ): Flow<*> =
        when (stream) {
            "agent" -> {
                val call = opening(OperationTable.Rest.getAgentRunEvents, "/v1/agent/runs/ru/events")
                EventStreamReader(
                    transport,
                    transport.prepare(call, EVENT_STREAM_MEDIA_TYPE),
                    reading(RestSchema.AgentRunEvent.serializer(), "getAgentRunEvents"),
                    observer = observer,
                ).frames()
            }

            "collaboration" -> {
                val call = opening(OperationTable.Rest.getItineraryEvents, "/v1/collab/itineraries/it/events")
                EventStreamReader(
                    transport,
                    transport.prepare(call, EVENT_STREAM_MEDIA_TYPE),
                    reading(RestSchema.CollabStreamEvent.serializer(), "getItineraryEvents"),
                    observer = observer,
                ).frames()
            }

            else -> {
                throw AssertionError("no stream is called `$stream`")
            }
        }

    /** The agent run's stream a printed method opens, read through the transport as the method reads it. */
    fun agentRun(transport: Transport): EventStream<RestSchema.AgentRunEvent> =
        transport.stream(
            opening(OperationTable.Rest.getAgentRunEvents, "/v1/agent/runs/ru/events"),
            RestSchema.AgentRunEvent.serializer(),
        )

    /** The itinerary's editor stream a printed method opens, read through the transport as the method reads it. */
    fun itineraryEditors(transport: Transport): EventStream<RestSchema.CollabStreamEvent> =
        transport.stream(
            opening(OperationTable.Rest.getItineraryEvents, "/v1/collab/itineraries/it/events"),
            RestSchema.CollabStreamEvent.serializer(),
        )

    private fun <Event> reading(
        serializer: kotlinx.serialization.KSerializer<Event>,
        operationId: String,
    ): StreamedEvents<Event> =
        StreamedEvents.of(serializer, operationId) ?: throw AssertionError("no reading of the printed stream")

    private fun opening(
        facts: com.mountaya.client.OperationFacts,
        path: String,
    ): OperationCall =
        OperationCall(facts, path, emptyList(), emptyList(), RequestBody.Empty, options = RequestOptions())
}
