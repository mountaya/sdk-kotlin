package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class LayersSelectionTest {
    private class Refused : Exception("refused")

    @Test
    fun aSelectionHoldsNothingUntilItIsGivenAChoice() {
        val selection = LayersSelection()

        assertNull(selection.catalogue)
        assertNull(selection.terrain)
        assertNull(selection.overlay)
        assertTrue(selection.enrichments.isEmpty())
        assertNull(selection.refusal)
        assertNull(selection.instant)
    }

    @Test
    fun aSelectionHoldsTheCatalogueTheChoicesAndARefusalAsGiven() {
        val catalogue = TileFixture.layers(emptyList())
        val pinned = Instant.parse("2026-03-15T12:00:00Z")
        val selection = LayersSelection(catalogue, "outdoors", "slopes", listOf("hillshade"), Refused(), pinned)

        assertEquals(catalogue, selection.catalogue)
        assertEquals("outdoors", selection.terrain)
        assertEquals("slopes", selection.overlay)
        assertEquals(listOf("hillshade"), selection.enrichments)
        assertIs<Refused>(selection.refusal)
        assertEquals(pinned, selection.instant)
    }

    /**
     * Kotlin's every value has an equality, which a state flow reports changes by: two selections holding the same
     * are one state, and two refusals, the same throwable or not, are two.
     */
    @Test
    fun twoSelectionsAreEqualWhereTheyHoldTheSameARefusalByItsIdentity() {
        val refused = Refused()

        assertEquals(LayersSelection(terrain = "outdoors"), LayersSelection(terrain = "outdoors"))
        assertEquals(LayersSelection(refusal = refused), LayersSelection(refusal = refused))
        assertEquals(LayersSelection(refusal = refused).hashCode(), LayersSelection(refusal = refused).hashCode())
        assertNotEquals(LayersSelection(refusal = refused), LayersSelection(refusal = Refused()))
        assertNotEquals(
            LayersSelection(enrichments = listOf("a", "b")),
            LayersSelection(enrichments = listOf("b", "a")),
        )
        val pinned = Instant.parse("2026-03-15T12:00:00Z")
        assertEquals(
            LayersSelection(instant = pinned),
            LayersSelection(instant = Instant.parse("2026-03-15T13:30:00+01:30")),
        )
        assertEquals(LayersSelection(instant = pinned).hashCode(), LayersSelection(instant = pinned).hashCode())
        assertNotEquals(LayersSelection(instant = pinned), LayersSelection())
    }
}
