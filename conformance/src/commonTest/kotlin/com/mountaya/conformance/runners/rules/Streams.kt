package com.mountaya.conformance

import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.Mountaya
import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Streams is what the event runners share: a stream by its declaration, a record's bytes, and a reader of a printed stream. */
internal object Streams {
    /** A stream this build prints no method for, by its declaration. */
    val aStream: OperationDeclaration =
        OperationDeclaration(
            "followThings",
            "GET",
            idempotent = true,
            credentials =
                CredentialDeclaration(
                    actingUser = CredentialDeclaration.ActingUser.Required,
                    requiresSession = true,
                ),
        )

    /** One record of a stream: its `event:`, its `id:` and its `data:`, each where the frame names one, and the empty line that ends it. */
    fun sse(
        event: String?,
        id: String?,
        data: String?,
    ): String =
        buildString {
            event?.let { append("event: ").append(it).append('\n') }
            id?.let { append("id: ").append(it).append('\n') }
            data?.let { append("data: ").append(it).append('\n') }
            append('\n')
        }

    /** Followed is what a reader of a printed stream method saw: each event's type, and how the collection ended. */
    class Followed(
        val types: List<String>,
        val reason: String?,
        val surfaced: List<String>,
        val ended: Boolean,
        val failure: MountayaException?,
    )

    /** The failure a reader's own handler raises. */
    class HandlerFailure : Exception("the reader's own handler failed")

    /**
     * Reads an agent run's stream or a collaboration feed through its printed method, handing each
     * event to a handler that fails on the one at [failsAt], where a case names one.
     */
    suspend fun follow(
        stream: String,
        client: Mountaya,
        failsAt: Int? = null,
    ): Followed {
        val types = mutableListOf<String>()
        var reason: String? = null
        val surfaced = mutableListOf<String>()
        var ended = false
        var failure: MountayaException? = null
        try {
            when (stream) {
                "agent" -> {
                    client.agent.runs.getAgentRunEvents("ru").collect { frame ->
                        types += typeOf(Json.encodeToJsonElement(RestSchema.AgentRunEvent.serializer(), frame.event))
                        if (types.size - 1 == failsAt) throw HandlerFailure()
                        when (val event = frame.event) {
                            is RestSchema.AgentDoneEvent -> reason = event.reason.value
                            is RestSchema.AgentErrorEvent -> surfaced += event.code.value
                            is RestSchema.AgentBudgetExhaustedEvent -> surfaced += "budget-exhausted"
                            else -> Unit
                        }
                    }
                }

                "collaboration" -> {
                    client.studio.collab.getItineraryEvents("it").collect { frame ->
                        types +=
                            typeOf(Json.encodeToJsonElement(RestSchema.CollabStreamEvent.serializer(), frame.event))
                        if (types.size - 1 == failsAt) throw HandlerFailure()
                    }
                }

                else -> {
                    throw AssertionError("no stream is called `$stream`")
                }
            }
            ended = true
        } catch (error: MountayaException) {
            failure = error
        } catch (handler: HandlerFailure) {
            // The handler's own failure ends the reading, and is no failure of the stream.
        }
        return Followed(types, reason, surfaced, ended, failure)
    }

    private fun typeOf(written: kotlinx.serialization.json.JsonElement): String =
        ((written as? JsonObject)?.get("type") as? JsonPrimitive)?.contentOrNull
            ?: throw AssertionError("an event writes no type")
}
