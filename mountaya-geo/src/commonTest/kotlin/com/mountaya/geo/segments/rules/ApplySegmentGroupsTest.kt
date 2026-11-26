package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Instant

class ApplySegmentGroupsTest {
    private val group =
        RestSchema.ItinerarySegmentGroup(
            color = "#e9501e",
            created_at = Instant.parse("2026-01-01T00:00:00Z"),
            created_by = RestSchema.RelationToUser(id = "user-1"),
            id = "g1",
            itinerary_id = "itin-1",
            name = "Approach",
            revision = 3,
            speed = 4.5,
        )

    private fun line(vararg longitudes: Double) = GeoJsonLineString(longitudes.map { listOf(it, 45.0) })

    private fun slope(vararg ranges: IntRange) = ranges.map { RestSchema.SlopeInfo(it.first, it.last, 5.0) }

    @Test
    fun aGroupCollapsesAtItsFirstMemberCarryingItsIdentity() {
        val s1 = restSegment("s1", 0, line(6.0, 6.01))
        val s2 =
            restSegment(
                "s2",
                1,
                line(6.02, 6.03, 6.04),
                distance = 1_000.0,
                ascent = 100,
                gradient = -2.0 to 12.0,
                groupId = "g1",
                slope =
                    slope(0..2),
            )
        val s3 =
            restSegment(
                "s3",
                2,
                line(6.05, 6.06, 6.07, 6.08),
                distance = 1_000.0,
                ascent = 50,
                gradient =
                    -8.0 to 12.0,
                groupId = "g1",
                slope = slope(0..3),
            )
        val s4 = restSegment("s4", 3, line(6.09, 6.1))
        val projected = applySegmentGroups(listOf(s1, s2, s3, s4), listOf(group))

        assertEquals(listOf("s1", "group:g1", "s4"), projected.map { it.id })
        assertSame(s1, projected[0])
        assertSame(s4, projected[2])
        val collapsed = projected[1]
        assertEquals(
            listOf("Approach", "#e9501e", "itin-1", "g1"),
            listOf(collapsed.name, collapsed.color, collapsed.itinerary_id, collapsed.group_id),
        )
        assertEquals(listOf(1, 3), listOf(collapsed.position, collapsed.revision))
        assertEquals(4.5, collapsed.speed)
        assertEquals(group.created_by, collapsed.created_by)
        assertEquals(2_000.0, collapsed.distance)
        assertEquals(150, collapsed.ascent)
        assertEquals(-8.0 to 12.0, collapsed.gradient_min to collapsed.gradient_max)
        assertNull(collapsed.duration)
        assertNull(collapsed.description)
        assertEquals(emptyList(), collapsed.steps)
        assertEquals(
            GeoJsonGeometryCollection(listOf(line(6.02, 6.03, 6.04), line(6.05, 6.06, 6.07, 6.08))),
            collapsed.geojson,
        )
        assertEquals(slope(0..2, 3..6), collapsed.slope_info)
        assertEquals(emptyList(), collapsed.speed_info)
        assertNull(collapsed.road_access_info)
    }

    private fun access(
        range: IntRange,
        vararg values: String,
    ) = RestSchema.RoadAccessInfo(range.first, range.last, values.map(RestSchema::RoadAccessRestrictionType))

    private fun group(id: String) =
        RestSchema.ItinerarySegmentGroup(
            color = "#1e88e9",
            created_at = Instant.parse("2026-01-02T00:00:00Z"),
            created_by = RestSchema.RelationToUser(id = "user-1"),
            id = id,
            itinerary_id = "itin-1",
            name = id,
            revision = 1,
            speed = null,
        )

