package com.mountaya.elements

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle

/**
 * textWidth is how wide [text] is laid out in [style] by [measurer], in pixels, which a row of names measures to
 * leave the crowded ones out (`geo.profile.axis-label-culling`).
 */
internal fun textWidth(
    text: String,
    measurer: TextMeasurer,
    style: TextStyle,
): Float =
    measurer
        .measure(text, style, maxLines = 1, softWrap = false)
        .size.width
        .toFloat()
