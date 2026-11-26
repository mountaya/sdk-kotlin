package com.mountaya.geo

/**
 * ItineraryResource is what an element handed an itinerary's id shows: nothing while it has no id,
 * nothing while its itinerary is read, every projection at once once it is, and nothing but why where it
 * could not be read (`geo.data.itinerary-resource`).
 *
 * No projection is ever shown without the others, and none of an itinerary is shown while another is
 * asked about. Two idles are one state, two resolvings one, and two resolved states one where they show
 * the same itinerary; a failure is equal to itself alone, since what a source threw is no value to
 * compare, so two failures are two states.
 */
public sealed interface ItineraryResource {
    /** Idle is an element with no itinerary to show, which asks nothing. */
    public object Idle : ItineraryResource {
        override fun toString(): String = "Idle"
    }

    /** Resolving is an element whose itinerary is being read, showing nothing until it lands. */
    public object Resolving : ItineraryResource {
        override fun toString(): String = "Resolving"
    }

    /** Resolved is an element showing its [itinerary], every projection at once. */
    public class Resolved(
        /** The itinerary shown, every projection of one answer. */
        public val itinerary: ResolvedItinerary,
    ) : ItineraryResource {
        override fun equals(other: Any?): Boolean = other is Resolved && other.itinerary == itinerary

        override fun hashCode(): Int = itinerary.hashCode()

        override fun toString(): String = "Resolved($itinerary)"
    }

    /** Failed is an element whose itinerary could not be read, showing nothing, with what its source threw. */
    public class Failed(
        /** What the source threw. */
        public val cause: Throwable,
    ) : ItineraryResource {
        override fun toString(): String = "Failed($cause)"
    }
}
