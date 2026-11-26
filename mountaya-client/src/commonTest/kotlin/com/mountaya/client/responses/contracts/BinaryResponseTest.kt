package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class BinaryResponseTest {
    @Test
    fun aFileIsComparedByItsBytesItsContentTypeAndWhatItsAnswerSaid() {
        assertEquals(BinaryResponse(byteArrayOf(1), "text/csv"), BinaryResponse(byteArrayOf(1), "text/csv"))
        assertEquals(
            BinaryResponse(byteArrayOf(1), "text/csv").hashCode(),
            BinaryResponse(byteArrayOf(1), "text/csv").hashCode(),
        )
        assertNotEquals(
            BinaryResponse(byteArrayOf(1), "text/csv"),
            BinaryResponse(byteArrayOf(1), "application/gpx+xml"),
        )
        assertNotEquals(
            BinaryResponse(byteArrayOf(1), "text/csv", requestId = "req_a"),
            BinaryResponse(byteArrayOf(1), "text/csv", requestId = "req_b"),
        )
        assertEquals(
            "BinaryResponse(1 bytes, contentType=text/csv, status=200, requestId=req_a)",
            BinaryResponse(byteArrayOf(1), "text/csv", requestId = "req_a").toString(),
        )
    }

    @Test
    fun aFileBuiltWithNoAnswerSaidArrivedWithA200AndNoRequestId() {
        val file = BinaryResponse(byteArrayOf(1), "application/gpx+xml")

        assertEquals(200, file.status)
        assertNull(file.requestId)
        assertEquals(206, BinaryResponse(byteArrayOf(1), "application/gpx+xml", status = 206).status)
    }
}
