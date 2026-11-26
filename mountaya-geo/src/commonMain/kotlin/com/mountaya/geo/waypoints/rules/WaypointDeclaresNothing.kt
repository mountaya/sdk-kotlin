package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * waypointDeclaresNothing is whether [waypoint] has no fact for a card to state: no service, no
 * timing, no contact, no photo, no link and no custom field. Its description is no part of it, since
 * a card states the facts under a header that already carries the description.
 */
public fun waypointDeclaresNothing(waypoint: RestSchema.ItineraryWaypoint): Boolean =
    waypoint.amenities.isEmpty() &&
        waypoint.timing == null &&
        waypoint.contact == null &&
        waypoint.media?.photos.isNullOrEmpty() &&
        waypoint.media?.links.isNullOrEmpty() &&
        waypoint.properties.isNullOrEmpty()
