package com.mountaya.elements

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * LocalMountayaRenderProbe is the probe the composables under it report each composition and drawing of a part a
 * hot-path budget reads to (`RenderProbe`), or null, which it is everywhere a test provides none: a part then reads
 * null and reports nothing, allocating nothing. It is static, since a test provides one probe for the whole content it
 * draws and never changes it.
 */
internal val LocalMountayaRenderProbe: ProvidableCompositionLocal<RenderProbe?> = staticCompositionLocalOf { null }
