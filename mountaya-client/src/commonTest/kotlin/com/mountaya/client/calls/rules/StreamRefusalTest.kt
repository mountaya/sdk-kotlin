package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreamRefusalTest {
    @Test
    fun anAcceptNamingAnEventStreamAmongItsRangesInAnyCaseAndWithParametersIsOne() {
        assertTrue(StreamRefusal.asksForStream("text/event-stream"))
        assertTrue(StreamRefusal.asksForStream("Text/Event-Stream; charset=utf-8"))
        assertTrue(StreamRefusal.asksForStream("application/json, text/event-stream"))
        assertFalse(StreamRefusal.asksForStream("application/json"))
        assertFalse(StreamRefusal.asksForStream("text/event-streams"))
    }

    @Test
    fun aBufferedRequestAskingForAStreamIsRefusedAndOneWithNoAcceptIsNot() {
        assertFailsWith<ConfigurationException> { StreamRefusal.check(mapOf("accept" to "text/event-stream")) }
        StreamRefusal.check(emptyMap())
    }
}
