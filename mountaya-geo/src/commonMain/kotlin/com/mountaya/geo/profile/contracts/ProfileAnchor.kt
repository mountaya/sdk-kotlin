package com.mountaya.geo

/** ProfileAnchor is what a profile's elevation scale is measured from (`geo.profile.domain`). */
public enum class ProfileAnchor(
    /** The anchor as `geo/profile-domain.json` spells it, `sea-level`. */
    public val id: String,
) {
    /** The itinerary's own extent, framed whatever window is drawn, so a zoomed curve keeps the height it had. */
    Route("route"),

    /**
     * The itinerary's extent with its floor moved to zero, its ceiling kept, so two
     * itineraries drawn side by side are comparable.
     */
    SeaLevel("sea-level"),

    /** The stretch the window drawn holds, framed alone, so zooming into a section rescales to its own relief. */
    Window("window"),
}
