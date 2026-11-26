package com.mountaya.elements

/**
 * WaypointEdit is a waypoint being placed on a map: the waypoint's id, where it was placed, and
 * whether the edit's magnet placed it on one of the map's active segments (`map.edit.snap`).
 *
 * It is what `MapEngine.beginWaypointEdit` opens and `MapEngine.endWaypointEdit` answers. Two are
 * equal where every member is the same, a position by the relation every Geo value compares numbers
 * by, which a `List<Double>` already compares by, and a text as it is written.
 */
public class WaypointEdit(
    /** The id of the waypoint the edit places, the app's own. */
    public val waypointId: String,
    /** Where the waypoint was placed, `[longitude, latitude]`, or null before it is. */
    public val position: List<Double>? = null,
    /** Whether the magnet moved the waypoint onto an active segment. */
    public val isSnapped: Boolean = false,
) {
    override fun equals(other: Any?): Boolean =
        other is WaypointEdit &&
            other.waypointId == waypointId &&
            other.position == position &&
            other.isSnapped == isSnapped

    override fun hashCode(): Int =
        (waypointId.hashCode() * 31 + (position?.hashCode() ?: 0)) * 31 + isSnapped.hashCode()

    override fun toString(): String = "WaypointEdit(waypointId=$waypointId, position=$position, isSnapped=$isSnapped)"
}
