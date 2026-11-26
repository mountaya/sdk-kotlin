package com.mountaya.elements

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.mountaya.geo.generated.ElementTokens

/**
 * LocalMountayaTokens is the design tokens every element under it is drawn with, read under the colour scheme in
 * force (`geo.style.theme-tokens`), the map's markers among them. It is `ElementTokens.standard` where no composable
 * provides others: a token a consumer sets whole stands in both schemes, and one the table states reads its dark
 * value under the dark scheme.
 *
 * ```kotlin
 * val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
 * CompositionLocalProvider(LocalMountayaTokens provides tokens) { MapView(map) }
 * ```
 *
 * Every element under the provider, the map's markers among them, then draws in that accent in both schemes.
 */
public val LocalMountayaTokens: ProvidableCompositionLocal<ElementTokens> =
    staticCompositionLocalOf { ElementTokens.standard }
