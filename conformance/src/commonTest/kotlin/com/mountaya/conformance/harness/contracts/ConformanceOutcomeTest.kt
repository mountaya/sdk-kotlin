package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ConformanceOutcomeTest {
    @Test
    fun aRefusalIsNamedByItsClass() {
        val outcome: ConformanceOutcome = ConformanceOutcome.Refused("ConfigurationError")

        assertEquals("ConfigurationError", assertIs<ConformanceOutcome.Refused>(outcome).name)
    }

    @Test
    fun aValueCarriesTheAnswer() {
        val outcome: ConformanceOutcome = ConformanceOutcome.Value(FixtureValue.Number(60.0))

        assertEquals(60.0, assertIs<FixtureValue.Number>(assertIs<ConformanceOutcome.Value>(outcome).value).value)
    }

    @Test
    fun theMembersOfASetCarryTheAnswerInTheOrderTheSdkListsThem() {
        val outcome: ConformanceOutcome =
            ConformanceOutcome.Members(
                listOf(FixtureValue.Text("b"), FixtureValue.Text("a")),
            )

        assertEquals(
            listOf("b", "a"),
            assertIs<ConformanceOutcome.Members>(outcome).members.map { assertIs<FixtureValue.Text>(it).value },
        )
    }

    @Test
    fun anOutcomeIsAValueTheMembersOfASetOrARefusalAndNothingElse() {
        val outcomes =
            listOf(
                ConformanceOutcome.Value(FixtureValue.Null),
                ConformanceOutcome.Members(emptyList()),
                ConformanceOutcome.Refused("SelectionError"),
            )

        // Exhaustive with three branches: a fourth kind of answer would not compile here.
        val kinds =
            outcomes.map { outcome ->
                when (outcome) {
                    is ConformanceOutcome.Value -> "value"
                    is ConformanceOutcome.Members -> "members"
                    is ConformanceOutcome.Refused -> "refused"
                }
            }

        assertEquals(listOf("value", "members", "refused"), kinds)
    }
}
