package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ResponseReportTest {
    @Test
    fun aReportIsWhatItsAnswerSaidEachPartLeftOutBeingNone() {
        val report = ResponseReport("getThing", Surface.Rest, 200, 0)

        assertFalse(report.isReplayed)
        assertNull(report.requestId)
        assertNull(report.rateLimit)
        assertEquals(ResponseReport("getThing", Surface.Rest, 200, 0), report)
        assertNotEquals(ResponseReport("getThing", Surface.Rest, 200, 1), report)
    }
}
