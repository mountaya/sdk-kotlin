package com.mountaya.client

/**
 * StructuredFieldList reads a Structured Field List, as RFC 9651 section 4.2 has a recipient
 * parse one.
 *
 * A field that does not parse is ignored whole, which is what the RFC has a recipient do, so the
 * reader answers null for it rather than the members before the fault.
 */
internal object StructuredFieldList {
    /** Item is a bare item. */
    sealed interface Item {
        /** An integer, of up to fifteen digits. */
        data class Integer(
            val value: Long,
        ) : Item

        /** A decimal, of up to twelve digits before its point and three after it. */
        data class Decimal(
            val value: Double,
        ) : Item

        /** A string, its escapes read. */
        data class Text(
            val value: String,
        ) : Item

        /** A token, as written. */
        data class Token(
            val value: String,
        ) : Item

        /** A byte sequence, its base64 read. */
        data class Bytes(
            val value: List<Byte>,
        ) : Item

        /** A boolean. */
        data class Flag(
            val value: Boolean,
        ) : Item

        /** A date, in seconds from the epoch. */
        data class Date(
            val seconds: Long,
        ) : Item

        /** A display string, its percent-encoded UTF-8 read. */
        data class DisplayText(
            val value: String,
        ) : Item
    }

    /**
     * Member is one member of the list, an item or an inner list of them, with its parameters in
     * the order they were written, a key written twice holding its last value.
     */
    sealed interface Member {
        /** The member's parameters. */
        val parameters: List<Parameter>

        /** A bare item. */
        data class Single(
            val item: Item,
            override val parameters: List<Parameter>,
        ) : Member

        /** An inner list. */
        data class InnerList(
            val items: List<InnerItem>,
            override val parameters: List<Parameter>,
        ) : Member
    }

    /** InnerItem is one item of an inner list, with its parameters. */
    data class InnerItem(
        val item: Item,
        val parameters: List<Parameter>,
    )

    /** Parameter is one parameter of an item or an inner list. */
    data class Parameter(
        val key: String,
        val value: Item,
    )

    /** Returns the members of a field's value, or null where it does not parse. */
    fun parse(text: String): List<Member>? = Reader(text.encodeToByteArray()).list()

