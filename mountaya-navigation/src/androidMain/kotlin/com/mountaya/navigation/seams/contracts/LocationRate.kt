package com.mountaya.navigation

/**
 * LocationRate is how often [DeviceLocationSource] reads the device's satellite positioning: every fix the
 * platform reads for a navigation, or fewer for a traveller on foot.
 *
 * A maneuver's prompt falls due 55 m before it, its 50 m trigger and the 5 m of slack it is read with, so
 * the fix that says it on time lands between 55 and 50 m before the maneuver. The platform hands a fix on
 * only once both the time and the distance a rate holds it back for have passed, so [Walking]'s 5 m hold
 * fixes 5 m apart up to 2.5 m/s, 9 km/h, and its 2 seconds hold them farther apart than the prompt's slack
 * above that, which is why the rate is a walker's.
 */
public enum class LocationRate(
    /** The least time between two fixes the platform hands on, in milliseconds. */
    internal val intervalMilliseconds: Long,
    /** The least distance between two fixes the platform hands on, in meters. */
    internal val distanceMeters: Float,
) {
    /** Every fix the platform reads for a navigation: a fix every second, whatever the distance moved. The default. */
    Navigation(intervalMilliseconds = 1_000L, distanceMeters = 0f),

    /** Fewer fixes, for a traveller on foot: a fix every two seconds once the device has moved 5 m. */
    Walking(intervalMilliseconds = 2_000L, distanceMeters = 5f),
}
