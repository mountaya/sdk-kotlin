package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mountaya.geo.ZoomStep
import com.mountaya.geo.zoomControlsDisabled
import com.mountaya.geo.zoomLimitReached

/**
 * ZoomControl is a map's zoom in and zoom out, one chip, zoom in above zoom out, each half a step of the camera
 * (`map.camera.control-moves`).
 *
 * Each half drives the map, `MapEngine.zoomIn()` and `zoomOut()`, unless the app hands a handler in its place,
 * [onZoomInClick] or [onZoomOutClick], which a press then calls alone; a half with neither a map nor a handler is inert.
 * A step eases the camera, and moves it at once where the reader turned animations off, Android's animator duration
 * scale at 0.
 * Each half follows the map's zoom window whoever answers its press: zoom in is disabled while the camera stands at
 * the window's ceiling, and zoom out at its floor (`geo.camera.zoom-limit-reached`). [enabled] false disables both.
 *
 * Its words are the scope's, `controls.zoomIn` and `controls.zoomOut` naming each half and the `tooltips` describing
 * it, and its glyphs the scope's control set (`MountayaScope`); its colours are the tokens' (`LocalMountayaTokens`).
 *
 * @param map The map the halves step, or null for none.
 * @param modifier The host's modifier, as every composable takes one.
 * @param enabled Whether either half may be pressed at all.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param onZoomInClick Handed a press on zoom in, in place of the map's step, where the app hands one.
 * @param onZoomOutClick Handed a press on zoom out, in place of the map's step, where the app hands one.
 */
@Composable
public fun ZoomControl(
    map: MapEngine?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onZoomInClick: (() -> Unit)? = null,
    onZoomOutClick: (() -> Unit)? = null,
) {
    ProvideStrings(strings) {
        val scope = LocalMountayaScope.current
        val disabled = disabledZoomSteps(map?.state?.collectAsState()?.value)
        val jumps = motionRemoved()
        ControlChip(modifier) {
            ControlButton(
                label = scope.word("controls.zoomIn"),
                help = scope.word("tooltips.zoomIn.description"),
                onClick = { zoomIn(map, onZoomInClick, jumps) },
                enabled = enabled && ZoomStep.ZoomIn !in disabled && (map != null || onZoomInClick != null),
            ) { color -> Glyph("zoom-in", set = scope.controlGlyphs, size = 20.dp, color = color) }
            ControlButton(
                label = scope.word("controls.zoomOut"),
                help = scope.word("tooltips.zoomOut.description"),
                onClick = { zoomOut(map, onZoomOutClick, jumps) },
                enabled = enabled && ZoomStep.ZoomOut !in disabled && (map != null || onZoomOutClick != null),
            ) { color -> Glyph("zoom-out", set = scope.controlGlyphs, size = 20.dp, color = color) }
        }
    }
}

/** A press on zoom in: the handler where the app handed one, the map's step otherwise, made at once where it [jumps]. */
internal fun zoomIn(
    map: MapEngine?,
    onZoomInClick: (() -> Unit)?,
    jumps: Boolean = false,
) {
    if (onZoomInClick != null) onZoomInClick() else map?.controlMove(ControlMove.ZoomIn, jumps)
}

/** A press on zoom out: the handler where the app handed one, the map's step otherwise, made at once where it [jumps]. */
internal fun zoomOut(
    map: MapEngine?,
    onZoomOutClick: (() -> Unit)?,
    jumps: Boolean = false,
) {
    if (onZoomOutClick != null) onZoomOutClick() else map?.controlMove(ControlMove.ZoomOut, jumps)
}

/** The halves the end of its zoom window the camera of a map in [state] stands at disables, none with no map. */
internal fun disabledZoomSteps(state: MapState?): List<ZoomStep> =
    if (state == null) emptyList() else zoomControlsDisabled(zoomLimitReached(state.camera.zoom, state.zoomBounds))
