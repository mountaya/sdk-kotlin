package com.mountaya.navigation

/** LocationAccuracy is how precisely the app may read the device's location. */
public enum class LocationAccuracy {
    /** The platform's full accuracy, which a navigation reads. */
    Full,

    /** An approximate position, kilometres from where the reader stands, which a navigation reads nothing of. */
    Reduced,
}
