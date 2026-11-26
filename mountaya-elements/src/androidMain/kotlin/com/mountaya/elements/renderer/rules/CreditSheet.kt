package com.mountaya.elements

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import com.mountaya.geo.trimWhiteSpace

/** The named character references a credit writes, each with the text it reads as. */
private val namedEntities: Map<String, String> =
    mapOf(
        "amp" to "&",
        "lt" to "<",
        "gt" to ">",
        "quot" to "\"",
        "apos" to "'",
        "copy" to "©",
        "reg" to "®",
        "nbsp" to " ",
    )

/** The radix a hexadecimal character reference is written in. */
private const val HEXADECIMAL = 16

/**
 * creditSheet is the sheet a map's credit button opens: one row per credit, in the order the engine credits
 * them, Mountaya first (`map.style.attribution`), each titled with the text the credit's markup shows and
 * opening its link through [open], and a button that closes the sheet. A credit with no link is shown and
 * opens nothing.
 */
internal fun creditSheet(
    context: Context,
    credits: List<String>,
    open: (Uri) -> Unit,
): AlertDialog.Builder {
    val links = credits.map(::creditLink)
    return AlertDialog
        .Builder(context)
        .setItems(links.map { it.first }.toTypedArray()) { _, which ->
            links.getOrNull(which)?.second?.let { open(Uri.parse(it)) }
        }.setPositiveButton(android.R.string.ok, null)
}

/**
 * creditLink reads a credit's [markup], as a style's sources write one, into the text it shows, its tags
 * dropped, its entities read and the white space at its ends trimmed, and the address its first `href`
 * names, null where it names none: `<a href="https://mountaya.com/">&copy; Mountaya</a>` reads `© Mountaya`
 * and opens `https://mountaya.com/`, as MapLibre's own credit button shows a source's.
 */
internal fun creditLink(markup: String): Pair<String, String?> {
    val text = StringBuilder()
    var inTag = false
    for (char in markup) {
        when (char) {
            '<' -> inTag = true
            '>' -> inTag = false
            else -> if (!inTag) text.append(char)
        }
    }
    return trimWhiteSpace(readEntities(text.toString())) to firstHref(markup)?.let(::readEntities)
}

/** The value of the first `href` attribute [markup] writes, between the quotes that open it, or null. */
private fun firstHref(markup: String): String? {
    val attribute = markup.indexOf("href=")
    if (attribute < 0) return null
    val rest = markup.substring(attribute + "href=".length)
    val quote = rest.firstOrNull()?.takeIf { it == '"' || it == '\'' } ?: return null
    val end = rest.indexOf(quote, startIndex = 1)
    if (end < 0) return null
    return rest.substring(1, end)
}

/** [text] with its character references read: the named ones a credit writes and every numeric one, a reference it cannot read kept as written. */
private fun readEntities(text: String): String {
    val read = StringBuilder()
    var at = 0
    while (true) {
        val start = text.indexOf('&', at)
        if (start < 0) break
        read.append(text, at, start)
        val end = text.indexOf(';', start + 1)
        if (end < 0) {
            at = start
            break
        }
        val name = text.substring(start + 1, end)
        val replacement =
            when {
                name.startsWith("#x") || name.startsWith("#X") -> codePointText(name.drop(2).toIntOrNull(HEXADECIMAL))
                name.startsWith("#") -> codePointText(name.drop(1).toIntOrNull())
                else -> namedEntities[name]
            }
        if (replacement != null) {
            read.append(replacement)
            at = end + 1
        } else {
            read.append('&')
            at = start + 1
        }
    }
    read.append(text, at, text.length)
    return read.toString()
}

/** The text of the Unicode scalar [codePoint], or null where it is none. */
private fun codePointText(codePoint: Int?): String? {
    if (codePoint == null || codePoint < 0 || codePoint > Character.MAX_CODE_POINT) return null
    if (codePoint in Character.MIN_SURROGATE.code..Character.MAX_SURROGATE.code) return null
    return String(Character.toChars(codePoint))
}
