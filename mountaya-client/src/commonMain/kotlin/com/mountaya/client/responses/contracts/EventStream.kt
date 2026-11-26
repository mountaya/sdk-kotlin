package com.mountaya.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

/**
 * EventStream is what a printed stream method returns: the cold flow of an operation's frames,
 * read by the rules of `sdk/SPEC.md` section 12, which say when the stream is over, when it is
 * opened again, after how long and from which record.
 *
 * Collecting it opens the stream, a second collection opens a second one, and a collector that is
 * cancelled or stops early closes it. It is a `Flow`, so every operator a flow takes applies to it;
 * what it adds is what a stream is read as, such as the user an editor stream is read as
 * (`readingAsUser`), which an operator applied before it would hide.
 */
public class EventStream<out Event> internal constructor(
    /** Opens one reading of the stream as the user named, or as none where it is null. */
    private val open: (member: String?) -> Flow<EventFrame<Event>>,
    /** The user the caller says it reads the stream as, or null where it says none. */
    internal val member: String? = null,
) : Flow<EventFrame<Event>> {
    override suspend fun collect(collector: FlowCollector<EventFrame<Event>>): Unit = open(member).collect(collector)

    /** Returns the same stream read as [member], which replaces the user named before. */
    internal fun readingAs(member: String): EventStream<Event> = EventStream(open, member)

    override fun toString(): String = "EventStream(member=$member)"
}
