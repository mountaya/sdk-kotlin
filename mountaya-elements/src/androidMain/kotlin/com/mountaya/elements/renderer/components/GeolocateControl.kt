package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.GeolocateControlEngine
import com.mountaya.geo.GeolocateControlTarget

/**
 * GeolocateControl centres a map on the device, framing the box a fix's accuracy spans (`map.camera.locate`), and says
 * what its fix and the standing permission say (`geo.controls.geolocate-state`).
 *
 * It shows a `GeolocateControlEngine` standing over the map: drawn in the `accent` from a press until every fix it
 * asked for settles, and live after a refusal, since a live control can say where the setting is and be pressed again
 * once it changed. It never asks the reader for access: while the standing permission, read without prompting when it
 * comes to stand over a map and again once each fix settles, answers refused, the action it reads out says location is
 * blocked and to allow it in the settings (`tooltips.geolocate.deniedDescription`); a fix that found no signal, or one
 * refused while the permission was never asked, keeps the ordinary one.
 *
 * A press asks the map for one fix, unless the app hands [onClick] in its place, which a press then calls alone, for
 * an app that runs its own flow, a prompt of its own among it; [isLocating] then says when its fix is out, since the
 * control tracks only its own. The camera eases onto a fix, and moves onto it at once where the reader turned
 * animations off, Android's animator duration scale at 0. [onError] is handed each fix of the control's that fails,
 * the map's `MapException` naming the reason. With neither a map nor a handler it is inert, and [enabled] false
 * disables it.
 *
 * @param map The map a press locates on, or null for none.
 * @param modifier The host's modifier, as every composable takes one.
 * @param enabled Whether it may be pressed at all.
 * @param isLocating Whether the app's own fix is out, where the app runs its own flow, or null for the control's word.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param onClick Handed a press, in place of the map's fix, where the app hands one.
 * @param onError Handed each fix of the control's that fails.
 */
@Composable
public fun GeolocateControl(
    map: MapEngine?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLocating: Boolean? = null,
    strings: Map<String, String>? = null,
    onClick: (() -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        val coroutines = rememberCoroutineScope()
        val engine = remember { GeolocateControlEngine(coroutines) }
        DisposableEffect(engine) { onDispose { engine.close() } }
        GeolocateControl(map, engine, modifier, enabled, isLocating, onClick, onError)
    }
}

/** The control over [map], showing [engine], which it stands over [map] and which its host closes. */
@Composable
internal fun GeolocateControl(
    map: MapEngine?,
    engine: GeolocateControlEngine,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLocating: Boolean? = null,
    onClick: (() -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    val scope = LocalMountayaScope.current
    // A fix is framed at once for a reader who turned animations off, read as the fix is framed.
    val jumps by rememberUpdatedState(motionRemoved())
    val target = remember(map) { map?.let { LocatedOn(it) { jumps } } }
    // The map the engine stands over, which its state speaks of; a state heard over another map says nothing here.
    var heard by remember { mutableStateOf<MapEngine?>(null) }
    LaunchedEffect(target) {
        engine.configure(target)
        heard = map
    }
    val state by engine.state.collectAsState()
    val isBlocked = heard === map && state.isBlocked
    ControlChip(modifier) {
        ControlButton(
            label = scope.word("controls.geolocate"),
            help = geolocateDescription(isBlocked, scope),
            onClick = { geolocatePress(engine, onClick, onError) },
            enabled = enabled && (map != null || onClick != null),
            isBusy = isLocating ?: state.isBusy,
        ) { color -> Glyph("geolocate", set = scope.controlGlyphs, size = 20.dp, color = color) }
    }
}

/**
 * LocatedOn is [map] as a locate control stands over it: its standing permission read as the map reads it, and each fix
 * framed at once where [jumps] answers true as the fix arrives, the reader having turned animations off, and eased
 * otherwise. One stands for one map, so a control composed again over the same map configures nothing again.
 */
internal class LocatedOn(
    private val map: MapEngine,
    private val jumps: () -> Boolean,
) : GeolocateControlTarget {
    override suspend fun locate(): List<Double> = map.locate(jumps())

    override suspend fun isLocationRefused(): Boolean = map.isLocationRefused()
}

/** A press: [onClick] where the app handed one, a fix [engine] asks its map for otherwise. */
internal fun geolocatePress(
    engine: GeolocateControlEngine,
    onClick: (() -> Unit)?,
    onError: ((Throwable) -> Unit)?,
) {
    if (onClick != null) onClick() else engine.locate(onError = { error -> onError?.invoke(error) })
}

/**
 * What the control says a press does, in the words of [scope]: that location is blocked while the standing permission
 * answers refused, and what a press does otherwise.
 */
internal fun geolocateDescription(
    isBlocked: Boolean,
    scope: ElementsScope,
): String = scope.word(if (isBlocked) "tooltips.geolocate.deniedDescription" else "tooltips.geolocate.description")
