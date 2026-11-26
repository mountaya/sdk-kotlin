package com.mountaya.client

/**
 * EventRecord is one record of a Server-Sent Events stream that carried data: its `event:` name,
 * its `data:` lines joined by a line feed, and its own `id:`.
 *
 * It is what a stream opened by an operation's declaration hands on, since the client knows no
 * vocabulary for one this build prints no method for.
 */
public class EventRecord(
    /**
     * The record's `event:` name, or null where it names none. A record that names none is not read
     * as `message`: the API names every record it writes.
     */
    public val event: String?,
    /** The record's `data:` lines, joined by a line feed. */
    public val data: String,
    /**
     * The record's own `id:`, or null where it names none: the position a stream resumes from
     * carries over a record that names none, and the record's own id does not.
     */
    public val id: String?,
) {
    override fun equals(other: Any?): Boolean =
        other is EventRecord && other.event == event && other.data == data && other.id == id

    override fun hashCode(): Int = ((event?.hashCode() ?: 0) * 31 + data.hashCode()) * 31 + (id?.hashCode() ?: 0)

    override fun toString(): String = "EventRecord(event=$event, id=$id, data=$data)"
}
