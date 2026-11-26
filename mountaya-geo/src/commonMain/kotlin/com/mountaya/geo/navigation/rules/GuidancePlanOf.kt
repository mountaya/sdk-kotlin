package com.mountaya.geo

/**
 * PROMPT_METERS is how far before a maneuver its prompt is said: about half a minute ahead at a walker's pace,
 * and ten seconds at a cyclist's. A step shorter than that says the maneuver that ends it as the traveller
 * enters it. It is the `promptMeters` `geo/navigation-prompts.json` declares.
 */
public const val PROMPT_METERS: Double = 50.0

/**
 * guidancePlanOf lays a followed [route] out as an engine that speaks reads it (`geo.navigation.prompts`), or
 * answers null where no vertex of its line names a place, which leaves nothing to guide.
 *
 * Each step is the followed route's own, its length read off the measure the route was followed with,
 * the `cumulativeMeters` of its line taken once (`FollowedRoute.measured`), so a step's length is the one
 * the traveller is told and the line is never measured again. The prompts announce each maneuver from
 * the step that leads to it, [PROMPT_METERS] before its end or as the step begins where it is shorter,
 * and the first step announces its own departure as it begins, ahead of the maneuver that ends it. The
 * last step leads to no maneuver, and announces none.
 *
 * A step of no length covers no ground, so the progress a prompt is read from never stands in it, and it
 * keeps no prompt of its own, even where Geo's progress names it, as it does a step whose vertices after
 * the first name no place. Its prompts move:
 *
 * - to the nearest earlier step of some length, after that step's own prompts and any moved there before
 *   them, each due as that step ends (a trigger of 0), at the vertex the maneuvers share;
 * - where the steps of no length open the route, to the first step of some length, before its own
 *   prompts and in travel order, each due as that step begins (a trigger of its length), and the
 *   departure is dropped, since it covers no ground: as the walk begins, the last maneuver of the opening
 *   vertex is said, and the departure is not;
 * - nowhere, where no step has any length, a route of one vertex: each prompt stays on its step.
 *
 * Since [duePrompt] lets a step's last prompt due win and each prompt is said once, two maneuvers on one
 * vertex are said in order, the first [PROMPT_METERS] before it and the second at it; of three or more,
 * the first and the last are said, and those between are not. The prompts are numbered from 0 in travel
 * order, once they are moved.
 *
 * It is the plan every SDK's guidance lays out, prompt for prompt, and the one an engine an app writes that
 * speaks lays out as `DeviceNavigationEngine` does.
 */
public fun guidancePlanOf(route: FollowedRoute): GuidancePlan? {
    if (route.line.none(::namesPlace)) return null
    val measured = route.measured
    val steps = route.steps
    val lengths = steps.map { measured[it.toIndex] - measured[it.fromIndex] }
    val instructions = steps.map(::stepInstruction)
    // Each step's own prompts, as a step that covers ground says them: what is said and its trigger.
    val own =
        steps.indices.map { index ->
            buildList {
                if (index == 0) add(instructions[0] to lengths[0])
                if (index + 1 < steps.size) add(instructions[index + 1] to minOf(PROMPT_METERS, lengths[index]))
            }
        }
    val placed = placedPrompts(own, lengths)
    var nextPrompt = 0
    val planned =
        steps.indices.map { index ->
            val prompts =
                placed[index].map { (instruction, meters) ->
                    GuidancePrompt(nextPrompt++, instruction, meters)
                }
            GuidanceStep(lengths[index], instructions[index], prompts)
        }
    return GuidancePlan(planned)
}

/**
 * Each step's prompts once the prompts of every step of no length have moved to a step of some length, as
 * [guidancePlanOf] states it, in travel order; [own] is each step's own, the departure first on the first.
 */
private fun placedPrompts(
    own: List<List<Pair<NavigationInstruction, Double>>>,
    lengths: List<Double>,
): List<List<Pair<NavigationInstruction, Double>>> {
    val first = lengths.indexOfFirst { it > 0.0 }
    // No step covers any ground: there is nowhere to move a prompt to.
    if (first < 0) return own
    val placed = List(own.size) { ArrayList<Pair<NavigationInstruction, Double>>() }
    // The steps of no length that open the route: their departure covers no ground and is dropped, and
    // what they announce is due as the first step of some length begins.
    for (index in 0 until first) {
        val announced = if (index == 0) own[index].drop(1) else own[index]
        for ((instruction, _) in announced) placed[first] += instruction to lengths[first]
    }
    placed[first] += own[first]
    var reached = first
    for (index in first + 1 until own.size) {
        if (lengths[index] > 0.0) {
            placed[index] += own[index]
            reached = index
        } else {
            // Due as the step of some length before it ends, at the vertex the maneuvers share.
            for ((instruction, _) in own[index]) placed[reached] += instruction to 0.0
        }
    }
    return placed
}
