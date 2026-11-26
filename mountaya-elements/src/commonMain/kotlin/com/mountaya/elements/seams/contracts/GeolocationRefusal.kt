package com.mountaya.elements

/** GeolocationRefusal is why a geolocation answered no fix, carrying the contract's spelling (`map.camera.locate`). */
public enum class GeolocationRefusal(
    /** The reason as `map/camera-locate.json` spells it. */
    public val id: String,
) {
    /**
     * A fix the app holds no permission to read: one never asked for, one the reader refused, or one the
     * reader allows only approximately, which a map reads nothing of.
     */
    Denied("denied"),

    /** A platform with no location services to ask. */
    NoApi("no_api"),

    /** Location services that answered no fix in time. */
    Unavailable("unavailable"),
}
