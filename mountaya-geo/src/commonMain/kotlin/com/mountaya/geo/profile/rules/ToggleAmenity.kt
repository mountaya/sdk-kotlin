package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * toggleAmenity is the services [picked] after a reader presses the chip of [amenity]
 * (`geo.profile.amenity-filter`): taken out of the choice where it was picked, every time it was listed, and
 * added at its end where it was not.
 */
public fun toggleAmenity(
    picked: List<RestSchema.WaypointAmenity>,
    amenity: RestSchema.WaypointAmenity,
): List<RestSchema.WaypointAmenity> = if (amenity in picked) picked.filter { it != amenity } else picked + amenity
