package com.mountaya.geo

/** WaypointTableColumn is one column a waypoint table draws for itself (`geo.profile.waypoint-table-cells`). */
public enum class WaypointTableColumn(
    /** The column as `geo/profile-waypoint-table-cells.json` spells it. */
    public val id: String,
) {
    /** The place's glyph and name. */
    Waypoint("waypoint"),

    /** The whole itinerary's sparkline with the stretch reaching the place lit. */
    Profile("profile"),

    /** The running distance to the place. */
    Distance("distance"),

    /** The climb and loss since the previous place. */
    Elevation("elevation"),

    /** The place's altitude. */
    Altitude("altitude"),

    /** The services the place offers. */
    Amenities("amenities"),

    /** The place's cut-off, as the caller's formatter writes it. */
    Cutoff("cutoff"),

    /** The fields the place's author published. */
    Properties("properties"),
}
