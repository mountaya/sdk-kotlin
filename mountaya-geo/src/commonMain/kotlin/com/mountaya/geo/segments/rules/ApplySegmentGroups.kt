package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.RestSchema

/**
 * SEGMENT_GROUP_ID_PREFIX opens the id of the one segment a collapsed group stands as, `group:`
 * then the group's id. A segment's own id is a UUID, which never holds a colon, so no stored
 * segment can carry it.
 */
public const val SEGMENT_GROUP_ID_PREFIX: String = "group:"

/**
 * applySegmentGroups projects an itinerary's chain of [segments], in chain order, for display with
 * each of its [groups] collapsed into one segment, so a group reads as one leg.
 *
 * A group's members become one segment at the place of its first member in the chain, the others
 * dropped from where they stand. Its id is [SEGMENT_GROUP_ID_PREFIX] then the group's id, and it
 * carries the group's id, name, colour, speed, revision, creator and creation instant, and the
 * first member's mode, position and start, with no description and no steps. Its geometry is a
 * GeometryCollection of every line its members hold, in chain order, a directions member's vias left
 * out. Each analysis range, slope, speed, surface, way type and trail difficulty, is its member's
 * own, its indexes moved on by the count of positions the lines of the members before it hold, so
 * every band stands where it stood; the five lists are empty where no member states an entry. Its
 * road access ranges are moved on the same way, a member that states none, null, adding none, and
 * are null where no member states them: one member that states `[]`, analysed with no restriction
 * found, makes the group's `[]`, so a group with an analysed member never reads as unanalysed.
 * Distance, duration, ascent and descent are summed and the altitude and gradient extremes taken
 * across the members, a figure a member leaves null left out, and null where none states it.
 *
 * A segment of no group, or of a group [groups] does not list, passes as itself, the same instance,
 * and with no group to collapse the answer is [segments] itself.
 */
public fun applySegmentGroups(
    segments: List<RestSchema.ItinerarySegment>,
    groups: List<RestSchema.ItinerarySegmentGroup>,
): List<RestSchema.ItinerarySegment> {
    if (groups.isEmpty()) return segments
    val byId = groups.associateBy { it.id }
    val members = segments.filter { it.group_id in byId }.groupBy { it.group_id }
    if (members.isEmpty()) return segments
    val collapsed = HashSet<String>()
    val projected = ArrayList<RestSchema.ItinerarySegment>(segments.size)
    for (segment in segments) {
        val groupId = segment.group_id
        val group = groupId?.let(byId::get)
        if (groupId == null || group == null) {
            projected += segment
            continue
        }
        if (!collapsed.add(groupId)) continue
        projected += collapsedGroup(group, members.getValue(groupId), segment)
    }
    return projected
}

/** The one segment [group] stands as over its [members], in chain order, [head] the first. */
private fun collapsedGroup(
    group: RestSchema.ItinerarySegmentGroup,
    members: List<RestSchema.ItinerarySegment>,
    head: RestSchema.ItinerarySegment,
): RestSchema.ItinerarySegment {
    val lines = ArrayList<GeoJsonLineString>()
    val slope = ArrayList<RestSchema.SlopeInfo>()
    val speed = ArrayList<RestSchema.SpeedInfo>()
    val surface = ArrayList<RestSchema.SurfaceInfo>()
    val wayType = ArrayList<RestSchema.WayTypeInfo>()
    val trailDifficulty = ArrayList<RestSchema.TrailDifficultyInfo>()
    // Null until a member states its road access, so the group tells unanalysed from analysed.
    var roadAccess: ArrayList<RestSchema.RoadAccessInfo>? = null
    var figures = Figures()
    // The count of positions the members before this one hold, which its indexes are moved on by.
    var offset = 0
    for (member in members) {
        var positions = 0
        for (line in linesOf(member.geojson)) {
            lines += GeoJsonLineString(line)
            positions += line.size
        }
        member.slope_info.orEmpty().mapTo(slope) {
            RestSchema.SlopeInfo(it.from_index + offset, it.to_index + offset, it.value)
        }
        member.speed_info.orEmpty().mapTo(speed) {
            RestSchema.SpeedInfo(it.from_index + offset, it.to_index + offset, it.value)
        }
        member.surface_info.orEmpty().mapTo(surface) {
            RestSchema.SurfaceInfo(
                it.from_index + offset,
                it.to_index + offset,
                it.value,
            )
        }
        member.way_type_info.orEmpty().mapTo(wayType) {
            RestSchema.WayTypeInfo(
                it.from_index + offset,
                it.to_index + offset,
                it.value,
            )
        }
        member.trail_difficulty_info.orEmpty().mapTo(trailDifficulty) {
            RestSchema.TrailDifficultyInfo(it.from_index + offset, it.to_index + offset, it.value)
        }
        val stated = member.road_access_info
        if (stated != null) {
            val held = roadAccess ?: ArrayList()
            stated.mapTo(held) {
                RestSchema.RoadAccessInfo(it.from_index + offset, it.to_index + offset, it.values)
            }
            roadAccess = held
        }
        offset += positions
        figures = figures.with(member)
    }
    return RestSchema.ItinerarySegment(
        altitude_max = figures.altitudeMax,
        altitude_min = figures.altitudeMin,
        ascent = figures.ascent,
        color = group.color,
        created_at = group.created_at,
        created_by = group.created_by,
        descent = figures.descent,
        description = null,
        distance = figures.distance,
        duration = figures.duration,
        geojson = GeoJsonGeometryCollection(lines),
        gradient_max = figures.gradientMax,
        gradient_min = figures.gradientMin,
        group_id = group.id,
        id = SEGMENT_GROUP_ID_PREFIX + group.id,
        itinerary_id = group.itinerary_id,
        mode = head.mode,
        name = group.name,
        position = head.position,
        revision = group.revision,
        road_access_info = roadAccess,
        slope_info = slope,
        speed = group.speed,
        speed_info = speed,
        start_at = head.start_at,
        steps = emptyList(),
        surface_info = surface,
        trail_difficulty_info = trailDifficulty,
        way_type_info = wayType,
    )
}

/** The figures a group folds from its members, each null until one states it. */
private class Figures(
    val altitudeMax: Int? = null,
    val altitudeMin: Int? = null,
    val ascent: Int? = null,
    val descent: Int? = null,
    val distance: Double? = null,
    val duration: Double? = null,
    val gradientMax: Double? = null,
    val gradientMin: Double? = null,
) {
    /** These figures with [member]'s folded in. */
    fun with(member: RestSchema.ItinerarySegment): Figures =
        Figures(
            altitudeMax = fold(altitudeMax, member.altitude_max) { a, b -> maxOf(a, b) },
            altitudeMin = fold(altitudeMin, member.altitude_min) { a, b -> minOf(a, b) },
            ascent = fold(ascent, member.ascent) { a, b -> a + b },
            descent = fold(descent, member.descent) { a, b -> a + b },
            distance = fold(distance, member.distance) { a, b -> a + b },
            duration = fold(duration, member.duration) { a, b -> a + b },
            gradientMax = fold(gradientMax, member.gradient_max) { a, b -> maxOf(a, b) },
            gradientMin = fold(gradientMin, member.gradient_min) { a, b -> minOf(a, b) },
        )
}

/** [held] and [stated] combined by [combine], either left out where it is null, and null where both are. */
private fun <T : Any> fold(
    held: T?,
    stated: T?,
    combine: (T, T) -> T,
): T? =
    if (held == null) {
        stated
    } else if (stated == null) {
        held
    } else {
        combine(held, stated)
    }
