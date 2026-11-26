package com.mountaya.elements

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * SlotRequests holds the requests made of a map's slots, and answers whether one still owns its slot, so a
 * load that lands late draws nothing and a call withdrawn gives the slot back (`map.slots.latest-request`).
 *
 * A slot's content arrives some time after it is asked for, a specification read by id, and a later request
 * or a clear may be made meanwhile, or the call that asked withdrawn. Each request holds a token no other
 * shares, and a load draws only while its token is its slot's latest. A Kotlin call can always be withdrawn,
 * its coroutine cancelled, so a request a later call takes the slot from is superseded rather than lost: it
 * waits for that call to draw, end or give the slot back, and draws as its own load answers once given it
 * back. A request that takes its slot for good, and a clear, end every such wait, each request finding as it
 * resumes that it lost.
 *
 * It draws nothing and names no renderer. It is driven from the engine's thread, as the engine is.
 */
internal class SlotRequests {
    /** A request a later call took its slot from, waiting for that call to settle. */
    private class Superseded(
        /** The slot the request was made for. */
        val slot: ContentSlot,
        /** The request's own token. */
        val token: Int,
        /** The token of the call that took the slot from it. */
        val by: Int,
    ) {
        /** Every wait on the request's supersession, completed as it ends. */
        val waiters = mutableListOf<CompletableDeferred<Unit>>()
    }

    /** The latest request for each slot a request is out for, by its token. */
    private val latest = mutableListOf<Pair<ContentSlot, Int>>()

    /** The last token handed out; tokens only grow, so none is ever reused. */
    private var count = 0

    /** Every request waiting for the call that took its slot to settle. */
    private val superseded = mutableListOf<Superseded>()

    /** Whether the request of [token] is still the latest for [slot]. */
    fun owns(
        slot: ContentSlot,
        token: Int,
    ): Boolean = latest.firstOrNull { it.first == slot }?.second == token

    /** Every slot a request is out for, its content drawn or not. */
    fun requestedSlots(): List<ContentSlot> = latest.map { it.first }

    /**
     * Records the request of a call that can be withdrawn, and answers its token: the request the slot held,
     * where it held one, is superseded, and waits for this one to settle.
     */
    fun claim(slot: ContentSlot): Int {
        count += 1
        latest.firstOrNull { it.first == slot }?.let { superseded += Superseded(slot, it.second, count) }
        hand(slot, count)
        return count
    }

    /** Records a request that takes its slot for good, and answers its token: every request still waiting to be given the slot back loses it. */
    fun take(slot: ContentSlot): Int {
        count += 1
        hand(slot, count)
        end(superseded.filter { it.slot == slot })
        return count
    }

    /** Ends the latest request for [slot], as the slot is cleared for good, and every wait to be given it back. */
    fun release(slot: ContentSlot) {
        latest.removeAll { it.first == slot }
        end(superseded.filter { it.slot == slot })
    }

    /** Ends the supersessions the call of [token] made, as it drew or ended: each request it took a slot from finds, as it resumes, whether it has the slot back. */
    fun settle(token: Int) {
        end(superseded.filter { it.by == token })
    }

    /**
     * Withdraws the call of [token], made for [slot], before it drew: where it still owns the slot, the slot
     * goes back to the request it took the slot from, where that request still waits, and is asked for by none
     * otherwise. Its own wait to be given the slot back, where a later call superseded it, ends as well.
     */
    fun giveBack(
        slot: ContentSlot,
        token: Int,
    ) {
        if (owns(slot, token)) {
            val taken = superseded.firstOrNull { it.by == token && it.slot == slot }
            if (taken != null) hand(slot, taken.token) else latest.removeAll { it.first == slot }
        }
        end(superseded.filter { it.token == token })
        settle(token)
    }

    /**
     * Waits, while a call that took the slot from the request of [token] has not settled, for it to settle,
     * again where another such call takes it meanwhile, and answers whether the request owns [slot] then. A wait
     * whose coroutine is cancelled ends as the request is withdrawn, and throws the cancellation.
     */
    suspend fun regains(
        slot: ContentSlot,
        token: Int,
    ): Boolean {
        while (true) {
            val record = superseded.firstOrNull { it.token == token } ?: break
            val waiter = CompletableDeferred<Unit>()
            record.waiters += waiter
            try {
                waiter.await()
            } catch (cancelled: CancellationException) {
                end(superseded.filter { it.token == token })
                throw cancelled
            }
        }
        currentCoroutineContext().ensureActive()
        return owns(slot, token)
    }

    /** Makes the request of [token] the latest for [slot]. */
    private fun hand(
        slot: ContentSlot,
        token: Int,
    ) {
        val index = latest.indexOfFirst { it.first == slot }
        if (index < 0) latest += slot to token else latest[index] = slot to token
    }

    /** Ends each of [records], completing every wait on it. */
    private fun end(records: List<Superseded>) {
        for (record in records) {
            superseded.removeAll { it.token == record.token && it.by == record.by }
            for (waiter in record.waiters) waiter.complete(Unit)
        }
    }
}
