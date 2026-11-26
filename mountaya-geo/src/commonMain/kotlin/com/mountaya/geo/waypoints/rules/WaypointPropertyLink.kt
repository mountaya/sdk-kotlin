package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * waypointPropertyLink is the address a renderer may anchor a place's custom field, [property], by
 * (`geo.profile.waypoint-property-value`): its value past the white space at its ends, only where the field is
 * declared a `url` and that value is a web address ([isWebAddress]), else null.
 *
 * The declared format is not enough on its own: a renderer told a value is a link anchors it, so a `url` field
 * holding `javascript:` would be a script a reader taps. The format says what the author meant and the value what
 * it is, and a link needs both.
 */
public fun waypointPropertyLink(property: WaypointDetails.Property): String? {
    if (property.format != RestSchema.WaypointPropertyFormat.Url) return null
    return trimWhiteSpace(property.value).takeIf(::isWebAddress)
}
