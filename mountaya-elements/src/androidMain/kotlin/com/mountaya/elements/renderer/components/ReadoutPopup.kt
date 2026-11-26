package com.mountaya.elements

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.DateLength
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ReadoutMeasure
import com.mountaya.geo.ReadoutPoint
import com.mountaya.geo.ReadoutRow
import com.mountaya.geo.TimeLength
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.filledText
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatElevation
import com.mountaya.geo.formatHumidity
import com.mountaya.geo.formatSlope
import com.mountaya.geo.formatSnowfall
import com.mountaya.geo.formatSpeed
import com.mountaya.geo.formatTemperature
import com.mountaya.geo.generated.ElementLayout
import com.mountaya.geo.localizedName
import com.mountaya.geo.overlayInstant
import com.mountaya.geo.readoutRows
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.math.ceil
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** How long a copy's button reads `readout.copied` once it copied. */
private val COPIED_FOR = 1.seconds

/**
 * ReadoutPopup is the readout a long press opens on [map] (`map.readout.open`, `geo.readout.rows`), `readout-width`
 * wide, its top-left corner at the point pressed, flipped to the left of the point or above it where it would leave the
 * map's [safeArea], `[left, top, right, bottom]` in density-independent pixels from the map's corner, or the whole map
 * where it is not yet measured, then moved clear of the watermark's frame [keepClear], `[x, y, width, height]` from the
 * same corner while the map keeps it clear, by the smallest move that clears it: where nothing inside the safe area
 * clears it, the card opens with its top at the frame's bottom and the map's edge cuts it, so the mark is never covered
 * (`geo.organization.watermark`). It states what [readout] holds open, the terrain it read off the elevation tiles, and
 * what the map draws at the point, its topmost point of interest, whatever it names, and zone and the band of its
 * classic overlay, through the point queries the map answers (`map.query.layers`), the band read off the classic
 * overlay's own layers alone. Each row is written in the scope's words and the SDK's default units, the metric system,
 * a speed, degrees and Celsius, in the scope's language, whatever the map's organization prefers, the moment by the
 * locale services as a medium date with a short time in the device's own time zone, read as it is drawn and never one
 * an app states, and none where the platform knows no zone of that name, never a UTC frame in its place, and the
 * coordinates with an action that copies the pair, written in the accent. It holds no place link, no waypoint action
 * and no street-level view.
 *
 * What stands at the point is read once, as the readout opens and as its terrain joins, the band among it; the moment
 * follows the map's instant live while the card is open, `overlayInstant` of the pin the map holds now on
 * `LocalMountayaClock`, so a pin moved under an open card moves its moment and leaves its band as it was read at the
 * press.
 */
@Composable
internal fun ReadoutPopup(
    map: MapEngine,
    readout: ReadoutEngine,
    safeArea: List<Double>?,
    keepClear: List<Double>?,
    modifier: Modifier = Modifier,
) {
    val open by readout.open.collectAsState()
    val pinned by map.pinnedInstant.collectAsState()
    val shown = open ?: return
    val scope = LocalMountayaScope.current
    val clock = LocalMountayaClock.current
    val units = UnitPreferences(scope.language.value)
    // The map is asked what stands at the point once a readout, as it opens and as its terrain joins.
    val point = remember(shown, scope) { readoutPoint(map, shown, scope) }
    // The moment follows the map's pin live; the band stays as it was read at the press.
    val rows = readoutRows(point.at(overlayInstant(pinned, clock.now())))
    Layout(
        content = { ReadoutCard(rows, units, scope) },
        // The map's edge cuts a card that no place inside the safe area clears the frame for.
        modifier = modifier.clipToBounds(),
    ) { measurables, constraints ->
        val card =
            measurables.first().measure(
                Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight),
            )
        layout(constraints.maxWidth, constraints.maxHeight) {
            val origin =
                readoutOrigin(
                    point = shown.point.map { it * density },
                    card = listOf(card.width.toDouble(), card.height.toDouble()),
                    bounds =
                        safeArea?.map { it * density }
                            ?: listOf(0.0, 0.0, constraints.maxWidth.toDouble(), constraints.maxHeight.toDouble()),
                    keepClear = keepClear?.map { it * density },
                )
            // Rounded up, so a readout moved clear of the frame never reaches a pixel over it.
            card.place(ceil(origin[0]).toInt(), ceil(origin[1]).toInt())
        }
    }
}

