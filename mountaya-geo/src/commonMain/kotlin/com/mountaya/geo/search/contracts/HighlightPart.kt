package com.mountaya.geo

/**
 * HighlightPart is one run of a title as a search result draws it: [text], as the title writes it,
 * and whether the query matched it. The parts of a title, joined in order, are the title.
 */
public class HighlightPart(
    /** The run, as the title spells it. */
    public val text: String,
    /** Whether the run is part of what the query matched. */
    public val match: Boolean,
) {
    override fun equals(other: Any?): Boolean = other is HighlightPart && other.text == text && other.match == match

    override fun hashCode(): Int = text.hashCode() * 31 + match.hashCode()

    override fun toString(): String = "HighlightPart(\"$text\", match=$match)"
}
