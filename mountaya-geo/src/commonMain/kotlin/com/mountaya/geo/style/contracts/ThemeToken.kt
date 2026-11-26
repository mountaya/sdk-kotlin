package com.mountaya.geo

/**
 * ThemeToken is one of the values the elements are drawn with: its value under the light scheme, and
 * its value under the dark scheme where it states one of its own.
 *
 * `ElementTokens` holds every token the contract's tokens table names, and a consumer restyles the
 * elements by copying `ElementTokens.standard` with the tokens it changes. Two are equal where both
 * values are.
 */
public class ThemeToken<out Value>(
    /** The value under the light scheme, and under the dark one where [dark] is null. */
    public val light: Value,
    /** The value under the dark scheme, or null where the token reads [light] there too. */
    public val dark: Value? = null,
) {
    override fun equals(other: Any?): Boolean = other is ThemeToken<*> && other.light == light && other.dark == dark

    override fun hashCode(): Int = light.hashCode() * 31 + dark.hashCode()

    override fun toString(): String = "ThemeToken(light=$light, dark=$dark)"
}
