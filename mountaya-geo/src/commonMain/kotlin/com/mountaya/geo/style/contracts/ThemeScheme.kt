package com.mountaya.geo

/**
 * ThemeScheme is the colour scheme an element is drawn under, light or dark, carrying the contract's
 * spelling (`geo.style.theme-tokens`).
 *
 * It names no platform's scheme, so it builds and compares on every target: a composable reads the
 * scheme its platform draws under into it, Compose's `isSystemInDarkTheme()` among them.
 */
public enum class ThemeScheme(
    /** The scheme as `geo/theme-tokens.json` spells it. */
    public val id: String,
) {
    /** The light scheme, under which every token reads its light value. */
    Light("light"),

    /** The dark scheme, under which a token reads its dark value where it states one. */
    Dark("dark"),
}
