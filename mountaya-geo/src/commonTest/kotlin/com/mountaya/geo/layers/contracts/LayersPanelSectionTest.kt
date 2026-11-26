package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LayersPanelSectionTest {
    @Test
    fun twoSectionsAreEqualWhereEveryMemberIs() {
        val rows = listOf(LayersPanelRow("slopes", "Slope", isChecked = false, mark = null))
        val heading = LayersPanelHeading("layers.overlays")
        val categories =
            listOf(LayersPanelCategory("conditions", "Conditions"), LayersPanelCategory("hazards", "Hazards"))
        val section = LayersPanelSection(heading, LayersPanelChoice.Overlay, "conditions", rows, categories)

        assertEquals(section, LayersPanelSection(heading, LayersPanelChoice.Overlay, "conditions", rows, categories))
        assertNotEquals(
            section,
            LayersPanelSection(heading, LayersPanelChoice.Enrichment, "conditions", rows, categories),
        )
        assertNotEquals(section, LayersPanelSection(heading, LayersPanelChoice.Overlay, null, rows, categories))
        assertNotEquals(
            section,
            LayersPanelSection(heading, LayersPanelChoice.Overlay, "conditions", emptyList(), categories),
        )
        assertNotEquals(
            section,
            LayersPanelSection(heading, LayersPanelChoice.Overlay, "conditions", rows, categories.take(1)),
        )
        assertNotEquals(
            section,
            LayersPanelSection(
                LayersPanelHeading("layers.time"),
                LayersPanelChoice.Overlay,
                "conditions",
                rows,
                categories,
            ),
        )
    }

    @Test
    fun aSectionOfferingNoSwitchStatesNoCategory() {
        assertTrue(
            LayersPanelSection(
                LayersPanelHeading("layers.time"),
                LayersPanelChoice.Time,
                null,
                emptyList(),
            ).categories.isEmpty(),
        )
    }
}