    /** Reader walks a field's bytes once, each step consuming what it read. */
    private class Reader(
        private val bytes: ByteArray,
    ) {
        private var position = 0

        private val current: Int? get() = if (position < bytes.size) bytes[position].toInt() and 0xFF else null

        private val isAtEnd: Boolean get() = position >= bytes.size

        fun list(): List<Member>? {
            skip { it == SPACE }
            val members = mutableListOf<Member>()
            while (!isAtEnd) {
                members += member() ?: return null
                skip { it == SPACE || it == TAB }
                if (isAtEnd) return members
                if (current != COMMA) return null
                position += 1
                skip { it == SPACE || it == TAB }
                if (isAtEnd) return null
            }
            return members
        }

        private fun member(): Member? {
            if (current == OPEN_PARENTHESIS) {
                val items = innerList() ?: return null
                val parameters = parameters() ?: return null
                return Member.InnerList(items, parameters)
            }
            val item = bareItem() ?: return null
            val parameters = parameters() ?: return null
            return Member.Single(item, parameters)
        }

        private fun innerList(): List<InnerItem>? {
            position += 1
            val items = mutableListOf<InnerItem>()
            while (!isAtEnd) {
                skip { it == SPACE }
                if (current == CLOSE_PARENTHESIS) {
                    position += 1
                    return items
                }
                val item = bareItem() ?: return null
                val parameters = parameters() ?: return null
                items += InnerItem(item, parameters)
                if (current != SPACE && current != CLOSE_PARENTHESIS) return null
            }
            return null
        }

        private fun parameters(): List<Parameter>? {
            val parameters = mutableListOf<Parameter>()
            while (current == SEMICOLON) {
                position += 1
                skip { it == SPACE }
                val key = key() ?: return null
                var value: Item = Item.Flag(true)
                if (current == EQUALS) {
                    position += 1
                    value = bareItem() ?: return null
                }
                parameters.removeAll { it.key == key }
                parameters += Parameter(key, value)
            }
            return parameters
        }

        private fun key(): String? {
            val first = current ?: return null
            if (!isLowercaseLetter(first) && first != ASTERISK) return null
            val start = position
            skip {
                isLowercaseLetter(it) || isDigit(it) || it == UNDERSCORE || it == HYPHEN || it == PERIOD ||
                    it == ASTERISK
            }
            return bytes.decodeToString(start, position)
        }

        private fun bareItem(): Item? {
            val first = current ?: return null
            return when {
                first == HYPHEN || isDigit(first) -> {
                    number()
                }

                first == QUOTE -> {
                    string()?.let { Item.Text(it) }
                }

                isLetter(first) || first == ASTERISK -> {
                    Item.Token(token())
                }

                first == COLON -> {
                    byteSequence()?.let { Item.Bytes(it) }
                }

                first == QUESTION -> {
                    boolean()?.let { Item.Flag(it) }
                }

                first == AT -> {
                    position += 1
                    (number() as? Item.Integer)?.let { Item.Date(it.value) }
                }

                first == PERCENT -> {
                    displayString()?.let { Item.DisplayText(it) }
                }

                else -> {
                    null
                }
            }
        }

        private fun number(): Item? {
            var negative = false
            if (current == HYPHEN) {
                negative = true
                position += 1
            }
            val first = current ?: return null
            if (!isDigit(first)) return null
            val integerDigits = StringBuilder()
            val fractionDigits = StringBuilder()
            var isDecimal = false
            while (true) {
                val byte = current ?: break
                if (isDigit(byte)) {
                    (if (isDecimal) fractionDigits else integerDigits).append(byte.toChar())
                    position += 1
                } else if (byte == PERIOD && !isDecimal) {
                    if (integerDigits.length > 12) return null
                    isDecimal = true
                    position += 1
                } else {
                    break
                }
                if (!isDecimal && integerDigits.length > 15) return null
                if (isDecimal && fractionDigits.length > 3) return null
            }
            if (isDecimal) {
                if (fractionDigits.isEmpty()) return null
                val value = "$integerDigits.$fractionDigits".toDoubleOrNull() ?: return null
                return Item.Decimal(if (negative) -value else value)
            }
            val value = integerDigits.toString().toLongOrNull() ?: return null
            return Item.Integer(if (negative) -value else value)
        }

        private fun string(): String? {
            position += 1
            val characters = mutableListOf<Byte>()
            while (true) {
                val byte = current ?: return null
                position += 1
                when {
                    byte == BACKSLASH -> {
                        val escaped = current ?: return null
                        if (escaped != QUOTE && escaped != BACKSLASH) return null
                        characters += escaped.toByte()
                        position += 1
                    }

                    byte == QUOTE -> {
                        return characters.toByteArray().decodeToString()
                    }

                    byte < 0x20 || byte > 0x7E -> {
                        return null
                    }

                    else -> {
                        characters += byte.toByte()
                    }
                }
            }
        }

        private fun token(): String {
            val start = position
            position += 1
            skip { isTokenCharacter(it) || it == COLON || it == SLASH }
            return bytes.decodeToString(start, position)
        }

        private fun byteSequence(): List<Byte>? {
            position += 1
            val start = position
            skip { isLetter(it) || isDigit(it) || it == PLUS || it == SLASH || it == EQUALS }
            if (current != COLON) return null
            val encoded = bytes.copyOfRange(start, position)
            position += 1
            return decodeBase64(encoded)
        }

        private fun boolean(): Boolean? {
            position += 1
            return when (current) {
                '1'.code -> {
                    position += 1
                    true
                }

                '0'.code -> {
                    position += 1
                    false
                }

                else -> {
                    null
                }
            }
        }

        private fun displayString(): String? {
            position += 1
            if (current != QUOTE) return null
            position += 1
            val decoded = mutableListOf<Byte>()
            while (true) {
                val byte = current ?: return null
                position += 1
                when {
                    byte == PERCENT -> {
                        if (position + 1 >= bytes.size) return null
                        val high = lowercaseHexValue(bytes[position].toInt() and 0xFF) ?: return null
                        val low = lowercaseHexValue(bytes[position + 1].toInt() and 0xFF) ?: return null
                        decoded += ((high shl 4) or low).toByte()
                        position += 2
                    }

                    byte == QUOTE -> {
                        return strictUtf8(decoded.toByteArray())
                    }

                    byte < 0x20 || byte > 0x7E -> {
                        return null
                    }

                    else -> {
                        decoded += byte.toByte()
                    }
                }
            }
        }

        private fun skip(matches: (Int) -> Boolean) {
            while (true) {
                val byte = current ?: return
                if (!matches(byte)) return
                position += 1
            }
        }
    }

