package com.mountaya.elements

/**
 * ProfilePart is one part an `ItineraryProfile` draws beside its curve and its stat strip, which it always draws. A
 * profile draws the set of parts it is handed: each part is off until named, but the amenities, which the itinerary's
 * data switch on, so [standard] holds them alone.
 */
public enum class ProfilePart {
    /** The stack of each pinned place's services above its pin (`geo.profile.amenity-stacks`). */
    Amenities,

    /** The chip stating each leg under the chart (`geo.profile.section-chips`). */
    Sections,

    /**
     * The card at the cursor stating what it stands on (`geo.profile.cursor-readout`), the stat strip then
     * stating the itinerary's own figures.
     */
    Tooltip,

    /**
     * The whole itinerary drawn under the chart with the window drawn marked on it, whose handles a reader
     * drags to zoom (`geo.profile.window-clamp`).
     */
    Navigator,

    /** The labels of the chart's floor, middle and ceiling (`geo.profile.domain`). */
    ElevationAxis,

    /**
     * The distance labelled under the chart, with the names of the places that divide the itinerary, a crowded name
     * left out (`geo.profile.axis-label-culling`).
     */
    DistanceAxis,
    ;

    /** The parts a profile draws where the app names none. */
    public companion object {
        /** What a profile draws where the app names nothing: the amenities. */
        public val standard: Set<ProfilePart> = setOf(Amenities)
    }
}
