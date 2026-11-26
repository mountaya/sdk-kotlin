package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.ItinerarySegmentRow
import com.mountaya.geo.ItinerarySegmentRowMetrics
import com.mountaya.geo.ItineraryWaypointRowMetrics
import com.mountaya.geo.ProfilePlace
import com.mountaya.geo.ProfileWaypoint
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.WaypointDetails
import com.mountaya.geo.WaypointField
import com.mountaya.geo.WordBundle
import com.mountaya.geo.WordOverrides
import com.mountaya.geo.scopeOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Instant

/** The texts [value] lists, failing on any other value. */
internal fun profileTextList(value: FixtureValue?): List<String> {
    val items = (value as? FixtureValue.Array)?.items ?: throw AssertionError("a list of texts is not an array")
    return items.map {
        (it as? FixtureValue.Text)?.value
            ?: throw AssertionError("a list of texts holds a value that is no text")
    }
}

/** The texts [value] lists, empty where the case leaves it out. */
internal fun profileOptionalTextList(value: FixtureValue?): List<String> =
    if (value == null || value is FixtureValue.Null) emptyList() else profileTextList(value)

/** A segment's stated `metrics`, any of its ten figures. */
internal fun fixtureSegmentMetrics(value: FixtureValue?): ItinerarySegmentRowMetrics {
    val metrics = value as? FixtureValue.Object ?: throw AssertionError("`metrics` is not an object")
    return ItinerarySegmentRowMetrics(
        distance = metrics.numberOrNull("distance"),
        sectionDistance = metrics.numberOrNull("sectionDistance"),
        ascent = metrics.numberOrNull("ascent"),
        descent = metrics.numberOrNull("descent"),
        sectionAscent = metrics.numberOrNull("sectionAscent"),
        sectionDescent = metrics.numberOrNull("sectionDescent"),
        altitudeMin = metrics.numberOrNull("altitudeMin"),
        altitudeMax = metrics.numberOrNull("altitudeMax"),
        duration = metrics.numberOrNull("duration"),
        difficulty = metrics.textOrNull("difficulty")?.let { RestSchema.TrailDifficultyType(it) },
    )
}

/** A row's ten figures, each null where it has none. */
internal fun segmentMetricsValue(metrics: ItinerarySegmentRowMetrics): FixtureValue =
    objectValue(
        "altitudeMax" to numberOrNull(metrics.altitudeMax),
        "altitudeMin" to numberOrNull(metrics.altitudeMin),
        "ascent" to numberOrNull(metrics.ascent),
        "descent" to numberOrNull(metrics.descent),
        "difficulty" to textOrNull(metrics.difficulty?.value),
        "distance" to numberOrNull(metrics.distance),
        "duration" to numberOrNull(metrics.duration),
        "sectionAscent" to numberOrNull(metrics.sectionAscent),
        "sectionDescent" to numberOrNull(metrics.sectionDescent),
        "sectionDistance" to numberOrNull(metrics.sectionDistance),
    )

/** A segment row, its colour and members only where it has them. */
internal fun segmentRowValue(row: ItinerarySegmentRow): FixtureValue {
    val entries =
        mutableListOf(
            "from" to FixtureValue.Number(row.from),
            "id" to FixtureValue.Text(row.id),
            "index" to FixtureValue.Number(row.index.toDouble()),
            "key" to FixtureValue.Text(row.key),
            "metrics" to segmentMetricsValue(row.metrics),
            "name" to FixtureValue.Text(row.name),
            "to" to FixtureValue.Number(row.to),
        )
    row.color?.let { entries += "color" to FixtureValue.Text(it) }
    if (row.memberIds.isNotEmpty()) entries += "memberIds" to textArray(row.memberIds)
    return FixtureValue.Object(entries)
}

/**
 * A waypoint a profile fixture writes: its id, placed at its `distance` or its `lngLat`, with its name, glyph,
 * flags, stated metrics and details where it states them.
 */
internal fun fixturePlace(value: FixtureValue): ProfilePlace {
    val place = value as? FixtureValue.Object ?: throw AssertionError("a waypoint is not an object")
    val id = place.text("id")
    val placement =
        if (place["lngLat"] != null) {
            ProfileWaypoint.AtPosition(id, fixturePosition(place["lngLat"], "lngLat"))
        } else {
            ProfileWaypoint.AtDistance(id, place.number("distance"))
        }
    val metrics = place["metrics"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureSegmentMetrics)
    return ProfilePlace(
        placement,
        name = place.textOrNull("name"),
        glyph = place.textOrNull("glyph")?.let { RestSchema.WaypointIcon(it) },
        divides = (place["divides"] as? FixtureValue.Bool)?.value,
        isPinned = (place["pin"] as? FixtureValue.Bool)?.value ?: true,
        metrics = metrics,
        details = fixtureDetails(place["details"]),
    )
}

