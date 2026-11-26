package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class SearchRowTest {
    private fun row(
        title: String,
        matched: List<IntRange> = listOf(0 until 1),
    ) = SearchRow(
        "zone:z",
        title,
        matched,
        SearchRowSecondary.Text("x"),
        emptyList(),
        SearchRowPayload.Zone("z", null, null),
    )

    @Test
    fun twoRowsAreEqualWhereEveryMemberIs() {
        assertEquals(row("Zone"), row("Zone"))
        assertEquals(row("Zone").hashCode(), row("Zone").hashCode())
        assertNotEquals(row("Zone"), row("Other"))
    }

    /** The parts are the title cut at what the query matched, ready to draw, the unit every SDK shares. */
    @Test
    fun thePartsAreTheTitleCutAtItsMatchedRuns() {
        assertEquals(
            listOf(
                HighlightPart("Col du ", match = false),
                HighlightPart("Gali", match = true),
                HighlightPart("bier", match = false),
            ),
            row("Col du Galibier", listOf(7 until 11)).parts,
        )
        assertEquals(listOf(HighlightPart("Chamonix", match = false)), row("Chamonix", emptyList()).parts)
        assertEquals("Col du Galibier", row("Col du Galibier", listOf(7 until 11)).parts.joinToString("") { it.text })
    }

    @Test
    fun aRowBuiltForAQueryIsDrawnInTheSamePartsAsItsTitleMatchedAgainstIt() {
        val zone =
            DataSchema.ZoneSearchResult {
                id = "z"
                name = "Réserve des Aiguilles Rouges"
            }
        val built = zoneRow(zone, "aiguilles") ?: throw AssertionError("no row")

        assertEquals(highlightParts("Réserve des Aiguilles Rouges", "aiguilles"), built.parts)
        assertSame(built.parts, built.parts, "cut once, when first read")
    }
}
