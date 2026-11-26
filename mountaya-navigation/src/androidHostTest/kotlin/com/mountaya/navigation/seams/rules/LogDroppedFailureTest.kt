package com.mountaya.navigation

import android.content.pm.ApplicationInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogDroppedFailureTest {
    /** A failure no onError was given for is logged where the app is debuggable alone, so a release build is silent. */
    @Test
    fun aDebuggableAppAloneLogsADroppedFailure() {
        assertTrue(logsDroppedFailures(ApplicationInfo.FLAG_DEBUGGABLE))
        assertTrue(logsDroppedFailures(ApplicationInfo.FLAG_DEBUGGABLE or ApplicationInfo.FLAG_HAS_CODE))
        assertFalse(logsDroppedFailures(0))
        assertFalse(logsDroppedFailures(ApplicationInfo.FLAG_HAS_CODE))
    }

    /** The line names the seam that dropped it and why, then the failure's own message, which ends on what to do. */
    @Test
    fun theLineNamesTheSeamAndCarriesTheFailuresMessage() {
        val failure = LocationException(LocationException.Kind.ProviderDisabled)

        val line = droppedFailureLine("DeviceLocationSource", failure)

        assertTrue(line.startsWith("DeviceLocationSource was made with no onError"), line)
        assertTrue(line.endsWith(failure.message.orEmpty()), line)
        assertEquals("MountayaNavigation", DROPPED_FAILURE_TAG)
    }
}
