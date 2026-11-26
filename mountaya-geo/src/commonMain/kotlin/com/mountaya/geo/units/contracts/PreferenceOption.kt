package com.mountaya.geo

/**
 * PreferenceOption is one option of a preference a preferences picker offers (`geo.units.preference-choices`):
 * the value a press on it asks for, what it is labelled with, and whether the set the picker is made over
 * holds it.
 *
 * Two are equal where every member is.
 */
public class PreferenceOption(
    /** The value the preference takes when the option is pressed. */
    public val value: PreferenceValue,
    /** What the option is labelled with. */
    public val label: PreferenceLabel,
    /** Whether the set holds the option, a language as the map reads one. */
    public val isChecked: Boolean,
) {
    override fun equals(other: Any?): Boolean =
        other is PreferenceOption && other.value == value && other.label == label && other.isChecked == isChecked

    override fun hashCode(): Int = (value.hashCode() * 31 + label.hashCode()) * 31 + isChecked.hashCode()

    override fun toString(): String = "PreferenceOption(value=$value, label=$label, isChecked=$isChecked)"
}
