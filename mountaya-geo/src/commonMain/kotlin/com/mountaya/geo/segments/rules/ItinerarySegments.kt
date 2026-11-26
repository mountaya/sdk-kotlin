package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * itinerarySegments answers an itinerary's legs: its [segments] in chain order, by their `position`,
 * two at one position in the order they are listed, each group [groups] lists collapsed into one leg as
 * [applySegmentGroups] collapses it, under the id [SEGMENT_GROUP_ID_PREFIX] then the group's id, naming
 * the segments it stands for in chain order (`geo.data.itinerary-segments`). A segment of no group, or
 * of a group [groups] does not list, is a leg of its own, its `memberIds` empty.
 *
 * Each leg carries its name, its colour where it states one and its geometry, and states the server's
 * figures as its own section: the distance, the ascent, the descent, the duration and the altitude band
 * its segment stores, or that a group sums and spans across its members, a figure the server does not
 * state being null.
 */
public fun itinerarySegments(
    segments: List<RestSchema.ItinerarySegment>,
    groups: List<RestSchema.ItinerarySegmentGroup>,
): List<ItineraryLeg> {
    val chain = inChainOrder(segments) { it.position }
    // Every member of a listed group is collapsed, so a leg whose group is listed is a collapsed one
    // whatever its id. The groups are keyed as `applySegmentGroups` keys them, so the members named are
    // the ones it collapsed.
    val listed = groups.mapTo(HashSet()) { it.id }
    val members = LinkedHashMap<String, MutableList<String>>()
    for (segment in chain) {
        val groupId = segment.group_id ?: continue
        if (groupId in listed) members.getOrPut(groupId) { mutableListOf() } += segment.id
    }
    return applySegmentGroups(chain, groups).map { leg ->
        ItineraryLeg(
            id = leg.id,
            memberIds = leg.group_id?.takeIf { it in listed }?.let { members[it]?.toList() } ?: emptyList(),
            name = leg.name,
            color = leg.color,
            geometry = leg.geojson,
            sectionDistance = leg.distance,
            sectionAscent = leg.ascent,
            sectionDescent = leg.descent,
            duration = leg.duration,
            altitudeMin = leg.altitude_min,
            altitudeMax = leg.altitude_max,
        )
    }
}
