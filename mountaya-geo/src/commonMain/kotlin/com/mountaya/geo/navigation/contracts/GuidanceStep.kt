package com.mountaya.geo

/**
 * GuidanceStep is one step of a followed route as an engine that speaks reads it: how long it is, the
 * maneuver that begins it, and the prompts said along it before the maneuver that ends it, which is the
 * next step's (`geo.navigation.prompts`).
 *
 * Two are equal where every member is, the length as the same number, `NaN` the same as `NaN` and 0
 * another than -0.
 */
public class GuidanceStep(
    /** The step's measured length, as Geo measures the route. */
    public val meters: Double,
    /** The maneuver that begins the step, as [stepInstruction] says it. */
    public val instruction: NavigationInstruction,
    /** The prompts said along the step, the farthest from its end first, the order [duePrompt] reads them in. */
    public val prompts: List<GuidancePrompt>,
) {
    override fun equals(other: Any?): Boolean =
        other is GuidanceStep && sameNumber(other.meters, meters) && other.instruction == instruction &&
            other.prompts == prompts

    override fun hashCode(): Int = (meters.hashCode() * 31 + instruction.hashCode()) * 31 + prompts.hashCode()

    override fun toString(): String = "GuidanceStep(meters=$meters, instruction=$instruction, prompts=$prompts)"
}
