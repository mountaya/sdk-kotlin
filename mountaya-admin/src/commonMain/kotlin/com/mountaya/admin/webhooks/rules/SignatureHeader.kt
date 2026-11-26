package com.mountaya.admin

/**
 * SignatureHeader is what a `Mountaya-Signature` header states: when the delivery was signed, and
 * each signature, `t=<unix seconds>,v1=<signature>` with one `v1` for each secret in force.
 */
internal class SignatureHeader(
    /** The header's `t` as written, which is what a `v1` signs. */
    val stated: String,
    /** The header's `t`: the Unix time, in seconds, the delivery was signed at. */
    val timestamp: Long,
    /** Every `v1` of the header, in the order written. */
    val signatures: List<String>,
) {
    override fun equals(other: Any?): Boolean =
        other is SignatureHeader && other.stated == stated && other.timestamp == timestamp &&
            other.signatures == signatures

    override fun hashCode(): Int = (stated.hashCode() * 31 + timestamp.hashCode()) * 31 + signatures.hashCode()

    override fun toString(): String = "SignatureHeader(t=$stated, v1=$signatures)"

    companion object {
        /**
         * The largest `t` read, the largest whole number a double holds exactly and so the largest every
         * SDK reads alike: one past it is no timestamp any SDK reads.
         */
        val largestSeconds: Long = 9_007_199_254_740_991

        /** Whether [header] is one the request does not carry, or one that holds nothing but white space, which states no signature. */
        fun isMissing(header: String?): Boolean = header == null || header.all(::isWhiteSpace)

        /**
         * Reads [header], or returns null where it is not one: an element that is no `key=value` pair,
         * no `t` or more than one, a `t` that is not whole seconds in digits alone, or no `v1`.
         *
         * The elements are what lies between the commas, each key and value with the white space
         * around it trimmed, and the first `=` of an element divides its key from its value. A key
         * other than `t` and `v1` names a signature scheme this release does not compute and is passed
         * over, so a header that adds one beside `v1` still verifies.
         */
        fun parse(header: String): SignatureHeader? {
            val timestamps = mutableListOf<String>()
            val signatures = mutableListOf<String>()
            for (element in header.split(',')) {
                val separator = element.indexOf('=')
                if (separator < 0) return null
                val key = element.substring(0, separator).trim(::isWhiteSpace)
                val value = element.substring(separator + 1).trim(::isWhiteSpace)
                when (key) {
                    "t" -> timestamps += value
                    "v1" -> signatures += value
                }
            }
            val stated = timestamps.singleOrNull() ?: return null
            val timestamp = wholeSeconds(stated) ?: return null
            if (signatures.isEmpty()) return null
            return SignatureHeader(stated, timestamp, signatures)
        }

        /**
         * Reads a `t` written in the digits `0` to `9` alone, leading zeros included, or returns null
         * where it is empty, holds any other character, or names more than [largestSeconds].
         */
        fun wholeSeconds(text: String): Long? {
            if (text.isEmpty()) return null
            var value = 0L
            for (character in text) {
                if (character !in '0'..'9') return null
                value = value * 10 + (character - '0')
                if (value > largestSeconds) return null
            }
            return value
        }

        /**
         * Whether [character] is the white space trimmed from either end of a key and a value: HTTP's
         * own, the space and the tab, and every other character ECMAScript's `trim` removes, the line
         * feed, the vertical tab, the form feed, the carriage return, every space separator, the line and
         * paragraph separators and the byte order mark, so a header reads here as `sdk-typescript` and
         * `sdk-swift` read it. The contract states no set (`sdk-swift`'s `CONTRACT-GAPS.md`). Each is one
         * UTF-16 unit, so a string's characters are read one by one.
         */
        private fun isWhiteSpace(character: Char): Boolean =
            when (character.code) {
                in 0x09..0x0D, 0x20, 0xA0, 0x1680, in 0x2000..0x200A -> true
                0x2028, 0x2029, 0x202F, 0x205F, 0x3000, 0xFEFF -> true
                else -> false
            }
    }
}
