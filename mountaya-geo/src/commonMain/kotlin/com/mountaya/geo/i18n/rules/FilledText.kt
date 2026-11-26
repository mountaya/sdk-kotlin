package com.mountaya.geo

/**
 * filledText writes [template] with [value] in place of its placeholder (`geo.i18n.counted-words`): the
 * first `{name}` it holds, a name of ASCII letters and digits led by a letter, read code point by code
 * point.
 *
 * The value is the one an element hands the word, which the locale services already wrote, and it is written
 * exactly as handed: `$&` and `\1` are text, never a pattern, so a figure that holds them stays that figure. A
 * template that holds no placeholder is written as it is. The elements' words table writes a template with one
 * placeholder, `"{elapsed} stop"`.
 */
public fun filledText(
    template: String,
    value: String,
): String {
    val placeholder = firstPlaceholder(template) ?: return template
    return template.replaceRange(placeholder, value)
}

/**
 * filledText writes [template] with each of [values] in every placeholder of its name (`geo.i18n.counted-words`), as
 * an element hands a template several values, the readout's band range its `{min}` and its `{max}` and a zone's
 * window its `{start}` and its `{end}`: each takes its value wherever the words put it, whatever the order, and a
 * placeholder no value names is written as it stands.
 *
 * Each value is the one the element hands, which the locale services already wrote, written exactly as handed, so a
 * value that holds a placeholder of another name never has that one filled.
 */
public fun filledText(
    template: String,
    values: Map<String, String>,
): String =
    buildString {
        var index = 0
        while (index < template.length) {
            val placeholder = firstPlaceholder(template.substring(index))
            if (placeholder == null) {
                append(template, index, template.length)
                break
            }
            val start = index + placeholder.first
            val end = index + placeholder.last
            append(template, index, start)
            val name = template.substring(start + 1, end)
            append(values[name] ?: template.substring(start, end + 1))
            index = end + 1
        }
    }

/**
 * firstPlaceholder is where the first placeholder [text] holds stands, as a range of UTF-16 indices: `{`, a
 * name of ASCII letters and digits led by a letter, and `}`, the one named [name] where a name is given,
 * compared exactly, or null where it holds none. Every character it reads past is ASCII, which no surrogate
 * and no combining mark is, so it stands where a reading code point by code point finds it, a mark combining
 * with its brace included.
 */
internal fun firstPlaceholder(
    text: String,
    name: String? = null,
): IntRange? {
    var index = 0
    while (index < text.length) {
        val opening = index
        index += 1
        if (text[opening] != '{') continue
        var cursor = opening + 1
        while (cursor < text.length && isNameCharacter(text[cursor], leading = cursor == opening + 1)) cursor += 1
        if (cursor == opening + 1 || cursor >= text.length || text[cursor] != '}') continue
        if (name != null && text.substring(opening + 1, cursor) != name) continue
        return opening..cursor
    }
    return null
}

/** Whether [character] may stand in a placeholder's name: an ASCII letter, or an ASCII digit past its first character. */
private fun isNameCharacter(
    character: Char,
    leading: Boolean,
): Boolean =
    when (character) {
        in 'a'..'z', in 'A'..'Z' -> true
        in '0'..'9' -> !leading
        else -> false
    }
