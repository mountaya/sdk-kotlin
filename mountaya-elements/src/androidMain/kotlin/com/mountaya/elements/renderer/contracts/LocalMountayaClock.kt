package com.mountaya.elements

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Clock

/**
 * LocalMountayaClock is the clock the elements under it read the time through: a `LayersPanel`'s time section lays its
 * track out around its reading each time it is drawn (`geo.layers.time-window`), so the window follows the clock with no
 * timer of its own. It is the system's wall clock where no composable provides another; a host that pins or replays
 * time provides its own, and the window, the thumb's readout and a release's pin move with it.
 *
 * ```kotlin
 * CompositionLocalProvider(LocalMountayaClock provides replayClock) { LayersPanel(selection, offersTime = true) }
 * ```
 *
 * `replayClock` stands for a clock of the host's own, which the panel's window then follows.
 */
public val LocalMountayaClock: ProvidableCompositionLocal<Clock> = staticCompositionLocalOf { systemClock }
