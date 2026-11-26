package com.mountaya.geo

/** glyphShapeTags are the SVG shapes a glyph may hold, the only elements a parsed glyph answers. */
public val glyphShapeTags: Set<String> = setOf("circle", "ellipse", "line", "path", "polygon", "polyline", "rect")

/**
 * glyphAttributes are every attribute a glyph's shape keeps: its geometry, its fill rules, a fill and a
 * stroke that name no colour of their own, and how a stroked line ends and turns, and nothing else, so
 * no style, no handler, no reference and no namespaced attribute survives. A stroke's width is not among
 * them: the width is the set's, so no glyph draws heavier than the set it belongs to. A renderer that
 * maps them onto its own names covers every one.
 */
public val glyphAttributes: Set<String> =
    setOf(
        "clip-rule",
        "cx",
        "cy",
        "d",
        "fill",
        "fill-rule",
        "height",
        "points",
        "r",
        "rx",
        "ry",
        "stroke",
        "stroke-linecap",
        "stroke-linejoin",
        "stroke-miterlimit",
        "transform",
        "width",
        "x",
        "x1",
        "x2",
        "y",
        "y1",
        "y2",
    )

/**
 * The values an attribute read as a keyword may state, compared with the ASCII capitals folded, by the
 * attribute: a fill and a stroke name no colour of their own, the current colour or none, and a line's
 * cap and join are the keywords SVG spells them with.
 */
private val glyphKeywords: Map<String, Set<String>> =
    mapOf(
        "fill" to setOf("currentcolor", "none"),
        "stroke" to setOf("currentcolor", "none"),
        "stroke-linecap" to setOf("butt", "round", "square"),
        "stroke-linejoin" to setOf("arcs", "bevel", "miter", "miter-clip", "round"),
    )

/** The attribute whose value is kept only as a plain number of at least 1. */
private const val MITER_LIMIT = "stroke-miterlimit"

/**
 * keptGlyphValue answers the value an attribute of [glyphAttributes] named [name] is kept with, or null
 * for one a glyph may not carry.
 *
 * A fill, a stroke, a cap and a join are kept, past the white space at their ends, where the value is
 * one of the keywords the attribute takes in any letter case, as written. A miter limit is kept, past the
 * same white space, where it is a plain number of at least 1: ASCII digits with at most one decimal
 * point, a digit on one side of it at least, and no sign, unit or exponent, so `4.` and `.5` are numbers
 * and `+10`, `1e1` and `10px` are not, and `.5` is below 1. Every other attribute is kept as written.
 * `glyphPaint` reads a shape's own paint through it, so a value the parse would drop is no statement,
 * whoever built the shape.
 */
internal fun keptGlyphValue(
    name: String,
    value: String,
): String? {
    val keywords = glyphKeywords[name]
    if (keywords != null) return trimWhiteSpace(value).takeIf { asciiLowercase(it) in keywords }
    if (name != MITER_LIMIT) return value
    val limit = trimWhiteSpace(value)
    return limit.takeIf { isPlainNumber(it) && plainNumberValue(it) >= 1.0 }
}

/**
 * The value of [plain], a plain number as [keptGlyphValue] keeps one, a point with no digit on one side
 * read with a 0 there, so every target reads it alike.
 */
internal fun plainNumberValue(plain: String): Double =
    ((if (plain.startsWith('.')) "0" else "") + plain + (if (plain.endsWith('.')) "0" else "")).toDouble()

/** Whether [text] is a plain number: ASCII digits with at most one decimal point, and a digit on one side of it at least. */
private fun isPlainNumber(text: String): Boolean {
    var digits = 0
    var points = 0
    for (char in text) {
        when {
            char in '0'..'9' -> digits += 1
            char == '.' && points == 0 -> points += 1
            else -> return false
        }
    }
    return digits > 0
}

/** A group, read for its transform and never answered. */
private const val GROUP = "g"

/** The document element, read through. */
private const val DOCUMENT = "svg"

/** What stands where a comment or a CDATA section was, so the names on either side of one never join. */
private const val STAND_IN = ' '

