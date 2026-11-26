package com.mountaya.geo

/**
 * InfoBand is one band of an analysis bar: the stretch of an itinerary's distance axis an analysis
 * value holds over, in kilometers, so it stands under the points it colours.
 *
 * Two are equal where they hold the same numbers and equal values, as every value of the geo layer
 * compares.
 */
public class InfoBand<out T>(
    /** Where the band starts, in kilometers from the itinerary's start. */
    public val startKm: Double,
    /** Where it ends, past [startKm]. */
    public val endKm: Double,
    /** The value over the band. */
    public val value: T,
) {
    override fun equals(other: Any?): Boolean =
        other is InfoBand<*> && sameNumber(other.startKm, startKm) && sameNumber(other.endKm, endKm) &&
            other.value == value

    override fun hashCode(): Int = (startKm.hashCode() * 31 + endKm.hashCode()) * 31 + value.hashCode()

    override fun toString(): String = "InfoBand(startKm=$startKm, endKm=$endKm, value=$value)"
}
