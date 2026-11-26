package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class LayersPanelPressTest {
    @Test
    fun aPressAsksTheCommandOfItsSectionsChoiceWithTheRowsSlug() {
        val row = LayersPanelRow("snow-depth", "Snow depth", isChecked = false, mark = null)

        assertEquals<LayersPanelPress>(
            LayersPanelPress.Command(LayersPanelChoice.Overlay, "snow-depth"),
            layersPanelPress(LayersPanelChoice.Overlay, row),
        )
        assertEquals<LayersPanelPress>(
            LayersPanelPress.Command(LayersPanelChoice.Terrain, "snow-depth"),
            layersPanelPress(LayersPanelChoice.Terrain, row),
        )
        assertEquals<LayersPanelPress>(
            LayersPanelPress.Command(LayersPanelChoice.Enrichment, "pois"),
            layersPanelPress(
                LayersPanelChoice.Enrichment,
                LayersPanelRow("pois", "POIs", isChecked = true, mark = LayersPanelMark.Tick),
            ),
        )
    }

    @Test
    fun aPressOnARowShowingItsLockReportsTheEntryAndSelectsNothing() {
        val row = LayersPanelRow("snow-depth", "Snow depth", isChecked = true, mark = LayersPanelMark.Lock)

        assertEquals<LayersPanelPress>(
            LayersPanelPress.Locked("snow-depth"),
            layersPanelPress(LayersPanelChoice.Overlay, row),
        )
    }
}
