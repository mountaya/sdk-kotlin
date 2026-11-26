package com.mountaya.geo

/**
 * PROFILE_VIEW_BOX is the width and the height of the square a profile is drawn in, the `viewBox`
 * `geo/profile-plot-scale.json` declares: a position in it is also a percentage of the box the square is
 * stretched into, so a pin, a band and the curve align by construction.
 */
public const val PROFILE_VIEW_BOX: Double = 100.0

/**
 * PROFILE_TOP_INSET is the headroom above a profile's ceiling, in the square's units, the `topInset`
 * `geo/profile-plot-scale.json` declares, so a summit is not flush against the frame.
 */
public const val PROFILE_TOP_INSET: Double = 6.0

/**
 * PROFILE_BOTTOM_INSET is the room below a profile's floor, in the square's units, the `bottomInset`
 * `geo/profile-plot-scale.json` declares, so a valley reads as a floor.
 */
public const val PROFILE_BOTTOM_INSET: Double = 4.0

/**
 * PROFILE_BASELINE is how far down the square a profile's floor is drawn, [PROFILE_VIEW_BOX] less
 * [PROFILE_BOTTOM_INSET], the `baseline` `geo/profile-plot-scale.json` declares.
 */
public const val PROFILE_BASELINE: Double = PROFILE_VIEW_BOX - PROFILE_BOTTOM_INSET

/** The thousandths of a unit a position in a profile's square is rounded to. */
private const val PLOT_UNIT_SCALE = 1_000.0

/**
 * profileX is where a profile draws [meters] from the start of the itinerary across its square
 * (`geo.profile.plot-scale`): the window's start at 0 and its end at [PROFILE_VIEW_BOX], a distance outside
 * the window held to the frame. The window is the whole itinerary, `0` to [totalDistance], where none is
 * given, and is read in order; a window or an itinerary of no length draws every distance at 0.
 *
 * The position is rounded to a thousandth of a unit, a half up, finer than any screen resolves.
 */
public fun profileX(
    meters: Double,
    totalDistance: Double,
    window: ProfileRange? = null,
): Double {
    val span = orderedWindow(window, totalDistance)
    if (!(span.to > span.from)) return 0.0
    val fraction = (meters - span.from) / (span.to - span.from)
    return plotUnit(fraction.coerceIn(0.0, 1.0) * PROFILE_VIEW_BOX)
}

/**
 * projectX is where a profile's curve draws [meters] across its square, the way [profileX] places it but
 * never held to the frame, so a curve zoomed into a window leaves the frame at the ground's own slope. [span]
 * is read as given, in order.
 */
internal fun projectX(
    meters: Double,
    span: ProfileRange,
): Double {
    val width = span.to - span.from
    return if (width > 0.0) plotUnit((meters - span.from) / width * PROFILE_VIEW_BOX) else 0.0
}

/**
 * orderedWindow is the window a profile is drawn over, in order: the whole itinerary, from 0 to
 * [totalDistance], where none is given.
 */
internal fun orderedWindow(
    window: ProfileRange?,
    totalDistance: Double,
): ProfileRange {
    if (window == null) return ProfileRange(0.0, totalDistance)
    return ProfileRange(minOf(window.from, window.to), maxOf(window.from, window.to))
}

/** plotUnit is a position in a profile's square rounded to a thousandth of a unit, a half up. */
internal fun plotUnit(value: Double): Double = roundHalfUp(value * PLOT_UNIT_SCALE) / PLOT_UNIT_SCALE
