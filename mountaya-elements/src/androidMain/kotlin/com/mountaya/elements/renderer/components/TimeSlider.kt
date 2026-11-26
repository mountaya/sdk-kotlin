package com.mountaya.elements

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.client.InternalMountayaApi
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.TimeSliderReadout
import com.mountaya.geo.TimeSliderTick
import com.mountaya.geo.TimeSliderWindow
import com.mountaya.geo.countedText
import com.mountaya.geo.filledText
import com.mountaya.geo.formatNumber
import com.mountaya.geo.timeSliderReadout
import com.mountaya.geo.timeSliderRelease
import com.mountaya.geo.timeSliderTicks
import com.mountaya.geo.timeSliderWindow
import kotlinx.serialization.json.JsonObject
import kotlin.math.roundToInt
import kotlin.time.Instant

/** The radius of a time slider's thumb, in density-independent pixels. */
private const val THUMB_RADIUS_DP: Float = 9f

/** The thickness of a time slider's track and of its fill, in density-independent pixels. */
private const val TRACK_DP: Float = 4f

/** How far a time slider's tick reaches either side of its track, in density-independent pixels. */
private const val TICK_REACH_DP: Float = 6f

/** What a moment and the offset of a readout are joined by. */
private const val READOUT_JOIN: String = " · "

/**
 * LayersTime is a layers panel's time section (`geo.layers.time-slider`): its caption, the sentence holding it where its
 * track holds no instant, the thumb's readout and the slider, laid out around the reading of the clock
 * `LocalMountayaClock` provides each time it is drawn (`geo.layers.time-window`), so the window follows the clock with
 * no timer of its own. The thumb stands at [instant], the selection's pin, and a release hands [onRelease] the pin it
 * commits, none on the stop of the reading.
 */
@Composable
internal fun LayersTime(
    instant: Instant?,
    onRelease: (Instant?) -> Unit,
) {
    LocalMountayaRenderProbe.current?.report("LayersTime")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val dimmed = tokenColor(tokens.dimmed, scheme)
    val window = timeSliderWindow(LocalMountayaClock.current.now())
    val isHeld = window.start == null
    val hasKeyboard = LocalConfiguration.current.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BasicText(timeCaption(scope, hasKeyboard), style = TextStyle(color = dimmed, fontSize = 12.sp))
        if (isHeld) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Glyph("locked", set = scope.controlGlyphs, size = 14.dp, color = dimmed)
                BasicText(scope.word("layers.timeUnavailable"), style = TextStyle(color = dimmed, fontSize = 12.sp))
            }
        }
        TimeSlider(window, window.valueAt(instant), isHeld, onRelease)
    }
}

/**
 * The slider over [window]'s track, its thumb at [value] unless a drag holds it elsewhere: the track ticked at every day
 * and labelled at the four days `timeSliderTicks` names, filled from the stop of the reading to the thumb, read out above
 * as the moment and its offset, and read by accessibility services as an adjustable value whose state is that readout.
 * A tap, the end of a drag, an arrow key and an accessibility service's adjustment each release the thumb on a stop,
 * which [onRelease] is handed the pin of (`timeSliderRelease`). Held, [isHeld], it takes no press and no focus.
 */
@Composable
private fun TimeSlider(
    window: TimeSliderWindow,
    value: Int,
    isHeld: Boolean,
    onRelease: (Instant?) -> Unit,
) {
    LocalMountayaRenderProbe.current?.report("TimeSlider")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val density = LocalDensity.current.density
    val radius = THUMB_RADIUS_DP * density
    var dragged by remember { mutableStateOf<Int?>(null) }
    val shown = dragged ?: value
    val zone = deviceTimeZone()
    val readout = timeReadoutText(timeSliderReadout(window, shown), scope) { momentText(it, scope.locale, zone) }
    val ticks = timeSliderTicks(window)
    val maxValue = window.maxValue
    // What a release does, read from the slider as it was drawn last, behind handlers that stay as it is drawn again.
    val release by rememberUpdatedState { stop: Int ->
        timeSliderRelease(window, stop)?.let { onRelease(it.pin) }
        Unit
    }
    val interactions = remember { MutableInteractionSource() }
    val fill = tokenColor(if (isHeld) tokens.controlFgDisabled else tokens.accent, scheme)
    val track = tokenColor(tokens.panelBorder, scheme)
    val tick = tokenColor(tokens.dimmed, scheme)
    val ground = tokenColor(tokens.panelBg, scheme)
    val ink = tokenColor(if (isHeld) tokens.dimmed else tokens.panelFg, scheme)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        BasicText(readout, Modifier.clearAndSetSemantics {}, style = TextStyle(color = ink, fontSize = 12.sp))
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = controlSide)
                .keyboardFocusRing(interactions, RoundedCornerShape(6.dp))
                .semantics {
                    contentDescription = scope.word("layers.time")
                    stateDescription = readout
                    progressBarRangeInfo =
                        ProgressBarRangeInfo(shown.toFloat(), 0f..maxValue.toFloat(), steps = maxValue - 1)
                    if (isHeld) {
                        disabled()
                    } else {
                        setProgress { target ->
                            release(target.roundToInt().coerceIn(0, maxValue))
                            true
                        }
                    }
                }.onKeyEvent { event ->
                    val step = if (isHeld || event.type != KeyEventType.KeyDown) null else timeSliderKeyStep(event.key)
                    if (step != null) release(timeSliderStepped(shown, step, maxValue))
                    step != null
                }.focusable(enabled = !isHeld, interactionSource = interactions)
                .pointerInput(isHeld, maxValue, radius) {
                    if (isHeld) return@pointerInput
                    detectTapGestures { at -> release(timeSliderStopAt(at.x, size.width.toFloat(), radius, maxValue)) }
                }.pointerInput(isHeld, maxValue, radius) {
                    if (isHeld) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { at ->
                            dragged = timeSliderStopAt(at.x, size.width.toFloat(), radius, maxValue)
                        },
                        onDragEnd = {
                            dragged?.let { release(it) }
                            dragged = null
                        },
                        onDragCancel = { dragged = null },
                    ) { change, _ ->
                        change.consume()
                        dragged = timeSliderStopAt(change.position.x, size.width.toFloat(), radius, maxValue)
                    }
                }.drawBehind {
                    val middle = size.height / 2f
                    val width = size.width

                    fun x(stop: Int): Float = timeSliderX(stop, width, radius, maxValue)
                    drawLine(
                        track,
                        Offset(x(0), middle),
                        Offset(x(maxValue), middle),
                        TRACK_DP * density,
                        StrokeCap.Round,
                    )
                    for (mark in ticks) {
                        val reach = TICK_REACH_DP * density
                        drawLine(
                            tick,
                            Offset(x(mark.value), middle - reach),
                            Offset(x(mark.value), middle + reach),
                            density,
                        )
                    }
                    drawLine(
                        fill,
                        Offset(x(window.nowValue), middle),
                        Offset(x(shown), middle),
                        TRACK_DP * density,
                        StrokeCap.Round,
                    )
                    drawCircle(ground, radius, Offset(x(shown), middle))
                    drawCircle(fill, radius - 2f * density, Offset(x(shown), middle))
                },
        )
        TimeSliderLabels(ticks, maxValue, radius, scope, tick)
    }
}

