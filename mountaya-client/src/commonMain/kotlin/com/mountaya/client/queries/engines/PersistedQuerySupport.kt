package com.mountaya.client

/**
 * PersistedQuerySupport is what a transport knows, for its lifetime, of whether the server takes
 * automatic persisted queries, and so what each operation's first attempt sends.
 *
 * A declined protocol is remembered for the transport's lifetime, so it is not probed again. A
 * digest the server asked to have registered, and a digest that could not be computed, are
 * remembered by nothing: memoising a failure turns one bad moment into a permanent opt-out, so the
 * next operation computes its digest and probes again. The transport that holds it is its one
 * writer.
 */
internal class PersistedQuerySupport {
    /**
     * Whether the server answered that it does not implement persisted queries. It only ever turns
     * true, and calls on several threads read it, so every one reads the latest.
     */
    @kotlin.concurrent.Volatile
    var isDeclined: Boolean = false
        private set

    /**
     * Returns what an operation's first attempt sends: its digest alone where the digest was
     * computed and the protocol is not declined, and its document alone otherwise. [digest] is null
     * where the digest could not be computed, which sends the document in full rather than a
     * substitute the server would refuse.
     */
    fun firstAttempt(digest: String?): PersistedQueryAttempt =
        if (digest == null || isDeclined) {
            PersistedQueryAttempt(digest = null, sendsDocument = true)
        } else {
            PersistedQueryAttempt(digest = digest, sendsDocument = false)
        }

    /**
     * Returns what the [errors] that answered [attempt] mean, and declines the protocol for the rest
     * of the transport's lifetime where they say it is not implemented.
     */
    fun read(
        errors: List<ErrorEntry>,
        attempt: PersistedQueryAttempt,
    ): PersistedQueryRefusal? {
        val refusal = readPersistedQueryRefusal(errors, attempt)
        if (refusal == PersistedQueryRefusal.Unsupported) isDeclined = true
        return refusal
    }
}
