package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mountaya.geo.HexColor
import com.mountaya.geo.themeTokenValue

/**
 * ControlChip is the chip a map's controls stand on, one control or a flush group of them, top to bottom: its ground
 * is the `control-bg` token under the scheme in force, a rounded square, or a disc for the compass, lifted off the map
 * by the platform's elevation shadow in the `control-shadow` colours.
 *
 * Android draws an elevation shadow at the alpha its theme names, multiplying a colour's own, so the colours are
 * handed over opaque: a restyled hue tints the shadow, and the platform's alpha keeps it the platform's weight.
 */
@Composable
internal fun ControlChip(
    modifier: Modifier = Modifier,
    isRound: Boolean = false,
    content: @Composable () -> Unit,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val shape = if (isRound) CircleShape else RoundedCornerShape(8.dp)
    val shadows = themeTokenValue(tokens.controlShadow, scheme)
    Column(
        modifier
            .shadow(
                2.dp,
                shape,
                clip = false,
                ambientColor = shadowColor(shadows.getOrNull(0)),
                spotColor = shadowColor(shadows.getOrNull(1)),
            ).background(tokenColor(tokens.controlBg, scheme), shape)
            .clip(shape),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}

/** A shadow layer's colour, opaque, black where the token states none or no colour. */
private fun shadowColor(color: HexColor?): Color = color?.let { cssColor(it.hex) } ?: Color.Black
