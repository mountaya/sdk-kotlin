package com.mountaya.geo

import kotlin.time.Instant

/** The middle dot between two spaces a summary's two clauses are parted by. */
private const val SUMMARY_SEPARATOR = " · "

/**
 * waypointCard is everything a card about a place says, in the order a card says it, from what the place
 * records, [details] (`geo.profile.waypoint-card`), its words the reader's [scope]'s and its figures written for
 * [units].
 *
 * Its services by name ([amenityName]), a service the scope's words hold no name for named by its own value. Its
 * timing: the cut-off only through [formatCutoffAt], since an instant needs a time zone only the caller holds,
 * then the planned stop as `formatDuration` writes it, a stop that rounds to no minute taking no line. Its
 * contact lines as opening hours, address, phone, email, website and booking page, a phone carrying `tel:` and an
 * email `mailto:` before its value, and a website and a booking page carrying the address they read as past their
 * white space, leaving the app, only where it is a web address ([isWebAddress]), any other stated as text. Its
 * fields by their declared format ([waypointPropertyValue]), a `url` field anchored only where its value is a web
 * address ([waypointPropertyLink]). Its links, each anchored only where its address is a web address, and its
 * photos at a web address alone. A section with no line states nothing, and the headline is the author's
 * description, else the summary its record assembles in two clauses at most.
 */
public fun waypointCard(
    details: WaypointDetails?,
    scope: ElementsScope,
    units: UnitPreferences,
    formatCutoffAt: ((Instant) -> String)? = null,
): WaypointCardFacts {
    val word = { key: String -> scope.word("waypointCard.$key") }
    val timing = ArrayList<WaypointCardFacts.Line>()
    val cutoffAt = details?.timing?.cutoffAt
    if (cutoffAt != null && formatCutoffAt != null) {
        timing += WaypointCardFacts.Line(word("cutoff"), formatCutoffAt(cutoffAt))
    }
    val stop = formatDuration(details?.timing?.stopDuration?.toDouble(), units)
    if (stop.isNotEmpty()) timing += WaypointCardFacts.Line(word("stop"), stop)
    val contact = ArrayList<WaypointCardFacts.Line>()
    val lines = details?.contact
    if (lines != null) {
        lines.openingHours?.let { contact += WaypointCardFacts.Line(word("openingHours"), it) }
        lines.address?.let { contact += WaypointCardFacts.Line(word("address"), it) }
        lines.phone?.let { contact += WaypointCardFacts.Line(word("phone"), it, "tel:$it") }
        lines.email?.let { contact += WaypointCardFacts.Line(word("email"), it, "mailto:$it") }
        lines.website?.let { contact += webAddressLine(word("website"), it) }
        lines.bookingUrl?.let { contact += webAddressLine(word("booking"), it) }
    }
    return WaypointCardFacts(
        headline = cardHeadline(details, scope, units, formatCutoffAt),
        services = details?.amenities.orEmpty().map { WaypointCardFacts.Service(it, amenityName(it, scope)) },
        timing = timing,
        contact = contact,
        fields =
            details?.properties.orEmpty().map { property ->
                WaypointField(
                    property.key,
                    property.label,
                    waypointPropertyValue(property, units, formatCutoffAt),
                    waypointPropertyLink(property),
                )
            },
        links =
            details?.media?.links.orEmpty().map { link ->
                val address = trimWhiteSpace(link.url)
                WaypointCardFacts.MediaLink(link.label, link.url, address.takeIf(::isWebAddress))
            },
        photos =
            details?.media?.photos.orEmpty().mapNotNull { photo ->
                val address = trimWhiteSpace(photo.url)
                if (isWebAddress(address)) WaypointDetails.Photo(address, photo.caption) else null
            },
        isEmpty = declaresNothing(details),
    )
}

/**
 * A website's or a booking page's line: anchored, leaving the app, where its address read past its white space
 * is a web address, and text otherwise.
 */
private fun webAddressLine(
    label: String,
    value: String,
): WaypointCardFacts.Line {
    val address = trimWhiteSpace(value)
    if (!isWebAddress(address)) return WaypointCardFacts.Line(label, value)
    return WaypointCardFacts.Line(label, value, address, isExternal = true)
}

/** Whether a place records no fact a card states, its description apart. */
private fun declaresNothing(details: WaypointDetails?): Boolean =
    details?.amenities.isNullOrEmpty() &&
        details?.timing == null &&
        details?.contact == null &&
        details?.media?.photos.isNullOrEmpty() &&
        details?.media?.links.isNullOrEmpty() &&
        details?.properties.isNullOrEmpty()

/**
 * The line under a place's name: its description past the white space at its ends, else at most two clauses,
 * whether it is equipped and one timing fact, the opening hours, else the cut-off where the caller can write one,
 * else the planned stop, parted by a middle dot.
 */
private fun cardHeadline(
    details: WaypointDetails?,
    scope: ElementsScope,
    units: UnitPreferences,
    formatCutoffAt: ((Instant) -> String)?,
): String? {
    if (details == null) return null
    val description = details.description?.let(::trimWhiteSpace).orEmpty()
    if (description.isNotEmpty()) return description
    val clauses = ArrayList<String>()
    if (!details.amenities.isNullOrEmpty()) clauses += scope.word("waypointCard.amenities")
    val hours = details.contact?.openingHours
    val cutoffAt = details.timing?.cutoffAt
    if (!hours.isNullOrEmpty()) {
        clauses += filledText(scope.word("waypointCard.summary.openingHours"), hours)
    } else if (cutoffAt != null && formatCutoffAt != null) {
        clauses += filledText(scope.word("waypointCard.summary.cutoffAt"), formatCutoffAt(cutoffAt))
    } else {
        val stop = formatDuration(details.timing?.stopDuration?.toDouble(), units)
        if (stop.isNotEmpty()) clauses += filledText(scope.word("waypointCard.summary.stop"), stop)
    }
    return if (clauses.isEmpty()) null else clauses.joinToString(SUMMARY_SEPARATOR)
}
