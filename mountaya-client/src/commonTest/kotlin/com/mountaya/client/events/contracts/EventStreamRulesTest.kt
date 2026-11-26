package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EventStreamRulesTest {
    @Test
    fun anAgentRunsStreamStopsAfterSixReopensAndEndsOnAny5xxButA503OrA504() {
        assertEquals(6, EventStreamRules.agentRun.maxReopens)
        assertEquals(500.milliseconds, EventStreamRules.agentRun.baseWait)
        assertEquals(8.seconds, EventStreamRules.agentRun.maxWait)
        assertEquals(1, EventStreamRules.agentRun.framesBeforeProgress)
        assertTrue(EventStreamRules.agentRun.endsOnServerError)
    }

    @Test
    fun anAgentRunsOpensAnswered503AreHeldToTwoMinutesAndEveryArrivalIsProgress() {
        assertEquals(2.minutes, EventStreamRules.agentRun.maxUnavailable)
        assertTrue(EventStreamRules.agentRun.ownArrivalIsProgress)
    }

    @Test
    fun aListStreamNeverStopsOnA503AndReadsEveryArrivalAsProgress() {
        assertNull(EventStreamRules.collaborationFeed.maxUnavailable)
        assertTrue(EventStreamRules.collaborationFeed.ownArrivalIsProgress)
    }

    @Test
    fun anEditorStreamIsAFeedWhoseReadersOwnArrivalIsNoProgress() {
        val editor = EventStreamRules.collaborationEditor
        val feed = EventStreamRules.collaborationFeed
        assertEquals(feed.baseWait, editor.baseWait)
        assertEquals(feed.maxWait, editor.maxWait)
        assertEquals(feed.maxReopens, editor.maxReopens)
        assertEquals(feed.maxUnavailable, editor.maxUnavailable)
        assertEquals(feed.framesBeforeProgress, editor.framesBeforeProgress)
        assertEquals(feed.endsOnServerError, editor.endsOnServerError)
        assertFalse(editor.ownArrivalIsProgress)
    }

    @Test
    fun aCollaborationFeedNeverStopsOnItsOwnAndWaitsUpTo15Seconds() {
        assertNull(EventStreamRules.collaborationFeed.maxReopens)
        assertEquals(15.seconds, EventStreamRules.collaborationFeed.maxWait)
        assertEquals(0, EventStreamRules.collaborationFeed.framesBeforeProgress)
        assertFalse(EventStreamRules.collaborationFeed.endsOnServerError)
        assertEquals(45.seconds, EventStreamRules.stallWindow)
        assertEquals("Last-Event-ID", LAST_EVENT_ID_HEADER)
    }
}
