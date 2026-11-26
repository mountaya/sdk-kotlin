package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

/**
 * CompassControl turns a map back to north and level, and reads its bearing: its rose turns against the camera's
 * bearing so it keeps pointing north (`map.camera.control-moves`).
 *
 * A press drives the map, `MapEngine.resetNorth()`, unless the app hands [onClick] in its place, which a press then
 * calls alone; with neither a map nor a handler it is inert, and its rose points up. The reset eases the camera, and
 * moves it at once where the reader turned animations off, Android's animator duration scale at 0. [enabled] false
 * disables it. Its words are the scope's, `controls.compass` naming it and `tooltips.compass` describing it
 * (`MountayaScope`).
 *
 * @param map The map the compass turns and reads, or null for none.
 * @param modifier The host's modifier, as every composable takes one.
 * @param enabled Whether it may be pressed at all.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param onClick Handed a press, in place of the map's reset, where the app hands one.
 */
@Composable
public fun CompassControl(
    map: MapEngine?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onClick: (() -> Unit)? = null,
) {
    ProvideStrings(strings) {
        val scope = LocalMountayaScope.current
        val bearing =
            map
                ?.state
                ?.collectAsState()
                ?.value
                ?.camera
                ?.bearing ?: 0.0
        val jumps = motionRemoved()
        ControlChip(modifier, isRound = true) {
            ControlButton(
                label = scope.word("controls.compass"),
                help = scope.word("tooltips.compass.description"),
                onClick = { compassClick(map, onClick, jumps) },
                enabled = enabled && (map != null || onClick != null),
            ) { CompassRose(bearing) }
        }
    }
}

/** A press on the compass: the handler where the app handed one, the map's reset otherwise, made at once where it [jumps]. */
internal fun compassClick(
    map: MapEngine?,
    onClick: (() -> Unit)?,
    jumps: Boolean = false,
) {
    if (onClick != null) onClick() else map?.controlMove(ControlMove.ResetNorth, jumps)
}
