package com.mountaya.client

import com.mountaya.client.generated.RestSchema

/**
 * CollaborationFeed is the reading of a collaboration feed: an itinerary's or a zone's editor
 * stream, or an organization's list stream.
 *
 * A feed has no end of its own. Its change frames are durable, carry a stream id and move the
 * position a reopen resumes from; its presence, selection, cursor, lock and draft frames are live
 * only, and an id on one moves nothing. The roster and lock snapshots every open begins with are no
 * progress, and on an editor stream neither is the `presence.join` of the reader's own member, sent
 * as that member's first stream opens.
 */
internal object CollaborationFeed {
    /**
     * The operations whose stream is an itinerary's or a zone's editor stream, by the name the
     * contract gives each. Every other stream of the vocabulary is a list stream, an organization's,
     * where every `presence.join` is progress, since section 12 sets the reader's own apart on an
     * editor stream alone. The contract names the editor streams in prose, and no fact of the model
     * carries it, so a stream a later contract adds to the vocabulary is a list stream until it is
     * written here.
     */
    val editorStreams: Set<String> = setOf("getItineraryEvents", "getZoneEvents")

    /** Returns the rules of the stream the operation named [operationId] opens: an editor stream's, or a list stream's. */
    fun rules(operationId: String): EventStreamRules =
        if (operationId in editorStreams) EventStreamRules.collaborationEditor else EventStreamRules.collaborationFeed

    /** Returns the user whose arrival [event] announces, a `presence.join`'s, or null for every other event. */
    fun arrivingMember(event: RestSchema.CollabStreamEvent): String? =
        (event as? RestSchema.CollabPresenceJoinEvent)?.user_id

    /**
     * Returns the part [event] plays, or null for one this build does not know.
     *
     * A member the contract adds is ordinary until its part is decided here, as on an agent run's
     * stream: handed on, its id, where it carries one, moving the position.
     */
    fun role(event: RestSchema.CollabStreamEvent): EventRole? =
        when (event) {
            is RestSchema.CollabStreamEvent.Unknown -> {
                null
            }

            is RestSchema.CollabPresenceSnapshotEvent, is RestSchema.CollabLockSnapshotEvent -> {
                EventRole.openingSnapshot
            }

            is RestSchema.CollabPresenceJoinEvent,
            is RestSchema.CollabPresenceLeaveEvent,
            is RestSchema.CollabLockAcquiredEvent,
            is RestSchema.CollabLockReleasedEvent,
            is RestSchema.CollabSelectionEvent,
            is RestSchema.CollabCursorEvent,
            is RestSchema.CollabDraftUpdateEvent,
            is RestSchema.CollabDraftClearEvent,
            -> {
                EventRole.live
            }

            is RestSchema.CollabReconnectEvent -> {
                EventRole.reconnect
            }

            else -> {
                EventRole.ordinary
            }
        }
}
