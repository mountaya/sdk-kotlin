package com.mountaya.client

/**
 * EventFrame is one frame of an event stream: the event it carries, and its own id.
 *
 * A stream's reader reopens it where `sdk/SPEC.md` section 12 says, sending as `Last-Event-ID`
 * the id of the last frame it handed on whose id moves the position, so a collector reads the
 * frames of every open in turn.
 */
public class EventFrame<out Event>(
    /** The frame's `id:` field, or null where the frame carried none. */
    public val id: String?,
    /** The frame's event, decoded from its `data:` field. */
    public val event: Event,
) {
    override fun equals(other: Any?): Boolean = other is EventFrame<*> && other.id == id && other.event == event

    override fun hashCode(): Int = (id?.hashCode() ?: 0) * 31 + (event?.hashCode() ?: 0)

    override fun toString(): String = "EventFrame(id=$id, event=$event)"
}
