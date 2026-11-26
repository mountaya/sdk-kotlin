package com.mountaya.elements

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ReadoutPoint
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.time.Instant

/**
 * readoutZoneOf answers the zone a readout states for [feature], the topmost the map draws at the point pressed
 * (`map.readout.open`): its `name`, its `description` and its `level`, each the empty text where it holds no text, and
 * the bounds of its window, `start_at` and `end_at`, each a JSON number of seconds since the epoch cut to the whole
 * second toward zero, and none where the feature writes it as a string, digits or not, or states none.
 */
internal fun readoutZoneOf(feature: QueriedFeature): ReadoutPoint.Zone =
    ReadoutPoint.Zone(
        name = textOf(feature, "name"),
        description = textOf(feature, "description"),
        level = RestSchema.ZoneLevel(textOf(feature, "level")),
        start = secondsOf(feature, "start_at"),
        end = secondsOf(feature, "end_at"),
    )

/** The text [feature] holds under [key], or the empty text where it holds none there or holds no text. */
private fun textOf(
    feature: QueriedFeature,
    key: String,
): String = (feature.properties[key] as? JsonPrimitive)?.takeIf { it.isString }?.content.orEmpty()

/** The instant of the whole seconds [feature] holds as a finite JSON number under [key], or null otherwise. */
private fun secondsOf(
    feature: QueriedFeature,
    key: String,
): Instant? {
    val seconds = (feature.properties[key] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull ?: return null
    return if (seconds.isFinite()) Instant.fromEpochSeconds(seconds.toLong()) else null
}
