package com.mountaya.conformance

import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ItineraryLeg
import com.mountaya.geo.ItineraryPlace
import com.mountaya.geo.WaypointDetails
import kotlin.time.Instant

/** The segment group [fields] writes, built as the REST API's from every member the fixture writes. */
internal fun fixtureGroup(fields: FixtureValue.Object): RestSchema.ItinerarySegmentGroup =
    RestSchema.ItinerarySegmentGroup(
        color = fields.text("color"),
        created_at = Instant.parse(fields.text("created_at")),
        created_by = RestSchema.RelationToUser(id = fields.text("created_by")),
        id = fields.text("id"),
        itinerary_id = fields.text("itinerary_id"),
        name = fields.text("name"),
        revision = wholeOrNull(fields, "revision") ?: throw AssertionError("a group states no `revision`"),
        speed = fields.numberOrNull("speed"),
    )

/**
 * The waypoint [fields] writes, built as the REST API answers one from the members the fixture writes and
 * a placeholder for every other the type requires, which no rule reads: created at the epoch by `user`, a
 * `point`, at revision 1, in the itinerary `itinerary`.
 *
 * Each member is read in the shape the wire's type holds and left out in any other, a list's entries one
 * by one, as no decoder of the wire's hands a waypoint over holding a colour of 7, an icon that is an
 * object, an address that is a list or a stop of no whole number of seconds (`CONTRACT-GAPS.md`).
 */
internal fun fixtureItineraryWaypoint(fields: FixtureValue.Object): RestSchema.ItineraryWaypoint =
    RestSchema.ItineraryWaypoint(
        amenities =
            ((fields["amenities"] as? FixtureValue.Array)?.items.orEmpty())
                .mapNotNull { (it as? FixtureValue.Text)?.value?.let(RestSchema::WaypointAmenity) },
        color = text(fields["color"]),
        contact = contact(fields["contact"]),
        created_at = Instant.fromEpochMilliseconds(0),
        created_by = RestSchema.RelationToUser(id = "user"),
        description = text(fields["description"]),
        geojson = fields["geojson"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry),
        icon = text(fields["icon"])?.let(RestSchema::WaypointIcon),
        id = fields.text("id"),
        itinerary_id = "itinerary",
        media = media(fields["media"]),
        mode = RestSchema.WaypointMode.Point,
        name = text(fields["name"]),
        position = wholeOrNull(fields, "position") ?: throw AssertionError("a waypoint states no `position`"),
        properties = properties(fields["properties"]),
        revision = 1,
        timing = timing(fields["timing"]),
    )

/**
 * [leg] as `geo/itinerary-segments.json` writes one: its colour and its members only where it has them,
 * and the running totals and the difficulty a leg never states as null.
 */
internal fun writtenLeg(leg: ItineraryLeg): FixtureValue {
    val members =
        mutableListOf(
            "geojson" to (leg.geometry?.let(::writtenGeometry) ?: FixtureValue.Null),
            "id" to FixtureValue.Text(leg.id),
            "metrics" to
                FixtureValue.Object(
                    listOf(
                        "altitudeMax" to number(leg.altitudeMax?.toDouble()),
                        "altitudeMin" to number(leg.altitudeMin?.toDouble()),
                        "ascent" to FixtureValue.Null,
                        "descent" to FixtureValue.Null,
                        "difficulty" to FixtureValue.Null,
                        "distance" to FixtureValue.Null,
                        "duration" to number(leg.duration),
                        "sectionAscent" to number(leg.sectionAscent?.toDouble()),
                        "sectionDescent" to number(leg.sectionDescent?.toDouble()),
                        "sectionDistance" to number(leg.sectionDistance),
                    ),
                ),
            "name" to FixtureValue.Text(leg.name),
        )
    leg.color?.let { members += "color" to FixtureValue.Text(it) }
    if (leg.memberIds.isNotEmpty()) members += "memberIds" to textArray(leg.memberIds)
    return FixtureValue.Object(members)
}

/** [place] as `geo/itinerary-waypoints.json` writes one: each member only where it holds something. */
internal fun writtenPlace(place: ItineraryPlace): FixtureValue {
    val members = mutableListOf("id" to FixtureValue.Text(place.id), "lngLat" to numbersValue(place.lngLat))
    place.color?.let { members += "color" to FixtureValue.Text(it) }
    place.glyph?.let { members += "glyph" to FixtureValue.Text(it.value) }
    place.name?.let { members += "name" to FixtureValue.Text(it) }
    place.details?.let { members += "details" to writtenDetails(it) }
    return FixtureValue.Object(members)
}

