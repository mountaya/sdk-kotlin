package com.mountaya.elements

import com.mountaya.geo.PixelPadding
import kotlin.time.Duration

/**
 * CameraFit is how a camera frames a box (`map.camera.fit-options`): the inset kept clear between the box
 * and each edge of the canvas, the zoom it lands at at most, and how long it eases there, each null for
 * the default.
 *
 * `CameraFit()`, stating nothing, frames with no padding under the zoom window's ceiling, at the
 * renderer's own pace, and a fit stating members frames with those and no other. A padding or a duration
 * that is not a finite number from 0 up, and a ceiling that is not a finite number, are refused before
 * anything is read or drawn. Where the fit lands is `cameraForBounds`'s (`geo.camera.fit`), which this SDK
 * computes rather than asking its renderer.
 *
 * Two are equal where they state the same, each number as the same number, as every Geo value compares.
 */
public class CameraFit(
    /** The inset kept clear between the box and each edge of the canvas, in density-independent pixels, or null for none. */
    public val padding: PixelPadding? = null,
    /**
     * The highest zoom the fit lands at, or null for the zoom window's ceiling. A ceiling past the window is
     * handed on, and the window holds the camera under its own.
     */
    public val maxZoom: Double? = null,
    /** How long the camera eases to the fit, or null for the renderer's own pace. */
    public val duration: Duration? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is CameraFit &&
            other.padding == padding &&
            sameMember(other.maxZoom, maxZoom) &&
            other.duration == duration

    override fun hashCode(): Int {
        var hash = padding?.hashCode() ?: 0
        hash = 31 * hash + (maxZoom?.toBits()?.hashCode() ?: 0)
        hash = 31 * hash + duration.hashCode()
        return hash
    }

    override fun toString(): String = "CameraFit(padding=$padding, maxZoom=$maxZoom, duration=$duration)"
}

/** Whether [a] and [b] are both absent or the same number, its bits compared, every `NaN` read as one. */
private fun sameMember(
    a: Double?,
    b: Double?,
): Boolean = if (a == null || b == null) a == null && b == null else a.toBits() == b.toBits()
