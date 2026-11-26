package com.mountaya.elements

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * MapPanel names a panel a control stack opens: one the stack ships, [Layers], [Pois] or [Preferences], or one of the
 * app's own, by a name of its own (`PanelControl`). A stack holds one panel open at a time.
 *
 * Two are equal where their names are.
 */
@JvmInline
public value class MapPanel(
    /** The panel's name. */
    public val name: String,
) {
    /** The panels a control stack ships. */
    public companion object {
        /** The base-map and overlay picker (`LayersPanel`). */
        public val Layers: MapPanel = MapPanel("layers")

        /** The points-of-interest picker (`PoiPanel`). */
        public val Pois: MapPanel = MapPanel("pois")

        /** The units and language picker (`PreferencesPanel`). */
        public val Preferences: MapPanel = MapPanel("preferences")
    }
}

/**
 * The panel open in the control stack around a composable, which every panel control under it opens in place of any
 * other, or null outside a stack.
 */
internal val LocalMountayaOpenPanel: ProvidableCompositionLocal<MutableState<MapPanel?>?> = compositionLocalOf { null }
