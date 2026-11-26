package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry

/**
 * ItineraryLeg is one leg of an itinerary as an element draws it: a segment, or a listed group of
 * segments collapsed into one, with the figures the server stated for it as its own section. A table's
 * rows and a profile's chips are an itinerary's legs, `itinerarySegments`.
 *
 * Every figure is the server's, computed once over the full track, and keeps the wire's type: an
 * ascent is an `Int`. None is a running total, which the rows of a table accumulate, and none is a
 * difficulty, which a leg does not fold, so a leg states neither.
 *
 * Two are equal where they hold the same texts, the same geometry and the same figures, each number as
 * the same number, `NaN` the same as `NaN` and 0 another than -0, as every value of the geo layer
 * compares.
 */
public class ItineraryLeg(
    /** The segment's own id, or `SEGMENT_GROUP_ID_PREFIX` then the group's id for a collapsed group. */
    public val id: String,
    /**
     * The ids of the segments a collapsed group stands for, in chain order, and empty for a leg that
     * stands for itself alone. A map names the segment under its pointer, never the group, so a leg is
     * matched on its members as well as on its own id.
     */
    public val memberIds: List<String> = emptyList(),
    /** The segment's name, or the group's. */
    public val name: String,
    /** The segment's colour, or the group's, painted as given, null where it states none. */
    public val color: String? = null,
    /**
     * The segment's geometry as it stores it, or the lines of a group's members, null where it has
     * none: its span on a profile's axis is the length of its own lines.
     */
    public val geometry: GeoJsonGeometry? = null,
    /** The leg's own distance along the ground, in meters. */
    public val sectionDistance: Double? = null,
    /** The meters the leg climbs. */
    public val sectionAscent: Int? = null,
    /** The meters the leg loses, stated positive, as the wire states it. */
    public val sectionDescent: Int? = null,
    /** The seconds the leg is expected to take. */
    public val duration: Double? = null,
    /** The lowest altitude on the leg, in meters. */
    public val altitudeMin: Int? = null,
    /** The highest altitude on the leg, in meters. */
    public val altitudeMax: Int? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is ItineraryLeg &&
            other.id == id &&
            other.memberIds == memberIds &&
            other.name == name &&
            other.color == color &&
            other.geometry == geometry &&
            sameNumberOrNull(other.sectionDistance, sectionDistance) &&
            other.sectionAscent == sectionAscent &&
            other.sectionDescent == sectionDescent &&
            sameNumberOrNull(other.duration, duration) &&
            other.altitudeMin == altitudeMin &&
            other.altitudeMax == altitudeMax

    override fun hashCode(): Int =
        listOf(
            id,
            memberIds,
            name,
            color,
            geometry,
            sectionDistance,
            sectionAscent,
            sectionDescent,
            duration,
            altitudeMin,
            altitudeMax,
        ).hashCode()

    override fun toString(): String =
        "ItineraryLeg(id=$id, memberIds=$memberIds, name=$name, color=$color, geometry=$geometry, " +
            "sectionDistance=$sectionDistance, sectionAscent=$sectionAscent, sectionDescent=$sectionDescent, " +
            "duration=$duration, altitudeMin=$altitudeMin, altitudeMax=$altitudeMax)"
}
