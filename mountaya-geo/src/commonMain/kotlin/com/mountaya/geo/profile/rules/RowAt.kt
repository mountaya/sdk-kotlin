package com.mountaya.geo

/**
 * rowAt is the row of [rows] a profile's cursor [distance] meters from the itinerary's start stands in
 * (`geo.profile.row-match`): the one whose span holds the distance as [stretchAt] reads a stretch, so the later
 * of two rows holds the meter they share, the last meter of a row nothing follows stays in that row, and a row of
 * no length holds nothing. The row the map lights and the stretch a hover card names are then one answer. The row
 * is answered as given, null in none.
 */
public fun <Row : ProfileSpan<Row>> rowAt(
    rows: List<Row>,
    distance: Double,
): Row? {
    val spans = rows.mapIndexed { index, row -> RowSpan(row.from, row.to, index) }
    return stretchAt(spans, distance)?.let { rows[it.index] }
}

/** A row's span with its place in the list, so the stretch the cursor reads names the row as it was given. */
private class RowSpan(
    override val from: Double,
    override val to: Double,
    val index: Int,
) : ProfileSpan<RowSpan> {
    override fun spanning(
        from: Double,
        to: Double,
    ): RowSpan = RowSpan(from, to, index)
}
