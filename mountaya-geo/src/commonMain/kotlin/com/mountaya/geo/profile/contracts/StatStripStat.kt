package com.mountaya.geo

/** StatStripStat is one figure the stat strip over a profile can state (`geo.profile.stat-strip`). */
public enum class StatStripStat(
    /** The figure as `geo/profile-stat-strip.json` spells it. */
    public val id: String,
) {
    /** The itinerary's length, or the cursor's distance while a scrub stands. */
    Distance("distance"),

    /** The seconds the itinerary is expected to take. */
    Duration("duration"),

    /** The meters the itinerary climbs. */
    Ascent("ascent"),

    /** The meters the itinerary loses. */
    Descent("descent"),

    /** The itinerary's highest altitude, or the cursor's while a scrub stands. */
    Elevation("elevation"),

    /** The itinerary's steepest grade, or the grade at the cursor while a scrub stands. */
    Grade("grade"),

    /** The itinerary's hardest difficulty, as it writes it. */
    Difficulty("difficulty"),
}
