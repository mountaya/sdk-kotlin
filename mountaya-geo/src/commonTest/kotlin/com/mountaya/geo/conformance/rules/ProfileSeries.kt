package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlin.time.Instant

/** The case's `{from, to}` window, null where the case states none or writes it null. */
internal fun profileWindow(value: FixtureValue?): ProfileRange? {
    if (value == null || value is FixtureValue.Null) return null
    val window = value as? FixtureValue.Object ?: throw AssertionError("`window` is not an object")
    return ProfileRange(window.number("from"), window.number("to"))
}

/**
 * The elevation series [value] writes, as the profile fixtures write one, its `hasElevation` held to whether it
 * holds a sample, so a fixture that disagrees with itself fails rather than being read past.
 */
internal fun profileSeries(value: FixtureValue?): ElevationSeries {
    val series = value as? FixtureValue.Object ?: throw AssertionError("`series` is not an object")
    val points =
        series.items("points").map { item ->
            val point = item as? FixtureValue.Object ?: throw AssertionError("a point is not an object")
            ElevationPoint(
                point.number("distance"),
                point.number("elevation"),
                fixturePosition(point["lngLat"], "lngLat"),
                point.number("index").toInt(),
            )
        }
    val read =
        ElevationSeries(
            points,
            series.number("totalDistance"),
            series.numberOrNull("minElevation"),
            series.numberOrNull("maxElevation"),
        )
    if (read.hasElevation != series.bool("hasElevation")) {
        throw AssertionError("`hasElevation` disagrees with the points the series holds")
    }
    return read
}

/** An object of [entries], in the order given. */
internal fun profileObject(vararg entries: Pair<String, FixtureValue>): FixtureValue =
    FixtureValue.Object(entries.toList())

/** [value] as a number, or null where there is none. */
internal fun profileNumber(value: Double?): FixtureValue = value?.let { FixtureValue.Number(it) } ?: FixtureValue.Null

/** [value] as a text, or null where there is none. */
internal fun profileText(value: String?): FixtureValue = value?.let { FixtureValue.Text(it) } ?: FixtureValue.Null

/** [texts] as an array of texts. */
internal fun profileTexts(texts: List<String>): FixtureValue = FixtureValue.Array(texts.map { FixtureValue.Text(it) })

/** The texts [items] holds, failing on any other value. */
internal fun profileTextsOf(items: List<FixtureValue>): List<String> =
    items.map { (it as? FixtureValue.Text)?.value ?: throw AssertionError("a list holds a value that is no string") }

/** The texts [value] lists, empty where the case leaves it out, failing on any other value. */
internal fun profileOptionalTexts(value: FixtureValue?): List<String> {
    if (value == null || value is FixtureValue.Null) return emptyList()
    val items = (value as? FixtureValue.Array)?.items ?: throw AssertionError("a list of texts is not an array")
    return profileTextsOf(items)
}

/** The figures or columns the case lists under [key], each the entry of [entries] its id names, null where the case lists none. */
internal fun <Figure> profileFigures(
    input: FixtureValue.Object,
    key: String,
    entries: List<Figure>,
    id: (Figure) -> String,
): List<Figure>? {
    val listed = input[key]
    if (listed == null || listed is FixtureValue.Null) return null
    return profileOptionalTexts(listed).map { name ->
        entries.firstOrNull { id(it) == name } ?: throw AssertionError("`$name` is no figure of `$key`")
    }
}

/** A segment's stated `metrics`, any of its ten figures. */
internal fun profileSegmentMetrics(value: FixtureValue?): ItinerarySegmentRowMetrics {
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

/** A segment row as `geo.profile.segment-rows` writes one. */
internal fun profileSegmentRow(value: FixtureValue?): ItinerarySegmentRow {
    val row = value as? FixtureValue.Object ?: throw AssertionError("the case's `row` is not an object")
    return ItinerarySegmentRow(
        key = row.text("key"),
        id = row.text("id"),
        memberIds = profileOptionalTexts(row["memberIds"]),
        name = row.textOrNull("name") ?: "",
        color = row.textOrNull("color"),
        index = row.number("index").toInt(),
        from = row.number("from"),
        to = row.number("to"),
        metrics = profileSegmentMetrics(row["metrics"] ?: FixtureValue.Object(emptyList())),
    )
}

/** A waypoint row as `geo.profile.waypoint-rows` writes one, with the place's `details` beside it. */
internal fun profileWaypointRow(value: FixtureValue?): ItineraryWaypointRow {
    val row = value as? FixtureValue.Object ?: throw AssertionError("the case's `row` is not an object")
    val figures = row.objectOrNull("metrics") ?: throw AssertionError("the row's `metrics` is not an object")
    val place =
        ProfilePlace(
            ProfileWaypoint.AtDistance(row.text("id"), row.number("to")),
            name = row.textOrNull("name"),
            glyph = row.textOrNull("glyph")?.let { RestSchema.WaypointIcon(it) },
            details = profileDetails(row["details"]),
        )
    return ItineraryWaypointRow(
        key = row.text("key"),
        place = place,
        index = row.number("index").toInt(),
        from = row.number("from"),
        to = row.number("to"),
        metrics =
            ItineraryWaypointRowMetrics(
                distance = figures.number("distance"),
                sectionDistance = figures.numberOrNull("sectionDistance"),
                ascent = figures.numberOrNull("ascent"),
                descent = figures.numberOrNull("descent"),
                sectionAscent = figures.numberOrNull("sectionAscent"),
                sectionDescent = figures.numberOrNull("sectionDescent"),
                altitude = figures.numberOrNull("altitude"),
            ),
    )
}

/** What a place records: its services, its timing, its contact and its fields, null where the case states none. */
internal fun profileDetails(value: FixtureValue?): WaypointDetails? {
    if (value == null || value is FixtureValue.Null) return null
    val details = value as? FixtureValue.Object ?: throw AssertionError("`details` is not an object")
    val amenities =
        details["amenities"]?.takeUnless { it is FixtureValue.Null }?.let { written ->
            profileOptionalTexts(written).map { RestSchema.WaypointAmenity(it) }
        }
    val timing =
        details.objectOrNull("timing")?.let { written ->
            WaypointDetails.Timing(
                cutoffAt = written.textOrNull("cutoffAt")?.let { Instant.parse(it) },
                stopDuration = written.numberOrNull("stopDuration")?.toInt(),
            )
        }
    val properties =
        details["properties"]?.takeUnless { it is FixtureValue.Null }?.let { written ->
            val fields = (written as? FixtureValue.Array)?.items ?: throw AssertionError("`properties` is not an array")
            fields.map { item ->
                val field = item as? FixtureValue.Object ?: throw AssertionError("a field is not an object")
                WaypointDetails.Property(
                    field.text("key"),
                    field.text("label"),
                    field.text("value"),
                    field.textOrNull("format")?.let { RestSchema.WaypointPropertyFormat(it) },
                )
            }
        }
    return WaypointDetails(amenities = amenities, timing = timing, properties = properties)
}

/** A reader's `{language, length}`, every other preference the default. */
internal fun profileUnits(value: FixtureValue?): UnitPreferences {
    val units = value as? FixtureValue.Object ?: throw AssertionError("the case's `units` is not an object")
    return UnitPreferences(units.text("language"), RestSchema.UnitLength(units.text("length")))
}

/** The caller's formatter a case states by what it answers, at [key], or null where it states none. */
internal fun profileFormatter(
    input: FixtureValue.Object,
    key: String,
): ((Instant) -> String)? {
    val moment = input.textOrNull(key) ?: return null
    return { moment }
}
