package com.mountaya.geo

import kotlin.math.floor

/**
 * DEFAULT_AMENITY_STACK_MAX is how many badges a stack shows before it counts the rest, where its caller names no
 * other, the `defaultMax` `geo/profile-amenity-stacks.json` declares: four and a count, narrow enough that two
 * checkpoints a kilometer apart do not touch.
 */
public const val DEFAULT_AMENITY_STACK_MAX: Double = 4.0

/**
 * amenityStacks is the stack of services above each of [pins] whose place offers any, in the order the pins are
 * given (`geo.profile.amenity-stacks`).
 *
 * The services stack in the order the place states them. The first ones up to [max] are shown, [max] rounded
 * down, none shown below zero or for a maximum that is no number, and the rest are counted. Every service is
 * named in the stack's one label, the counted ones included, so a reader hears the whole list once. A place that
 * offers no service takes no stack.
 */
public fun amenityStacks(
    pins: List<CurvePin>,
    max: Double = DEFAULT_AMENITY_STACK_MAX,
): List<AmenityStack> {
    val shownCount = if (max >= 0.0) floor(max).coerceAtMost(Int.MAX_VALUE.toDouble()).toInt() else 0
    return pins.mapNotNull { pin ->
        val services =
            pin.place.details
                ?.amenities
                .orEmpty()
        if (services.isEmpty()) return@mapNotNull null
        val shown = services.take(shownCount)
        AmenityStack(pin, shown, services.size - shown.size, services)
    }
}
