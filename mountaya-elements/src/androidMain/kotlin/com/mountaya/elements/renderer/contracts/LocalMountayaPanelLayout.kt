package com.mountaya.elements

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mountaya.geo.PanelLayout

/**
 * LocalMountayaPanelLayout is how the panel a control opens is laid out over its map (`geo.panels.layout`), which the
 * control's toggle reads off the map and its own place on it, or null for a panel drawn on its own, which lays out at
 * the panel's own width.
 */
internal val LocalMountayaPanelLayout: ProvidableCompositionLocal<PanelLayout?> = compositionLocalOf { null }
