package com.mountaya.geo

/** What joins the tokens of a second line, so every kind of result reads with one rhythm: a middle dot between spaces. */
internal const val TOKEN_SEPARATOR: String = " · "

/** The first regional indicator symbol, which stands for the letter A. */
private const val REGIONAL_INDICATOR_A = 0x1F1E6

/**
 * Returns [value], a raw wire or OpenStreetMap value, as a reader reads it: each underscore a space
 * and the first character a capital, so `demanding_mountain_hiking` reads `Demanding mountain hiking`.
 * The first UTF-16 unit is written as the full Unicode capital of it, as every SDK writes one.
 */
internal fun titleCase(value: String): String {
    if (value.isEmpty()) return value
    val cleaned = value.replace('_', ' ')
    return cleaned.substring(0, 1).uppercase() + cleaned.substring(1)
}

/** The word [label] reads as for the reader: the host's, or [fallback] where the host has none. */
internal fun wordFor(
    label: SearchLabel,
    fallback: String,
    wording: SearchWording,
): String = wording.resolveLabel(label)?.takeIf { it.isNotEmpty() } ?: fallback

/** [tokens] that are words, joined by [TOKEN_SEPARATOR]. */
internal fun joinTokens(vararg tokens: String?): String =
    tokens.filterNot { it.isNullOrEmpty() }.joinToString(TOKEN_SEPARATOR)

/** [token], or nothing where it would repeat [name], the row's title, without regard to case. */
internal fun suppressIfMatchesName(
    token: String?,
    name: String,
): String = if (token.isNullOrEmpty() || token.lowercase() == name.lowercase()) "" else token

/**
 * The flag of the country an ISO 3166-1 alpha-2 [code] names, its two regional indicators, or
 * nothing for a code that is not two ASCII letters.
 */
internal fun countryFlag(code: String?): String {
    if (code == null || code.length != 2 || !code.all { it in 'a'..'z' || it in 'A'..'Z' }) return ""
    return buildString {
        for (letter in code.uppercase()) {
            val point = REGIONAL_INDICATOR_A + (letter - 'A')
            append(Char(0xD800 + ((point - 0x10000) shr 10)))
            append(Char(0xDC00 + ((point - 0x10000) and 0x3FF)))
        }
    }
}

/** A country as a second line states it: its name then its flag, either alone, or nothing. */
internal fun countryToken(
    code: String?,
    name: String?,
): String {
    val flag = countryFlag(code)
    return when {
        !name.isNullOrEmpty() && flag.isNotEmpty() -> "$name $flag"
        !name.isNullOrEmpty() -> name
        else -> flag
    }
}

/**
 * The `[longitude, latitude]` of [position] where it names a place, else null. An altitude the
 * wire's point carries is no part of the place a payload moves a map to, so it is dropped, as in
 * every SDK.
 */
internal fun placeOrNull(position: List<Double>?): List<Double>? =
    when {
        position == null || !namesPlace(position) -> null
        position.size == 2 -> position
        else -> listOf(position[0], position[1])
    }

/** [box] where it is four finite numbers, else null. */
internal fun boxOrNull(box: List<Double>?): List<Double>? = box?.takeIf { it.size == 4 && it.all(Double::isFinite) }

/** [text] as a second line, or none where it says nothing. */
internal fun textSecondary(text: String?): SearchRowSecondary? =
    text?.takeIf { it.isNotEmpty() }?.let { SearchRowSecondary.Text(it) }
