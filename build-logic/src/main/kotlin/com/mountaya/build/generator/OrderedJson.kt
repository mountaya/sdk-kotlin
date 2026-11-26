package com.mountaya.build.generator

/**
 * An object as a JSON document writes it: its members in the order written, a key written twice
 * kept twice, so a reader can refuse what a map would fold away in silence.
 */
internal class JsonMembers(
    val members: List<Pair<String, Any?>>,
)

/**
 * Reads [text], which holds one JSON value and nothing else: an object as [JsonMembers], a list as a
 * [List], a string, a number as a [Double], a boolean, and null. It refuses anything JSON does not
 * write, naming where it stopped.
 */
internal fun readOrderedJson(text: String): Any? = OrderedJsonReader(text).document()

/** A reader of one JSON document, by recursive descent over its characters. */
private class OrderedJsonReader(
    private val text: String,
) {
    private var at = 0

    fun document(): Any? {
        val value = value()
        skipWhiteSpace()
        if (at != text.length) refuse("text after the document's value")
        return value
    }

    private fun value(): Any? {
        skipWhiteSpace()
        if (at >= text.length) refuse("the document ends where a value stands")
        return when (text[at]) {
            '{' -> objectValue()
            '[' -> listValue()
            '"' -> stringValue()
            't' -> word("true", true)
            'f' -> word("false", false)
            'n' -> word("null", null)
            else -> numberValue()
        }
    }

    private fun objectValue(): JsonMembers {
        at++
        val members = mutableListOf<Pair<String, Any?>>()
        skipWhiteSpace()
        if (peek() == '}') {
            at++
            return JsonMembers(members)
        }
        while (true) {
            skipWhiteSpace()
            if (peek() != '"') refuse("a member's name, a string, where it stands")
            val key = stringValue()
            skipWhiteSpace()
            expect(':')
            members += key to value()
            skipWhiteSpace()
            when (peek()) {
                ',' -> {
                    at++
                }

                '}' -> {
                    at++
                    return JsonMembers(members)
                }

                else -> {
                    refuse("a comma or the object's end")
                }
            }
        }
    }

    private fun listValue(): List<Any?> {
        at++
        val elements = mutableListOf<Any?>()
        skipWhiteSpace()
        if (peek() == ']') {
            at++
            return elements
        }
        while (true) {
            elements += value()
            skipWhiteSpace()
            when (peek()) {
                ',' -> {
                    at++
                }

                ']' -> {
                    at++
                    return elements
                }

                else -> {
                    refuse("a comma or the list's end")
                }
            }
        }
    }

    private fun stringValue(): String {
        at++
        val read = StringBuilder()
        while (true) {
            if (at >= text.length) refuse("a string the document never closes")
            val character = text[at++]
            when {
                character == '"' -> return read.toString()
                character == '\\' -> read.append(escaped())
                character.code < 0x20 -> refuse("a control character inside a string")
                else -> read.append(character)
            }
        }
    }

    private fun escaped(): Char {
        if (at >= text.length) refuse("a string the document never closes")
        return when (val character = text[at++]) {
            '"', '\\', '/' -> {
                character
            }

            'b' -> {
                '\b'
            }

            'f' -> {
                '\u000C'
            }

            'n' -> {
                '\n'
            }

            'r' -> {
                '\r'
            }

            't' -> {
                '\t'
            }

            'u' -> {
                if (at + 4 > text.length) refuse("a \\u escape of fewer than four digits")
                val digits = text.substring(at, at + 4)
                at += 4
                digits.toIntOrNull(16)?.toChar() ?: refuse("a \\u escape that is no four hexadecimal digits")
            }

            else -> {
                refuse("the escape \\$character, which JSON does not write")
            }
        }
    }

    private fun numberValue(): Double {
        val match = NUMBER.matchAt(text, at) ?: refuse("a value JSON does not write")
        at = match.range.last + 1
        return match.value.toDouble()
    }

    private fun word(
        spelled: String,
        value: Boolean?,
    ): Boolean? {
        if (!text.startsWith(spelled, at)) refuse("a value JSON does not write")
        at += spelled.length
        return value
    }

    private fun expect(character: Char) {
        if (peek() != character) refuse("`$character`")
        at++
    }

    private fun peek(): Char? = text.getOrNull(at)

    private fun skipWhiteSpace() {
        while (at < text.length && text[at] in WHITE_SPACE) at++
    }

    private fun refuse(what: String): Nothing {
        val line = text.substring(0, at.coerceAtMost(text.length)).count { it == '\n' } + 1
        throw ModelException("not JSON: $what, at line $line")
    }

    private companion object {
        val WHITE_SPACE = setOf(' ', '\t', '\n', '\r')
        val NUMBER = Regex("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")
    }
}
