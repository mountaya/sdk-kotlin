package com.mountaya.geo

/**
 * ProfileWaypoint is a waypoint a caller places on an itinerary's distance axis: given at a
 * position, it stands at every pass of the line near it, and given at a distance, it stands there.
 *
 * Two are equal where they are of one kind and hold the same id and the same numbers, as every
 * value of the geo layer compares.
 */
public sealed class ProfileWaypoint {
    /** The caller's own id, which a placement's key is built from. */
    public abstract val id: String

    /** AtPosition is a waypoint given where it stands on the ground. */
    public class AtPosition(
        override val id: String,
        /** Where it stands, `[longitude, latitude]` in degrees. */
        public val position: List<Double>,
    ) : ProfileWaypoint() {
        override fun equals(other: Any?): Boolean =
            other is AtPosition && other.id == id && sameNumbers(other.position, position)

        override fun hashCode(): Int = id.hashCode() * 31 + position.hashCode()

        override fun toString(): String = "ProfileWaypoint.AtPosition(id=$id, position=$position)"
    }

    /** AtDistance is a waypoint given where it stands on the axis already. */
    public class AtDistance(
        override val id: String,
        /** Meters from the start of the itinerary. */
        public val distance: Double,
    ) : ProfileWaypoint() {
        override fun equals(other: Any?): Boolean =
            other is AtDistance && other.id == id && sameNumber(other.distance, distance)

        override fun hashCode(): Int = id.hashCode() * 31 + distance.hashCode()

        override fun toString(): String = "ProfileWaypoint.AtDistance(id=$id, distance=$distance)"
    }
}
