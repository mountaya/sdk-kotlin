package com.mountaya.navigation

/**
 * LocationException is a failure of the device's location reader, which `DeviceLocationSource` tells its
 * `onError`: the device has no satellite positioning to read, the permission went between the check and
 * the request, the reader turned the device's location off, the platform's reader failed otherwise, or the
 * app asked for background updates its manifest does not declare. The source keeps reading where it can,
 * and fixes resume once the platform hands them again.
 *
 * Its message says what to do, for a developer reading a log; a caller reads [kind]. The kinds are the ones
 * `sdk-dart`'s `LocationException` and `sdk-swift`'s `LocationError` carry.
 */
public class LocationException(
    /** Which failure it is. */
    public val kind: Kind,
    /** What the platform threw, where it threw anything. */
    cause: Throwable? = null,
) : Exception(messageOf(kind), cause) {
    /**
     * Kind is every failure of the location reader this build knows.
     *
     * It is open, so a `when` over it ends in `else`: the set grows where the reader meets another.
     */
    @JvmInline
    public value class Kind internal constructor(
        /** The failure, as a log names it. */
        public val value: String,
    ) {
        override fun toString(): String = value

        /** The failures this build knows. */
        public companion object {
            /** The device has no satellite positioning, or no location services at all, so nothing is read. */
            public val NoProvider: Kind = Kind("noProvider")

            /** The location permission went between the source's check and its request, so nothing is read. */
            public val PermissionRevoked: Kind = Kind("permissionRevoked")

            /** The reader turned the device's location off, so no fix comes until it is on again. */
            public val ProviderDisabled: Kind = Kind("providerDisabled")

            /** The platform's reader failed in a way none of the others names; what it threw is the cause. */
            public val PlatformFailed: Kind = Kind("platformFailed")

            /**
             * The app asked for background updates, and its manifest declares neither `ACCESS_BACKGROUND_LOCATION`
             * nor a foreground service of type `location`, so fixes come while the app is in use alone.
             */
            public val BackgroundUndeclared: Kind = Kind("backgroundUndeclared")

            /**
             * The app asked for background updates and declares no `audio` background mode, so a voice is not heard
             * with the screen off. iOS refuses that audio; Android has no such mode, and never raises it.
             */
            public val AudioUndeclared: Kind = Kind("audioUndeclared")
        }
    }
}

/** The message a failure of [kind] is logged with, ending on what to do. */
private fun messageOf(kind: LocationException.Kind): String =
    when (kind) {
        LocationException.Kind.NoProvider -> {
            "The device has no satellite positioning to read; navigate with an app's own LocationSource, or a replay."
        }

        LocationException.Kind.PermissionRevoked -> {
            "The location permission was revoked as the reader started; ask for ACCESS_FINE_LOCATION again, " +
                "then call DeviceLocationSource.recheckPermission()."
        }

        LocationException.Kind.ProviderDisabled -> {
            "The device's location is turned off; fixes resume once the reader turns it on in the system's settings."
        }

        LocationException.Kind.PlatformFailed -> {
            "The device's location reader failed, as its cause says; DeviceLocationSource.recheckPermission() " +
                "asks it again."
        }

        LocationException.Kind.BackgroundUndeclared -> {
            "Background updates were asked for, and the app's manifest declares neither ACCESS_BACKGROUND_LOCATION " +
                "nor a foreground service of type location, so fixes come while the app is in use alone; declare " +
                "one, or make the source without backgroundUpdates."
        }

        LocationException.Kind.AudioUndeclared -> {
            "Background updates were asked for, and the app declares no audio background mode, so a voice is not " +
                "heard with the screen off; on iOS, add audio under UIBackgroundModes."
        }

        else -> {
            "The device's location reader failed ($kind)."
        }
    }
