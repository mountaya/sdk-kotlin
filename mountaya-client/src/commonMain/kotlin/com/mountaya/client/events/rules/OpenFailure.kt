package com.mountaya.client

/**
 * OpenFailure decides whether a failed open of an event stream ends it, or whether the reader
 * opens it again.
 *
 * A failed open raises the error the same answer raises on a request. A 4xx other than a 429 ends
 * the stream, because opening again repeats the answer, and so does a credential refused locally.
 * A 429, a 503, a 504 and a dropped connection reopen it. Every other 5xx is read per stream: a
 * collaboration feed reopens on each, nobody waiting on one answer, and an agent run's stream ends
 * on each, its 502 being what the API answers for a run its provider's rate limit already ended.
 */
internal object OpenFailure {
    /** Whether an open that failed with [error] ends a stream read by [rules]. */
    fun ends(
        error: MountayaException,
        rules: EventStreamRules,
    ): Boolean =
        when (error) {
            is ConnectionException -> {
                false
            }

            is ApiException -> {
                when (error.status) {
                    429, 503, 504 -> false
                    in 500..599 -> rules.endsOnServerError
                    else -> true
                }
            }

            else -> {
                true
            }
        }
}
