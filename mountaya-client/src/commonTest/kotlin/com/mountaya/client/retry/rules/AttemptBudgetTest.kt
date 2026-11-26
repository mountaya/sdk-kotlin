package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AttemptBudgetTest {
    @Test
    fun theCallsBudgetWinsElseTheLargerOfTheClientsAndTheOperationsPlusAMargin() {
        assertEquals(1.seconds, AttemptBudget.budget(1.seconds, 30.seconds, 55.seconds))
        assertEquals(35.seconds, AttemptBudget.budget(null, 10.seconds, 30.seconds))
        assertEquals(120.seconds, AttemptBudget.budget(null, 120.seconds, 30.seconds))
        assertEquals(10.seconds, AttemptBudget.budget(null, 10.seconds, null))
        assertEquals(30.seconds, AttemptBudget.clientDefault)
    }

    @Test
    fun aBudgetOfZeroOrLessIsRefused() {
        assertFailsWith<ConfigurationException> { AttemptBudget.check(Duration.ZERO, "The budget") }
        assertFailsWith<ConfigurationException> { AttemptBudget.check((-1).seconds, "The budget") }
        AttemptBudget.check(1.milliseconds, "The budget")
    }
}
