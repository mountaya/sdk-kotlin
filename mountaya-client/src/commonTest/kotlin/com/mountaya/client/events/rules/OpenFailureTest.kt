package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenFailureTest {
    private fun ends(
        status: Int,
        rules: EventStreamRules,
    ): Boolean = OpenFailure.ends(ApiException.of(status), rules)

    @Test
    fun a4xxButA429EndsEitherStreamA429A503A504AndADropReopenEither() {
        for (rules in listOf(EventStreamRules.agentRun, EventStreamRules.collaborationFeed)) {
            assertTrue(ends(401, rules))
            assertTrue(ends(402, rules))
            assertFalse(ends(429, rules))
            assertFalse(ends(503, rules))
            assertFalse(ends(504, rules))
            assertFalse(OpenFailure.ends(ConnectionException(), rules))
            assertTrue(OpenFailure.ends(ConfigurationException("refused"), rules))
        }
    }

    @Test
    fun everyOther5xxEndsAnAgentRunsStreamAndReopensACollaborationFeed() {
        for (status in listOf(500, 501, 502)) {
            assertTrue(ends(status, EventStreamRules.agentRun))
            assertFalse(ends(status, EventStreamRules.collaborationFeed))
        }
    }
}
