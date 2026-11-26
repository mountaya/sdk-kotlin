package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EventRecordTest {
    @Test
    fun aRecordIsItsEventItsDataAndItsOwnId() {
        assertEquals(EventRecord("message", "{}", "1-0"), EventRecord("message", "{}", "1-0"))
        assertNotEquals(EventRecord("message", "{}", null), EventRecord("message", "{}", "1-0"))
        assertEquals("EventRecord(event=null, id=null, data=a)", EventRecord(null, "a", null).toString())
    }
}
