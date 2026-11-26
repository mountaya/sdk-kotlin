package com.mountaya.elements

import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment

/**
 * MapCorner is the corner of a map a control stack sits in, as the reader sees it, whichever way the text around it
 * runs: top right unless the app names another.
 */
public enum class MapCorner {
    /** The map's top right corner, where a stack sits unless another is named. */
    TopRight,

    /** The map's top left corner. */
    TopLeft,

    /** The map's bottom right corner. */
    BottomRight,

    /** The map's bottom left corner. */
    BottomLeft,
    ;

    /** Where the corner stands in a box, the same in a layout that runs right to left, so it stays where the reader sees it. */
    internal val alignment: Alignment
        get() =
            when (this) {
                TopRight -> AbsoluteAlignment.TopRight
                TopLeft -> AbsoluteAlignment.TopLeft
                BottomRight -> AbsoluteAlignment.BottomRight
                BottomLeft -> AbsoluteAlignment.BottomLeft
            }
}
