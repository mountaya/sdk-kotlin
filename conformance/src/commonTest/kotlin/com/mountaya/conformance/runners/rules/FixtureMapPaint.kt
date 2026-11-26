package com.mountaya.conformance

import com.mountaya.geo.MapPaint

/**
 * The paint a case's [patch] resolves to: `MapPaint.standard` with each member the patch states set on a
 * copy through the paint's own builder, as a consumer restyles a map, or the standard paint itself where
 * the case states none. A list replaces the shipped one whole, a nested member set leaves its siblings, a
 * rank band is added or moved alone, and a member written null is set on nothing, the shipped value
 * standing. A member the paint holds no place for, a value of another shape and a rank that is no whole
 * number fail the case rather than being read past.
 */
internal fun fixtureMapPaint(patch: FixtureValue?): MapPaint {
    if (patch == null || patch is FixtureValue.Null) return MapPaint.standard
    val members = (patch as? FixtureValue.Object)?.entries ?: throw AssertionError("a patch is not an object")
    return MapPaint.standard.copy {
        for ((key, value) in members) {
            if (value is FixtureValue.Null) continue
            when (key) {
                "itinerary" -> {
                    for ((member, written) in entriesOf(value, key)) {
                        if (written is FixtureValue.Null) continue
                        when (member) {
                            "casingColor" -> itinerary.casingColor = textOf(written, member)
                            "casingOpacity" -> itinerary.casingOpacity = numberOf(written, member)
                            "casingWidthStops" -> itinerary.casingWidthStops = numbersOf(written, member)
                            "dashArray" -> itinerary.dashArray = numbersOf(written, member)
                            "dashedOpacity" -> itinerary.dashedOpacity = numberOf(written, member)
                            "lineWidthStops" -> itinerary.lineWidthStops = numbersOf(written, member)
                            else -> throw AssertionError("an itinerary's paint holds no member `$member`")
                        }
                    }
                }

                "layerRanks" -> {
                    for ((band, written) in entriesOf(value, key)) {
                        if (written is FixtureValue.Null) continue
                        val rank = numberOf(written, band)
                        if (rank !=
                            rank.toInt().toDouble()
                        ) {
                            throw AssertionError("the band `$band` is not a whole number")
                        }
                        layerRanks += band to rank.toInt()
                    }
                }

                "segments" -> {
                    for ((member, written) in entriesOf(value, key)) {
                        if (written is FixtureValue.Null) continue
                        when (member) {
                            "defaultColor" -> segments.defaultColor = textOf(written, member)
                            "palette" -> segments.palette = textsOf(written, member)
                            else -> throw AssertionError("a segment's paint holds no member `$member`")
                        }
                    }
                }

                else -> {
                    throw AssertionError("a paint holds no member `$key`")
                }
            }
        }
    }
}

/** [paint] as a fixture writes one. */
internal fun writtenMapPaint(paint: MapPaint): FixtureValue {
    val itinerary = paint.itinerary
    return FixtureValue.Object(
        listOf(
            "itinerary" to
                FixtureValue.Object(
                    listOf(
                        "casingColor" to FixtureValue.Text(itinerary.casingColor),
                        "casingOpacity" to FixtureValue.Number(itinerary.casingOpacity),
                        "casingWidthStops" to numbersValue(itinerary.casingWidthStops),
                        "dashArray" to numbersValue(itinerary.dashArray),
                        "dashedOpacity" to FixtureValue.Number(itinerary.dashedOpacity),
                        "lineWidthStops" to numbersValue(itinerary.lineWidthStops),
                    ),
                ),
            "layerRanks" to
                FixtureValue.Object(
                    paint.layerRanks.entries.sortedBy { it.key }.map {
                        it.key to
                            FixtureValue.Number(it.value.toDouble())
                    },
                ),
            "segments" to
                FixtureValue.Object(
                    listOf(
                        "defaultColor" to FixtureValue.Text(paint.segments.defaultColor),
                        "palette" to textArray(paint.segments.palette),
                    ),
                ),
        ),
    )
}

/** [numbers] as a fixture's array of numbers. */
internal fun numbersValue(numbers: List<Double>): FixtureValue =
    FixtureValue.Array(numbers.map { FixtureValue.Number(it) })

private fun entriesOf(
    value: FixtureValue,
    what: String,
): List<Pair<String, FixtureValue>> =
    (value as? FixtureValue.Object)?.entries ?: throw AssertionError("`$what` is not an object")

private fun textOf(
    value: FixtureValue,
    what: String,
): String = (value as? FixtureValue.Text)?.value ?: throw AssertionError("`$what` is not a string")

private fun numberOf(
    value: FixtureValue,
    what: String,
): Double = (value as? FixtureValue.Number)?.value ?: throw AssertionError("`$what` is not a number")

private fun numbersOf(
    value: FixtureValue,
    what: String,
): List<Double> =
    (value as? FixtureValue.Array)?.items?.map { numberOf(it, what) } ?: throw AssertionError("`$what` is not an array")

private fun textsOf(
    value: FixtureValue,
    what: String,
): List<String> =
    fixtureTexts((value as? FixtureValue.Array)?.items ?: throw AssertionError("`$what` is not an array"), "`$what`")
