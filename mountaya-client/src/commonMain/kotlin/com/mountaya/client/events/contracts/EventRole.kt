package com.mountaya.client

/** EventRole is the part an event plays in its stream. */
internal class EventRole private constructor(
    /**
     * Whether the record's `id:` becomes the position a reopen resumes from. A collaboration feed's
     * live frames carry none, and an id on one moves nothing.
     */
    val movesPosition: Boolean,
    /** Whether nothing is read after the event: an agent run's `done`. */
    val endsStream: Boolean,
    /**
     * Whether the event is the `reconnect` the API writes before it closes a stream whose
     * credential ended or that it could not keep delivering: no failure, and reopened at once.
     */
    val isReconnect: Boolean,
    /** Whether the event is a roster or lock snapshot a collaboration feed's every open begins with, which is not progress. */
    val isOpeningSnapshot: Boolean,
) {
    companion object {
        /** An event that moves the position and plays no other part. */
        val ordinary: EventRole = EventRole(true, false, false, false)

        /** A collaboration feed's live frame, which moves no position. */
        val live: EventRole = EventRole(false, false, false, false)

        /** A live frame a collaboration feed's open begins with. */
        val openingSnapshot: EventRole = EventRole(false, false, false, true)

        /** The event that ends the stream. */
        val end: EventRole = EventRole(true, true, false, false)

        /** The event that asks for a reopen at once. */
        val reconnect: EventRole = EventRole(false, false, true, false)
    }
}
