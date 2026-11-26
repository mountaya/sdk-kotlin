package com.mountaya.elements

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.mountaya.geo.ThemeScheme

/**
 * LocalMountayaScheme is the colour scheme a `MountayaScope` states for the elements under it, light or dark, or null
 * where no scope around them states one: the platform's configuration then decides, its night mode reading the dark
 * values (`themeSchemeInForce`).
 */
internal val LocalMountayaScheme: ProvidableCompositionLocal<ThemeScheme?> = staticCompositionLocalOf { null }
