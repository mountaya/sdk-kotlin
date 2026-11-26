package com.mountaya.build.contract

/**
 * Spells [text] as a Kotlin string literal in plain ASCII.
 *
 * Everything outside printable ASCII is written as a `\uXXXX` escape, so the generated file's
 * bytes do not depend on the encoding it is later read with.
 */
internal fun kotlinLiteral(text: String): String =
    buildString(text.length + 2) {
        append('"')
        for (character in text) {
            when {
                character == '\\' -> append("\\\\")
                character == '"' -> append("\\\"")
                character == '$' -> append("\\$")
                character == '\n' -> append("\\n")
                character == '\r' -> append("\\r")
                character == '\t' -> append("\\t")
                character.code in 0x20..0x7E -> append(character)
                else -> append("\\u").append(character.code.toString(16).padStart(4, '0'))
            }
        }
        append('"')
    }

/**
 * Splits [text] so that no piece becomes a string constant larger than the class file format
 * allows. A surrogate pair is never split across two pieces.
 */
internal fun chunks(
    text: String,
    size: Int = 8_000,
): List<String> {
    val pieces = mutableListOf<String>()
    var start = 0
    while (start < text.length) {
        var end = minOf(start + size, text.length)
        if (end < text.length && text[end - 1].isHighSurrogate()) end -= 1
        pieces += text.substring(start, end)
        start = end
    }
    return pieces
}

/** The header every generated file carries. */
internal fun generatedHeader(by: String): String =
    """
    |// GENERATED FILE. DO NOT EDIT.
    |//
    |// Written by `$by` from the contract checkout (mountaya/schemas). The next build
    |// overwrites it, and nothing here is the place to make a change.
    |
    """.trimMargin()
