package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ProfilePlace is a place an element marks on an itinerary's profile and lists in its waypoint table: where
 * it stands, given on the ground or at a distance on the axis, its name, colour and glyph, whether it divides
 * the itinerary and is pinned on the curve, the figures stated for the stretch that reaches it, and what it
 * says about itself. An itinerary's own places are its [ItineraryPlace]s, each read through the secondary
 * constructor.
 *
 * Two are equal where they hold the same placement, texts written alike, the same flags and the same
 * figures and details, as every value of the geo layer compares.
 */
public class ProfilePlace(
    /**
     * Where the place stands and its id: at a position, at every close pass of the line, or at a distance on
     * the axis (`resolveWaypoints`).
     */
    public val placement: ProfileWaypoint,
    /** The place's name, null for a bare marker. */
    public val name: String? = null,
    /** The colour the place is pinned in, painted as given, null for the profile's own. */
    public val color: String? = null,
    /** The glyph the place is pinned with, null where it names none. */
    public val glyph: RestSchema.WaypointIcon? = null,
    /** Whether the place ends a section of the itinerary, its name under the axis, null to read as [dividesRoute] reads it. */
    public val divides: Boolean? = null,
    /** Whether a pin stands for the place on the curve. */
    public val isPinned: Boolean = true,
    /**
     * The figures stated for the stretch that reaches the place, which a waypoint row states over the ones the
     * itinerary's geometry reads on its primary pass, where the place divides the itinerary
     * (`itineraryWaypointRows`).
     */
    public val metrics: ItinerarySegmentRowMetrics? = null,
    /** What the place says about itself, null where it says nothing. */
    public val details: WaypointDetails? = null,
) {
    /** Creates the place an itinerary's waypoint marks, at its position, with its name, colour, glyph and details. */
    public constructor(place: ItineraryPlace) : this(
        ProfileWaypoint.AtPosition(place.id, place.lngLat),
        name = place.name,
        color = place.color,
        glyph = place.glyph,
        details = place.details,
    )

    /** The place's own id, which its placements are keyed by. */
    public val id: String get() = placement.id

    /**
     * Whether the place ends a section of the itinerary: as [divides] states, and where it states nothing,
     * whether it has a name, since a named place ends a section and a bare marker is decoration.
     */
    public val dividesRoute: Boolean get() = divides ?: (name != null)

    override fun equals(other: Any?): Boolean =
        other is ProfilePlace &&
            other.placement == placement &&
            other.name == name &&
            other.color == color &&
            other.glyph == glyph &&
            other.divides == divides &&
            other.isPinned == isPinned &&
            other.metrics == metrics &&
            other.details == details

    override fun hashCode(): Int = listOf(placement, name, color, glyph, divides, isPinned, metrics, details).hashCode()

    override fun toString(): String =
        "ProfilePlace(placement=$placement, name=$name, color=$color, glyph=$glyph, divides=$divides, " +
            "isPinned=$isPinned, metrics=$metrics, details=$details)"
}
