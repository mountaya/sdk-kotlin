package com.mountaya.geo

/**
 * WaypointField is one field a place's author published, as a card and a waypoint table state it
 * (`geo.profile.waypoint-property-value`): its key, the label its author gave it, its value written by the
 * format its author declared, and the address a renderer may anchor it by, only where it is declared a link
 * and its value is a web address.
 *
 * Two are equal where every text is written alike, or absent on both.
 */
public class WaypointField(
    /** The field's key, unique within its place. */
    public val key: String,
    /** The field's name as a reader reads it. */
    public val label: String,
    /** The field's value as `waypointPropertyValue` writes it. */
    public val value: String,
    /** The address a renderer anchors the field by, null where it is no link. */
    public val href: String? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is WaypointField &&
            other.key == key &&
            other.label == label &&
            other.value == value &&
            other.href == href

    override fun hashCode(): Int = listOf(key, label, value, href).hashCode()

    override fun toString(): String = "WaypointField(key=$key, label=$label, value=$value, href=$href)"
}
