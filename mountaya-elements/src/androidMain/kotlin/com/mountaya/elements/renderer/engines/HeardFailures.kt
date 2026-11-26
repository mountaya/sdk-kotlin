package com.mountaya.elements

/**
 * HeardFailures carries the failures the request hook hears for one map, on OkHttp's threads, to the main thread a
 * batch at a time: the first failure heard while no hop waits posts one, through [post], and that hop hands [tell]
 * every failure heard until it runs, each once and in the order heard. A storm of refused tiles heard while the main
 * thread draws a frame is one hop, never one a failure.
 *
 * The hop is posted as the first failure of its batch is heard, never held for a later frame, so a failure still
 * reaches the main thread before anything MapLibre posts there once it has read the answer: the failure to load a
 * style above all, which the renderer reads after the style's own failure.
 *
 * It holds nothing of the renderer: the process's request hook holds it for as long as the map holds the hook, and
 * [tell] reaches the renderer weakly.
 */
internal class HeardFailures(
    private val post: (Runnable) -> Unit,
    private val tell: (Failure) -> Unit,
) {
    /**
     * Failure is one failed request the hook heard: its address, the status its answer carried or null where none
     * came, whether it left under a credential the client no longer sends, and what the read of its credential threw,
     * where it never left for want of one.
     */
    class Failure(
        val url: String,
        val status: Int?,
        val superseded: Boolean,
        val cause: Throwable?,
    )

    /** Guards [waiting] and [isPosted], written on OkHttp's threads and on the main thread. */
    private val lock = Any()

    /** The failures heard since the last hop took its batch, in the order heard. */
    private var waiting = ArrayList<Failure>()

    /** Whether a hop is posted that has not taken its batch yet. */
    private var isPosted = false

    /** How many hops were posted, one a batch, which a test reads. */
    @Volatile
    var hops: Int = 0
        private set

    /** Hears [failure], on any thread: it joins the batch of the hop posted already, or posts one. */
    fun hear(failure: Failure) {
        val wakes: Boolean
        synchronized(lock) {
            waiting.add(failure)
            wakes = !isPosted
            if (wakes) {
                isPosted = true
                hops += 1
            }
        }
        if (wakes) post(Runnable(::drain))
    }

    /** Takes the batch heard so far and tells each of its failures, in the order heard, on the hop's own thread. */
    private fun drain() {
        val batch: List<Failure>
        synchronized(lock) {
            batch = waiting
            waiting = ArrayList()
            isPosted = false
        }
        for (failure in batch) tell(failure)
    }
}
