package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ItineraryResourceEngine resolves an itinerary by its id into what an element draws: the geometry its
 * profile walks, the figures the server stated, its legs and its waypoints, shown all at once
 * (`geo.data.itinerary-resource`). Its answers are its cache's, shared with every engine handed that
 * cache, so a profile and a table over one itinerary make one read between them
 * ([ItineraryResourceCache]).
 *
 * It is idle with no id, and an empty id is none, since it is the id a binding holds while it holds no
 * itinerary. It is resolving, showing nothing, until the answer to its question lands, then shows every
 * projection at once, and a failure shows nothing but what its source threw. A new question, another
 * id, surface or cache, shows nothing of the one before it, since another itinerary drawn under it would
 * read as this one, and an answer to a question it no longer asks never lands, a failure of one
 * included. A question whose answer its cache holds is shown from it at once, before the call that asked
 * returns, and a question asked again asks nothing. An invalidation of what it watches asks again, and it
 * shows what it showed until the new answer lands.
 *
 * ```kotlin
 * val profile = ItineraryResourceEngine(itineraries, itineraryId = "itin_abc")
 * profile.state.collect { resource ->
 *     if (resource is ItineraryResource.Resolved) chips = resource.itinerary.legs.map { it.name }
 * }
 * ```
 *
 * It asks as soon as it is handed a question, by its constructor as by [configure], and launches
 * nothing of its own: each read runs in its cache's scope. It never cancels a read, since an answer is
 * shared, and holds no clock. It is driven from one thread, its cache's, and each command has its effect
 * before it returns. [state] holds a state only where it differs from the one before it. Common Kotlin
 * has no weak reference, so its cache keeps it watching until it is closed: an app closes an engine it
 * lets go of, as a view's `onDispose` or a `ViewModel`'s `onCleared` does, and a closed engine is inert.
 * It is an `AutoCloseable`, as every engine is, so `use` closes it.
 */
public class ItineraryResourceEngine(
    cache: ItineraryResourceCache,
    itineraryId: String? = null,
    surface: RestSchema.ReadSurface? = null,
) : AutoCloseable {
    private val held = MutableStateFlow<ItineraryResource>(ItineraryResource.Idle)

    /** What the element shows, [ItineraryResource.Idle] until it is handed an id. */
    public val state: StateFlow<ItineraryResource> = held.asStateFlow()

    /** The cache the itinerary is read through, which the engine watches. */
    private var cache: ItineraryResourceCache = cache

    /** The id asked about, as the engine was handed it. */
    private var itineraryId: String? = itineraryId

    /** The surface asked about, null for the one the credential reads by default. */
    private var surface: RestSchema.ReadSurface? = surface

    /** The ticket of the read whose answer the state waits for, null where it waits for none. */
    private var waiting: Int? = null
    private var nextTicket = 0
    private var closed = false

    init {
        cache.watch(this)
        ask(keepingShown = false)
    }

    /**
     * Hands the engine a question: the itinerary [itineraryId] names on [surface], read through [cache]. It asks where
     * the question differs from the one held, by its cache, its id or its surface, an empty id being none, and does
     * nothing where it is the same.
     *
     * [surface] is null for the surface the credential reads by default, the draft for a member of its
     * organization, and `ReadSurface.Published` for the published snapshot.
     */
    public fun configure(
        cache: ItineraryResourceCache,
        itineraryId: String?,
        surface: RestSchema.ReadSurface? = null,
    ) {
        if (closed) return
        val unchanged =
            cache === this.cache && asked(this.itineraryId) == asked(itineraryId) && this.surface == surface
        if (unchanged) return
        if (cache !== this.cache) {
            this.cache.unwatch(this)
            cache.watch(this)
            this.cache = cache
        }
        this.itineraryId = itineraryId
        this.surface = surface
        ask(keepingShown = false)
    }

    /**
     * Stops the engine for good: no answer lands after it, its cache asks nothing more for it, and every later command
     * does nothing. The state stays as it was. Closing twice does nothing more.
     */
    override fun close() {
        if (closed) return
        closed = true
        waiting = null
        cache.unwatch(this)
    }

    /**
     * landed acts on how the read the engine asked under [ticket] ended, where the state still waits for
     * it: an answer is shown, and a failure shows nothing.
     */
    internal fun landed(
        ticket: Int,
        outcome: ItineraryResourceCache.Outcome,
    ) {
        if (closed || ticket != waiting) return
        waiting = null
        held.value =
            when (outcome) {
                is ItineraryResourceCache.Outcome.Answered -> ItineraryResource.Resolved(outcome.resolved)
                is ItineraryResourceCache.Outcome.Failed -> ItineraryResource.Failed(outcome.cause)
            }
    }

    /**
     * revalidate asks again where an invalidation of the itinerary [dropped] names, or of every itinerary
     * where it is null, dropped what the engine asks about, showing what it shows until the new answer
     * lands.
     */
    internal fun revalidate(dropped: String?) {
        if (closed) return
        val asked = asked(itineraryId) ?: return
        if (dropped != null && dropped != asked) return
        ask(keepingShown = true)
    }

    /**
     * ask asks the cache the question held: idle where it names no id, shown at once where the cache holds
     * its answer, and otherwise waited for, showing nothing, or, where [keepingShown], what it shows.
     */
    private fun ask(keepingShown: Boolean) {
        val id = asked(itineraryId)
        if (id == null) {
            waiting = null
            held.value = ItineraryResource.Idle
            return
        }
        val ticket = nextTicket++
        // Waited for before the cache is asked: on a dispatcher that runs a read at once, as
        // `Dispatchers.Main.immediate` does on its own thread, a source that answers without suspending lands
        // within the asking, and an answer landing for a ticket not yet waited for would be lost.
        waiting = ticket
        val resolved = cache.read(id, surface, this, ticket)
        if (resolved != null) {
            waiting = null
            held.value = ItineraryResource.Resolved(resolved)
            return
        }
        if (waiting == ticket && !keepingShown) held.value = ItineraryResource.Resolving
    }

    /** The id a question asks about: none for no id and for an empty one. */
    private fun asked(itineraryId: String?): String? = itineraryId?.takeIf { it.isNotEmpty() }
}
