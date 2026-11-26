package com.mountaya.client

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpExchangeTest {
    @Test
    fun anExchangeOfTheCallersOwnAnswersEachAttemptItIsSent() =
        runTest {
            val sent = mutableListOf<ExchangeRequest>()
            val exchange =
                object : HttpExchange {
                    override suspend fun send(request: ExchangeRequest): ExchangeResponse {
                        sent += request
                        return ExchangeResponse(204)
                    }
                }
            val answer = exchange.send(ExchangeRequest("GET", Url("https://api.mountaya.com/v1/things")))

            assertEquals(204, answer.status)
            assertEquals(listOf("GET"), sent.map { it.method })
        }
}
