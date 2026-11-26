package com.mountaya.geo

/** ReadoutStat is one figure a profile's cursor readout can state (`geo.profile.cursor-readout`). */
public enum class ReadoutStat(
    /** The figure as `geo/profile-cursor-readout.json` spells it. */
    public val id: String,
) {
    /** The sample's distance from the start of the itinerary. */
    Distance("distance"),

    /** The sample's altitude. */
    Elevation("elevation"),

    /** The meters climbed from the start of the itinerary to the sample. */
    Ascent("ascent"),

    /** The meters lost from the start of the itinerary to the sample. */
    Descent("descent"),
}
