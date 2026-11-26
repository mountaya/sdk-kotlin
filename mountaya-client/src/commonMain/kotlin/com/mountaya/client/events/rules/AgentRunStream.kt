package com.mountaya.client

import com.mountaya.client.generated.RestSchema

/**
 * AgentRunStream is the reading of an agent run's event stream.
 *
 * It ends on `done`, and only on `done`, which carries how the run ended: an `error` or a
 * `budget-exhausted` is published immediately before it and is handed on and read past, and
 * `awaiting-input` ends a turn and not the run. Every event moves the position a reopen resumes
 * from, and an event this build does not know is passed over.
 */
internal object AgentRunStream {
    /**
     * Returns the part [event] plays, or null for one this build does not know.
     *
     * A member the contract adds is printed as a class of its own, and Kotlin has no exhaustive
     * `when` over an open union to stop the build until its part is decided, so it is ordinary,
     * the part every member but `done` and `reconnect` plays: handed on, never an end. Its own test
     * names the part of every member printed today.
     */
    fun role(event: RestSchema.AgentRunEvent): EventRole? =
        when (event) {
            is RestSchema.AgentRunEvent.Unknown -> null
            is RestSchema.AgentDoneEvent -> EventRole.end
            is RestSchema.AgentReconnectEvent -> EventRole.reconnect
            else -> EventRole.ordinary
        }
}
