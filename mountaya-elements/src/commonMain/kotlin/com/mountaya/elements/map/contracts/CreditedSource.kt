package com.mountaya.elements

/**
 * CreditedSource is a source of a map's style as its credit is read (`map.style.attribution`): the credit it
 * states, which may be absent or blank, and whether a layer draws it at the camera's zoom. Two are equal
 * where both are.
 */
internal class CreditedSource(
    /** The credit the source states, or null where it states none. */
    val attribution: String?,
    /** Whether a layer of the style draws the source at the camera's zoom. */
    val isDrawn: Boolean,
) {
    override fun equals(other: Any?): Boolean =
        other is CreditedSource && other.attribution == attribution && other.isDrawn == isDrawn

    override fun hashCode(): Int = (attribution?.hashCode() ?: 0) * 31 + isDrawn.hashCode()

    override fun toString(): String = "CreditedSource(attribution=$attribution, isDrawn=$isDrawn)"
}
