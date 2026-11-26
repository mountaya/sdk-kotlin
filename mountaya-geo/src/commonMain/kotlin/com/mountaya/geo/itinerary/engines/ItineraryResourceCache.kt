package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

/**
 * ItineraryResourceCache is the answers about itineraries that the elements reading through one client
 * share: one read for every element asking about one itinerary on one surface, whatever their number
 * (`geo.data.itinerary-cache`).
 *
 * An app makes one per client, over that client's [ItinerarySource], and hands it to every
 * [ItineraryResourceEngine] that reads through the client. Answers are shared by cache, itinerary and
 * surface, so two clients, whose credentials may see two itineraries under one id, never share one. A
 * Kotlin function has no identity an app can be held to keying answers by, so the cache an app makes is
 * what holds its client's answers, and they go with it.
 *
 * ```kotlin
 * val itineraries = ItineraryResourceCache(viewModelScope) { id, surface -> readItineraryWithYourClient(id, surface) }
 * val profile = ItineraryResourceEngine(itineraries, itineraryId = "itin_abc")
 * // Once the app has saved the itinerary, every element over it reads it again.
 * itineraries.invalidate(itineraryId = "itin_abc")
 * ```
 *
 * An element asking a question already asked joins its read, or is shown its answer, and asks nothing.
 * The [capacity] answers read most recently are kept, a read in flight among them, the one read least
 * recently dropped past it, and a read dropped while in flight still lands for the elements waiting on
 * it. A failure is dropped at once, so the next question asks again, and [invalidate] drops answers the
 * app knows are stale and asks again for every element watching one, each showing what it showed until
 * the new answer lands.
 *
 * Each read is asked in a coroutine of its own in [scope], the caller's, whose dispatcher is the thread
 * the cache and its engines are driven from, so an answer lands there: `viewModelScope` on Android, a
 * test's own scope under `runTest`. It holds no lock. No read is cancelled while [scope] stands, since an
 * answer is shared and an element that stopped waiting for one would cancel it for every element still
 * waiting; cancelling [scope] cancels every read in flight, whose answers then land for nobody.
 */
public class ItineraryResourceCache(
    private val scope: CoroutineScope,
    private val source: ItinerarySource,
) {
    /** Outcome is how a read ended: the itinerary's projections, or what its source threw. */
    internal sealed interface Outcome {
        /** A read whose source answered, projected once for every engine it lands for. */
        class Answered(
            val resolved: ResolvedItinerary,
        ) : Outcome

        /** A read whose source threw [cause]. */
        class Failed(
            val cause: Throwable,
        ) : Outcome
    }

    /** One answer within a cache: an itinerary on one surface. */
    private data class Key(
        val itineraryId: String,
        val surface: RestSchema.ReadSurface?,
    )

    /** What a cache holds for one key: a read in flight, by its number, or its answer. */
    private sealed interface Entry {
        class Reading(
            val number: Int,
        ) : Entry

        class Answered(
            val resolved: ResolvedItinerary,
        ) : Entry
    }

    /** An engine waiting on a read's answer, and the ticket it asked under. */
    private class Waiter(
        val engine: ItineraryResourceEngine,
        val ticket: Int,
    )

    /** One read in flight: the key it reads, and every engine waiting on its answer. */
    private class Flight(
        val key: Key,
        val waiting: MutableList<Waiter>,
    )

    private val entries = HashMap<Key, Entry>()

    /** Every key the cache holds, the one read least recently first. */
    private val recency = ArrayList<Key>()

    /** Every read in flight, by its number, whether or not the cache still keeps it. */
    private val flights = HashMap<Int, Flight>()
    private var nextFlight = 0

    /** Every engine watching an answer of the cache, in the order each began to. */
    private val watchers = ArrayList<ItineraryResourceEngine>()

    /**
     * Drops what the cache holds of the itinerary [itineraryId] names, on every surface, or every answer it holds where
     * [itineraryId] is null, and asks again for every engine watching what it dropped, in the order each began to
     * watch, each showing what it showed until the new answer lands. The engines over one itinerary on one surface make
     * one read between them.
     *
     * An app calls it once it has changed an itinerary (a save, a publish), so every element over it
     * shows the itinerary as it now stands. A read in flight it drops still lands for the engines waiting
     * on it, and is not kept.
     */
    public fun invalidate(itineraryId: String? = null) {
        if (itineraryId == null) {
            entries.clear()
            recency.clear()
        } else {
            entries.keys.removeAll { it.itineraryId == itineraryId }
            recency.removeAll { it.itineraryId == itineraryId }
        }
        for (watcher in watchers.toList()) watcher.revalidate(itineraryId)
    }

    /**
     * read answers a question from the cache: the answer where it holds one, which is then the answer
     * read most recently, and null where [engine] is to wait, under [ticket], for a read it joins or one
     * sent for it now.
     */
    internal fun read(
        itineraryId: String,
        surface: RestSchema.ReadSurface?,
        engine: ItineraryResourceEngine,
        ticket: Int,
    ): ResolvedItinerary? {
        val key = Key(itineraryId, surface)
        val waiter = Waiter(engine, ticket)
        val entry = entries[key]
        if (entry is Entry.Answered) {
            touch(key)
            return entry.resolved
        }
        if (entry is Entry.Reading) {
            touch(key)
            flights[entry.number]?.waiting?.add(waiter)
            return null
        }
        val number = nextFlight++
        flights[number] = Flight(key, mutableListOf(waiter))
        entries[key] = Entry.Reading(number)
        recency += key
        while (recency.size > capacity) entries.remove(recency.removeAt(0))
        scope.launch {
            val outcome =
                try {
                    Outcome.Answered(ResolvedItinerary(source.read(itineraryId, surface)))
                } catch (failure: Throwable) {
                    // A read the scope cancelled lands for nobody; any other failure is the read's.
                    currentCoroutineContext().ensureActive()
                    Outcome.Failed(failure)
                }
            land(number, outcome)
        }
        return null
    }

    /** watch has the cache ask again for [engine] when an invalidation drops what it watches. */
    internal fun watch(engine: ItineraryResourceEngine) {
        if (watchers.none { it === engine }) watchers += engine
    }

    /** unwatch has the cache ask nothing more for [engine]. */
    internal fun unwatch(engine: ItineraryResourceEngine) {
        watchers.removeAll { it === engine }
    }

    /** Moves [key] to the most recent end, read again. */
    private fun touch(key: Key) {
        recency.remove(key)
        recency += key
    }

    /**
     * land keeps a read's answer where the cache still holds the read, drops its failure at once, and
     * tells every engine waiting on it, each under the ticket it asked with.
     */
    private fun land(
        number: Int,
        outcome: Outcome,
    ) {
        val flight = flights.remove(number) ?: return
        // An invalidation or the bound may have dropped the read, and a later read replaced it: the entry
        // is then another's, and is left as it is.
        val entry = entries[flight.key]
        if (entry is Entry.Reading && entry.number == number) {
            when (outcome) {
                is Outcome.Answered -> {
                    entries[flight.key] = Entry.Answered(outcome.resolved)
                }

                is Outcome.Failed -> {
                    entries.remove(flight.key)
                    recency.remove(flight.key)
                }
            }
        }
        for (waiter in flight.waiting) waiter.engine.landed(waiter.ticket, outcome)
    }

    /** The bounds every cache holds to. */
    public companion object {
        /** How many answers a cache keeps: the itineraries it read most recently, each on one surface, a read in flight counted. */
        public val capacity: Int = 16
    }
}
