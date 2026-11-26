package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * PreferencePress is what a press on an option of a preferences picker asks for
 * (`geo.units.preference-choices`): the whole preference set, and the language the map relabels in where the
 * press is on a language.
 *
 * Two are equal where they ask for the same.
 */
public class PreferencePress(
    /** The set the press asks for: the set it was made over, with one preference changed. */
    public val next: UnitPreferences,
    /** The language the map relabels in, for a press on a language, and null otherwise. */
    public val relabel: RestSchema.Language?,
) {
    override fun equals(other: Any?): Boolean =
        other is PreferencePress && other.next == next && other.relabel == relabel

    override fun hashCode(): Int = next.hashCode() * 31 + relabel.hashCode()

    override fun toString(): String = "PreferencePress(next=$next, relabel=$relabel)"
}
