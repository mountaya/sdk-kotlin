package com.mountaya.geo

/**
 * HexColor is a colour as the contract's tables write one: its red, green and blue as six hexadecimal
 * digits, `#rrggbb`, and its opacity, 1 where it is opaque.
 *
 * It names no platform's colour type, so it builds and compares on every target; a renderer reads it
 * into its own where it draws. Two are equal where both parts are.
 */
public class HexColor(
    /** The colour's red, green and blue, `#rrggbb`, as the table writes them. */
    public val hex: String,
    /** How opaque the colour is, from 0 to 1. */
    public val opacity: Double = 1.0,
) {
    override fun equals(other: Any?): Boolean =
        other is HexColor && other.hex == hex && other.opacity.compareTo(opacity) == 0

    override fun hashCode(): Int = hex.hashCode() * 31 + opacity.hashCode()

    override fun toString(): String = "HexColor(hex=$hex, opacity=$opacity)"
}
