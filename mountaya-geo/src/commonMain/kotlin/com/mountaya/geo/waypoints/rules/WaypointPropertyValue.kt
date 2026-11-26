package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.client.parseTimestamp
import kotlin.time.Instant

/** The decimals a `number` field keeps at most, every locale's own for a plain number. */
private const val NUMBER_FIELD_FRACTION_DIGITS = 3

/**
 * waypointPropertyValue is how a place's custom field, [property], reads, by the format its author declared
 * (`geo.profile.waypoint-property-value`), since `3600` is a duration, a distance or a number by what its author
 * meant and a renderer guessing from the digits is right two times in three.
 *
 * A `duration`, a `distance` and a `number` whose value reads as a finite number ([readNumber]) are written by
 * the formatters for [units]: the duration from seconds (`formatDuration`), the distance from meters in the
 * reader's system (`formatDistance`), and the number grouped as the language groups it, with at most three
 * decimals (`formatNumber`). A `date` is an instant, written only by [formatInstant], since only the caller holds
 * the time zone to write it in: its value is read as RFC 3339 by the client's anchor, and one that is no instant reads
 * as written. Any other field, a `text` and a `url` among them, and a value its format cannot read, reads as written.
 */
public fun waypointPropertyValue(
    property: WaypointDetails.Property,
    units: UnitPreferences,
    formatInstant: ((Instant) -> String)? = null,
): String {
    val value = property.value
    val format = property.format ?: return value
    if (format == RestSchema.WaypointPropertyFormat.Date) {
        if (formatInstant == null) return value
        val instant = rfc3339Instant(value) ?: return value
        return formatInstant(instant)
    }
    val number = readNumber(value)
    if (!number.isFinite()) return value
    return when (format) {
        RestSchema.WaypointPropertyFormat.Duration -> {
            formatDuration(number, units)
        }

        RestSchema.WaypointPropertyFormat.Distance -> {
            formatDistance(number, units)
        }

        RestSchema.WaypointPropertyFormat.Number -> {
            formatNumber(number, units.language, NUMBER_FIELD_FRACTION_DIGITS)
        }

        else -> {
            value
        }
    }
}

/**
 * The instant an RFC 3339 [text] names, with or without fractional seconds, null for any other text: read by the
 * client's anchor, `parseTimestamp`, as every timestamp the wire carries is read and as `sdk-swift` and `sdk-dart`
 * read a place's date, never by `Instant.parse`, which takes a lowercase `t` or `z` and an offset to the second
 * that name no instant by the contract's grammar.
 */
internal fun rfc3339Instant(text: String): Instant? = parseTimestamp(text)
