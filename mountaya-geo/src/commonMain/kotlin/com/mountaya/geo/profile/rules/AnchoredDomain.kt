package com.mountaya.geo

/**
 * anchoredDomain is the elevation range a chart is drawn against, by what its scale is anchored on
 * (`geo.profile.domain`): [ProfileAnchor.Route] frames the whole itinerary whatever the window, so a zoomed
 * curve keeps the height it had; [ProfileAnchor.Window] frames the stretch [window] holds, as
 * [profileDomain] reads one, so a zoomed section rescales to its own relief; and [ProfileAnchor.SeaLevel]
 * moves only the floor of the route's frame to zero, its ceiling kept, so a sea-level scale is the
 * itinerary's own scale extended downward rather than a second rounding.
 */
public fun anchoredDomain(
    series: ElevationSeries,
    anchor: ProfileAnchor,
    window: ProfileRange? = null,
): ElevationDomain =
    when (anchor) {
        ProfileAnchor.Route -> profileDomain(series)
        ProfileAnchor.Window -> profileDomain(series, window)
        ProfileAnchor.SeaLevel -> ElevationDomain(0.0, profileDomain(series).max)
    }