/**
 * parseGlyph reads [markup], a glyph's inner SVG markup as an icon set answers it, into the shapes a
 * renderer builds, in document order, with nothing of it executed or fetched.
 *
 * A shape (one of [glyphShapeTags]) is read at the top level, inside a group and inside an `svg`
 * wrapper. It keeps the attributes of [glyphAttributes], quoted either way, with white space around the
 * `=` or none, and keyed lowercase: a `fill` or a `stroke` only where it is `currentColor` or `none`, a
 * `stroke-linecap` or a `stroke-linejoin` only as a keyword SVG names it with, each in any letter case,
 * and a `stroke-miterlimit` only as a plain number of at least 1, each kept as written past the white
 * space at its ends ([keptGlyphValue]). Every group around it hands it its `transform`, outermost first
 * and before its own, joined by a space. Everything else is dropped: an attribute off the list, a
 * `stroke-width` among them, a name holding a namespace or opening with a digit, a hyphen or a dot, and
 * an element that is neither a shape nor a group with all it holds, up to the tag that closes it, since
 * SVG draws nothing inside a `defs`, a `clipPath` or a `mask`. A tag whose name carries a namespace
 * prefix reads as the prefix alone, so `<svg:path>` is an `svg` wrapper and no shape. A shape is no
 * container, so what it holds is dropped too. An element the markup never closes holds nothing and is
 * dropped alone, so a group it never closes hands its transform to no shape. A name written twice keeps
 * its first place and the later of its values a glyph may carry.
 *
 * A comment and a CDATA section, to their end or to the end of the markup where they never end, read
 * as one space: they hold no shape, and one written inside a name splits it. A tag is `<`, an
 * optional `/`, then its name at once; one the markup never ends, past its quoted values, is no tag,
 * and the scan goes on from the next `<`. A closing tag closes the nearest element of its own name
 * still open, so a `</g>` ends the group it is paired with wherever it is written, inside an element
 * that is dropped included, and a group opened inside a dropped element closes no other.
 *
 * It never refuses: markup that holds no shape answers an empty list. It is a scanner of its own, never
 * a platform's XML parser, which refuses markup that is not well formed, expands entities and may
 * fetch, and it reads the markup in one pass, so the time a glyph takes grows with its length and no
 * faster.
 */
public fun parseGlyph(markup: String): List<GlyphShape> {
    val scan = scanTags(withoutComments(markup))
    val tags = scan.tags
    val shapes = mutableListOf<GlyphShape>()
    val groups = ArrayDeque<OpenGroup>()

    // The place the walk resumes from after the element opened at [index], dropped with what it holds:
    // the tag that closes it, or the tag itself where the markup never closes it. A group closed inside
    // the dropped element ends there.
    fun skip(index: Int): Int {
        val end = scan.closings[index] ?: index
        while (groups.lastOrNull()?.let { it.closesAt in (index + 1)..end } == true) groups.removeLast()
        return end
    }

    var index = 0
    while (index < tags.size) {
        val tag = tags[index]
        when {
            tag.name == GROUP -> {
                val closesAt = scan.closings[index]
                if (tag.isClosing && groups.lastOrNull()?.closesAt == index) {
                    groups.removeLast()
                } else if (tag.isOpen && closesAt != null) {
                    groups.addLast(OpenGroup(closesAt, attributesOf(tag.attributes)["transform"].orEmpty()))
                }
            }

            tag.isClosing || tag.name == DOCUMENT -> {
                // A closing tag that ends no group the walk holds, and the document element: read through.
            }

            tag.name !in glyphShapeTags -> {
                index = skip(index)
            }

            else -> {
                val attributes = attributesOf(tag.attributes)
                val transform =
                    (groups.map { it.transform } + attributes["transform"].orEmpty()).filter {
                        it
                            .isNotEmpty()
                    }
                if (transform.isNotEmpty()) attributes["transform"] = transform.joinToString(" ")
                shapes += GlyphShape(tag.name, attributes)
                index = skip(index)
            }
        }
        index += 1
    }
    return shapes
}

/** A group the walk stands inside: the place of the tag that closes it, and its own transform. */
private class OpenGroup(
    val closesAt: Int,
    val transform: String,
)

/** One tag of the markup: its name lowercase, whether it closes or opens an element, and its attribute list unread. */
private class Tag(
    val name: String,
    val isClosing: Boolean,
    val isOpen: Boolean,
    val attributes: String,
)

/** The markup's tags in order, and the place of the tag that closes each element closed, keyed by the place of its opening tag. */
private class TagScan(
    val tags: List<Tag>,
    val closings: Map<Int, Int>,
)

/** [markup] with each comment and CDATA section, to its end or to the markup's, written as one space. */
private fun withoutComments(markup: String): String {
    if ("<!" !in markup) return markup
    val out = StringBuilder(markup.length)
    var at = 0
    while (at < markup.length) {
        val end =
            when {
                markup.startsWith("<!--", at) -> {
                    markup.indexOf("-->", at + 4).let {
                        if (it <
                            0
                        ) {
                            markup.length
                        } else {
                            it + 3
                        }
                    }
                }

                markup.startsWith("<![CDATA[", at) -> {
                    markup.indexOf("]]>", at + 9).let {
                        if (it <
                            0
                        ) {
                            markup.length
                        } else {
                            it + 3
                        }
                    }
                }

                else -> {
                    -1
                }
            }
        if (end < 0) {
            out.append(markup[at])
            at += 1
        } else {
            out.append(STAND_IN)
            at = end
        }
    }
    return out.toString()
}

