package com.mountaya.geo

/**
 * PROMPT_SLACK_METERS is how far short of its trigger a prompt already falls due, in meters, so a prompt read a
 * little early is said rather than missed between two positions: the `promptSlackMeters`
 * `geo/navigation-prompts.json` declares.
 */
public const val PROMPT_SLACK_METERS: Double = 5.0

/**
 * duePrompt answers the prompt of [plan] due where the traveller stands, [progress] being where Geo reads
 * them on the route the plan lays out (`geo.navigation.progress`), or null before any position is read
 * there; null where none is due.
 *
 * The step read is the one [progress] names, held within the plan, and the first before any position,
 * where the traveller stands at the route's start. The distance read is what is left to that step's end,
 * [NavigationProgress.toManeuver], measured from the traveller's foot on the route, which never moves back
 * and is found on every stretch ahead, and the whole step before any position. A prompt is due where that
 * distance less its trigger, [GuidancePrompt.meters], is [PROMPT_SLACK_METERS] or less, and of the step's
 * prompts due the last wins: the farther a prompt stands from its step's end the earlier it comes, so the
 * one nearest the maneuver is the one standing.
 *
 * That is the rule every SDK's guidance reads over the same plan (`geo.navigation.prompts`), so a prompt is
 * said at the meter Geo's progress names, which every SDK reads alike, and an engine an app writes that
 * speaks reads it as `DeviceNavigationEngine` does. Each prompt is said once per route, and only while the
 * navigation is `navigating`: that is the speaking engine's to keep, which this rule leaves to it.
 */
public fun duePrompt(
    plan: GuidancePlan,
    progress: NavigationProgress?,
): GuidancePrompt? {
    val steps = plan.steps
    if (steps.isEmpty()) return null
    val step = steps[(progress?.step ?: 0).coerceIn(0, steps.size - 1)]
    val left = progress?.toManeuver?.distance ?: step.meters
    for (prompt in step.prompts.asReversed()) {
        if (left - prompt.meters <= PROMPT_SLACK_METERS) return prompt
    }
    return null
}
