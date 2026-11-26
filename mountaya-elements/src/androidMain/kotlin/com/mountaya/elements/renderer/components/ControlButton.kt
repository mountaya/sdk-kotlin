package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import kotlin.reflect.KProperty1

/** The side of a control's button, the touch target Android's guidelines name, in density-independent pixels. */
internal val controlSide: Dp = 48.dp

/**
 * Whether the controls under it may be pressed: false under a stack its app disabled, which shuts every control under
 * it whatever its own flag says, as SwiftUI's environment shuts every button under a disabled view.
 */
internal val LocalMountayaControlsEnabled: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { true }

/**
 * ControlButton is one button of a map's controls: a square the platform's touch target across, its face drawn in the
 * elements' tokens under the scheme in force, named to accessibility services by [label], the action a press takes
 * read out as [help].
 *
 * Its face is drawn in `control-fg`, in `accent` while what it asked for is out, and in `control-fg-disabled` while it
 * is disabled, by its own flag or a stack's around it, and handed to [face] as the colour to draw in, since Compose's
 * foundation keeps no colour of the content around a composable; its ground is the chip's around it,
 * `control-bg-hover` while a pointer hovers over it or its panel is open. It draws no look of the platform's over its
 * face: a disabled control is drawn in its own disabled colour, and a pressed one as it stands; a ring in `focus-ring`
 * marks it while it holds the keyboard's focus ([keyboardFocusRing]).
 */
@Composable
internal fun ControlButton(
    label: String,
    help: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isBusy: Boolean = false,
    isHeld: Boolean = false,
    face: @Composable (Color) -> Unit,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val isEnabled = enabled && LocalMountayaControlsEnabled.current
    val interactions = remember { MutableInteractionSource() }
    val isHovered by interactions.collectIsHoveredAsState()
    val ground = controlGroundToken(isHovered, isHeld)?.let { tokenColor(it.get(tokens), scheme) }
    Box(
        modifier
            .size(controlSide)
            .then(if (ground == null) Modifier else Modifier.background(ground))
            .keyboardFocusRing(interactions)
            .clickable(
                interactionSource = interactions,
                indication = null,
                enabled = isEnabled,
                onClickLabel = help.ifEmpty { null },
                role = Role.Button,
                onClick = onClick,
            ).semantics {
                contentDescription = label
                if (isHeld) selected = true
            },
        contentAlignment = Alignment.Center,
    ) {
        face(tokenColor(controlFaceToken(isEnabled, isBusy).get(tokens), scheme))
    }
}

/** The token a control's face is drawn in: disabled, busy, or neither. */
internal fun controlFaceToken(
    isEnabled: Boolean,
    isBusy: Boolean,
): KProperty1<ElementTokens, ThemeToken<HexColor>> =
    when {
        !isEnabled -> ElementTokens::controlFgDisabled
        isBusy -> ElementTokens::accent
        else -> ElementTokens::controlFg
    }

/**
 * The token a control's ground is drawn in where it draws one of its own: while a pointer hovers over it or its panel
 * is open, and none otherwise, the chip's showing.
 */
internal fun controlGroundToken(
    isHovered: Boolean,
    isHeld: Boolean,
): KProperty1<ElementTokens, ThemeToken<HexColor>>? = if (isHovered || isHeld) ElementTokens::controlBgHover else null
