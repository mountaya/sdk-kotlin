package com.mountaya.geo

/**
 * ItinerarySegmentRow is one leg of an itinerary as its table states it, and as a chip under its profile states
 * it (`geo.profile.segment-rows`): its key, its own id and the members it stands for, its name and colour, its
 * place in the table, the stretch of the distance axis it spans, and its figures.
 *
 * Its span is always read off its own geometry and never stated, since it is what a sparkline lights and a map
 * emphasises: a leg whose stored distance differs from its geometry's prints the server's figure and still
 * lights its own stretch. Two are equal where they hold the same texts, written alike, and the same numbers, as
 * every value of the geo layer compares.
 */
public class ItinerarySegmentRow(
    /** The row's key, the leg's own id, which a chain holds once. */
    public val key: String,
    /** The leg's own id, as its leg states it. */
    public val id: String,
    /** The ids of the segments a collapsed group stands for, and empty for a leg that stands for itself alone. */
    public val memberIds: List<String> = emptyList(),
    /** The leg's name, empty where it has none. */
    public val name: String,
    /** The leg's colour, painted as given, null where it states none. */
    public val color: String? = null,
    /** The row's place in the table, from zero. */
    public val index: Int,
    /** Where the leg starts on the distance axis, in meters. */
    override val from: Double,
    /** Where the leg ends on the distance axis, in meters. */
    override val to: Double,
    /** The leg's figures, stated over derived, with the running totals. */
    public val metrics: ItinerarySegmentRowMetrics,
) : ProfileSpan<ItinerarySegmentRow> {
    override fun spanning(
        from: Double,
        to: Double,
    ): ItinerarySegmentRow = ItinerarySegmentRow(key, id, memberIds, name, color, index, from, to, metrics)

    override fun equals(other: Any?): Boolean =
        other is ItinerarySegmentRow &&
            other.key == key &&
            other.id == id &&
            other.memberIds == memberIds &&
            other.name == name &&
            other.color == color &&
            other.index == index &&
            sameNumber(other.from, from) &&
            sameNumber(other.to, to) &&
            other.metrics == metrics

    override fun hashCode(): Int = listOf(key, id, memberIds, name, color, index, from, to, metrics).hashCode()

    override fun toString(): String =
        "ItinerarySegmentRow(key=$key, id=$id, memberIds=$memberIds, name=$name, color=$color, index=$index, " +
            "from=$from, to=$to, metrics=$metrics)"
}
