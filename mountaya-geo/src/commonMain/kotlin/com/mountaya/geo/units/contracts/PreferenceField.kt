package com.mountaya.geo

/**
 * PreferenceField is a preference a preferences picker offers, a member of `UnitPreferences`, carrying the
 * contract's spelling, which is also the key of the word heading it in the elements' `preferences` section
 * (`geo.units.preference-choices`).
 */
public enum class PreferenceField(
    /** The preference as `geo/preference-choices.json` spells it. */
    public val id: String,
) {
    /** The measurement system lengths are stated in. */
    Length("length"),

    /** The form a travel rate takes. */
    Pace("pace"),

    /** The notation a slope is stated in. */
    Slope("slope"),

    /** The scale a temperature is stated in. */
    Temperature("temperature"),

    /** The language the reader reads, which the map labels in too. */
    Language("language"),
}
