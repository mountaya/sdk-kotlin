package com.mountaya.elements

import com.mountaya.client.ConnectionException
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * OrganizationReads is every map's read of its organization, one per publishable key for as long as the process lives
 * (`map.organization.read`): a read in flight is waited on rather than sent again, an answer is kept and serves every
 * map whose client holds that key, a map made later included, and a read that fails is kept by nobody, so every load
 * waiting on it fails and the next one reads again. A read is kept by its key and the REST API's origin its client
 * sends to, so a client of another origin, a staging API's, never shares an answer with one of Mountaya's.
 *
 * A read is sent in a coroutine of its own, on the dispatcher of the load that asked first and in no job of its, so a
 * load that stops waiting, its map closed or its caller gone, never cancels the read for the loads still waiting on
 * it. It ends within the transport's own budgets, as every call does. Beside it runs the read's one-attempt window, the
 * client's budget for one attempt of `postOrganizationLookup`: once that has passed without an answer the read is
 * overdue, which a map's watermark draws on while the read's further attempts go on (`geo.organization.watermark`).
 */
internal object OrganizationReads {
    /** What one read is kept by: the publishable key and the origin it is sent to. */
    private class Key(
        val publishableKey: String,
        val origin: String,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Key && other.publishableKey == publishableKey && other.origin == origin

        override fun hashCode(): Int = publishableKey.hashCode() * 31 + origin.hashCode()
    }

    /**
     * Read is one read as a load waits on it: its [outcome], the organization or what the read threw, and [overdue],
     * completed once one attempt's budget has passed without an answer, and never where the read answered first.
     */
    class Read(
        val outcome: Deferred<RestSchema.Organization>,
        val overdue: Deferred<Unit>,
    )

    /** What is kept for one key: a read in flight, or its answer. */
    private sealed interface Entry {
        class Reading(
            val read: Read,
        ) : Entry

        class Answered(
            val organization: RestSchema.Organization,
        ) : Entry
    }

    private val lock = Mutex()
    private val entries = mutableMapOf<Key, Entry>()

    /**
     * Answers the read of the organization [publishableKey] belongs to, as [client] reads it, through
     * `postOrganizationLookup` with the key in its body and no credential sent: the answer kept, the read in flight, or
     * a read sent now.
     */
    @OptIn(InternalMountayaApi::class)
    suspend fun request(
        client: Mountaya,
        publishableKey: String,
    ): Read {
        val key = Key(publishableKey, client.baseUrls.rest.toString())
        val context = currentCoroutineContext().minusKey(Job)
        return lock.withLock {
            when (val entry = entries[key]) {
                is Entry.Answered -> {
                    Read(CompletableDeferred(entry.organization), CompletableDeferred())
                }

                is Entry.Reading -> {
                    entry.read
                }

                null -> {
                    val outcome = CompletableDeferred<RestSchema.Organization>()
                    val overdue = CompletableDeferred<Unit>()
                    val read = Read(outcome, overdue)
                    entries[key] = Entry.Reading(read)
                    val budget = client.organizationLookupBudget
                    CoroutineScope(context).launch {
                        val window =
                            launch {
                                delay(budget)
                                overdue.complete(Unit)
                            }
                        send(client, key, outcome)
                        window.cancel()
                    }
                    read
                }
            }
        }
    }

    /**
     * Sends the read of [key] and settles [outcome] with how it ended, keeping an answer and nothing else. The read runs
     * in no caller's job, so a `CancellationException` it meets is never a caller's: it fails the read as a request that
     * never arrived, a `ConnectionException` carrying it, so every load waiting on it fails rather than ending as if its
     * own caller had left.
     */
    private suspend fun send(
        client: Mountaya,
        key: Key,
        outcome: CompletableDeferred<RestSchema.Organization>,
    ) {
        val organization =
            try {
                client.studio.organizations
                    .postOrganizationLookup(RestSchema.OrganizationLookupParams(publishable_key = key.publishableKey))
                    .data
            } catch (failure: Throwable) {
                lock.withLock { entries.remove(key) }
                outcome.completeExceptionally(
                    if (failure is CancellationException) ConnectionException(cause = failure) else failure,
                )
                return
            }
        lock.withLock { entries[key] = Entry.Answered(organization) }
        outcome.complete(organization)
    }
}
