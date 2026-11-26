package com.mountaya.geo

/**
 * ProfileSpan is anything that stands on an itinerary's distance axis between two distances: a window, a
 * coloured stretch of a profile's curve, a classified stretch and a table's row. The rules that place, paint
 * and read stretches take any of them, so a stretch keeps its own members through them.
 *
 * [Self] is the stretch's own type, which [spanning] answers, so a rule that moves a stretch's ends answers
 * the stretch it was given, a classified stretch with its word and a row with its own members.
 */
public interface ProfileSpan<out Self : ProfileSpan<Self>> {
    /** Where the span starts, in meters from the start of the itinerary. */
    public val from: Double

    /** Where the span ends, in meters from the start of the itinerary. */
    public val to: Double

    /** Returns this stretch standing between [from] and [to], every other member as it is. */
    public fun spanning(
        from: Double,
        to: Double,
    ): Self
}
