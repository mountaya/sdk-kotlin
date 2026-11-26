package com.mountaya.conformance

import com.mountaya.conformance.contract.generated.EmbeddedContract
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * runConformanceCase runs one fixture case of [capability] with [runner], as a test.
 *
 * This is what every generated conformance test calls. A failure's message starts with the
 * exact `"<id> · <case>"` string, so it names the capability and the case without opening the
 * fixture, whatever name the test function had to take.
 */
public fun runConformanceCase(
    runner: ConformanceRunner,
    capability: String,
    case: String,
): TestResult = runTest { runCase(this, runner, capability, case) }

/** The body of [runConformanceCase], for a test that asserts what a case reports. */
internal suspend fun runCase(
    scope: TestScope,
    runner: ConformanceRunner,
    capability: String,
    case: String,
    contract: (String) -> String? = { id ->
        EmbeddedContract.entries.firstOrNull { it.id == id }?.let { EmbeddedContract.fixture(it.fixture) }
    },
) {
    val label = "$capability · $case"

    fun fail(problem: String): Nothing = throw AssertionError("$label: $problem")

    val fixture =
        parseFixture(contract(capability) ?: fail("the registry carries no such capability, or its fixture is missing"))
    if (fixture !is FixtureValue.Object) fail("the fixture is not a JSON object")
    // Read before the case runs, so a kind the harness does not know fails every case of the
    // fixture, a refusal case included.
    val tolerance =
        try {
            Tolerance.of(fixture)
        } catch (error: AssertionError) {
            fail(error.message.orEmpty())
        }
    val cases = fixture["cases"] as? FixtureValue.Array ?: fail("the fixture has no `cases` list")
    val found =
        cases.items
            .filterIsInstance<FixtureValue.Object>()
            .firstOrNull { (it["name"] as? FixtureValue.Text)?.value == case }
            ?: fail("the fixture has no case of that name")
    val expect = found["expect"] ?: fail("the case has no `expect`")

    val outcome =
        try {
            runner.run(scope, ConformanceCase(case, found["input"], fixture))
        } catch (error: AssertionError) {
            throw AssertionError("$label: ${error.message}", error)
        } catch (error: Exception) {
            throw AssertionError("$label: the runner threw ${error::class.simpleName}: ${error.message}", error)
        }

    val refusal = expectedRefusal(expect)
    val problem =
        when (outcome) {
            is ConformanceOutcome.Refused -> {
                when (refusal) {
                    null -> "expected a value, got the refusal ${outcome.name}"
                    outcome.name -> null
                    else -> "expected the refusal $refusal, got the refusal ${outcome.name}"
                }
            }

            is ConformanceOutcome.Value -> {
                if (refusal != null) {
                    "expected the refusal $refusal, got a value"
                } else {
                    compareFixtureValues(expect, outcome.value, tolerance)
                }
            }

            is ConformanceOutcome.Members -> {
                if (refusal != null) {
                    "expected the refusal $refusal, got a value"
                } else {
                    compareFixtureMembers(expect, outcome.members, tolerance)
                }
            }
        }
    if (problem != null) fail(problem)
}
