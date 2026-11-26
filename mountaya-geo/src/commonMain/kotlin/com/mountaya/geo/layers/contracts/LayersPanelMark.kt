package com.mountaya.geo

/**
 * LayersPanelMark is what a row of a layers picker shows beside its label (`geo.layers.picker-rows`),
 * carrying the contract's spelling.
 */
public enum class LayersPanelMark(
    /** The mark as `geo/layers-picker-rows.json` spells it. */
    public val id: String,
) {
    /** An entry out of the viewer's reach, shown whether or not the selection holds it. */
    Lock("lock"),

    /** An entry the selection holds. */
    Tick("tick"),
}
