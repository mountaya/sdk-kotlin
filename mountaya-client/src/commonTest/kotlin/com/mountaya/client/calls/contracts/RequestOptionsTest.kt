package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class RequestOptionsTest {
    @Test
    fun noOverrideLeavesTheClientsPolicyAndBudget() {
        assertNull(RequestOptions().retry)
        assertNull(RequestOptions().timeout)
    }

    @Test
    fun twoOptionsAreEqualWhenTheyOverrideAlike() {
        assertEquals(RequestOptions(RetryPolicy(maxRetries = 0)), RequestOptions(RetryPolicy(maxRetries = 0)))
        assertEquals(RequestOptions(timeout = 3.seconds), RequestOptions(timeout = 3.seconds))
        assertNotEquals(RequestOptions(RetryPolicy(maxRetries = 0)), RequestOptions())
        assertNotEquals(RequestOptions(timeout = 3.seconds), RequestOptions())
        assertEquals("RequestOptions(retry=null, timeout=3s)", RequestOptions(timeout = 3.seconds).toString())
    }
}
