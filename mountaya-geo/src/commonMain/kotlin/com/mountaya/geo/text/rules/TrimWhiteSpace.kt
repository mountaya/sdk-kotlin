package com.mountaya.geo

/**
 * trimWhiteSpace returns [text] past the white space at its ends: the tab, the line feed, the line
 * tabulation, the form feed, the carriage return, every space separator (U+0020, U+00A0, U+1680, U+2000
 * to U+200A, U+202F, U+205F and U+3000), the line and paragraph separators U+2028 and U+2029, and the
 * byte order mark U+FEFF.
 *
 * The set is written out rather than read off a platform's notion of white space, which differs
 * between platforms and between the SDKs, so a value typed with a stray character at either end
 * reads the same everywhere. It is the set ECMAScript's own trim removes, and the one a map reads a
 * blank credit by (`map.style.attribution`).
 */
public fun trimWhiteSpace(text: String): String = text.trim(::isWhiteSpace)

/** Returns whether [char] is white space as [trimWhiteSpace] reads it. */
internal fun isWhiteSpace(char: Char): Boolean =
    when (char) {
        '\t', '\n', '\u000B', '\u000C', '\r', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
        '　', '﻿',
        -> true

        else -> char in ' '..' '
    }
