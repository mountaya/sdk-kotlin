package com.mountaya.geo

/** The schemes an address may open with, each in lower case and followed by two slashes. */
private val schemes = listOf("http://", "https://")

/** The highest port a host may name. */
private const val MAX_PORT = 65_535

/** The hexadecimal radix an escape is written in. */
private const val HEXADECIMAL = 16

/** The radixes a number of an IPv4 address is written in. */
private const val OCTAL = 8
private const val DECIMAL = 10

/** The highest value one part of a dotted IPv4 address holds. */
private const val MAX_IPV4_PART = 255

/** The parts of an IPv4 address. */
private const val IPV4_PARTS = 4

/** The 16-bit pieces of an IPv6 address; its trailing IPv4 form stands for two. */
private const val IPV6_PIECES = 8

/** The most hexadecimal digits one IPv6 piece holds. */
private const val IPV6_PIECE_DIGITS = 4

/** The characters a host may not hold once its escapes are read. */
private const val FORBIDDEN_HOST_CHARACTERS = " #%/:<>?@[\\]^|"

/**
 * isWebAddress is whether [address], read as written, is a web address the API accepts on a write,
 * so a form shows its field error by it: an absolute `http` or `https` address, the scheme in lower
 * case, two slashes, then a host, and nothing the API's parser refuses.
 *
 * Refused are: a control character anywhere, U+0000 to U+001F and U+007F; white space or a backslash
 * before the host ends; no host; a percent sign that two hexadecimal digits do not follow, outside
 * the query, where the API reads the query as written; and an escape in the host that spells an
 * ASCII character. The host is then read as a URL parser reads it: an IPv6 literal in brackets
 * holds a well-formed address, a name holds no character a host may not, its escapes spell UTF-8,
 * and a name ending in a number is a well-formed IPv4 address; a port is a number up to 65,535.
 *
 * White space at either end is no part of an address, so `  https://refuge.example ` is refused
 * here; [linkedWebAddress] reads a stored address past it.
 */
public fun isWebAddress(address: String): Boolean {
    val scheme = schemes.firstOrNull { address.startsWith(it) } ?: return false
    if (address.any { it < ' ' || it == '\u007F' }) return false
    val afterScheme = address.substring(scheme.length)
    // The host the API reads runs up to the first `/`, `?` or `#`, and holds no white space or backslash.
    val hostEnd =
        afterScheme.indexOfFirst { it == '/' || it == '?' || it == '#' }.takeIf { it >= 0 } ?: afterScheme.length
    val authority = afterScheme.substring(0, hostEnd)
    if (authority.isEmpty() || authority.any { isWhiteSpace(it) || it == '\\' }) return false
    if (holdsMalformedEscape(address)) return false
    val hostAndPort = authority.substringAfterLast('@')
    if (holdsAsciiEscape(hostAndPort)) return false
    return isHostAndPort(hostAndPort)
}

/** Whether a percent sign outside the query opens no escape, two hexadecimal digits following it. */
private fun holdsMalformedEscape(address: String): Boolean {
    val beforeFragment = address.substringBefore('#')
    val fragment = if ('#' in address) address.substringAfter('#') else ""
    return isMalformedEscape(beforeFragment.substringBefore('?')) || isMalformedEscape(fragment)
}

/** Whether [text] holds a percent sign that two hexadecimal digits do not follow. */
private fun isMalformedEscape(text: String): Boolean =
    text.indices.any { index ->
        text[index] == '%' && (index + 2 >= text.length || !isHexDigit(text[index + 1]) || !isHexDigit(text[index + 2]))
    }

/** Whether [host] holds an escape that spells an ASCII character, `%00` to `%7F`. */
private fun holdsAsciiEscape(host: String): Boolean =
    host.indices.any { index ->
        host[index] == '%' && index + 2 < host.length && host[index + 1] in '0'..'7' && isHexDigit(host[index + 2])
    }

/** Whether [hostAndPort] is a host a URL parser reads, then an optional port. */
private fun isHostAndPort(hostAndPort: String): Boolean {
    val host: String
    val port: String?
    if (hostAndPort.startsWith('[')) {
        val close = hostAndPort.indexOf(']')
        if (close < 0 || !isIpv6(hostAndPort.substring(1, close))) return false
        val rest = hostAndPort.substring(close + 1)
        if (rest.isNotEmpty() && !rest.startsWith(':')) return false
        host = hostAndPort.substring(0, close + 1)
        port = rest.takeIf { it.isNotEmpty() }?.substring(1)
    } else {
        host = hostAndPort.substringBefore(':')
        port = if (':' in hostAndPort) hostAndPort.substringAfter(':') else null
        if (!isDomain(host)) return false
    }
    return host.isNotEmpty() && (port == null || isPort(port))
}

/** Whether [port] is empty, the scheme's own, or a decimal number up to [MAX_PORT]. */
private fun isPort(port: String): Boolean {
    if (!port.all { it in '0'..'9' }) return false
    val significant = port.trimStart('0')
    return significant.isEmpty() ||
        (significant.length <= MAX_PORT.toString().length && significant.toInt() <= MAX_PORT)
}