/** Whether [char] may open a tag's name: an ASCII letter. */
private fun opensName(char: Char): Boolean = char in 'a'..'z' || char in 'A'..'Z'

/** Whether [char] may continue a tag's name: an ASCII letter, a digit or a hyphen. */
private fun continuesName(char: Char): Boolean = opensName(char) || char in '0'..'9' || char == '-'

/** Whether [char] may be in an attribute's name: an ASCII letter, a digit, `_`, `.`, `:` or `-`. */
private fun inAttributeName(char: Char): Boolean = continuesName(char) || char == '_' || char == '.' || char == ':'

/** The markup's tags, each closing tag paired with the nearest element of its name still open. */
private fun scanTags(markup: String): TagScan {
    val tags = mutableListOf<Tag>()
    val closings = HashMap<Int, Int>()
    val opened = HashMap<String, ArrayDeque<Int>>()
    // A mark at each place an attribute list was read from without finding its end: what follows a
    // place reads the same whichever tag reached it, so a run of tags that never end is one pass.
    val read = BooleanArray(markup.length)
    var at = markup.indexOf('<')
    while (at >= 0) {
        val closing = at + 1 < markup.length && markup[at + 1] == '/'
        val nameAt = at + 1 + (if (closing) 1 else 0)
        var nameEnd = nameAt
        if (nameEnd < markup.length && opensName(markup[nameEnd])) {
            nameEnd += 1
            while (nameEnd < markup.length && continuesName(markup[nameEnd])) nameEnd += 1
        }
        val end = if (nameEnd == nameAt) -1 else tagEnd(markup, nameEnd, read)
        if (end < 0) {
            at = markup.indexOf('<', at + 1)
            continue
        }
        val name = asciiLowercase(markup.substring(nameAt, nameEnd))
        val selfClosing = end > nameEnd && markup[end - 1] == '/'
        val open = !closing && !selfClosing
        if (open) {
            opened.getOrPut(name) { ArrayDeque() }.addLast(tags.size)
        } else if (closing) {
            opened[name]?.removeLastOrNull()?.let { closings[it] = tags.size }
        }
        tags += Tag(name, closing, open, markup.substring(nameEnd, if (selfClosing) end - 1 else end))
        at = markup.indexOf('<', end + 1)
    }
    return TagScan(tags, closings)
}

/**
 * The place of the `>` that ends the tag whose attribute list starts at [from], read quote by quote,
 * or -1 for a tag the markup never ends: a quote never closed ends none.
 */
private fun tagEnd(
    markup: String,
    from: Int,
    read: BooleanArray,
): Int {
    var index = from
    while (index < markup.length && !read[index]) {
        read[index] = true
        val char = markup[index]
        if (char == '>') return index
        if (char != '"' && char != '\'') {
            index += 1
            continue
        }
        val closing = markup.indexOf(char, index + 1)
        if (closing < 0) return -1
        index = closing + 1
    }
    return -1
}

/**
 * The attributes of one tag's [list] a glyph keeps, keyed lowercase. A name is a whole run of the
 * characters names are written in, so `sodipodi:rx` is never the `rx` it ends in; one with no
 * quoted value is skipped whole; a name written twice keeps its first place and the later of its
 * values a glyph may carry.
 */
private fun attributesOf(list: String): MutableMap<String, String> {
    val attributes = LinkedHashMap<String, String>()
    var index = 0
    while (index < list.length) {
        if (!inAttributeName(list[index])) {
            index += 1
            continue
        }
        val nameEnd = (index until list.length).firstOrNull { !inAttributeName(list[it]) } ?: list.length
        val name = list.substring(index, nameEnd)
        index = nameEnd
        var at = index
        while (at < list.length && isWhiteSpace(list[at])) at += 1
        if (at >= list.length || list[at] != '=') continue
        at += 1
        while (at < list.length && isWhiteSpace(list[at])) at += 1
        if (at >= list.length || (list[at] != '"' && list[at] != '\'')) continue
        val closing = list.indexOf(list[at], at + 1)
        if (closing < 0) continue
        val value = list.substring(at + 1, closing)
        index = closing + 1
        val key = asciiLowercase(name)
        if (key !in glyphAttributes) continue
        attributes[key] = keptGlyphValue(key, value) ?: continue
    }
    return attributes
}
