package com.mountaya.elements

import androidx.compose.ui.graphics.Color
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.themeTokenValue

/**
 * tokenColor is the colour [token] reads under [scheme] (`geo.style.theme-tokens`) as Compose draws it, at the
 * token's opacity, and transparent where the value is no CSS hexadecimal colour.
 */
internal fun tokenColor(
    token: ThemeToken<HexColor>,
    scheme: ThemeScheme,
): Color = hexColor(themeTokenValue(token, scheme))

/** [color] as Compose draws it, at its opacity, and transparent where it is no CSS hexadecimal colour. */
internal fun hexColor(color: HexColor): Color = cssColor(color.hex, color.opacity) ?: Color.Transparent

/** The scheme an element is drawn in where the platform is [isDark] or not: dark under the dark one, light otherwise. */
internal fun themeScheme(isDark: Boolean): ThemeScheme = if (isDark) ThemeScheme.Dark else ThemeScheme.Light