/**
 * Whether [host] is a name a URL parser reads: not empty once its escapes are read, its escapes
 * spelling UTF-8, no character a host may not hold, and, where it ends in a number, an IPv4 address.
 */
private fun isDomain(host: String): Boolean {
    val decoded = percentDecoded(host) ?: return false
    if (decoded.isEmpty() || decoded.any { it in FORBIDDEN_HOST_CHARACTERS }) return false
    return !endsInNumber(decoded) || isIpv4(decoded)
}

/** [text] with its escapes read as UTF-8, or null where they spell none. */
private fun percentDecoded(text: String): String? {
    if ('%' !in text) return text
    // Read over the UTF-8 bytes: a percent sign and its digits are ASCII, so an escape is three bytes.
    val source = text.encodeToByteArray()
    val bytes = ArrayList<Byte>(source.size)
    var index = 0
    while (index < source.size) {
        if (source[index] == '%'.code.toByte() && index + 2 < source.size) {
            bytes += (source.decodeToString(index + 1, index + 3).toIntOrNull(HEXADECIMAL) ?: return null).toByte()
            index += 3
        } else {
            bytes += source[index]
            index += 1
        }
    }
    return try {
        bytes.toByteArray().decodeToString(throwOnInvalidSequence = true)
    } catch (_: CharacterCodingException) {
        null
    }
}

/**
 * Whether [host] ends in a number, as a URL parser reads it: its last label, past one trailing dot,
 * is all decimal digits or a hexadecimal number, `0x` then hexadecimal digits.
 */
private fun endsInNumber(host: String): Boolean {
    val labels = host.split('.').let { if (it.size > 1 && it.last().isEmpty()) it.dropLast(1) else it }
    val last = labels.last()
    return last.isNotEmpty() && (last.all { it in '0'..'9' } || ipv4Number(last) != null)
}

/** Whether [host] is an IPv4 address as a URL parser reads one: up to four numbers, the last filling the rest. */
private fun isIpv4(host: String): Boolean {
    val parts = host.split('.').let { if (it.size > 1 && it.last().isEmpty()) it.dropLast(1) else it }
    if (parts.size > IPV4_PARTS) return false
    val numbers = parts.map { ipv4Number(it) ?: return false }
    if (numbers.dropLast(1).any { it > MAX_IPV4_PART }) return false
    var limit = 1L
    repeat(IPV4_PARTS + 1 - numbers.size) { limit *= MAX_IPV4_PART + 1 }
    return numbers.last() < limit
}

/**
 * One part of an IPv4 address: decimal, `0x` hexadecimal, or octal after a leading zero; null where
 * it is none. A number too long for 64 bits is past every limit an address holds, and still a number.
 */
private fun ipv4Number(part: String): Long? {
    if (part.isEmpty()) return null
    val (digits, radix) =
        when {
            part.startsWith("0x") || part.startsWith("0X") -> part.substring(2) to HEXADECIMAL
            part.length > 1 && part.startsWith('0') -> part.substring(1) to OCTAL
            else -> part to DECIMAL
        }
    if (!digits.all { it.digitToIntOrNull(radix) != null }) return null
    return if (digits.isEmpty()) 0L else digits.toLongOrNull(radix) ?: Long.MAX_VALUE
}

/** Whether [text] is an IPv6 address: eight hexadecimal pieces, one run of them compressed, the last two as IPv4. */
private fun isIpv6(text: String): Boolean {
    val compressed = text.indexOf("::")
    if (compressed >= 0 && text.indexOf("::", compressed + 1) >= 0) return false
    val head = if (compressed >= 0) text.substring(0, compressed) else text
    val tail = if (compressed >= 0) text.substring(compressed + 2) else ""
    val pieces =
        (if (head.isEmpty()) emptyList() else head.split(':')) + (if (tail.isEmpty()) emptyList() else tail.split(':'))
    var count = 0
    for ((index, piece) in pieces.withIndex()) {
        if (index == pieces.lastIndex && '.' in piece) {
            if (!isDottedIpv4(piece)) return false
            count += 2
        } else {
            if (piece.isEmpty() || piece.length > IPV6_PIECE_DIGITS || !piece.all(::isHexDigit)) return false
            count += 1
        }
    }
    return if (compressed >= 0) count < IPV6_PIECES else count == IPV6_PIECES
}

/** Whether [text] is four decimal numbers up to 255, dotted, none with a leading zero. */
private fun isDottedIpv4(text: String): Boolean {
    val parts = text.split('.')
    return parts.size == IPV4_PARTS &&
        parts.all { part ->
            part.length in 1..3 && part.all { it in '0'..'9' } && (part.length == 1 || part[0] != '0') &&
                part.toInt() <= MAX_IPV4_PART
        }
}

/** Whether [char] is a hexadecimal digit, in either case. */
private fun isHexDigit(char: Char): Boolean = char in '0'..'9' || char in 'a'..'f' || char in 'A'..'F'
