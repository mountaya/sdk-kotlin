package com.mountaya.elements

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * QueryAbort abandons the query a call of the map runs, once the map pulls it: a re-run of the result asking
 * again, its removal, or a clear of its kind (`map.analysis.requests`).
 *
 * The call races its query against the abort in a scope of its own coroutine, so the query runs in the
 * caller's coroutine, and is cancelled as the abort is pulled, the call throwing a `CancellationException`.
 */
internal class QueryAbort {
    /** Completed once the abort is pulled. */
    private val pulled = CompletableDeferred<Unit>()

    /** Abandons the query, at once. */
    fun pull() {
        pulled.complete(Unit)
    }

    /**
     * Runs [query] in the calling coroutine and answers what it answers, or throws a `CancellationException`
     * where the abort is pulled first, the query cancelled. An abort pulled once the query answered changes
     * nothing.
     */
    suspend fun <Answer> race(query: suspend () -> Answer): Answer =
        coroutineScope {
            val answer = async { query() }
            val abort =
                launch {
                    pulled.await()
                    answer.cancel()
                }
            try {
                answer.await()
            } finally {
                abort.cancel()
            }
        }
}
