package com.mountaya.elements

/** GeolocationPermission is what asking for a fix would do, where that can be known without asking, carrying its spelling. */
public enum class GeolocationPermission(
    /** The permission as a geolocation control spells it. */
    public val id: String,
) {
    /** A fix that would be refused: the reader refused it, the platform restricts it, or the reader allows only an approximate one. */
    Denied("denied"),

    /** A fix that would be answered. */
    Granted("granted"),

    /** A permission never asked for, which the app asks the reader for in its own flow. */
    Prompt("prompt"),

    /** A platform that does not say. */
    Unknown("unknown"),
}