private fun writtenDetails(details: WaypointDetails): FixtureValue {
    val members = mutableListOf<Pair<String, FixtureValue>>()
    details.amenities?.let { members += "amenities" to textArray(it.map { amenity -> amenity.value }) }
    details.description?.let { members += "description" to FixtureValue.Text(it) }
    details.contact?.let {
        members +=
            "contact" to
            texts(
                "address" to it.address,
                "bookingUrl" to it.bookingUrl,
                "email" to it.email,
                "openingHours" to it.openingHours,
                "phone" to it.phone,
                "website" to it.website,
            )
    }
    details.media?.let { media ->
        val lists = mutableListOf<Pair<String, FixtureValue>>()
        media.links?.let { links ->
            lists += "links" to FixtureValue.Array(links.map { texts("label" to it.label, "url" to it.url) })
        }
        media.photos?.let { photos ->
            lists +=
                "photos" to
                FixtureValue.Array(photos.map { texts("caption" to it.caption, "url" to it.url) })
        }
        members += "media" to FixtureValue.Object(lists)
    }
    details.properties?.let { fields ->
        members +=
            "properties" to
            FixtureValue.Array(
                fields.map {
                    texts("format" to it.format?.value, "key" to it.key, "label" to it.label, "value" to it.value)
                },
            )
    }
    details.timing?.let { timing ->
        val written = mutableListOf<Pair<String, FixtureValue>>()
        timing.cutoffAt?.let { written += "cutoffAt" to FixtureValue.Text(it.toString()) }
        timing.stopDuration?.let { written += "stopDuration" to FixtureValue.Number(it.toDouble()) }
        members += "timing" to FixtureValue.Object(written)
    }
    return FixtureValue.Object(members)
}

/** An object of the texts present among [members], each under its key. */
private fun texts(vararg members: Pair<String, String?>): FixtureValue =
    FixtureValue.Object(members.mapNotNull { (key, text) -> text?.let { key to FixtureValue.Text(it) } })

private fun number(value: Double?): FixtureValue = value?.let { FixtureValue.Number(it) } ?: FixtureValue.Null

/** The text [value] is, or null where it is no text. */
private fun text(value: FixtureValue?): String? = (value as? FixtureValue.Text)?.value

/** The web address [value] is, as the wire's `Url` holds one: the text as sent, one with no scheme included. */
private fun webAddress(value: FixtureValue?): Url? = text(value)?.let(::Url)

private fun contact(value: FixtureValue?): RestSchema.WaypointContact? {
    val written = value as? FixtureValue.Object ?: return null
    return RestSchema.WaypointContact {
        address = text(written["address"])
        booking_url = webAddress(written["booking_url"])
        email = text(written["email"])
        opening_hours = text(written["opening_hours"])
        phone = text(written["phone"])
        website = webAddress(written["website"])
    }
}

private fun media(value: FixtureValue?): RestSchema.WaypointMedia? {
    val written = value as? FixtureValue.Object ?: return null
    return RestSchema.WaypointMedia {
        links =
            (written["links"] as? FixtureValue.Array)?.items?.mapNotNull { entry ->
                val link = entry as? FixtureValue.Object ?: return@mapNotNull null
                val label = text(link["label"]) ?: return@mapNotNull null
                val url = webAddress(link["url"]) ?: return@mapNotNull null
                RestSchema.WaypointLink(label = label, url = url)
            }
        photos =
            (written["photos"] as? FixtureValue.Array)?.items?.mapNotNull { entry ->
                val photo = entry as? FixtureValue.Object ?: return@mapNotNull null
                val url = webAddress(photo["url"]) ?: return@mapNotNull null
                RestSchema.WaypointPhoto(url = url) { caption = text(photo["caption"]) }
            }
    }
}

private fun properties(value: FixtureValue?): List<RestSchema.WaypointProperty>? =
    (value as? FixtureValue.Array)?.items?.mapNotNull { entry ->
        val field = entry as? FixtureValue.Object ?: return@mapNotNull null
        val key = text(field["key"]) ?: return@mapNotNull null
        val label = text(field["label"]) ?: return@mapNotNull null
        val written = text(field["value"]) ?: return@mapNotNull null
        RestSchema.WaypointProperty(key = key, label = label, value = written) {
            format = text(field["format"])?.let(RestSchema::WaypointPropertyFormat)
        }
    }

private fun timing(value: FixtureValue?): RestSchema.WaypointTiming? {
    val written = value as? FixtureValue.Object ?: return null
    return RestSchema.WaypointTiming {
        cutoff_at = text(written["cutoff_at"])?.let { runCatching { Instant.parse(it) }.getOrNull() }
        stop_duration =
            (written["stop_duration"] as? FixtureValue.Number)
                ?.value
                ?.takeIf {
                    it.isFinite() &&
                        it == it.toInt().toDouble()
                }?.toInt()
    }
}
