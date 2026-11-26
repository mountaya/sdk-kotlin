package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NavigationErrorTest {
    /**
     * A failure is identified where it is recorded: it equals itself alone, so two recorded apart are two, the same
     * throwable included, and two empty routes are two failures.
     */
    @Test
    fun aFailureIsIdentifiedWhereItIsRecorded() {
        val offline = IllegalStateException("offline")
        val failed = NavigationError.RouteFailed(offline)
        val empty = NavigationError.EmptyRoute()

        assertEquals(failed, failed)
        assertEquals(failed.hashCode(), failed.hashCode())
        assertNotEquals(failed, NavigationError.RouteFailed(offline))
        assertNotEquals<NavigationError>(failed, empty)
        assertEquals(empty, empty)
        assertNotEquals(empty, NavigationError.EmptyRoute())
        assertEquals("NavigationError.EmptyRoute", empty.toString())
    }

    /** The class is open to kinds a later release adds, so a host's `when` keeps an `else`. */
    @Test
    fun aHostReadsTheKindsItKnowsAndKeepsAnElse() {
        fun said(error: NavigationError): String =
            when (error) {
                is NavigationError.RouteFailed -> "failed: ${error.cause.message}"
                is NavigationError.EmptyRoute -> "empty"
                else -> "other"
            }

        assertEquals("failed: offline", said(NavigationError.RouteFailed(IllegalStateException("offline"))))
        assertEquals("empty", said(NavigationError.EmptyRoute()))
    }
}
