package com.mountaya.geo

/**
 * PoiPanelRows is what a points-of-interest picker offers (`geo.layers.poi-rows`): its rows, in order, and
 * whether its master row, which turns the whole overlay on and off, is on.
 *
 * Two are equal where both members are.
 */
public class PoiPanelRows(
    /** The picker's rows, in order. None where nothing is drawn, and the picker then shows nothing. */
    public val rows: List<PoiPanelRow>,
    /** Whether the master row is on: while any row is. */
    public val isAnyVisible: Boolean,
) {
    override fun equals(other: Any?): Boolean =
        other is PoiPanelRows && other.rows == rows && other.isAnyVisible == isAnyVisible

    override fun hashCode(): Int = rows.hashCode() * 31 + isAnyVisible.hashCode()

    override fun toString(): String = "PoiPanelRows(rows=$rows, isAnyVisible=$isAnyVisible)"
}
