package com.mountaya.build.generator

import com.mountaya.build.contract.kotlinLiteral

/**
 * Kotlin spelling: identifiers, literals and documentation, as the printer writes them.
 *
 * A name is the contract's, verbatim, but an enum member's, which [EnumMembers] reads off it. Where
 * it is not a plain Kotlin identifier it is written between backticks, which changes nothing a
 * caller types beyond them; where not even backticks can hold it on every target, the printer
 * refuses it rather than rename it.
 */
internal object KotlinText {
    /** The words Kotlin reserves everywhere. A soft keyword such as `data` or `value` is a plain name. */
    private val HARD_KEYWORDS =
        setOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while",
        )

    private val PLAIN = Regex("[A-Za-z_][A-Za-z0-9_]*")

    /**
     * What a name between backticks may hold on the jvm, on Android and on Kotlin/Native alike. A
     * hyphen may lead, as a descending sort order's wire value does (`-updated_at`).
     */
    private val QUOTABLE = Regex("[A-Za-z0-9_-][A-Za-z0-9_ -]*")

    /** Returns [name] as a Kotlin identifier: as written, or between backticks where it must be. */
    fun identifier(name: String): String =
        when {
            PLAIN.matches(name) && name !in HARD_KEYWORDS -> name
            QUOTABLE.matches(name) -> "`$name`"
            else -> throw ModelException("`$name` is a name no Kotlin target can declare, even between backticks")
        }

    /** Whether [name] is written as it is, with no backticks. */
    fun isPlain(name: String): Boolean = PLAIN.matches(name) && name !in HARD_KEYWORDS

    /** Returns [name] with its first letter uppercased, for a name the printer derives from it. */
    fun capitalized(name: String): String = name.replaceFirstChar(Char::uppercaseChar)

    /** Returns [text] as a Kotlin string literal. */
    fun literal(text: String): String = kotlinLiteral(text)

    /** Returns a JSON value the model carries as Kotlin source: a string, a number or a boolean. */
    fun value(value: Any): String =
        when (value) {
            is String -> literal(value)
            is Boolean, is Int, is Long -> value.toString()
            is Number -> value.toString()
            else -> throw ModelException("`$value` is not a value the printer can write as Kotlin")
        }

    /** Returns a JSON value as a reader of the documentation sees it. */
    fun shown(value: Any?): String =
        when (value) {
            null -> "null"
            is String -> "\"$value\""
            else -> value.toString()
        }

    /**
     * Returns the first sentence of [description], on one line, which is what a deprecation's
     * message carries. A description with none yields a sentence that says so.
     */
    fun firstSentence(description: String?): String {
        val text = description?.replace(Regex("\\s+"), " ")?.trim().orEmpty()
        if (text.isEmpty()) return "The contract deprecates this, and a later major removes it."
        val end = Regex("\\.(\\s|$)").find(text)
        return if (end == null) text else text.substring(0, end.range.first + 1)
    }

    /**
     * Appends a KDoc block holding [paragraphs], each a block of text, separated by a blank line.
     * Nothing is appended where every paragraph is empty.
     */
    fun StringBuilder.kdoc(
        indent: String,
        paragraphs: List<String?>,
    ) {
        val blocks = paragraphs.mapNotNull { paragraph -> paragraph?.trim()?.takeIf { it.isNotEmpty() } }
        if (blocks.isEmpty()) return
        val lines =
            blocks.flatMapIndexed { index, block ->
                (if (index == 0) emptyList() else listOf("")) +
                    block.lines()
            }
        if (lines.size == 1) {
            append(indent).append("/** ").append(escape(lines.single())).append(" */\n")
            return
        }
        append(indent).append("/**\n")
        for (line in lines) {
            val text = escape(line).trimEnd()
            if (text.isEmpty()) {
                append(indent).append(" *\n")
            } else {
                append(indent).append(" * ").append(text).append('\n')
            }
        }
        append(indent).append(" */\n")
    }

    /**
     * Returns [text] with each of its lines longer than [width] broken at the last space that keeps it
     * within, for prose the printer writes itself; the contract's own descriptions keep their breaks.
     */
    fun wrapped(
        text: String,
        width: Int,
    ): String =
        text.lines().joinToString("\n") { line ->
            val broken = mutableListOf<String>()
            var rest = line
            while (rest.length > width) {
                val at = rest.lastIndexOf(' ', width)
                if (at <= 0) break
                broken += rest.substring(0, at)
                rest = rest.substring(at + 1)
            }
            (broken + rest).joinToString("\n")
        }

    /** A comment cannot hold its own delimiters, and Kotlin nests block comments. */
    private fun escape(line: String): String = line.replace("*/", "* /").replace("/*", "/ *")
}
