package com.mountaya.geo

/**
 * GuidancePrompt is one maneuver said aloud before the traveller reaches it: the instruction said, and how
 * far before the end of the step that leads to it the prompt falls due, its trigger, as [duePrompt] reads it
 * (`geo.navigation.prompts`).
 *
 * Two are equal where they hold one place, one instruction and the same distance, `NaN` the same as `NaN`
 * and 0 another than -0.
 */
public class GuidancePrompt(
    /** The prompt's place among every prompt of its route, counted from 0 in travel order. */
    public val id: Int,
    /** The maneuver said. */
    public val instruction: NavigationInstruction,
    /** The distance before the end of its step at which the prompt falls due, its trigger. */
    public val meters: Double,
) {
    override fun equals(other: Any?): Boolean =
        other is GuidancePrompt && other.id == id && other.instruction == instruction &&
            sameNumber(other.meters, meters)

    override fun hashCode(): Int = (id * 31 + instruction.hashCode()) * 31 + meters.hashCode()

    override fun toString(): String = "GuidancePrompt(id=$id, instruction=$instruction, meters=$meters)"
}
