package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.prompts`: the prompts a speaking engine announces the case's route with, or the one due
 * where the traveller stands. The route is built from its `line` and `steps` through [followedRouteOf], as
 * an engine follows one, and laid out by [guidancePlanOf]; `ask` names the question: `plan` answers each
 * step's length and its prompts, each its `id`, its instruction's key and arguments and its trigger, and
 * `due` the `id` [duePrompt] finds due at `progress`, a whole `geo.navigation.progress` answer, or null
 * before any position. The fixture's `promptMeters` and `promptSlackMeters` are held to [PROMPT_METERS]
 * and [PROMPT_SLACK_METERS].
 */
object GeoNavigationPromptsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNavigationConstants(
            case,
            mapOf(
                "promptMeters" to FixtureValue.Number(PROMPT_METERS),
                "promptSlackMeters" to FixtureValue.Number(PROMPT_SLACK_METERS),
            ),
        )
        val input = case.fields
        val plan = guidancePlanOf(fixtureFollowedRoute(input)) ?: throw AssertionError("the route lays out no plan")
        return when (val ask = input.text("ask")) {
            "plan" -> {
                ConformanceOutcome.Value(FixtureValue.Array(plan.steps.map(::stepValue)))
            }

            "due" -> {
                if (input["progress"] == null) throw AssertionError("a `due` case states its `progress`, null included")
                val due = duePrompt(plan, fixtureProgress(input["progress"]))
                ConformanceOutcome.Value(due?.let { FixtureValue.Number(it.id.toDouble()) } ?: FixtureValue.Null)
            }

            else -> {
                throw AssertionError("no question is `$ask`")
            }
        }
    }

    /** [step] as the fixture writes a step of the plan: its length and its prompts. */
    private fun stepValue(step: GuidanceStep): FixtureValue =
        FixtureValue.Object(
            listOf(
                "length" to FixtureValue.Number(step.meters),
                "prompts" to
                    FixtureValue.Array(
                        step.prompts.map { prompt ->
                            FixtureValue.Object(
                                listOf(
                                    "id" to FixtureValue.Number(prompt.id.toDouble()),
                                    "instruction" to instructionValue(prompt.instruction),
                                    "trigger" to FixtureValue.Number(prompt.meters),
                                ),
                            )
                        },
                    ),
            ),
        )

    /** [instruction] as `geo.navigation.instruction-keys` writes one: its key, and its arguments by name. */
    private fun instructionValue(instruction: NavigationInstruction): FixtureValue {
        val arguments =
            instruction.key.arguments.map { argument ->
                argument.id to
                    when (argument) {
                        NavigationInstructionArgument.Exit -> {
                            instruction.exit?.let { FixtureValue.Number(it.toDouble()) } ?: FixtureValue.Null
                        }

                        NavigationInstructionArgument.Name -> {
                            instruction.name?.let { FixtureValue.Text(it) } ?: FixtureValue.Null
                        }
                    }
            }
        return FixtureValue.Object(
            listOf(
                "arguments" to FixtureValue.Object(arguments),
                "key" to FixtureValue.Text(instruction.key.id),
            ),
        )
    }
}
