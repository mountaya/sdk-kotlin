package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.formatTimestamp
import com.mountaya.client.generated.TileSchema
import kotlin.time.Instant

/**
 * TIMESTAMP_PARAMETER is the query parameter a server reads the instant an overlay is drawn for from
 * (`geo.layers.tile-timestamp`).
 */
public const val TIMESTAMP_PARAMETER: String = "timestamp"

/** A scheme, letters, digits, `+`, `-` or `.` led by a letter, then `:`: an address without one is kept as it is. */
private val SCHEME = Regex("^[A-Za-z][A-Za-z0-9+.-]*:")

/** The characters a URI component keeps as they are, as ECMAScript's `encodeURIComponent` keeps them. */
private const val UNRESERVED = "-_.!~*'()"

/** The digits a percent escape writes, upper case. */
private const val HEX_DIGITS = "0123456789ABCDEF"

/** The radix of a percent escape's digits. */
private const val HEX_RADIX = 16

/** The bits of a byte's high hexadecimal digit, below its low one. */
private const val NIBBLE_BITS = 4

/** The mask of a byte's low hexadecimal digit. */
private const val NIBBLE_MASK = 0x0F

/** The mask of a byte read as unsigned. */
private const val BYTE_MASK = 0xFF

/**
 * timestampedSources answers the tile sources of the selected classic overlay asked again for [instant]
 * (`geo.layers.tile-timestamp`): those of [overlay], the slug a layers selection holds, looked up among the catalogue's
 * classic categories alone (`findOverlay`), where the catalogue says the server resolves its time,
 * `capabilities.server.timestamp`. None is answered with no overlay selected, for a slug no classic category carries, an
 * enrichment's among them, and for an overlay the server does not stamp, a time the client resolves on data it holds
 * included.
 *
 * Otherwise each of its sources is answered, in its order, each of its addresses carrying the instant as its `timestamp`
 * parameter ([TIMESTAMP_PARAMETER]), in UTC to the millisecond as `2026-03-14T09:26:53.900Z` writes it, a whole second
 * written `.000`, its colons percent-encoded. The address is edited as text and never written again from a parse: the
 * fragment from the first `#` and the query from the first `?` before it are set apart and the query split at each `&`;
 * the parameter takes the place of the first pair whose name, the text before its first `=` or the whole pair, decodes to
 * `timestamp`, `+` read as a space and each percent escape as the UTF-8 byte it names, or is appended after every other
 * pair; every other such pair and every empty pair is dropped, and the path, its braces, the fragment and every other
 * pair are kept byte for byte, so a signature over them still verifies. An address with no scheme is kept as it is.
 */
@OptIn(InternalMountayaApi::class)
public fun timestampedSources(
    catalogue: TileSchema.Layers,
    overlay: String?,
    instant: Instant,
): List<TimestampedSource> {
    val found = overlay?.let { findOverlay(catalogue, it, OverlayKind.Classic) } ?: return emptyList()
    if (!found.capabilities.server.timestamp) return emptyList()
    val stamp = "$TIMESTAMP_PARAMETER=${componentEncoded(formatTimestamp(instant))}"
    return found.sources.map { source -> TimestampedSource(source.id, source.tiles.map { withStamp(it, stamp) }) }
}

/** [address] with [stamp], the parameter and its value as written, in place of its `timestamp` parameter. */
private fun withStamp(
    address: String,
    stamp: String,
): String {
    if (!SCHEME.containsMatchIn(address)) return address
    val fragmentAt = address.indexOf('#')
    val fragment = if (fragmentAt < 0) "" else address.substring(fragmentAt)
    val located = if (fragmentAt < 0) address else address.substring(0, fragmentAt)
    val queryAt = located.indexOf('?')
    val path = if (queryAt < 0) located else located.substring(0, queryAt)
    val pairs = if (queryAt < 0) emptyList() else located.substring(queryAt + 1).split('&')
    val kept = mutableListOf<String>()
    var placed = false
    for (pair in pairs) {
        when {
            // An empty pair is a stray `&`, which a server reads as nothing.
            pair.isEmpty() -> {}

            !namesTimestamp(pair) -> {
                kept += pair
            }

            !placed -> {
                kept += stamp
                placed = true
            }
        }
    }
    if (!placed) kept += stamp
    return "$path?${kept.joinToString("&")}$fragment"
}

/** Whether the name of [pair], before its first `=` or the whole pair, decodes to the parameter's own name. */
private fun namesTimestamp(pair: String): Boolean {
    val name = pair.substringBefore('=')
    val bytes = mutableListOf<Byte>()
    var at = 0
    while (at < name.length) {
        val character = name[at]
        when (character) {
            '+' -> {
                bytes += ' '.code.toByte()
                at += 1
            }

            '%' -> {
                // A percent that does not lead two hexadecimal digits decodes to nothing, so the name is another.
                if (at + 2 >= name.length) return false
                val high = name[at + 1].digitToIntOrNull(HEX_RADIX) ?: return false
                val low = name[at + 2].digitToIntOrNull(HEX_RADIX) ?: return false
                bytes += ((high shl NIBBLE_BITS) or low).toByte()
                at += 3
            }

            else -> {
                val end = if (character.isHighSurrogate() && at + 1 < name.length) at + 2 else at + 1
                bytes += name.substring(at, end).encodeToByteArray().toList()
                at = end
            }
        }
    }
    val decoded =
        try {
            bytes.toByteArray().decodeToString(throwOnInvalidSequence = true)
        } catch (invalid: CharacterCodingException) {
            return false
        }
    return decoded == TIMESTAMP_PARAMETER
}

/** [text] as a URI component writes it: every byte of its UTF-8 percent-encoded but the unreserved characters. */
private fun componentEncoded(text: String): String =
    buildString {
        for (byte in text.encodeToByteArray()) {
            val code = byte.toInt() and BYTE_MASK
            val character = code.toChar()
            if (character.isAsciiLetterOrDigit() || character in UNRESERVED) {
                append(character)
            } else {
                append('%')
                append(HEX_DIGITS[code shr NIBBLE_BITS])
                append(HEX_DIGITS[code and NIBBLE_MASK])
            }
        }
    }

/** Whether this is an ASCII letter or digit. */
private fun Char.isAsciiLetterOrDigit(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'
