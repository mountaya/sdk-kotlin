package com.mountaya.geo

/**
 * layersPanelPress is what a press on [row] of a layers picker's section making [choice] asks for
 * (`geo.layers.picker-rows`): the report of the entry where the row shows its lock, which selects nothing, and
 * otherwise the command of the row's choice with its slug, which the layers selection carries out
 * (`LayersSelectionEngine`).
 */
public fun layersPanelPress(
    choice: LayersPanelChoice,
    row: LayersPanelRow,
): LayersPanelPress =
    if (row.mark ==
        LayersPanelMark.Lock
    ) {
        LayersPanelPress.Locked(row.slug)
    } else {
        LayersPanelPress.Command(choice, row.slug)
    }
