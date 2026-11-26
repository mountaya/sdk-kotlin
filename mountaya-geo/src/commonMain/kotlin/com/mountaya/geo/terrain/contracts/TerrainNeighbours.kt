package com.mountaya.geo

/**
 * TerrainNeighbours is the elevation of the ground one spacing to each side of a point, in meters:
 * north and south along its meridian, east and west along its parallel.
 *
 * Two are equal where they hold the same numbers, `NaN` the same as `NaN` and 0 another than -0, as
 * every value of the geo layer compares.
 */
public class TerrainNeighbours(
    /** The elevation one spacing to the north, in meters. */
    public val north: Double,
    /** The elevation one spacing to the south, in meters. */
    public val south: Double,
    /** The elevation one spacing to the east, in meters. */
    public val east: Double,
    /** The elevation one spacing to the west, in meters. */
    public val west: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is TerrainNeighbours &&
            sameNumber(other.north, north) &&
            sameNumber(other.south, south) &&
            sameNumber(other.east, east) &&
            sameNumber(other.west, west)

    override fun hashCode(): Int =
        ((north.hashCode() * 31 + south.hashCode()) * 31 + east.hashCode()) * 31 + west.hashCode()

    override fun toString(): String = "TerrainNeighbours(north=$north, south=$south, east=$east, west=$west)"
}
