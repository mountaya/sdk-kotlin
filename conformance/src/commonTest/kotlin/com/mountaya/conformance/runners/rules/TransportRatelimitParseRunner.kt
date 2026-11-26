package com.mountaya.conformance

import com.mountaya.client.ApiException
import com.mountaya.client.MountayaException
import com.mountaya.client.RateLimitState
import com.mountaya.client.ResponseReport
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

/**
 * `transport.ratelimit.parse`: the state the case's headers describe where the case asks for it,
 * off the `onResponse` hook for a 200, or off the error an answer of the case's status raises,
 * which the hook must have been told alike.
 */
object TransportRatelimitParseRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val ask = input.text("ask")
        val status =
            when (ask) {
                "response" -> 200
                "error" -> input.number("status").toInt()
                else -> throw AssertionError("no ask is called `$ask`")
            }
        val body = if (status == 200) Scripted.emptySuccess else Scripted.errorBody("TOO_MANY_REQUESTS")
        val exchange = Scripted.Exchange(scope, listOf(Scripted.Reply.Answer(status, Scripted.headers(input), body)))
        val reports = mutableListOf<ResponseReport>()
        val client = Scripted.client(scope, exchange, retry = Scripted.noRetries, reports = reports)
        val state: RateLimitState? =
            try {
                client.send(aRead, "/v1/things", JsonElement.serializer(), JsonElement.serializer().nullable)
                if (ask != "response") throw AssertionError("the answer raised nothing")
                (reports.firstOrNull() ?: throw AssertionError("the answer was reported to no hook")).rateLimit
            } catch (error: MountayaException) {
                val api = error as? ApiException
                if (ask != "error" || api == null) throw AssertionError("the call raised ${error.name}")
                if (reports.firstOrNull()?.rateLimit != api.rateLimit) {
                    throw AssertionError("the hook and the error were told different states")
                }
                api.rateLimit
            }
        return ConformanceOutcome.Value(rateLimitValue(state))
    }
}
