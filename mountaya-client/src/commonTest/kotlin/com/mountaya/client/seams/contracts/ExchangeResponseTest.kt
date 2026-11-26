package com.mountaya.client

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExchangeResponseTest {
    @Test
    fun anAnswerIsItsStatusItsHeadersReadInAnyCaseAndItsBody() =
        runTest {
            val answer = ExchangeResponse(200, mapOf("x-request-id" to "4bf9"), ExchangeBody("{}".encodeToByteArray()))

            assertEquals("4bf9", answer.header("X-Request-Id"))
            assertNull(answer.header("ETag"))
            assertEquals("{}", answer.body.readAll().decodeToString())
            assertEquals("ExchangeResponse(200)", answer.toString())
        }

    @Test
    fun anAnswerWithNoBodyReadsEmpty() =
        runTest {
            assertEquals(0, ExchangeResponse(204).body.readAll().size)
        }
}
