package com.mountaya.geo

/**
 * NavigationStepsReading is a route's step list as it was read from its surface: the steps, and
 * whether the list was refused for not tiling the route's geometry (`geo.navigation.steps`).
 *
 * A refused list reads as no steps, never as a repair of them, and the route is followed as one with
 * none. Two are equal where both members are.
 */
public class NavigationStepsReading(
    /** Whether the list was refused, its ranges not covering the geometry once in travel order. */
    public val refused: Boolean,
    /** The steps in the one vocabulary, in travel order: empty where there were none or the list was refused. */
    public val steps: List<NavigationStep>,
) {
    override fun equals(other: Any?): Boolean =
        other is NavigationStepsReading && other.refused == refused && other.steps == steps

    override fun hashCode(): Int = refused.hashCode() * 31 + steps.hashCode()

    override fun toString(): String = "NavigationStepsReading(refused=$refused, steps=$steps)"
}
