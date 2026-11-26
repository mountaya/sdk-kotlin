package com.mountaya.geo

/**
 * GuidancePlan is a followed route as an engine that speaks reads it: its steps, the followed route's
 * own, one for one and in the same order, each with the prompts said along it
 * (`geo.navigation.prompts`). [guidancePlanOf] lays a route's out, and [duePrompt] reads the prompt due
 * from it.
 *
 * Two are equal where every step is.
 */
public class GuidancePlan(
    /** The route's steps, one for each of the followed route's. */
    public val steps: List<GuidanceStep>,
) {
    override fun equals(other: Any?): Boolean = other is GuidancePlan && other.steps == steps

    override fun hashCode(): Int = steps.hashCode()

    override fun toString(): String = "GuidancePlan(steps=$steps)"
}
