package com.mountaya.geo

/**
 * The canvas height, in screen pixels, a camera's altitude and zoom are converted at where the
 * caller names none, the `defaultViewportHeightPx` `geo/zoom-altitude.json` declares: a nominal
 * canvas, so a conversion needs no live map.
 */
internal const val DEFAULT_VIEWPORT_HEIGHT_PIXELS: Double = 800.0

/**
 * The camera's distance from the screen in canvas heights, `1 / (2 × tan(fov / 2))` for a vertical
 * field of view of about 36.87 degrees, the renderer's default. `geo/zoom-altitude.json` states no
 * such constant and its cases need this one, so it is declared here with the fixture as its source.
 */
internal const val FIELD_OF_VIEW_DISTANCE_FACTOR: Double = 1.5

/**
 * The camera's distance from the screen, in screen pixels, for a canvas [viewportHeightPixels]
 * tall: the height the perspective model scales an altitude to ground by. A height that is not a
 * finite number above zero is no canvas, and the nominal one is read instead.
 */
internal fun cameraDistancePixels(viewportHeightPixels: Double): Double {
    val height =
        if (viewportHeightPixels.isFinite() && viewportHeightPixels > 0.0) {
            viewportHeightPixels
        } else {
            DEFAULT_VIEWPORT_HEIGHT_PIXELS
        }
    return height * FIELD_OF_VIEW_DISTANCE_FACTOR
}
