package com.mountaya.geo

/**
 * SlopeBucket is one bucket of the ramp a profile's own steepness is painted from
 * (`geo.profile.slope-stretches`): every grade from [from] up to the next bucket's, in degrees, signed, so a
 * descent bucket is negative, painted in [color] and read by [key], a key rather than a word, since a word is
 * the renderer's to write in the reader's language.
 *
 * Two are equal where they hold the same number and the same texts, as every value of the geo layer
 * compares.
 */
public class SlopeBucket(
    /** The lowest grade the bucket holds, in degrees; the first bucket reaches down without bound. */
    public val from: Double,
    /** The bucket's name, the word a renderer reads it by: `steepAscent`. */
    public val key: String,
    /** The colour the bucket's stretches are painted in. */
    public val color: String,
) {
    override fun equals(other: Any?): Boolean =
        other is SlopeBucket && sameNumber(other.from, from) && other.key == key && other.color == color

    override fun hashCode(): Int = (from.hashCode() * 31 + key.hashCode()) * 31 + color.hashCode()

    override fun toString(): String = "SlopeBucket(from=$from, key=$key, color=$color)"
}
