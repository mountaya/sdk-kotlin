package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive

/** MATRIX_LINE_MIN_WIDTH is the width the cheapest connector of a matrix draws at, in pixels. */
public const val MATRIX_LINE_MIN_WIDTH: Double = 1.0

/** MATRIX_LINE_MAX_WIDTH is the width the dearest connector of a matrix draws at, in pixels. */
public const val MATRIX_LINE_MAX_WIDTH: Double = 6.0

/**
 * matrixLineWidth answers the width a connector costing [value] draws at in a matrix whose cheapest
 * connector costs [min] and dearest [max]: from [MATRIX_LINE_MIN_WIDTH] to [MATRIX_LINE_MAX_WIDTH],
 * `1 + (value - min) / (max - min) * 5`, held within the two. A grid with no spread, and a value or a
 * bound that is no finite number, draws at the least, never at a width that is no number.
 */
public fun matrixLineWidth(
    value: Double,
    min: Double,
    max: Double,
): Double {
    if (!value.isFinite() || !min.isFinite() || !max.isFinite() || max <= min) return MATRIX_LINE_MIN_WIDTH
    val width = MATRIX_LINE_MIN_WIDTH + (value - min) / (max - min) * (MATRIX_LINE_MAX_WIDTH - MATRIX_LINE_MIN_WIDTH)
    return width.coerceIn(MATRIX_LINE_MIN_WIDTH, MATRIX_LINE_MAX_WIDTH)
}

/**
 * matrixGeoJson draws a matrix answer, which holds no geometry: one straight `matrix-connector` line
 * per solved pair of distinct locations of [input], of the first metric it asked for, then one
 * `matrix-location` point per location.
 *
 * A row of the grid is the location `sources` lists at its place, every location where it lists none,
 * and a column the one `destinations` lists, so a subset grid draws from the right places. A cell the
 * router could not solve (null, or no finite number) and a location's pair with itself draw nothing,
 * nor does a metric this build does not know, which names no grid. A connector's properties are
 * `from` and `to` (the locations' places), `metric`, `value`, `mtyaKind` and `mtyaWidth`, its
 * [matrixLineWidth] between the least and the greatest cost drawn; a location's are `mtyaIndex` and
 * `mtyaKind`.
 */
public fun matrixGeoJson(
    input: DataSchema.MatrixInput,
    result: DataSchema.MatrixResult,
): GeoJsonFeatureCollection {
    val metric = input.metrics.firstOrNull() ?: DataSchema.MatrixMetric.Duration
    val grid =
        when (metric) {
            DataSchema.MatrixMetric.Distance -> result.distances
            DataSchema.MatrixMetric.Duration -> result.durations
            else -> null
        }
    val locations = input.locations
    val sources = input.sources ?: locations.indices.toList()
    val destinations = input.destinations ?: locations.indices.toList()
    val solved = mutableListOf<Triple<Int, Int, Double>>()
    var least = Double.POSITIVE_INFINITY
    var greatest = Double.NEGATIVE_INFINITY
    sources.forEachIndexed { row, from ->
        destinations.forEachIndexed { column, to ->
            val value = grid?.getOrNull(row)?.getOrNull(column)
            if (value != null && value.isFinite() && from != to) {
                solved += Triple(from, to, value)
                least = minOf(least, value)
                greatest = maxOf(greatest, value)
            }
        }
    }
    val connectors =
        solved.mapNotNull { (from, to, value) ->
            val start = locations.getOrNull(from) ?: return@mapNotNull null
            val end = locations.getOrNull(to) ?: return@mapNotNull null
            GeoJsonFeature(
                GeoJsonLineString(listOf(start, end)),
                featureProperties(
                    "from" to JsonPrimitive(from),
                    "metric" to JsonPrimitive(metric.value),
                    "to" to JsonPrimitive(to),
                    "value" to JsonPrimitive(value),
                    FEATURE_KIND_PROPERTY to JsonPrimitive("matrix-connector"),
                    FEATURE_WIDTH_PROPERTY to JsonPrimitive(matrixLineWidth(value, least, greatest)),
                ),
            )
        }
    val points =
        locations.mapIndexed { index, location ->
            GeoJsonFeature(
                pointOrNull(location),
                featureProperties(
                    FEATURE_INDEX_PROPERTY to JsonPrimitive(index),
                    FEATURE_KIND_PROPERTY to JsonPrimitive("matrix-location"),
                ),
            )
        }
    return GeoJsonFeatureCollection(connectors + points)
}