    private const val SPACE = ' '.code
    private const val TAB = '\t'.code
    private const val COMMA = ','.code
    private const val SEMICOLON = ';'.code
    private const val EQUALS = '='.code
    private const val QUOTE = '"'.code
    private const val BACKSLASH = '\\'.code
    private const val COLON = ':'.code
    private const val QUESTION = '?'.code
    private const val AT = '@'.code
    private const val PERCENT = '%'.code
    private const val HYPHEN = '-'.code
    private const val PERIOD = '.'.code
    private const val ASTERISK = '*'.code
    private const val UNDERSCORE = '_'.code
    private const val SLASH = '/'.code
    private const val PLUS = '+'.code
    private const val OPEN_PARENTHESIS = '('.code
    private const val CLOSE_PARENTHESIS = ')'.code

    private fun isDigit(byte: Int): Boolean = byte in '0'.code..'9'.code

    private fun isLowercaseLetter(byte: Int): Boolean = byte in 'a'.code..'z'.code

    private fun isLetter(byte: Int): Boolean = isLowercaseLetter(byte) || byte in 'A'.code..'Z'.code

    /** RFC 9110's `tchar`. */
    private fun isTokenCharacter(byte: Int): Boolean =
        isLetter(byte) || isDigit(byte) || byte.toChar() in "!#$%&'*+-.^_`|~"

    private fun lowercaseHexValue(byte: Int): Int? =
        when (byte) {
            in '0'.code..'9'.code -> byte - '0'.code
            in 'a'.code..'f'.code -> byte - 'a'.code + 10
            else -> null
        }

    /** [bytes] as UTF-8, or null where they are not, since a display string that is no UTF-8 does not parse. */
    private fun strictUtf8(bytes: ByteArray): String? =
        try {
            bytes.decodeToString(throwOnInvalidSequence = true)
        } catch (malformed: CharacterCodingException) {
            null
        }

    /** RFC 4648's alphabet with its padding, as a byte sequence carries it. */
    private fun decodeBase64(encoded: ByteArray): List<Byte>? {
        var value = 0
        var bits = 0
        val decoded = mutableListOf<Byte>()
        for (unsigned in encoded) {
            val byte = unsigned.toInt() and 0xFF
            if (byte == EQUALS) continue
            val sextet =
                when (byte) {
                    in 'A'.code..'Z'.code -> byte - 'A'.code
                    in 'a'.code..'z'.code -> byte - 'a'.code + 26
                    in '0'.code..'9'.code -> byte - '0'.code + 52
                    PLUS -> 62
                    SLASH -> 63
                    else -> return null
                }
            value = ((value shl 6) or sextet) and 0xFFFFFF
            bits += 6
            if (bits >= 8) {
                bits -= 8
                decoded += (value shr bits).toByte()
            }
        }
        return decoded
    }
}
