package com.mountaya.geo

/**
 * STEEPNESS_OVERLAY_SLUG is the slug of the overlay the steepness classes paint, the
 * `steepnessOverlaySlug` `geo/slope-ramp.json` declares.
 */
public const val STEEPNESS_OVERLAY_SLUG: String = "steepness"

/**
 * classifySteepness is the steepness class a slope of [degrees] falls in: the last class whose
 * [SteepnessClass.fromDegrees] it reaches, so 30 degrees is `30-35` and 45 or more `45+`. Null under
 * 25 degrees, where no class begins, and for a slope that is no number. A descent is read as
 * written, so it falls in none.
 */
public fun classifySteepness(degrees: Double): SteepnessClass? =
    SteepnessClass.entries.lastOrNull {
        degrees >=
            it.fromDegrees
    }
