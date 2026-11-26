package com.mountaya.elements

import com.mountaya.geo.ItinerarySegmentRow
import com.mountaya.geo.ItinerarySegmentRowMetrics
import kotlin.test.Test
import kotlin.test.assertEquals

class IsSegmentRowLitTest {
    private fun row(
        id: String,
        index: Int,
        members: List<String> = emptyList(),
    ) = ItinerarySegmentRow(id, id, members, id, null, index, 0.0, 1.0, ItinerarySegmentRowMetrics())

    private val rows =
        listOf(row("valley", 0), row("col", 1), row("group:climb", 2, listOf("lower-climb", "upper-climb")))

    private fun lit(
        pointed: String? = null,
        highlighted: String? = null,
        hovered: String? = null,
    ): List<String> = rows.filter { isSegmentRowLit(it, pointed, highlighted, hovered) }.map { it.id }

    /**
     * A leg the map's cursor is over lights its row, a collapsed group's for each member, beside the row pointed at.
     */
    @Test
    fun aLegHoveredOnTheMapLightsItsRowAGroupsForEachMember() {
        assertEquals(listOf("col"), lit(hovered = "col"))
        assertEquals(listOf("group:climb"), lit(hovered = "upper-climb"))
        assertEquals(listOf("valley", "col"), lit(pointed = "valley", hovered = "col"))
        assertEquals(emptyList(), lit(hovered = "elsewhere"))
    }

    /** A row the app names is lit in place of the one the map's cursor would light, whatever leg it is over. */
    @Test
    fun aRowTheAppNamesIsLitInPlaceOfTheHoveredOne() {
        assertEquals(listOf("col"), lit(highlighted = "col", hovered = "valley"))
        assertEquals(listOf("valley", "col"), lit(pointed = "valley", highlighted = "col", hovered = "valley"))
        assertEquals(listOf("group:climb"), lit(highlighted = "lower-climb"))
    }
}
