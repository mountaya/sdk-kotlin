package com.mountaya.client

import com.mountaya.client.generated.RestSchema

/**
 * readingAsUser answers this collaboration stream read as the user [userId] names, for a reader whose
 * session token does not tell the client whom it names: a plain token, or a function's.
 *
 * On an itinerary's or a zone's editor stream the reader's own `presence.join`, which the API sends
 * as that user's first stream opens, is no progress, so a reader told its user reopens a stream
 * that delivered only its snapshots and that join on the schedule, as `sdk/SPEC.md` section 12
 * has it, rather than at once and forever. The user a minted session or a session source names
 * wins over the one given here, and a reader that can name neither reads every `presence.join` as
 * progress. An organization's list stream reads every `presence.join` as progress whatever user is
 * named. A second call replaces the user the first named, and users are compared by their Unicode
 * scalars, exactly.
 */
public fun EventStream<RestSchema.CollabStreamEvent>.readingAsUser(
    userId: String,
): EventStream<RestSchema.CollabStreamEvent> = readingAs(userId)
