package com.mountaya.geo

/** SegmentTableColumn is one column a segment table draws for itself (`geo.profile.segment-table-cells`). */
public enum class SegmentTableColumn(
    /** The column as `geo/profile-segment-table-cells.json` spells it. */
    public val id: String,
) {
    /** The leg's name. */
    Segment("segment"),

    /** The whole itinerary's sparkline with the leg's own stretch lit. */
    Profile("profile"),

    /** The leg's length over the running distance. */
    Distance("distance"),

    /** The leg's climb and loss over the running climb. */
    Elevation("elevation"),

    /** The leg's altitude band. */
    Altitude("altitude"),

    /** The leg's expected duration. */
    Duration("duration"),

    /** The leg's difficulty, as the itinerary writes it. */
    Difficulty("difficulty"),
}
