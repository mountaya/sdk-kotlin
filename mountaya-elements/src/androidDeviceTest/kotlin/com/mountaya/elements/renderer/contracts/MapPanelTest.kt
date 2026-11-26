package com.mountaya.elements

import androidx.compose.runtime.MutableState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class MapPanelTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theStacksPanelsAreNamedAsTheStackNamesThemAndAnAppsByItsOwnName() {
        assertEquals(
            listOf(
                "layers",
                "pois",
                "preferences",
            ),
            listOf(MapPanel.Layers, MapPanel.Pois, MapPanel.Preferences).map {
                it.name
            },
        )
        val own = MapPanel("filters")
        assertEquals(MapPanel("filters"), own)
        assertNotEquals(MapPanel.Layers, own)
    }

    @Test
    fun twoPanelsAreOneWhereTheirNamesAreWrittenAlike() {
        assertNotEquals(MapPanel("café"), MapPanel("café"))
        assertEquals(MapPanel("café").hashCode(), MapPanel("café").hashCode())
    }

    @Test
    fun aComposableOutOfAnyStackReadsNoPanelOpen() {
        var seen: MutableState<MapPanel?>? = null
        compose.setContent { seen = LocalMountayaOpenPanel.current }

        compose.runOnIdle { assertNull(seen) }
    }
}
