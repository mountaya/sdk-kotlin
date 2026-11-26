package com.mountaya.geo

/**
 * ProfileClass is a stretch of an itinerary classified into a colour and a word: what a hover card names and
 * colours where a cursor stands in it (`geo.profile.stretch-at`), and what the curve is painted in. A caller
 * states its own taxonomy this way, a surface, a way type or a sponsor's sectors, and `slopeStretches`
 * computes the one a profile derives, its steepness.
 *
 * Two are equal where they hold the same numbers and the same texts, as every value of the geo layer
 * compares.
 */
public class ProfileClass(
    /** Where the stretch starts, in meters from the start of the itinerary. */
    override val from: Double,
    /** Where the stretch ends, in meters from the start of the itinerary. */
    override val to: Double,
    /** The colour the stretch is painted in, any CSS colour, painted as given. */
    public val color: String,
    /** The word the stretch reads as: a caller's own word, or the key of a slope bucket. */
    public val label: String,
) : ProfileSpan<ProfileClass> {
    override fun spanning(
        from: Double,
        to: Double,
    ): ProfileClass = ProfileClass(from, to, color, label)

    override fun equals(other: Any?): Boolean =
        other is ProfileClass &&
            sameNumber(other.from, from) &&
            sameNumber(other.to, to) &&
            other.color == color &&
            other.label == label

    override fun hashCode(): Int =
        ((from.hashCode() * 31 + to.hashCode()) * 31 + color.hashCode()) * 31 + label.hashCode()

    override fun toString(): String = "ProfileClass(from=$from, to=$to, color=$color, label=$label)"
}
