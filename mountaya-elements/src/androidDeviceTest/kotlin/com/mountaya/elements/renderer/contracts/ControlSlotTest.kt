package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class ControlSlotTest {
    @Test
    fun theStackReadsZoomThePanelTogglesAsAGroupTheAppsOwnGeolocateTheCompass() {
        assertEquals(
            listOf(
                ControlSlot.Zoom,
                ControlSlot.Panels(listOf(MapPanel.Layers, MapPanel.Pois, MapPanel.Preferences)),
                ControlSlot.Content,
                ControlSlot.Geolocate,
                ControlSlot.Compass,
            ),
            ControlSlot.stack(
                zoom = true,
                layers = true,
                pois = true,
                preferences = true,
                geolocate = true,
                compass = true,
            ),
        )
    }

    @Test
    fun eachControlIsOffUntilItsFlagTurnsItOnTheAppsOwnStandingWhatever() {
        assertEquals(
            listOf(ControlSlot.Content),
            ControlSlot.stack(
                zoom = false,
                layers = false,
                pois = false,
                preferences = false,
                geolocate = false,
                compass = false,
            ),
        )
        assertEquals(
            listOf(ControlSlot.Panels(listOf(MapPanel.Pois)), ControlSlot.Content, ControlSlot.Compass),
            ControlSlot.stack(
                zoom = false,
                layers = false,
                pois = true,
                preferences = false,
                geolocate = false,
                compass = true,
            ),
        )
        assertEquals(
            listOf(
                ControlSlot.Zoom,
                ControlSlot.Panels(listOf(MapPanel.Preferences)),
                ControlSlot.Content,
                ControlSlot.Geolocate,
            ),
            ControlSlot.stack(
                zoom = true,
                layers = false,
                pois = false,
                preferences = true,
                geolocate = true,
                compass = false,
            ),
        )
    }
}
