package com.mountaya.navigation

/**
 * locationStandingOf answers where the app stands on location access from the permissions it holds:
 * [fine], `ACCESS_FINE_LOCATION`, [coarse], `ACCESS_COARSE_LOCATION`, and [background], a permission to
 * read with the screen off, `ACCESS_BACKGROUND_LOCATION` from API 29 and either location permission below.
 *
 * Android tells a permission never asked for from one refused only to the app's own activity, so holding
 * neither reads as [LocationAuthorization.NotDetermined]; a background permission reads as
 * [LocationAuthorization.Always] and either other alone as [LocationAuthorization.WhenInUse]; and the
 * approximate permission without the fine one is [LocationAccuracy.Reduced], as `sdk-dart`'s Android side
 * reads the same permissions.
 */
internal fun locationStandingOf(
    fine: Boolean,
    coarse: Boolean,
    background: Boolean,
): LocationStanding {
    val authorization =
        when {
            !fine && !coarse -> LocationAuthorization.NotDetermined
            background -> LocationAuthorization.Always
            else -> LocationAuthorization.WhenInUse
        }
    val accuracy = if (!fine && coarse) LocationAccuracy.Reduced else LocationAccuracy.Full
    return LocationStanding(authorization, accuracy)
}
