package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.geo.generated.ElementLayout

/** The narrowest a card drawn on the panel's surface is, in density-independent pixels. */
internal const val PANEL_MIN_WIDTH_DP: Int = 240

/** The tallest a panel drawn on its own is before its sections scroll, in density-independent pixels. */
internal const val PANEL_MAX_HEIGHT_DP: Int = 420

/** The padding around a panel's title and its body, in density-independent pixels. */
internal const val PANEL_PADDING_DP: Int = 12

/**
 * PanelSurface is the surface a control's panel is drawn on, as the embed lays out its own (`geo.panels.layout`): its
 * ground `panel-bg` and its edge `panel-border`, each under the scheme in force, a title row naming it, [title], which
 * stays put, and its sections top to bottom under it. Its rows' text is `panel-fg`, which each row reads, since
 * Compose's foundation hands no colour of the content down to text.
 *
 * A panel a control stack opens is laid out by `LocalMountayaPanelLayout`: `panel-width` wide, the width the map's safe
 * area leaves on a narrow map, and no taller than the room the safe area leaves it beside its toggle and clear of the
 * watermark's frame; one drawn on its own is `panel-width`
 * wide and at most 420 density-independent pixels high. Sections taller than it scroll within it, under the title, its
 * ground and its edge standing still, rather than growing past the map.
 */
@Composable
internal fun PanelSurface(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val layout = LocalMountayaPanelLayout.current
    val map = LocalMountayaMapBounds.current
    val density = LocalDensity.current
    val shape = RoundedCornerShape(8.dp)
    val stated = layout?.width
    val sized =
        when {
            // A card that is no control's panel, the waypoint card's, is as wide as its widest row.
            layout == null && title == null -> {
                modifier.widthIn(min = PANEL_MIN_WIDTH_DP.dp).width(IntrinsicSize.Max)
            }

            layout == null -> {
                modifier.width(ElementLayout.panelWidth.dp)
            }

            stated != null -> {
                modifier.width(stated.dp)
            }

            // A narrow map's panel fills the width its safe area leaves it, inside the stack's own padding.
            map != null -> {
                modifier.width(
                    (
                        with(
                            density,
                        ) { map.safeArea.width.toDp() } - (2 * MAP_CONTROLS_PADDING_DP).dp
                    ).coerceAtLeast(0.dp),
                )
            }

            else -> {
                modifier.fillMaxWidth()
            }
        }
    Column(
        sized
            .heightIn(max = layout?.maxHeight?.dp ?: PANEL_MAX_HEIGHT_DP.dp)
            .background(tokenColor(tokens.panelBg, scheme), shape)
            .border(1.dp, tokenColor(tokens.panelBorder, scheme), shape),
    ) {
        if (title != null) {
            BasicText(
                title,
                Modifier
                    .padding(
                        start = PANEL_PADDING_DP.dp,
                        end = PANEL_PADDING_DP.dp,
                        top = PANEL_PADDING_DP.dp,
                    ).semantics {
                        heading()
                    },
                style =
                    TextStyle(
                        color = tokenColor(tokens.panelFg, scheme),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
            )
        }
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(PANEL_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

/** PanelHeading heads a section of a panel, in the `dimmed` token under the scheme in force, and is read by accessibility services as a heading. */
@Composable
internal fun PanelHeading(
    text: String,
    modifier: Modifier = Modifier,
) {
    val color = tokenColor(LocalMountayaTokens.current.dimmed, themeSchemeInForce())
    BasicText(
        text,
        modifier.semantics { heading() },
        style = TextStyle(color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    )
}

/**
 * PanelRow is one row of a panel: its label, in `panel-fg`, and beside it what the row shows, a tick, a lock or a
 * glyph, drawn by [accessory] in the colour it is handed; pressed as a whole, the platform's touch target high, ringed
 * in `focus-ring` while it holds the keyboard's focus, and read by accessibility services as selected while it is
 * checked. Each composition is told to the render probe a test provides, by the row's label.
 */
@Composable
internal fun PanelRow(
    label: String,
    isChecked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accessory: @Composable (Color) -> Unit = {},
) {
    LocalMountayaRenderProbe.current?.report("PanelRow $label")
    val ink = tokenColor(LocalMountayaTokens.current.panelFg, themeSchemeInForce())
    val interactions = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = controlSide)
            .keyboardFocusRing(interactions)
            .selectable(
                selected = isChecked,
                interactionSource = interactions,
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(label, Modifier.weight(1f), style = TextStyle(color = ink, fontSize = 14.sp))
        accessory(ink)
    }
}
