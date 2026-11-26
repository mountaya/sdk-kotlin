package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive

/** SUGGESTION_CHOSEN_COLOR is the colour the chosen suggested itinerary draws in. */
public const val SUGGESTION_CHOSEN_COLOR: String = "#2b8a3e"

/**
 * SUGGESTION_ALTERNATIVE_COLOR is the colour a suggested itinerary not chosen draws in, the neutral one an
 * unconfirmed draft takes.
 */
public const val SUGGESTION_ALTERNATIVE_COLOR: String = "#868e96"

/**
 * suggestionsGeoJson draws [result]'s suggested itineraries, one line feature each in the order the
 * answer holds them, best first: properties `chosen`, `distance`, `duration`, `seed` and `summary`,
 * `mtyaColor` [SUGGESTION_CHOSEN_COLOR] for the one at [chosen] and [SUGGESTION_ALTERNATIVE_COLOR] for
 * every other, `mtyaIndex` and `mtyaKind` `suggestion`. The first is chosen unless [chosen] names
 * another, so a reader's pick redraws by its place rather than by reordering the answer.
 */
public fun suggestionsGeoJson(
    result: DataSchema.SuggestionsResult,
    chosen: Int = 0,
): GeoJsonFeatureCollection =
    GeoJsonFeatureCollection(
        result.routes.orEmpty().mapIndexed { index, route ->
            GeoJsonFeature(
                route.geometry,
                featureProperties(
                    "chosen" to JsonPrimitive(index == chosen),
                    "distance" to numberProperty(route.distance),
                    "duration" to numberProperty(route.duration),
                    "seed" to numberProperty(route.seed),
                    "summary" to textProperty(route.summary),
                    FEATURE_COLOR_PROPERTY to
                        JsonPrimitive(if (index == chosen) SUGGESTION_CHOSEN_COLOR else SUGGESTION_ALTERNATIVE_COLOR),
                    FEATURE_INDEX_PROPERTY to JsonPrimitive(index),
                    FEATURE_KIND_PROPERTY to JsonPrimitive("suggestion"),
                ),
            )
        },
    )
