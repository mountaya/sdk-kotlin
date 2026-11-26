package com.mountaya.geo

/**
 * HoveredItinerary is what a pointer over a drawn itinerary is over (`hoveredItinerary`): a leg, a place,
 * both or neither, which a data element beside the map lights its row by. Two are equal where both ids are.
 */
public class HoveredItinerary(
    /** The id of the leg under the pointer, or null where it is over none. */
    public val segmentId: String?,
    /** The id of the place under the pointer, or null where it is over none. */
    public val waypointId: String?,
) {
    override fun equals(other: Any?): Boolean =
        other is HoveredItinerary && other.segmentId == segmentId && other.waypointId == waypointId

    override fun hashCode(): Int = segmentId.hashCode() * 31 + waypointId.hashCode()

    override fun toString(): String = "HoveredItinerary(segmentId=$segmentId, waypointId=$waypointId)"
}
