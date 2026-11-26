package com.mountaya.elements

/**
 * ShownMap is where the view drawing a map laid it out last, which the view publishes on the map's engine so every part
 * of the SDK's own interface laid over the map reads the map's real frame, never a box of its own (`sdk/SPEC.md`
 * section 15): the [map] itself and its [safeArea], the part of it the device leaves visible, each `[left, top, right,
 * bottom]` in density-independent pixels from the top-left corner of the window the map is drawn in.
 *
 * Two are equal where every figure is, so a view laid out again where it stood records nothing new.
 */
internal class ShownMap(
    /** The map's rect in its window, `[left, top, right, bottom]`. */
    val map: List<Double>,
    /** The map's safe area in its window, `[left, top, right, bottom]`, which never leaves [map]. */
    val safeArea: List<Double>,
) {
    override fun equals(other: Any?): Boolean = other is ShownMap && other.map == map && other.safeArea == safeArea

    override fun hashCode(): Int = map.hashCode() * 31 + safeArea.hashCode()

    override fun toString(): String = "ShownMap(map=$map, safeArea=$safeArea)"
}
