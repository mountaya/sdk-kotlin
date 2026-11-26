package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * amenityName names a service, [amenity], for a reader of [scope]: the word its `amenities` section holds for it,
 * else the English one of the elements' own words, else the service's own value, since the vocabulary is open
 * and an itinerary served by a newer deployment carries values no words name, and `hot-meal` is a better label
 * than nothing. It is the one lookup behind every surface that names a service: a stack's label, a table's cell
 * and its filter chips, and a card.
 */
public fun amenityName(
    amenity: RestSchema.WaypointAmenity,
    scope: ElementsScope,
): String = scope.text("amenities.${amenity.value}") ?: amenity.value
