package com.mountaya.geo

/**
 * GeolocateControlState is what a locate control shows (`geo.controls.geolocate-state`): whether a fix it
 * asked for is still out, whether location stands refused, and whether a press has a map to ask.
 *
 * Two are equal where every flag is.
 */
public class GeolocateControlState(
    /**
     * Whether a fix the control asked for is still out: from a press until every fix it asked for settles,
     * whatever map it stands over meanwhile.
     */
    public val isBusy: Boolean = false,
    /**
     * Whether the control says location is blocked: while the latest read of the standing permission, through
     * the map it stands over, answers refused.
     */
    public val isBlocked: Boolean = false,
    /** Whether a press has a map to ask, which no refusal changes. */
    public val isLive: Boolean = false,
) {
    override fun equals(other: Any?): Boolean =
        other is GeolocateControlState && other.isBusy == isBusy && other.isBlocked == isBlocked &&
            other.isLive == isLive

    override fun hashCode(): Int = (isBusy.hashCode() * 31 + isBlocked.hashCode()) * 31 + isLive.hashCode()

    override fun toString(): String = "GeolocateControlState(isBusy=$isBusy, isBlocked=$isBlocked, isLive=$isLive)"
}
