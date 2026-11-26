package com.mountaya.geo

/**
 * MIN_RANGE_SPAN_METERS is the narrowest window a profile is drawn over, the `minSpanMeters`
 * `geo/profile-window-clamp.json` declares, its one home in the contract: a window narrower than a hundred
 * meters shows a curve no reader can follow.
 */
public const val MIN_RANGE_SPAN_METERS: Double = 100.0

/**
 * clampRange is the window a profile draws for the [window] asked of it (`geo.profile.window-clamp`):
 * ordered, held inside the itinerary of [totalDistance], and never narrower than [MIN_RANGE_SPAN_METERS], a
 * narrower one widened forward from its start where the itinerary has room and back from the itinerary's end
 * where it does not, so a window dragged to the very end stays there. An end that is no number reads as the
 * itinerary's end on that side, `from` its start and `to` its finish, and an infinite end is held to the
 * itinerary like any other. An itinerary shorter than the minimum is its own whole width, and one of no
 * length, or whose length is no number, has a window of no length at 0.
 *
 * A handle dragged past its partner swaps the two rather than inverting the window, and a window a caller
 * states comes back as the one the chart will draw, so a controlled window and one the element moves itself
 * behave the same.
 */
public fun clampRange(
    window: ProfileRange,
    totalDistance: Double,
): ProfileRange {
    val span = if (totalDistance.isNaN()) 0.0 else maxOf(0.0, totalDistance)
    if (!(span > 0.0)) return ProfileRange(0.0, 0.0)
    val start = if (window.from.isNaN()) 0.0 else window.from
    val end = if (window.to.isNaN()) span else window.to
    val low = maxOf(0.0, minOf(start, end))
    val high = minOf(span, maxOf(start, end))
    val minimum = minOf(MIN_RANGE_SPAN_METERS, span)
    if (high - low >= minimum) return ProfileRange(low, high)
    val from = maxOf(0.0, minOf(low, span - minimum))
    return ProfileRange(from, from + minimum)
}
