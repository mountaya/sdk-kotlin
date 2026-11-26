package com.mountaya.geo

/**
 * PlacedSpan is a stretch placed under a profile's chart, its band strip or its section chips
 * (`geo.profile.window-spans`): the stretch as given, its place in the list given, its two ends in order, and
 * where its start and its end are drawn across the plot, held to the frame, the end always past the start.
 *
 * Two are equal where the stretches are, at one place in the list, with the same numbers.
 */
public class PlacedSpan<out Span : ProfileSpan<Span>>(
    /** The stretch as the caller gave it. */
    public val span: Span,
    /** The stretch's place in the list the caller gave, from zero. */
    public val index: Int,
    /** Where the stretch starts, in meters from the start of the itinerary: the lower of its ends. */
    public val from: Double,
    /** Where the stretch ends: the higher of its ends. */
    public val to: Double,
    /** How far across the plot the stretch's start is drawn. */
    public val start: Double,
    /** How far across the plot the stretch's end is drawn. */
    public val end: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is PlacedSpan<*> &&
            other.span == span &&
            other.index == index &&
            sameNumber(other.from, from) &&
            sameNumber(other.to, to) &&
            sameNumber(other.start, start) &&
            sameNumber(other.end, end)

    override fun hashCode(): Int = listOf(span, index, from, to, start, end).hashCode()

    override fun toString(): String = "PlacedSpan(span=$span, index=$index, from=$from, to=$to, start=$start, end=$end)"
}
