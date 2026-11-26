package com.mountaya.conformance

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConformanceRunnerTest {
    /**
     * Thirty seconds of waiting, sequenced on the scope's scheduler: a timer the runner starts
     * fires before a longer wait of its own ends, and no real second passes.
     */
    private object VirtualTimeRunner : ConformanceRunner {
        override suspend fun run(
            scope: TestScope,
            case: ConformanceCase,
        ): ConformanceOutcome {
            var fired = false
            scope.launch {
                delay(30_000)
                fired = true
            }
            delay(30_001)
            return ConformanceOutcome.Value(FixtureValue.Bool(fired))
        }
    }

    @Test
    fun aRunnerWaitsOnTheTestsVirtualClock() =
        runTest {
            val outcome = VirtualTimeRunner.run(this, ConformanceCase("wait", null, FixtureValue.Object(emptyList())))

            val value = assertIs<ConformanceOutcome.Value>(outcome).value
            assertTrue(assertIs<FixtureValue.Bool>(value).value)
        }
}
