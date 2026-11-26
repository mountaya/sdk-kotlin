package com.mountaya.geo

/**
 * resolveCharacteristics is an itinerary's figures as a surface states them: each the one [stated] holds, the
 * server's, over the one [derived] reads off the geometry, figure by figure, so a figure the server left out is
 * still stated where the geometry answers it. The geometry's lowest and highest altitude are the altitude band
 * and its steepest grade the steepest ascent; the duration, the steepest descent and the difficulties have no
 * derived figure, and are the stated ones or none.
 */
public fun resolveCharacteristics(
    derived: ProfileTotals,
    stated: ProfileCharacteristics?,
): ResolvedCharacteristics =
    ResolvedCharacteristics(
        distance = stated?.distance ?: derived.distance,
        duration = stated?.duration,
        ascent = stated?.ascent?.toDouble() ?: derived.ascent,
        descent = stated?.descent?.toDouble() ?: derived.descent,
        altitudeMin = stated?.altitudeMin?.toDouble() ?: derived.elevationMin,
        altitudeMax = stated?.altitudeMax?.toDouble() ?: derived.elevationMax,
        slopeMin = stated?.slopeMin,
        slopeMax = stated?.slopeMax ?: derived.gradeMax,
        trailDifficultyMin = stated?.trailDifficultyMin,
        trailDifficultyMax = stated?.trailDifficultyMax,
    )
