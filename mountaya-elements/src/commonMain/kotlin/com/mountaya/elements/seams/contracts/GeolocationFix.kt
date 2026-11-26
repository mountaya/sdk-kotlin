package com.mountaya.elements

/**
 * GeolocationFix is what asking for the device's position answered: where it stands and how precisely, or
 * the reason there is no fix.
 *
 * The accuracy travels with the position because framing a camera on a fix needs both: a coarse fix that
 * filled the view at street zoom would claim a precision it does not have. Two are equal where they hold the
 * same, each number as the same number, `NaN` the same as `NaN` and 0 another than -0.
 */
public sealed interface GeolocationFix {
    /** Located is a fix: [position] is `[longitude, latitude]`, and [accuracyMeters] how far from it the device may stand. */
    public class Located(
        /** Where the device stands, `[longitude, latitude]`. */
        public val position: List<Double>,
        /** How far from [position] the device may stand, in meters. */
        public val accuracyMeters: Double,
    ) : GeolocationFix {
        override fun equals(other: Any?): Boolean =
            other is Located && other.position == position && other.accuracyMeters.toBits() == accuracyMeters.toBits()

        override fun hashCode(): Int = position.hashCode() * 31 + accuracyMeters.toBits().hashCode()

        override fun toString(): String = "Located(position=$position, accuracyMeters=$accuracyMeters)"
    }

    /** Refused is no fix, for the [reason] named. */
    public class Refused(
        /** Why there is no fix. */
        public val reason: GeolocationRefusal,
    ) : GeolocationFix {
        override fun equals(other: Any?): Boolean = other is Refused && other.reason == reason

        override fun hashCode(): Int = reason.hashCode()

        override fun toString(): String = "Refused($reason)"
    }
}
