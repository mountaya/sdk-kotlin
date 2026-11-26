package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationInstructionArgument
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.geo.NavigationStep
import com.mountaya.geo.stepInstruction
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.instruction-keys`: the copy key the case's step is said with, and the arguments it
 * takes, by name. The case writes a step's `maneuver`, `name` and `exitNumber`, the members an
 * instruction reads, and the runner builds the step with a range of vertex 0 alone, which no
 * instruction reads. The fixture's `keys`, every key with the arguments it takes in order, is held to
 * `NavigationInstructionKey`'s entries.
 */
object GeoNavigationInstructionKeysRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "keys" to
                        FixtureValue.Object(
                            NavigationInstructionKey.entries.map { key ->
                                key.id to
                                    textArray(key.arguments.map { it.id })
                            },
                        ),
                ),
        )
        val input = case.fields
        val instruction =
            stepInstruction(
                NavigationStep(
                    fromIndex = 0,
                    toIndex = 0,
                    maneuver = RestSchema.ManeuverType(input.text("maneuver")),
                    name = input.textOrNull("name"),
                    exitNumber = wholeOrNull(input, "exitNumber"),
                ),
            )
        val arguments =
            instruction.key.arguments.map { argument ->
                argument.id to
                    when (argument) {
                        NavigationInstructionArgument.Exit -> {
                            numberOrNull(instruction.exit?.toDouble())
                        }

                        NavigationInstructionArgument.Name -> {
                            instruction.name?.let { FixtureValue.Text(it) }
                                ?: FixtureValue.Null
                        }
                    }
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "arguments" to FixtureValue.Object(arguments),
                    "key" to FixtureValue.Text(instruction.key.id),
                ),
            ),
        )
    }
}
