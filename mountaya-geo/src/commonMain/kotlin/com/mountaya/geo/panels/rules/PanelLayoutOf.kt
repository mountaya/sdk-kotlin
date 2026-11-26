package com.mountaya.geo

import com.mountaya.geo.generated.ElementLayout

/**
 * panelLayoutOf lays out a panel the controls open over a map [mapWidth] wide and [mapHeight] tall, its top
 * [topInset] below the map's top edge, each in density-independent pixels (`geo.panels.layout`), as the embed lays out
 * its own: `ElementLayout.panelWidth` wide, and filling the width its corner leaves on a map no wider than
 * `ElementLayout.panelNarrowMapWidth`; its height capped at the map's less its top inset, and on that narrow map less
 * `ElementLayout.panelNarrowBottomInset` more, the room the embed keeps under it there, never below 0; a layer card's
 * picture `ElementLayout.panelPictureHeight` tall, `ElementLayout.panelPictureHeightNarrow` on that narrow map; and the
 * cards in the columns the tokens table counts.
 *
 * The inset is each platform's own, since it follows the controls a panel opens beside.
 */
public fun panelLayoutOf(
    mapWidth: Double,
    mapHeight: Double,
    topInset: Double,
): PanelLayout {
    val narrow = mapWidth <= ElementLayout.panelNarrowMapWidth
    val room = mapHeight - topInset - if (narrow) ElementLayout.panelNarrowBottomInset else 0.0
    return PanelLayout(
        width = if (narrow) null else ElementLayout.panelWidth,
        fill = narrow,
        maxHeight = if (room > 0.0) room else 0.0,
        pictureHeight = if (narrow) ElementLayout.panelPictureHeightNarrow else ElementLayout.panelPictureHeight,
        layerCardColumns = ElementLayout.panelLayerCardColumns,
        preferenceCardColumns = ElementLayout.panelPreferenceCardColumns,
    )
}
