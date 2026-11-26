package com.mountaya.elements

import com.mountaya.geo.CurveColor
import com.mountaya.geo.ProfileClass
import com.mountaya.geo.SlopeBucket
import com.mountaya.geo.slopeBuckets

/**
 * ProfileColoring is how an `ItineraryProfile` paints its curve and the area under it (`geo.profile.curve-stops`):
 * in the accent alone, in stretches the app coloured, by the itinerary's own steepness, or in stretches the app
 * classified, whose word the cursor's readout names.
 *
 * Two are equal where they are one coloring of the same stretches.
 */
public sealed interface ProfileColoring {
    /** Plain paints the whole curve in the accent. */
    public object Plain : ProfileColoring {
        override fun toString(): String = "ProfileColoring.Plain"
    }

    /** Stretches paints each of [stretches] in its colour, ground no stretch covers in the accent. */
    public class Stretches(
        /** The coloured stretches. */
        public val stretches: List<CurveColor>,
    ) : ProfileColoring {
        override fun equals(other: Any?): Boolean = other is Stretches && other.stretches == stretches

        override fun hashCode(): Int = stretches.hashCode()

        override fun toString(): String = "ProfileColoring.Stretches($stretches)"
    }

    /**
     * Slope paints the itinerary's own steepness, bucketed by [buckets] (`geo.profile.slope-stretches`), each named by
     * the scope's `profile.slope` word for its key: Geo's own ramp, `slopeBuckets`, unless the app names another, as
     * `sdk-dart`'s and `sdk-typescript`'s default theirs.
     */
    public class Slope(
        /** The buckets the steepness is painted from, `slopeBuckets` unless the app names others. */
        public val buckets: List<SlopeBucket> = slopeBuckets,
    ) : ProfileColoring {
        override fun equals(other: Any?): Boolean = other is Slope && other.buckets == buckets

        override fun hashCode(): Int = buckets.hashCode() * 31 + 1

        override fun toString(): String = "ProfileColoring.Slope($buckets)"
    }

    /** Classes paints [classes], stretches the app classified, each named by its own word. */
    public class Classes(
        /** The classified stretches. */
        public val classes: List<ProfileClass>,
    ) : ProfileColoring {
        override fun equals(other: Any?): Boolean = other is Classes && other.classes == classes

        override fun hashCode(): Int = classes.hashCode() * 31 + 2

        override fun toString(): String = "ProfileColoring.Classes($classes)"
    }
}
