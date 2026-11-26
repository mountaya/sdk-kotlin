package com.mountaya.navigation

/**
 * LocationStanding is where the app stands on location access: whether it may read the device's
 * location, and how precisely.
 *
 * `DeviceLocationSource` reads fixes only where [authorization] is [LocationAuthorization.WhenInUse] or
 * [LocationAuthorization.Always] and [accuracy] is [LocationAccuracy.Full]. Two are equal where both
 * members are.
 */
public class LocationStanding(
    /** Whether the app may read the device's location. */
    public val authorization: LocationAuthorization,
    /** How precisely it may read it. */
    public val accuracy: LocationAccuracy,
) {
    override fun equals(other: Any?): Boolean =
        other is LocationStanding && other.authorization == authorization && other.accuracy == accuracy

    override fun hashCode(): Int = authorization.hashCode() * 31 + accuracy.hashCode()

    override fun toString(): String = "LocationStanding(authorization=$authorization, accuracy=$accuracy)"
}
