package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry

/**
 * placeOnCurve places an itinerary's [places] on its profile, once for the pins, the names and the drop lines
 * (`geo.profile.curve-placement`).
 *
 * Each pass of a place, as [resolveWaypoints] places it on [geometry], that stands within [window], its two
 * ends included, stands at the x [profileX] draws its distance at. It is a pin on the curve, at the curve's
 * altitude at its distance read on the line between the two samples either side, unless the place is not
 * pinned or [series] has no sample; a name under the axis where the place divides the itinerary
 * ([ProfilePlace.dividesRoute]), the curve's height under it, null with no sample; and a cut-off where the
 * place publishes one, elevation or not. A pass outside the window is left out rather than clipped, since a
 * pin and a name are read aloud.
 */
public fun placeOnCurve(
    series: ElevationSeries,
    geometry: GeoJsonGeometry?,
    domain: ElevationDomain,
    window: ProfileRange,
    places: List<ProfilePlace>,
): CurvePlacement {
    val pins = ArrayList<CurvePin>()
    val dividers = ArrayList<CurveDivider>()
    val cutoffs = ArrayList<CurveCutoff>()
    for (pass in waypointPasses(geometry, places.map { it.placement })) {
        val distance = pass.distance
        if (!(distance >= window.from && distance <= window.to)) continue
        val place = places[pass.listed]
        val x = profileX(distance, series.totalDistance, window)
        val altitude = elevationAt(series.points, distance)
        if (place.isPinned && altitude != null) {
            pins += CurvePin(pass.key, place, distance, x, profileY(altitude, domain), altitude)
        }
        if (place.dividesRoute) {
            dividers += CurveDivider(pass.key, place, distance, x, altitude?.let { profileY(it, domain) })
        }
        val cutoffAt = place.details?.timing?.cutoffAt
        if (cutoffAt != null) cutoffs += CurveCutoff(pass.key, place, distance, x, cutoffAt)
    }
    return CurvePlacement(pins, dividers, cutoffs)
}
