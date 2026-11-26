package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/** The creation instant every segment a fixture does not date is given; no rule reads it. */
private val placeholderInstant = Instant.fromEpochMilliseconds(0)

/**
 * The segment [fields] writes, built as the REST API's `ItinerarySegment` with the members the
 * fixture writes, in the wire's spelling, and a placeholder for every other the type requires, which
 * no profile rule reads: an id of `segment`, position 0, the `linestring` mode, and so on, its [steps] the ones a
 * runner read for it, none where it hands none. A number JSON cannot carry is kept as the fixture decodes it, which
 * a JSON decoder would refuse.
 */
internal fun fixtureSegment(
    fields: FixtureValue.Object,
    steps: List<RestSchema.ItineraryDirectionsStep>? = null,
): RestSchema.ItinerarySegment {
    val id = fields.textOrNull("id") ?: "segment"
    return RestSchema.ItinerarySegment(
        altitude_max = wholeOrNull(fields, "altitude_max"),
        altitude_min = wholeOrNull(fields, "altitude_min"),
        ascent = wholeOrNull(fields, "ascent"),
        color = fields.textOrNull("color"),
        created_at = fields.textOrNull("created_at")?.let(Instant::parse) ?: placeholderInstant,
        created_by = RestSchema.RelationToUser(id = fields.textOrNull("created_by") ?: "user"),
        descent = wholeOrNull(fields, "descent"),
        description = fields.textOrNull("description"),
        distance = fields.numberOrNull("distance"),
        duration = fields.numberOrNull("duration"),
        geojson = fields["geojson"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry),
        gradient_max = fields.numberOrNull("gradient_max"),
        gradient_min = fields.numberOrNull("gradient_min"),
        group_id = fields.textOrNull("group_id"),
        id = id,
        itinerary_id = fields.textOrNull("itinerary_id") ?: "itinerary",
        mode =
            fields.textOrNull("mode")?.let { RestSchema.SegmentMode(it) }
                ?: RestSchema.SegmentMode.Linestring,
        name = fields.textOrNull("name") ?: id,
        position = wholeOrNull(fields, "position") ?: 0,
        revision = wholeOrNull(fields, "revision") ?: 1,
        road_access_info =
            ranges(fields, "road_access_info", member = "values") { from, to, values ->
                RestSchema.RoadAccessInfo(from, to, texts(values).map(RestSchema::RoadAccessRestrictionType))
            },
        slope_info = ranges(fields, "slope_info") { from, to, value -> RestSchema.SlopeInfo(from, to, number(value)) },
        speed = fields.numberOrNull("speed"),
        speed_info = ranges(fields, "speed_info") { from, to, value -> RestSchema.SpeedInfo(from, to, number(value)) },
        start_at = fields.textOrNull("start_at")?.let(Instant::parse),
        steps = steps,
        surface_info =
            ranges(
                fields,
                "surface_info",
            ) { from, to, value -> RestSchema.SurfaceInfo(from, to, RestSchema.SurfaceType(text(value))) },
        trail_difficulty_info =
            ranges(fields, "trail_difficulty_info") { from, to, value ->
                RestSchema.TrailDifficultyInfo(from, to, RestSchema.TrailDifficultyType(text(value)))
            },
        way_type_info =
            ranges(
                fields,
                "way_type_info",
            ) { from, to, value -> RestSchema.WayTypeInfo(from, to, RestSchema.WayType(text(value))) },
    )
}

/** The whole number [fields] writes under [key], or null where it writes none; fails on a fraction. */
internal fun wholeOrNull(
    fields: FixtureValue.Object,
    key: String,
): Int? {
    val value = fields.numberOrNull(key) ?: return null
    if (value != value.toInt().toDouble()) throw AssertionError("`$key` is $value, which no whole number is")
    return value.toInt()
}

/**
 * The ranges [fields] writes under [key], each `{from_index, to_index}` and what it states under
 * [member], `value` for an analysis range and `values` for a road access one, or null where it
 * writes none.
 */
private fun <T> ranges(
    fields: FixtureValue.Object,
    key: String,
    member: String = "value",
    build: (from: Int, to: Int, value: FixtureValue) -> T,
): List<T>? {
    if (fields[key] == null || fields[key] is FixtureValue.Null) return null
    return fields.items(key).map { item ->
        val range = item as? FixtureValue.Object ?: throw AssertionError("an entry of `$key` is not an object")
        build(
            wholeOrNull(range, "from_index") ?: throw AssertionError("an entry of `$key` has no `from_index`"),
            wholeOrNull(range, "to_index") ?: throw AssertionError("an entry of `$key` has no `to_index`"),
            range[member] ?: throw AssertionError("an entry of `$key` has no `$member`"),
        )
    }
}

private fun number(value: FixtureValue): Double =
    (value as? FixtureValue.Number)?.value ?: throw AssertionError("a range's value is not a number")

private fun text(value: FixtureValue): String =
    (value as? FixtureValue.Text)?.value ?: throw AssertionError("a range's value is not a string")

private fun texts(value: FixtureValue): List<String> =
    (value as? FixtureValue.Array)?.items?.map(::text) ?: throw AssertionError("a range's values are not an array")
