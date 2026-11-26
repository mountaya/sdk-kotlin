package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * DirectionsControl asks the app to open directions for what the map shows. It drives nothing of the map's, so its
 * handler is required: what directions open, a panel or a screen, is the app's and cannot be guessed. It is live
 * whether or not the map has loaded, and [enabled] false disables it. Its words are the scope's, `controls.directions`
 * naming it and `tooltips.directions` describing it (`MountayaScope`).
 *
 * @param onClick Handed each press.
 * @param modifier The host's modifier, as every composable takes one.
 * @param enabled Whether it may be pressed at all.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 */
@Composable
public fun DirectionsControl(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
) {
    ProvideStrings(strings) {
        val scope = LocalMountayaScope.current
        ControlChip(modifier) {
            ControlButton(
                label = scope.word("controls.directions"),
                help = scope.word("tooltips.directions.description"),
                onClick = onClick,
                enabled = enabled,
            ) { color -> Glyph("directions", set = scope.controlGlyphs, size = 20.dp, color = color) }
        }
    }
}
