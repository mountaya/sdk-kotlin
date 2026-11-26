package com.mountaya.geo

/**
 * PreferenceChoice is one preference a preferences picker offers (`geo.units.preference-choices`): the field
 * it sets, headed by the word of the elements' `preferences` section its id names, and its options, in the
 * order the contract lists them.
 *
 * Two are equal where every member is.
 */
public class PreferenceChoice(
    /** The preference the choice sets. */
    public val field: PreferenceField,
    /** The preference's options, in the order the contract lists them. */
    public val options: List<PreferenceOption>,
) {
    override fun equals(other: Any?): Boolean =
        other is PreferenceChoice && other.field == field && other.options == options

    override fun hashCode(): Int = field.hashCode() * 31 + options.hashCode()

    override fun toString(): String = "PreferenceChoice(field=$field, options=$options)"
}
