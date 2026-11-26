package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * AMENITY_GLYPH_PREFIX is the namespace an amenity's glyph name opens with, so an amenity never shares a name
 * with an icon.
 */
public const val AMENITY_GLYPH_PREFIX: String = "amenity-"

/**
 * waypointAmenities is every amenity the contract lets a waypoint state, in the order a picker or a
 * legend walks them: sustenance first, then care, comfort, logistics, shelter and the place itself.
 */
public val waypointAmenities: List<RestSchema.WaypointAmenity> =
    listOf(
        RestSchema.WaypointAmenity.Water,
        RestSchema.WaypointAmenity.Drinks,
        RestSchema.WaypointAmenity.Food,
        RestSchema.WaypointAmenity.HotMeal,
        RestSchema.WaypointAmenity.Snacks,
        RestSchema.WaypointAmenity.Medical,
        RestSchema.WaypointAmenity.FirstAid,
        RestSchema.WaypointAmenity.Toilets,
        RestSchema.WaypointAmenity.Showers,
        RestSchema.WaypointAmenity.DropBag,
        RestSchema.WaypointAmenity.LuggageStorage,
        RestSchema.WaypointAmenity.Assistance,
        RestSchema.WaypointAmenity.RestArea,
        RestSchema.WaypointAmenity.Accommodation,
        RestSchema.WaypointAmenity.Shelter,
        RestSchema.WaypointAmenity.Camping,
        RestSchema.WaypointAmenity.Picnic,
        RestSchema.WaypointAmenity.Shop,
        RestSchema.WaypointAmenity.Parking,
        RestSchema.WaypointAmenity.Shuttle,
        RestSchema.WaypointAmenity.PublicTransport,
        RestSchema.WaypointAmenity.CableCar,
        RestSchema.WaypointAmenity.PowerCharging,
        RestSchema.WaypointAmenity.Wifi,
        RestSchema.WaypointAmenity.MobileSignal,
        RestSchema.WaypointAmenity.Waste,
        RestSchema.WaypointAmenity.PetFriendly,
        RestSchema.WaypointAmenity.WheelchairAccess,
        RestSchema.WaypointAmenity.Viewpoint,
        RestSchema.WaypointAmenity.TimingCheckpoint,
    )

/**
 * amenityGlyph answers the name of the glyph a badge for [amenity] is drawn with, `amenity-` then the
 * amenity as the wire spells it: `water` is `amenity-water`. An amenity this build does not know is
 * named the same way, and an icon set either answers the name or falls back.
 */
public fun amenityGlyph(amenity: RestSchema.WaypointAmenity): String = AMENITY_GLYPH_PREFIX + amenity.value