/** What [map] holds at the point [open] was pressed at, read through its point queries, its moment yet to be read. */
private fun readoutPoint(
    map: MapEngine,
    open: OpenReadout,
    scope: ElementsScope,
): ReadoutPoint {
    // The topmost point of interest and zone, whatever they name: the rows rule decides what an empty one states.
    val poi = map.poisAt(open.point).firstOrNull()?.let(::readoutPoiOf)
    val zone = map.zonesAt(open.point).firstOrNull()?.let(::readoutZoneOf)
    val drawn = map.classicOverlay
    val overlay =
        drawn?.let {
            ReadoutPoint.Overlay(
                slug = it.slug,
                name = localizedName(it.locales, it.slug, scope.language.value),
                isVector = it.content_type == TileSchema.OverlayContentType.Protobuf,
            )
        }
    val band = map.classicOverlayBandAt(open.point)
    return ReadoutPoint(
        position = open.position,
        zoom = open.zoom,
        terrain = open.terrain?.let { ReadoutPoint.Terrain(it) },
        poi = poi,
        zone = zone,
        overlay = overlay,
        band = band,
    )
}

/** This point as read at the press, at the moment [instant] shows. */
private fun ReadoutPoint.at(instant: Instant): ReadoutPoint =
    ReadoutPoint(
        position = position,
        zoom = zoom,
        terrain = terrain,
        poi = poi,
        zone = zone,
        overlay = overlay,
        band = band,
        instant = instant,
    )

