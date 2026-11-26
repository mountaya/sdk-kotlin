package com.mountaya.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The width of the ring a control draws while it holds the keyboard's focus. */
internal val focusRingWidth: Dp = 2.dp

/**
 * Whether a control draws its focus ring: while it holds focus and the input mode is the keyboard's, never where a
 * touch moved the focus to it, as the web's `:focus-visible` tells a keyboard's focus from a press's.
 */
internal fun showsFocusRing(
    isFocused: Boolean,
    inputMode: InputMode,
): Boolean = isFocused && inputMode == InputMode.Keyboard

/**
 * keyboardFocusRing draws a ring [focusRingWidth] wide in the `focus-ring` token under the scheme in force, inside the
 * bounds of what it modifies and in [shape], while the control [interactions] tells of holds the keyboard's focus
 * ([showsFocusRing]), and nothing otherwise. The control hands it the interaction source it is pressed through, which
 * is told each focus it takes. The ring is a colour of its own rather than the accent, so a consumer's accent leaves it
 * standing at least 3 to 1 on the panel in both schemes, as WCAG 1.4.11 asks of a focus indicator.
 */
@Composable
internal fun Modifier.keyboardFocusRing(
    interactions: InteractionSource,
    shape: Shape = RectangleShape,
): Modifier {
    val isFocused by interactions.collectIsFocusedAsState()
    if (!showsFocusRing(isFocused, LocalInputModeManager.current.inputMode)) return this
    return border(focusRingWidth, tokenColor(LocalMountayaTokens.current.focusRing, themeSchemeInForce()), shape)
}
