package com.mountaya.geo

/**
 * ItineraryWaypointRow is one row of a waypoint table: a pass of a place, and the stretch that reaches it from the
 * row before (`geo.profile.waypoint-rows`). Its key is unique per row, so a loop passing a place twice lists it
 * twice.
 *
 * Two are equal where they hold the same key, written alike, the same place and the same numbers, as every
 * value of the geo layer compares.
 */
public class ItineraryWaypointRow(
    /** The key, unique per row: `id#pass`, counting the place's passes from zero. */
    public val key: String,
    /** The place the row lists, with its id, name, colour, glyph and details. */
    public val place: ProfilePlace,
    /** The row's place in the table, from zero. */
    public val index: Int,
    /** Where the stretch reaching the place starts, where the row before stands, in meters. */
    override val from: Double,
    /** Where the place stands, in meters. */
    override val to: Double,
    /** The row's figures. */
    public val metrics: ItineraryWaypointRowMetrics,
) : ProfileSpan<ItineraryWaypointRow> {
    override fun spanning(
        from: Double,
        to: Double,
    ): ItineraryWaypointRow = ItineraryWaypointRow(key, place, index, from, to, metrics)

    override fun equals(other: Any?): Boolean =
        other is ItineraryWaypointRow &&
            other.key == key &&
            other.place == place &&
            other.index == index &&
            sameNumber(other.from, from) &&
            sameNumber(other.to, to) &&
            other.metrics == metrics

    override fun hashCode(): Int = listOf(key, place, index, from, to, metrics).hashCode()

    override fun toString(): String =
        "ItineraryWaypointRow(key=$key, place=$place, index=$index, from=$from, to=$to, metrics=$metrics)"
}
