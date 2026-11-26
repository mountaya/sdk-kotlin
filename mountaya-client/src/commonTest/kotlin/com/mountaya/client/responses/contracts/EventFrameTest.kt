package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EventFrameTest {
    @Test
    fun aFrameIsItsIdAndEvent() {
        assertEquals(EventFrame("41", "done"), EventFrame("41", "done"))
        assertNotEquals(EventFrame("41", "done"), EventFrame(null, "done"))
        assertEquals("EventFrame(id=null, event=done)", EventFrame(null, "done").toString())
    }
}
