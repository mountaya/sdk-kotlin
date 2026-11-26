package com.mountaya.conformance

import com.mountaya.conformance.contract.generated.EmbeddedContract
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class RunConformanceCaseTest {
    private val fixture =
        """
        {
          "capability": "example.doubling",
          "tolerance": {"kind": "relative", "value": 1e-9},
          "cases": [
            {"name": "two", "input": 2, "expect": 4},
            {"name": "refused", "input": -1, "expect": {"refused": "ConfigurationError"}}
          ]
        }
        """.trimIndent()

    private val contract: (String) -> String? = { id -> if (id == "example.doubling") fixture else null }

    private class Doubling(
        private val wrongBy: Double = 0.0,
    ) : ConformanceRunner {
        override suspend fun run(
            scope: TestScope,
            case: ConformanceCase,
        ): ConformanceOutcome {
            val input = (case.input as FixtureValue.Number).value
            if (input < 0) return ConformanceOutcome.Refused("ConfigurationError")
            return ConformanceOutcome.Value(FixtureValue.Number(input * 2 + wrongBy))
        }
    }

    private suspend fun TestScope.failure(
        runner: ConformanceRunner,
        case: String,
        id: String = "example.doubling",
    ): String = assertFailsWith<AssertionError> { runCase(this, runner, id, case, contract) }.message.orEmpty()

    /**
     * Every fixture the registry names, a scoped one included, so a tolerance kind the harness does
     * not know fails here the day it lands, whether or not anything runs the fixture yet.
     */
    @Test
    fun everyCapabilityOfTheEmbeddedRegistryCarriesAFixtureWithCasesAndAToleranceTheHarnessKnows() {
        assertEquals(EmbeddedContract.PRESENT, EmbeddedContract.entries.isNotEmpty())
        for (entry in EmbeddedContract.entries) {
            val text = assertNotNull(EmbeddedContract.fixture(entry.fixture), "${entry.id}: no fixture is embedded")
            val fixture = assertIs<FixtureValue.Object>(parseFixture(text), entry.id)
            assertIs<FixtureValue.Array>(fixture["cases"], "${entry.id}: no `cases` list")
            try {
                Tolerance.of(fixture)
            } catch (error: AssertionError) {
                throw AssertionError("${entry.id}: ${error.message}", error)
            }
        }
    }

    @Test
    fun aMatchingAnswerPasses() =
        runTest {
            runCase(this, Doubling(), "example.doubling", "two", contract)
            runCase(this, Doubling(), "example.doubling", "refused", contract)
        }

    @Test
    fun aFailureLeadsWithTheCapabilityAndTheCase() =
        runTest {
            assertEquals("example.doubling · two: at $: expected 4.0, got 5.0", failure(Doubling(wrongBy = 1.0), "two"))
        }

    @Test
    fun aValueWhereARefusalIsExpectedFails() =
        runTest {
            val refusingNothing =
                object : ConformanceRunner {
                    override suspend fun run(
                        scope: TestScope,
                        case: ConformanceCase,
                    ): ConformanceOutcome = ConformanceOutcome.Value(FixtureValue.Null)
                }
            assertEquals(
                "example.doubling · refused: expected the refusal ConfigurationError, got a value",
                failure(refusingNothing, "refused"),
            )
        }

    @Test
    fun aRunnerThatThrowsIsReportedAgainstItsCase() =
        runTest {
            val throwing =
                object : ConformanceRunner {
                    override suspend fun run(
                        scope: TestScope,
                        case: ConformanceCase,
                    ): ConformanceOutcome = throw IllegalStateException("boom")
                }
            assertEquals(
                "example.doubling · two: the runner threw IllegalStateException: boom",
                failure(throwing, "two"),
            )
        }

    @Test
    fun anUnknownCapabilityOrCaseIsReported() =
        runTest {
            assertEquals(
                "example.unknown · two: the registry carries no such capability, or its fixture is missing",
                failure(Doubling(), "two", id = "example.unknown"),
            )
            assertEquals("example.doubling · three: the fixture has no case of that name", failure(Doubling(), "three"))
        }

    @Test
    fun aToleranceTheHarnessDoesNotKnowFailsEveryCaseOfItsFixture() =
        runTest {
            val unknown = fixture.replace("\"relative\"", "\"percent\"")
            val reading: (String) -> String? = { id -> if (id == "example.doubling") unknown else null }

            for (case in listOf("two", "refused")) {
                val message =
                    assertFailsWith<AssertionError> {
                        runCase(this, Doubling(), "example.doubling", case, reading)
                    }.message

                assertEquals(
                    "example.doubling · $case: a tolerance of kind \"percent\", which the harness does not know",
                    message,
                )
            }
        }

    @Test
    fun aNumberJsonCannotCarryIsDecodedInTheInputAndTheExpectation() =
        runTest {
            val nonFinite =
                """
                {"cases": [{"name": "nan", "input": {"${'$'}number": "NaN"}, "expect": {"${'$'}number": "NaN"}}]}
                """.trimIndent()
            val reading: (String) -> String? = { id -> if (id == "example.doubling") nonFinite else null }

            runCase(this, Doubling(), "example.doubling", "nan", reading)
        }
}
