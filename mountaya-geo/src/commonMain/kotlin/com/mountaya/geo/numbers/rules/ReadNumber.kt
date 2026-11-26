package com.mountaya.geo

/** The radixes a prefixed whole number is written in. */
private const val HEXADECIMAL = 16
private const val OCTAL = 8
private const val BINARY = 2

/** The value a lower-case hexadecimal letter stands for past its code, `a` being 10. */
private const val LETTER_DIGIT = 10

/**
 * readNumber reads [text] as a number the way ECMAScript's `Number` reads a string, so a value an author typed
 * reads as one number in every SDK: the white space at its ends, as `trimWhiteSpace` reads it, is left out; the
 * empty text is 0; a decimal literal, signed or not, with a fraction and an exponent or not, `.5` and `5.`
 * included, is its value; `Infinity`, signed or not, is an infinity; an unsigned `0x`, `0o` or `0b` literal is
 * the whole number its digits state in base 16, 8 or 2; and anything else, `1_000`, `0x1p3`, `nan` and `inf`
 * among them, is `NaN`. A digit is an ASCII digit, never another script's.
 */
internal fun readNumber(text: String): Double {
    val trimmed = trimWhiteSpace(text)
    if (trimmed.isEmpty()) return 0.0
    prefixedInteger(trimmed)?.let { return it }
    var body = trimmed
    var sign = 1.0
    val first = body.first()
    if (first == '+' || first == '-') {
        sign = if (first == '-') -1.0 else 1.0
        body = body.substring(1)
    }
    if (body == "Infinity") return sign * Double.POSITIVE_INFINITY
    if (!isDecimalLiteral(body)) return Double.NaN
    return trimmed.toDoubleOrNull() ?: Double.NaN
}

/** The value of an unsigned `0x`, `0o` or `0b` literal, null for any other text. */
private fun prefixedInteger(text: String): Double? {
    if (text.length <= 2 || text[0] != '0') return null
    val base =
        when (text[1]) {
            'x', 'X' -> HEXADECIMAL
            'o', 'O' -> OCTAL
            'b', 'B' -> BINARY
            else -> return null
        }
    var value = 0.0
    for (char in text.substring(2)) {
        val digit = hexDigitValue(char)
        if (digit == null || digit >= base) return Double.NaN
        value = value * base + digit
    }
    return value
}

/** The value of an ASCII hexadecimal digit, null for any other character. */
private fun hexDigitValue(char: Char): Int? =
    when (char) {
        in '0'..'9' -> char - '0'
        in 'a'..'f' -> char - 'a' + LETTER_DIGIT
        in 'A'..'F' -> char - 'A' + LETTER_DIGIT
        else -> null
    }

/**
 * Whether unsigned [text] is a decimal literal: digits with an optional fraction, or a fraction alone, then an
 * optional exponent of signed or unsigned digits.
 */
private fun isDecimalLiteral(text: String): Boolean {
    var index = 0

    fun digits(): Int {
        val start = index
        while (index < text.length && text[index] in '0'..'9') index += 1
        return index - start
    }
    var mantissa = digits()
    if (index < text.length && text[index] == '.') {
        index += 1
        mantissa += digits()
    }
    if (mantissa == 0) return false
    if (index < text.length && (text[index] == 'e' || text[index] == 'E')) {
        index += 1
        if (index < text.length && (text[index] == '+' || text[index] == '-')) index += 1
        if (digits() == 0) return false
    }
    return index == text.length
}
