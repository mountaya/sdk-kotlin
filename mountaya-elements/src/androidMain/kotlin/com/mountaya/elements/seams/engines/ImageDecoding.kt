package com.mountaya.elements

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * imageDecoding answers the context a map's elevation tiles are decoded in, off the main thread the map is driven
 * from: the platform's pool for work that keeps a processor busy, `Dispatchers.Default`, which `MapView` hands its
 * readout's reader (`map.readout.open`).
 */
internal fun imageDecoding(): CoroutineContext = Dispatchers.Default
