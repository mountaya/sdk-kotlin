package com.mountaya.geo

/**
 * AspectCardinal is the compass direction an aspect faces, the eighth of the compass whose 45-degree sector,
 * centred on the direction's own heading, holds the bearing (`geo.terrain.aspect-cardinal`). Each is spelled by
 * its [code], the key its word takes under the elements' `readout.aspects`.
 */
public enum class AspectCardinal(
    /** The direction as the contract spells it, `NE`, the key of its word under `readout.aspects`. */
    public val code: String,
    /** The bearing the direction's sector is centred on, in degrees, 0 north and 90 east. */
    public val heading: Double,
) {
    /** The sector from 337.5 degrees to 22.5, facing north. */
    North("N", 0.0),

    /** The sector from 22.5 degrees to 67.5, facing north-east. */
    NorthEast("NE", 45.0),

    /** The sector from 67.5 degrees to 112.5, facing east. */
    East("E", 90.0),

    /** The sector from 112.5 degrees to 157.5, facing south-east. */
    SouthEast("SE", 135.0),

    /** The sector from 157.5 degrees to 202.5, facing south. */
    South("S", 180.0),

    /** The sector from 202.5 degrees to 247.5, facing south-west. */
    SouthWest("SW", 225.0),

    /** The sector from 247.5 degrees to 292.5, facing west. */
    West("W", 270.0),

    /** The sector from 292.5 degrees to 337.5, facing north-west. */
    NorthWest("NW", 315.0),
}
