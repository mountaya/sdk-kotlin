package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class EventRoleTest {
    @Test
    fun eachRolePlaysItsOnePart() {
        val roles =
            listOf(EventRole.ordinary, EventRole.live, EventRole.openingSnapshot, EventRole.end, EventRole.reconnect)

        assertEquals(listOf(true, false, false, true, false), roles.map { it.movesPosition })
        assertEquals(listOf(false, false, false, true, false), roles.map { it.endsStream })
        assertEquals(listOf(false, false, false, false, true), roles.map { it.isReconnect })
        assertEquals(listOf(false, false, true, false, false), roles.map { it.isOpeningSnapshot })
    }
}