    /**
     * A member stating null adds no range yet its positions still move the next member's on; `[]`
     * stated by one member makes the group's `[]`, and a group none of whose members states one is null.
     */
    @Test
    fun roadAccessRangesMoveOnAndAGroupIsNullOnlyWhereNoMemberStatesThem() {
        val s1 =
            restSegment("s1", 0, line(6.0, 6.01, 6.02), groupId = "g1", roadAccess = listOf(access(0..1, "private")))
        val s2 = restSegment("s2", 1, line(6.03, 6.04), groupId = "g1")
        val s3 =
            restSegment(
                "s3",
                2,
                line(6.05, 6.06, 6.07, 6.08),
                groupId = "g1",
                roadAccess = listOf(access(1..3, "destination", "permissive")),
            )
        val s4 = restSegment("s4", 3, line(6.09, 6.1), groupId = "g2")
        val s5 = restSegment("s5", 4, line(6.11, 6.12), groupId = "g2", roadAccess = emptyList())
        val s6 = restSegment("s6", 5, line(6.13, 6.14), groupId = "g3")
        val s7 = restSegment("s7", 6, line(6.15, 6.16), groupId = "g3")
        val s8 = restSegment("s8", 7, line(6.17, 6.18), roadAccess = listOf(access(0..1, "no")))
        val projected =
            applySegmentGroups(listOf(s1, s2, s3, s4, s5, s6, s7, s8), listOf(group, group("g2"), group("g3")))

        assertEquals(listOf("group:g1", "group:g2", "group:g3", "s8"), projected.map { it.id })
        assertEquals(
            listOf(access(0..1, "private"), access(6..8, "destination", "permissive")),
            projected[0].road_access_info,
        )
        assertEquals(emptyList(), projected[1].road_access_info)
        assertNull(projected[2].road_access_info)
        assertSame(s8, projected[3])
    }

    @Test
    fun aRangeAMemberDrawsAsAMultiLineStringMovesOnByEveryPositionOfItsLines() {
        val s1 =
            restSegment(
                "s1",
                0,
                GeoJsonGeometryCollection(listOf(line(6.0, 6.01), GeoJsonMultiPoint(listOf(listOf(6.0, 45.0))))),
                groupId = "g1",
                roadAccess = emptyList(),
            )
        val s2 =
            restSegment(
                "s2",
                1,
                GeoJsonMultiLineString(
                    listOf(listOf(listOf(6.02, 45.0), listOf(6.03, 45.0)), listOf(listOf(6.04, 45.0))),
                ),
                groupId = "g1",
            )
        val s3 = restSegment("s3", 2, line(6.05, 6.06), groupId = "g1", roadAccess = listOf(access(0..1, "permit")))
        val projected = applySegmentGroups(listOf(s1, s2, s3), listOf(group))

        assertEquals(listOf(access(5..6, "permit")), projected.single().road_access_info)
    }

    @Test
    fun membersApartInTheChainCollapseAtTheFirstAndTheirViasAreLeftOut() {
        val routed = GeoJsonGeometryCollection(listOf(line(6.0, 6.01), GeoJsonMultiPoint(listOf(listOf(6.0, 45.0)))))
        val s1 = restSegment("s1", 0, routed, groupId = "g1", slope = slope(0..1))
        val s2 = restSegment("s2", 1, line(6.02, 6.03))
        val s3 = restSegment("s3", 2, line(6.04, 6.05), groupId = "g1", slope = slope(0..1))
        val projected = applySegmentGroups(listOf(s1, s2, s3), listOf(group))

        assertEquals(listOf("group:g1", "s2"), projected.map { it.id })
        assertEquals(GeoJsonGeometryCollection(listOf(line(6.0, 6.01), line(6.04, 6.05))), projected[0].geojson)
        assertEquals(slope(0..1, 2..3), projected[0].slope_info)
        assertEquals(0, projected[0].position)
    }

    @Test
    fun withNoGroupOrNoListedOneTheChainPassesAsItIs() {
        val segments = listOf(restSegment("s1", 0, groupId = "g9"), restSegment("s2", 1))

        assertSame(segments, applySegmentGroups(segments, emptyList()))
        assertSame(segments, applySegmentGroups(segments, listOf(group)))
        assertEquals("group:", SEGMENT_GROUP_ID_PREFIX)
    }
}
