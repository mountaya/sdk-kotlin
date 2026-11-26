package com.mountaya.elements

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Rect

/**
 * MapBounds is where the map a control stack lies over stands in its window, in the window's pixels: the [map] itself,
 * its [safeArea], the part of it the device leaves visible that the stack's space covers, and the watermark's frame the
 * stack keeps clear, [keepClear], or null while the map keeps none clear. `MapControls` reads them off the map's real
 * frame, which the `MapView` drawing the map publishes on its engine, and off the space it fills where no view draws
 * a map; a panel control in no stack reads them off its window, the frame held as a waiting read holds it.
 */
internal class MapBounds(
    val map: Rect,
    val safeArea: Rect,
    val keepClear: Rect?,
) {
    override fun equals(other: Any?): Boolean =
        other is MapBounds && other.map == map && other.safeArea == safeArea && other.keepClear == keepClear

    override fun hashCode(): Int = (map.hashCode() * 31 + safeArea.hashCode()) * 31 + keepClear.hashCode()

    override fun toString(): String = "MapBounds(map=$map, safeArea=$safeArea, keepClear=$keepClear)"
}

/**
 * LocalMountayaMapBounds is where the map a control stack lies over stands in its window ([MapBounds]), or null outside
 * a stack or before it is laid out: a panel the stack opens is laid out by it (`geo.panels.layout`), its width, its
 * height and its top inset read off the map's safe area, and placed beside its toggle inside that safe area and clear of
 * the watermark's frame (`geo.organization.watermark`).
 */
internal val LocalMountayaMapBounds: ProvidableCompositionLocal<MapBounds?> = compositionLocalOf { null }
