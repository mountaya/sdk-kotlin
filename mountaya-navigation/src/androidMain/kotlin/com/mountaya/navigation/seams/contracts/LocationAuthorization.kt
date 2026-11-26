package com.mountaya.navigation

/**
 * LocationAuthorization is whether the app may read the device's location, as the platform states it,
 * the vocabulary `sdk-dart` states it in.
 *
 * Android tells a permission never asked for from one refused only to the app's own activity, so an app
 * that holds no location permission reads as [NotDetermined], and Android never states [Restricted] or
 * [Denied], which are iOS's.
 */
public enum class LocationAuthorization {
    /** A permission the app has never asked for, which the platform would prompt the reader for. */
    NotDetermined,

    /** A refusal by a policy the reader cannot change, such as parental controls. */
    Restricted,

    /** A refusal by the reader. */
    Denied,

    /** A permission the reader grants only while the app is in use. */
    WhenInUse,

    /** A permission the reader grants at all times, with the screen off included. */
    Always,
}
