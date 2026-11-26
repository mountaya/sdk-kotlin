package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class PanelLayoutOfTest {
    @Test
    fun aWideMapLaysThePanelAtItsWidth() {
        assertEquals(PanelLayout(340.0, false, 428.0, 52.0, 3, 2), panelLayoutOf(1024.0, 520.0, 92.0))
        assertEquals(PanelLayout(340.0, false, 428.0, 52.0, 3, 2), panelLayoutOf(577.0, 520.0, 92.0))
    }

    @Test
    fun aNarrowMapFillsItsWidthAndKeepsRoomUnderThePanel() {
        assertEquals(PanelLayout(null, true, 412.0, 40.0, 3, 2), panelLayoutOf(576.0, 520.0, 92.0))
        assertEquals(PanelLayout(null, true, 812.0, 40.0, 3, 2), panelLayoutOf(390.0, 844.0, 16.0))
    }

    @Test
    fun aMapShorterThanItsInsetLeavesNoHeight() {
        assertEquals(0.0, panelLayoutOf(800.0, 60.0, 92.0).maxHeight)
    }
}
