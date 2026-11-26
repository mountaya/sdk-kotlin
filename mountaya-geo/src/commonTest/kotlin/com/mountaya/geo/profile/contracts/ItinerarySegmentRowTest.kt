package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ItinerarySegmentRowTest {
    private fun row(
        memberIds: List<String> = emptyList(),
        color: String? = null,
    ): ItinerarySegmentRow =
        ItinerarySegmentRow(
            key = "group:g1",
            id = "group:g1",
            memberIds = memberIds,
            name = "Over the col",
            color = color,
            index = 1,
            from = 1_000.0,
            to = 3_000.0,
            metrics = ItinerarySegmentRowMetrics(sectionDistance = 2_000.0),
        )

    @Test
    fun aRowIsASpanOfTheAxis() {
        val span: ProfileSpan<*> = row()

        assertEquals(1_000.0, span.from)
        assertEquals(3_000.0, span.to)
    }

    @Test
    fun twoRowsAreEqualWhereEveryMemberIs() {
        assertEquals(row(memberIds = listOf("s2", "s3")), row(memberIds = listOf("s2", "s3")))
        assertEquals(row(memberIds = listOf("s2", "s3")).hashCode(), row(memberIds = listOf("s2", "s3")).hashCode())
        assertNotEquals(row(memberIds = listOf("s3", "s2")), row(memberIds = listOf("s2", "s3")))
        assertNotEquals(row(), row(color = "#e8590c"))
    }
}
