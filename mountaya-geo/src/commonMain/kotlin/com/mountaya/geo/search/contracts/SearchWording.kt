package com.mountaya.geo

/**
 * SearchWording is how a search result's title and second line are worded for a reader: [resolveLabel]
 * answers the word each wire value reads as, or null or the empty string for a value it has no word
 * for, which then reads as the English title of the value (`POPULATED_PLACE` as `Populated place`),
 * and [units] states a distance and an elevation in the reader's system, language and digits, and with
 * none they read in metric, in Latin digits.
 *
 * The default words nothing: English titles of the wire values and metric figures. Two are equal
 * where they hold the same resolver, by identity, and the same units.
 */
public class SearchWording(
    /** The reader's units, or null for metric in Latin digits. */
    public val units: UnitPreferences? = null,
    /** The word a wire value reads as, or null where the host has none. The last member, so it may trail. */
    public val resolveLabel: (SearchLabel) -> String? = { null },
) {
    override fun equals(other: Any?): Boolean =
        other is SearchWording && other.resolveLabel === resolveLabel && other.units == units

    override fun hashCode(): Int = resolveLabel.hashCode() * 31 + units.hashCode()

    override fun toString(): String = "SearchWording(units=$units)"
}
