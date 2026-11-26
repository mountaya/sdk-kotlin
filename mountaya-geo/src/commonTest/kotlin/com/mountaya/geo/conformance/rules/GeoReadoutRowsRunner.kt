package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/**
 * `geo.readout.rows`: the rows the readout opened at the case's `position` and `zoom` states, from what the map holds
 * there, its `terrain`, `poi`, `zone`, `overlay` with its `band`, and the `instant` it shows, a zone's window read in
 * Unix seconds and a moment in epoch milliseconds. The detail zoom, the copy's places, the overlays' readouts, the
 * points of interest's categories and the zones' colours are held to the ones Geo states. A point of interest's label
 * is worded as a host stating no words words it, in English.
 */
object GeoReadoutRowsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(
            case,
            mapOf(
                "copyDecimals" to FixtureValue.Number(READOUT_COPY_DECIMALS.toDouble()),
                "detailZoom" to FixtureValue.Number(READOUT_DETAIL_ZOOM),
                "overlayReadouts" to
                    FixtureValue.Object(
                        overlayReadouts.map { (slug, readout) ->
                            slug to
                                FixtureValue.Object(
                                    listOfNotNull(
                                        "measure" to FixtureValue.Text(readout.measure.id),
                                        readout.pace?.let { "pace" to FixtureValue.Text(it.value) },
                                        if (readout.scale !=
                                            1.0
                                        ) {
                                            "scale" to FixtureValue.Number(readout.scale)
                                        } else {
                                            null
                                        },
                                    ),
                                )
                        },
                    ),
                "poiCategories" to
                    FixtureValue.Object(
                        poiTileCategories.map { (tile, category) ->
                            tile to
                                FixtureValue.Text(category.value)
                        },
                    ),
                "zoneLevelColors" to
                    FixtureValue.Object(
                        zoneLevelColors.map { (level, color) -> level.value to FixtureValue.Text(color) },
                    ),
                "zoneLevelFallback" to FixtureValue.Text(zoneLevelFallback.value),
            ),
        )
        val input = case.fields
        val point =
            ReadoutPoint(
                position = fixturePosition(input["position"], "position"),
                zoom = input.number("zoom"),
                terrain =
                    input.objectOrNull("terrain")?.let {
                        ReadoutPoint.Terrain(
                            it.numberOrNull("elevation"),
                            it.numberOrNull("slope"),
                            it.numberOrNull("aspect"),
                        )
                    },
                poi = input.objectOrNull("poi")?.let { ReadoutPoint.Poi(it.text("name"), it.text("category")) },
                zone =
                    input.objectOrNull("zone")?.let {
                        ReadoutPoint.Zone(
                            name = it.text("name"),
                            description = it.text("description"),
                            level = RestSchema.ZoneLevel(it.text("level")),
                            start =
                                it.numberOrNull("startAt")?.let { seconds ->
                                    Instant.fromEpochSeconds(seconds.toLong())
                                },
                            end =
                                it.numberOrNull("endAt")?.let { seconds ->
                                    Instant.fromEpochSeconds(seconds.toLong())
                                },
                        )
                    },
                overlay =
                    input.objectOrNull("overlay")?.let {
                        ReadoutPoint.Overlay(it.text("slug"), it.text("name"), it.bool("vector"))
                    },
                band =
                    input
                        .objectOrNull(
                            "band",
                        )?.let { ReadoutPoint.Band(it.numberOrNull("min"), it.numberOrNull("max")) },
                instant = input.numberOrNull("instant")?.let { Instant.fromEpochMilliseconds(it.toLong()) },
            )
        return ConformanceOutcome.Value(
            profileObject("rows" to FixtureValue.Array(readoutRows(point).map(::row))),
        )
    }

    private fun millis(instant: Instant?): FixtureValue =
        instant?.let { FixtureValue.Number(it.toEpochMilliseconds().toDouble()) } ?: FixtureValue.Null

    private fun row(row: ReadoutRow): FixtureValue =
        when (row) {
            is ReadoutRow.Poi -> {
                profileObject(
                    "category" to profileText(row.category?.value),
                    "label" to FixtureValue.Text(row.label),
                    "name" to profileText(row.name),
                    "row" to FixtureValue.Text("poi"),
                )
            }

            is ReadoutRow.Band -> {
                profileObject(
                    "measure" to FixtureValue.Text(row.measure.id),
                    "overlay" to FixtureValue.Text(row.overlay),
                    "pace" to profileText(row.pace?.value),
                    "row" to FixtureValue.Text("band"),
                    "time" to
                        (
                            row.time?.let {
                                profileObject("moment" to millis(it.moment), "word" to FixtureValue.Text(it.word))
                            } ?: FixtureValue.Null
                        ),
                    "values" to FixtureValue.Array(row.values.map { FixtureValue.Number(it) }),
                    "word" to profileText(row.word),
                )
            }

            is ReadoutRow.Terrain -> {
                profileObject(
                    "readings" to FixtureValue.Array(row.readings.map(::reading)),
                    "row" to FixtureValue.Text("terrain"),
                )
            }

            is ReadoutRow.Moment -> {
                profileObject(
                    "moment" to millis(row.moment),
                    "row" to FixtureValue.Text("instant"),
                    "word" to FixtureValue.Text(row.word),
                )
            }

            is ReadoutRow.Coordinates -> {
                profileObject(
                    "copy" to FixtureValue.Text(row.copy),
                    "row" to FixtureValue.Text("coordinates"),
                    "text" to FixtureValue.Text(row.text),
                    "word" to FixtureValue.Text(row.word),
                )
            }

            is ReadoutRow.ZoomIn -> {
                profileObject("row" to FixtureValue.Text("zoom-in"), "word" to FixtureValue.Text(row.word))
            }

            is ReadoutRow.Zone -> {
                profileObject(
                    "color" to FixtureValue.Text(row.color),
                    "description" to profileText(row.description),
                    "level" to FixtureValue.Text(row.level.value),
                    "levelWord" to profileText(row.levelWord),
                    "name" to profileText(row.name),
                    "row" to FixtureValue.Text("zone"),
                    "window" to
                        (
                            row.window?.let {
                                profileObject(
                                    "end" to millis(it.end),
                                    "start" to millis(it.start),
                                    "word" to FixtureValue.Text(it.word),
                                )
                            } ?: FixtureValue.Null
                        ),
                    "word" to FixtureValue.Text(row.word),
                )
            }
        }

    private fun reading(reading: ReadoutRow.Terrain.Reading): FixtureValue {
        val kind =
            when (reading.kind) {
                ReadoutRow.Terrain.Reading.Kind.Altitude -> "altitude"
                ReadoutRow.Terrain.Reading.Kind.Slope -> "slope"
                ReadoutRow.Terrain.Reading.Kind.Aspect -> "aspect"
            }
        return FixtureValue.Object(
            listOfNotNull(
                reading.cardinal?.let { "cardinal" to FixtureValue.Text(it.code) },
                reading.cardinalWord?.let { "cardinalWord" to FixtureValue.Text(it) },
                reading.color?.let { "color" to FixtureValue.Text(it) },
                "reading" to FixtureValue.Text(kind),
                "value" to FixtureValue.Number(reading.value),
                "word" to FixtureValue.Text(reading.word),
            ),
        )
    }
}