/** The words of the labelled [ticks], each centred under its tick, held within the track's width, in [color]. */
@Composable
private fun TimeSliderLabels(
    ticks: List<TimeSliderTick>,
    maxValue: Int,
    radius: Float,
    scope: ElementsScope,
    color: Color,
) {
    val labelled = ticks.mapNotNull { tick -> tick.word?.let { tick.value to scope.word(it) } }
    Layout(
        content = {
            for ((_, word) in labelled) {
                BasicText(
                    word,
                    style = TextStyle(color = color, fontSize = 11.sp),
                    softWrap = false,
                )
            }
        },
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else placeables.sumOf { it.width }
        layout(width, placeables.maxOfOrNull { it.height } ?: 0) {
            placeables.forEachIndexed { index, placeable ->
                val centre = timeSliderX(labelled[index].first, width.toFloat(), radius, maxValue)
                val left = (centre - placeable.width / 2f).roundToInt()
                placeable.place(left.coerceIn(0, (width - placeable.width).coerceAtLeast(0)), 0)
            }
        }
    }
}

/**
 * The caption under a time section's heading: what the slider drives, `layers.timeHint`, how a keyboard moves it,
 * `layers.timeKeyboard`, only where [hasKeyboard] says a hardware keyboard is at hand, and the zone its times are shown
 * in, `layers.timeNote`, joined by a space.
 */
internal fun timeCaption(
    scope: ElementsScope,
    hasKeyboard: Boolean,
): String =
    listOfNotNull(
        scope.word("layers.timeHint"),
        if (hasKeyboard) scope.word("layers.timeKeyboard") else null,
        scope.word("layers.timeNote"),
    ).joinToString(" ")

/**
 * What a time slider's thumb reads (`geo.layers.time-readout`): the moment [readout] stands for, written by [moment],
 * then ` · `, then its offset, `layers.timeNow`, or its counted word with its count written in the scope's locale, the
 * scope's own strings standing over the word as a template where they name it; the offset alone where the stop stands
 * for no instant.
 */
@OptIn(InternalMountayaApi::class)
internal fun timeReadoutText(
    readout: TimeSliderReadout,
    scope: ElementsScope,
    moment: (Instant) -> String,
): String {
    val count = readout.count
    val offset =
        if (count == null) {
            scope.word(readout.word)
        } else {
            scope.strings[readout.word]?.let {
                filledText(it, formatNumber(count.toDouble(), scope.locale, maximumFractionDigits = 0))
            } ?: (scope.words[readout.word.substringBefore('.')] as? JsonObject)
                ?.get(readout.word.substringAfter('.'))
                ?.let { countedText(it, count, scope.language) }
                ?: readout.word
        }
    return readout.instant?.let { moment(it) + READOUT_JOIN + offset } ?: offset
}

/** Where the stop [stop] of a track whose last is [maxValue] stands across [width] pixels, a thumb's [radius] in from either end. */
internal fun timeSliderX(
    stop: Int,
    width: Float,
    radius: Float,
    maxValue: Int,
): Float = radius + (width - 2f * radius).coerceAtLeast(0f) * stop / maxValue

/** The stop nearest [x] pixels across a track [width] pixels wide whose last is [maxValue], held to the track. */
internal fun timeSliderStopAt(
    x: Float,
    width: Float,
    radius: Float,
    maxValue: Int,
): Int {
    val span = width - 2f * radius
    if (span <= 0f) return 0
    return ((x - radius) / span * maxValue).roundToInt().coerceIn(0, maxValue)
}

/** The stop [step] stops from [value], held to a track whose last is [maxValue]. */
internal fun timeSliderStepped(
    value: Int,
    step: Int,
    maxValue: Int,
): Int = (value + step).coerceIn(0, maxValue)

/** The stops an arrow key moves a time slider by, one an hour, or null for any other key. */
internal fun timeSliderKeyStep(key: Key): Int? =
    when (key) {
        Key.DirectionLeft, Key.DirectionDown -> -1
        Key.DirectionRight, Key.DirectionUp -> 1
        else -> null
    }