/** A waypoint row's figures, each null where it has none. */
internal fun waypointMetricsValue(metrics: ItineraryWaypointRowMetrics): FixtureValue =
    objectValue(
        "altitude" to numberOrNull(metrics.altitude),
        "ascent" to numberOrNull(metrics.ascent),
        "descent" to numberOrNull(metrics.descent),
        "distance" to FixtureValue.Number(metrics.distance),
        "sectionAscent" to numberOrNull(metrics.sectionAscent),
        "sectionDescent" to numberOrNull(metrics.sectionDescent),
        "sectionDistance" to numberOrNull(metrics.sectionDistance),
    )

/**
 * What a place records, as `geo.data.itinerary-waypoints` reads it: its services, its timing, its contact and its
 * fields, null where the case states none.
 */
internal fun fixtureDetails(value: FixtureValue?): WaypointDetails? {
    if (value == null || value is FixtureValue.Null) return null
    val details = value as? FixtureValue.Object ?: throw AssertionError("`details` is not an object")
    val amenities =
        details["amenities"]?.takeUnless { it is FixtureValue.Null }?.let { written ->
            profileTextList(written).map { RestSchema.WaypointAmenity(it) }
        }
    val timing =
        details.objectOrNull("timing")?.let { written ->
            WaypointDetails.Timing(
                cutoffAt = written.textOrNull("cutoffAt")?.let { Instant.parse(it) },
                stopDuration = written.numberOrNull("stopDuration")?.let(::wholeStop),
            )
        }
    val contact =
        details.objectOrNull("contact")?.let { written ->
            WaypointDetails.Contact(
                address = written.textOrNull("address"),
                bookingUrl = written.textOrNull("bookingUrl"),
                email = written.textOrNull("email"),
                openingHours = written.textOrNull("openingHours"),
                phone = written.textOrNull("phone"),
                website = written.textOrNull("website"),
            )
        }
    val properties =
        details["properties"]?.takeUnless { it is FixtureValue.Null }?.let { written ->
            val fields = (written as? FixtureValue.Array)?.items ?: throw AssertionError("`properties` is not an array")
            fields.map(::fixtureProperty)
        }
    return WaypointDetails(amenities = amenities, timing = timing, contact = contact, properties = properties)
}

/** A stop's seconds, which a planned stop states whole. */
private fun wholeStop(seconds: Double): Int {
    val whole = seconds.toInt()
    if (whole.toDouble() != seconds) throw AssertionError("`stopDuration` is no whole number")
    return whole
}

/** One custom field, its `format` where it declares one. */
internal fun fixtureProperty(value: FixtureValue?): WaypointDetails.Property {
    val field = value as? FixtureValue.Object ?: throw AssertionError("a field is not an object")
    return WaypointDetails.Property(
        field.text("key"),
        field.text("label"),
        field.text("value"),
        field.textOrNull("format")?.let { RestSchema.WaypointPropertyFormat(it) },
    )
}

/** A written field, its `href` only where it links. */
internal fun fieldValue(field: WaypointField): FixtureValue {
    val entries =
        mutableListOf<Pair<String, FixtureValue>>(
            "key" to FixtureValue.Text(field.key),
            "label" to FixtureValue.Text(field.label),
            "value" to FixtureValue.Text(field.value),
        )
    field.href?.let { entries += "href" to FixtureValue.Text(it) }
    return FixtureValue.Object(entries)
}

/** A reader's `{language, length}`, every other preference the default. */
internal fun readerUnits(value: FixtureValue?): UnitPreferences {
    val units = value as? FixtureValue.Object ?: throw AssertionError("the case's `units` is not an object")
    return UnitPreferences(units.text("language"), RestSchema.UnitLength(units.text("length")))
}

/** The caller's formatter a case states by what it answers, at [key], or null where it states none. */
internal fun fixtureFormatter(
    input: FixtureValue.Object,
    key: String,
): ((Instant) -> String)? {
    val moment = input.textOrNull(key) ?: return null
    return { moment }
}

/**
 * The elements' scope whose every word of the `amenities` and `waypointCard` sections is its own path,
 * `waypointCard.phone`, so a text read through it names its word.
 */
internal val referencedScope: ElementsScope by lazy {
    val sections =
        listOf("amenities", "waypointCard").mapNotNull { section ->
            WordBundle.elements.english[section]?.let { section to referenced(it, section) }
        }
    scopeOf(
        ElementsScope.standard,
        ElementsScopeStatement(words = WordOverrides.BySection(JsonObject(sections.toMap()))),
    )
}

/** A section of words with every word replaced by its path. */
private fun referenced(
    value: JsonElement,
    path: String,
): JsonElement {
    val members = value as? JsonObject ?: return JsonPrimitive(path)
    return JsonObject(members.mapValues { (key, member) -> referenced(member, "$path.$key") })
}

/** A text read through [referencedScope]: `{word}` where it is a word's path, else `{value}`, the text itself. */
internal fun wordValue(text: String): FixtureValue {
    val prefix = listOf("amenities.", "waypointCard.").firstOrNull { text.startsWith(it) }
    val isPath = prefix != null && text.removePrefix(prefix).all { it in 'a'..'z' || it in 'A'..'Z' || it == '-' }
    return objectValue((if (isPath) "word" else "value") to FixtureValue.Text(text))
}
