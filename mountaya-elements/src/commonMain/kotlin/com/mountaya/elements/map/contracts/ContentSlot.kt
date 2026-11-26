package com.mountaya.elements

/**
 * ContentSlot is one place a map holds content in, which draws one request at a time: the itinerary and
 * the collection asked for by id, the itineraries and zones switched on, the classic overlay, an enrichment
 * by its slug, a specification an app draws under a name of its own, a Data API result by its id, a geometry
 * slot, and the result a reader picked from a search.
 *
 * Each kind of content is a slot of its own, so drawing one never clears another, and a slot asked for
 * again draws in its place (`map.slots.latest-request`, `map.layers.keys`). Two slots are one where they
 * are of one kind and name the same.
 */
internal sealed interface ContentSlot {
    /** The itinerary drawn by id. */
    object Itinerary : ContentSlot {
        override fun toString(): String = "Itinerary"
    }

    /** The collection drawn by id. */
    object Collection : ContentSlot {
        override fun toString(): String = "Collection"
    }

    /** The itineraries of the credential's scope, switched on. */
    object Itineraries : ContentSlot {
        override fun toString(): String = "Itineraries"
    }

    /** The zones of the credential's scope, switched on. */
    object Zones : ContentSlot {
        override fun toString(): String = "Zones"
    }

    /** The classic overlay, of which a map draws one at a time. */
    object Overlay : ContentSlot {
        override fun toString(): String = "Overlay"
    }

    /** The enrichment of [slug], of which a map draws any number, stacked. */
    data class Enrichment(
        val slug: String,
    ) : ContentSlot

    /** A specification an app drew under [name], a name of its own. */
    data class Specification(
        val name: String,
    ) : ContentSlot

    /** A Data API result drawn under its [id] (`map.analysis.slots`). */
    data class Analysis(
        val id: String,
    ) : ContentSlot

    /** One of the preview, the highlight and the emphasis (`map.geometry.slots`). */
    data class Geometry(
        val slot: GeometrySlot,
    ) : ContentSlot

    /** The result a reader picked from a search (`map.search.result-highlight`). */
    object SearchHighlight : ContentSlot {
        override fun toString(): String = "SearchHighlight"
    }
}
