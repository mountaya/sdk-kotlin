package com.mountaya.client

/** The media type of a Server-Sent Events stream. */
internal const val EVENT_STREAM_MEDIA_TYPE = "text/event-stream"

/**
 * StreamRefusal refuses a buffered request that asks for an event stream.
 *
 * A stream has no end for a buffered read to wait on, so a request whose `Accept` names
 * `text/event-stream` among its media ranges, in any letter case and whatever parameters follow
 * it, is refused before its first attempt and nothing is sent: a stream is opened through the
 * stream methods, which read it as one.
 */
internal object StreamRefusal {
    /** Refuses [headers] whose `Accept` asks for an event stream, with [ConfigurationException]. */
    fun check(headers: Map<String, String>) {
        val accept = HeaderLookup.value("Accept", headers) ?: return
        if (!asksForStream(accept)) return
        throw ConfigurationException(
            "This request asks for `text/event-stream`, which has no end for a buffered call to read to: " +
                "open it with `events`, or with the operation's own stream method.",
        )
    }

    /** Whether an `Accept` value names an event stream among its ranges. */
    fun asksForStream(accept: String): Boolean =
        accept.split(',').any { range ->
            val type = range.substringBefore(';').trim(' ', '\t')
            HeaderLookup.asciiLowercase(type) == EVENT_STREAM_MEDIA_TYPE
        }
}
