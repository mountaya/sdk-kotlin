package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OrganizationReadTest {
    @Test
    fun anAnsweredReadIsEqualToAnotherOfTheSameWhiteLabel() {
        assertEquals(OrganizationRead.Answered(true), OrganizationRead.Answered(true))
        assertEquals(OrganizationRead.Answered(true).hashCode(), OrganizationRead.Answered(true).hashCode())
        assertNotEquals(OrganizationRead.Answered(true), OrganizationRead.Answered(false))
        assertNotEquals<OrganizationRead>(OrganizationRead.Waiting, OrganizationRead.Failed)
        assertEquals("OrganizationRead.NoKey", OrganizationRead.NoKey.toString())
    }

    @Test
    fun aReadStandsInOneOfFiveStatesTimedOutApartFromFailed() {
        val states =
            listOf(
                OrganizationRead.Waiting,
                OrganizationRead.Answered(whiteLabel = false),
                OrganizationRead.Failed,
                OrganizationRead.TimedOut,
                OrganizationRead.NoKey,
            )

        assertEquals(5, states.toSet().size)
        assertNotEquals<OrganizationRead>(OrganizationRead.TimedOut, OrganizationRead.Failed)
        assertEquals("OrganizationRead.TimedOut", OrganizationRead.TimedOut.toString())
    }
}
