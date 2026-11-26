package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * AmenityStack is the services a pinned place offers, stacked above its pin on a profile
 * (`geo.profile.amenity-stacks`): the pin, the services drawn as badges, how many follow them as one count, and
 * every service the place offers, the one label the stack is read by.
 *
 * Two are equal where they stand over one pin with the same services, as every value of the geo layer compares.
 */
public class AmenityStack(
    /** The pin the stack stands above. */
    public val pin: CurvePin,
    /** The services drawn as badges, the first ones in the order the place states them. */
    public val shown: List<RestSchema.WaypointAmenity>,
    /** How many services follow the ones shown, drawn as one count. */
    public val counted: Int,
    /** Every service the place offers, in its order. */
    public val named: List<RestSchema.WaypointAmenity>,
) {
    override fun equals(other: Any?): Boolean =
        other is AmenityStack &&
            other.pin == pin &&
            other.shown == shown &&
            other.counted == counted &&
            other.named == named

    override fun hashCode(): Int = ((pin.hashCode() * 31 + shown.hashCode()) * 31 + counted) * 31 + named.hashCode()

    override fun toString(): String = "AmenityStack(pin=$pin, shown=$shown, counted=$counted, named=$named)"
}
