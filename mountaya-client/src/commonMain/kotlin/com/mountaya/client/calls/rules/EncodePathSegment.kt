package com.mountaya.client

/** The characters RFC 3986 leaves unescaped anywhere: letters, digits, `-`, `.`, `_` and `~`. */
private fun isUnreserved(byte: Int): Boolean =
    byte in 'A'.code..'Z'.code ||
        byte in 'a'.code..'z'.code ||
        byte in '0'.code..'9'.code ||
        byte == '-'.code ||
        byte == '.'.code ||
        byte == '_'.code ||
        byte == '~'.code

private const val HEX = "0123456789ABCDEF"

/**
 * encodePathSegment writes [value] as one path segment: every byte of its UTF-8 that RFC 3986 does not
 * leave unreserved is percent-encoded, in uppercase hexadecimal.
 *
 * A path parameter is a value, never a path, so a `/` in it is escaped rather than read as a
 * separator, and `.` and `..` arrive at the server as the segment they are.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, calls it from beside the core, and no
 * consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public fun encodePathSegment(value: String): String =
    buildString {
        for (unsigned in value.encodeToByteArray()) {
            val byte = unsigned.toInt() and 0xFF
            if (isUnreserved(byte)) {
                append(byte.toChar())
            } else {
                append('%').append(HEX[byte shr 4]).append(HEX[byte and 0x0F])
            }
        }
    }
