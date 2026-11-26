package com.mountaya.geo.layers.contracts

import com.mountaya.geo.LayersPanelChoice
import com.mountaya.geo.LayersPanelPress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LayersPanelPressTest {
    @Test
    fun twoPressesAreEqualWhereTheyAreOfOneKindAndHoldTheSame() {
        val slopes = LayersPanelPress.Command(LayersPanelChoice.Overlay, "slopes")

        assertEquals<LayersPanelPress>(slopes, LayersPanelPress.Command(LayersPanelChoice.Overlay, "slopes"))
        assertNotEquals<LayersPanelPress>(slopes, LayersPanelPress.Command(LayersPanelChoice.Terrain, "slopes"))
        assertNotEquals<LayersPanelPress>(slopes, LayersPanelPress.Locked("slopes"))
        assertEquals<LayersPanelPress>(LayersPanelPress.Locked("slopes"), LayersPanelPress.Locked("slopes"))
        assertNotEquals<LayersPanelPress>(LayersPanelPress.Locked("é"), LayersPanelPress.Locked("é"))
    }
}
