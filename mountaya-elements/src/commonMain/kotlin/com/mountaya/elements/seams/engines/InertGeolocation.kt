package com.mountaya.elements

/**
 * InertGeolocation is a geolocation turned off: it answers as a platform with no location services does,
 * `GeolocationRefusal.NoApi`, and reads its permission as `GeolocationPermission.Denied`, so a map reads a
 * seam that will never locate rather than one still to be asked.
 *
 * A stated absence rather than an omitted seam, because "this app does not locate its reader" is a decision
 * a map has to be able to read.
 */
public class InertGeolocation : Geolocation {
    /** Answers no fix, as a platform with no location services does. */
    override suspend fun locate(): GeolocationFix = GeolocationFix.Refused(GeolocationRefusal.NoApi)

    /** Answers `GeolocationPermission.Denied`. */
    override suspend fun permission(): GeolocationPermission = GeolocationPermission.Denied

    override fun toString(): String = "InertGeolocation"
}
