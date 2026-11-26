package com.mountaya.elements

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit

/**
 * textGrowth is how many times larger [density] draws text of [fontSize] than a font scale of 1 draws it: the reader's
 * font scale, read through the platform's own conversion of a size where it scales large text less than small, and 1
 * where it draws the text no larger. A row laid out for its text at a scale of 1 grows by it, so the text it holds
 * never clips; a scale under 1 shrinks the text and leaves the row as it is.
 */
internal fun textGrowth(
    density: Density,
    fontSize: TextUnit,
): Float {
    val drawn = with(density) { fontSize.toDp().value }
    return maxOf(1f, drawn / fontSize.value)
}