/** The readout's card: its [rows], each written in [scope]'s words and [units]. */
@Composable
private fun ReadoutCard(
    rows: List<ReadoutRow>,
    units: UnitPreferences,
    scope: ElementsScope,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.panelFg, scheme)
    val dimmed = tokenColor(tokens.dimmed, scheme)
    val shape = RoundedCornerShape(8.dp)
    val strong = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    val plain = TextStyle(color = ink, fontSize = 13.sp)
    val quiet = TextStyle(color = dimmed, fontSize = 12.sp)
    // An action is drawn in the accent, as `sdk-swift` tints its copy button and `sdk-dart` writes its own.
    val action = quiet.copy(color = tokenColor(tokens.accent, scheme), fontWeight = FontWeight.SemiBold)
    val zone = deviceTimeZone().id

    // A moment in the device's own zone, or none where the platform knows no zone of its name: never a UTC frame.
    fun moment(instant: Instant): String? =
        formatDateTime(instant, scope.locale, zone, DateLength.Medium, TimeLength.Short)
    Column(
        Modifier
            .width(ElementLayout.readoutWidth.dp)
            .background(tokenColor(tokens.panelBg, scheme), shape)
            .border(1.dp, tokenColor(tokens.panelBorder, scheme), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in rows) {
            when (row) {
                is ReadoutRow.Poi -> {
                    Column {
                        row.name?.let { BasicText(it, style = strong) }
                        BasicText(row.label, style = quiet)
                    }
                }

                is ReadoutRow.Band -> {
                    val reader =
                        row.pace?.let {
                            UnitPreferences(
                                units.language,
                                units.length,
                                it,
                                units.slope,
                                units.temperature,
                            )
                        }
                            ?: units
                    val written = row.values.map { measured(row.measure, it, reader) }
                    val value =
                        row.word?.let { filledText(scope.word(it), mapOf("min" to written[0], "max" to written[1])) }
                            ?: written[0]
                    Column {
                        BasicText(row.overlay, style = quiet)
                        BasicText(value, style = strong)
                        row.time?.let { time ->
                            moment(time.moment)?.let { written ->
                                BasicText(filledText(scope.word(time.word), mapOf("moment" to written)), style = quiet)
                            }
                        }
                    }
                }

                is ReadoutRow.Terrain -> {
                    for (reading in row.readings) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText(scope.word(reading.word), Modifier.weight(1f), style = quiet)
                            reading.color?.let { color ->
                                Box(
                                    Modifier
                                        .size(
                                            10.dp,
                                        ).background(cssColor(color, 1.0) ?: Color.Transparent, CircleShape),
                                )
                            }
                            val written =
                                when (reading.kind) {
                                    ReadoutRow.Terrain.Reading.Kind.Altitude -> {
                                        formatElevation(reading.value, units)
                                    }

                                    ReadoutRow.Terrain.Reading.Kind.Slope -> {
                                        formatSlope(reading.value, units)
                                    }

                                    ReadoutRow.Terrain.Reading.Kind.Aspect -> {
                                        listOfNotNull(
                                            formatSlope(reading.value, UnitPreferences(units.language)),
                                            reading.cardinalWord?.let(scope::word),
                                        ).joinToString(" · ")
                                    }
                                }
                            BasicText(written, style = plain)
                        }
                    }
                }

                is ReadoutRow.Moment -> {
                    moment(row.moment)?.let { written ->
                        BasicText(filledText(scope.word(row.word), mapOf("moment" to written)), style = quiet)
                    }
                }

                is ReadoutRow.Coordinates -> {
                    ReadoutCoordinates(row, scope, plain, quiet, action)
                }

                is ReadoutRow.ZoomIn -> {
                    BasicText(scope.word(row.word), style = quiet)
                }

                is ReadoutRow.Zone -> {
                    Column {
                        BasicText(scope.word(row.word), style = quiet)
                        row.name?.let { BasicText(it, style = strong) }
                        row.description?.let { BasicText(it, style = plain) }
                        val level = row.levelWord?.let(scope::word) ?: row.level.value
                        BasicText(level, style = plain.copy(color = cssColor(row.color, 1.0) ?: ink))
                        row.window?.let { window ->
                            val start = window.start?.let { moment(it) }
                            val end = window.end?.let { moment(it) }
                            // A window whose moments cannot be written in the device's zone is left out whole.
                            if ((window.start == null) == (start == null) && (window.end == null) == (end == null)) {
                                val values = listOfNotNull(start?.let { "start" to it }, end?.let { "end" to it })
                                BasicText(filledText(scope.word(window.word), values.toMap()), style = quiet)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The coordinates' row: the point as written, and an action copying the pair, written in [action], the accent, which
 * reads `readout.copied` a second.
 */
@Composable
private fun ReadoutCoordinates(
    row: ReadoutRow.Coordinates,
    scope: ElementsScope,
    plain: TextStyle,
    quiet: TextStyle,
    action: TextStyle,
) {
    val context = LocalContext.current
    var copied by remember(row.copy) { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(COPIED_FOR)
            copied = false
        }
    }
    Column {
        BasicText(scope.word(row.word), style = quiet)
        BasicText(row.text, style = plain)
        BasicText(
            scope.word(if (copied) "readout.copied" else "readout.copyCoordinates"),
            Modifier.clickable(role = Role.Button) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText(scope.word(row.word), row.copy))
                copied = true
            },
            style = action,
        )
    }
}

/** [value] written in [measure] for [units], by the formatter of `geo.profile.unit-formatting` that writes it. */
private fun measured(
    measure: ReadoutMeasure,
    value: Double,
    units: UnitPreferences,
): String =
    when (measure) {
        ReadoutMeasure.Temperature -> formatTemperature(value, units)
        ReadoutMeasure.Humidity -> formatHumidity(value, units)
        ReadoutMeasure.Snowfall -> formatSnowfall(value, units)
        ReadoutMeasure.Distance -> formatDistance(value, units)
        ReadoutMeasure.Speed -> formatSpeed(value, units)
    }
