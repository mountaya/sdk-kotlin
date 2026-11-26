package com.mountaya.elements

import androidx.compose.ui.graphics.Color

/**
 * cssColor reads a CSS hexadecimal colour, `#rgb`, `#rrggbb` or `#rrggbbaa` in either case, into Compose's, at
 * [opacity] times its own alpha, or answers null where it is none: a colour of any other spelling, or a
 * digit of another script. Android's own parser reads eight digits as alpha first, where CSS writes it last.
 */
internal fun cssColor(
    written: String,
    opacity: Double = 1.0,
): Color? {
    if (!written.startsWith("#")) return null
    var digits = written.substring(1)
    if (!digits.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
    if (digits.length == 3) digits = digits.map { "$it$it" }.joinToString("")
    if (digits.length == 6) digits += "ff"
    if (digits.length != 8) return null
    val value = digits.toLong(16)
    return Color(
        red = (value shr 24 and 0xff).toInt() / 255f,
        green = (value shr 16 and 0xff).toInt() / 255f,
        blue = (value shr 8 and 0xff).toInt() / 255f,
        alpha = ((value and 0xff).toInt() / 255.0 * opacity).toFloat().coerceIn(0f, 1f),
    )
}
