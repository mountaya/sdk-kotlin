package com.mountaya.conformance

import com.mountaya.client.ConfigurationException
import com.mountaya.client.ConnectionException
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.RequestOptions
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration.Companion.milliseconds

/**
 * `transport.timeout.budget`: an attempt's budget measured as the virtual time from sending an
 * attempt nothing ever answers to the `ConnectionException` it settles as.
 *
 * The operation is called by its declaration, carrying the case's own budget as its `timeout`; the
 * client is built with the case's budget, or with none where the case leaves it out, so the
 * default is the client's own; and the call names its budget through `RequestOptions`. The
 * operation is neither idempotent nor keyed, so nothing repeats it. A budget of zero or less is
 * refused where it is given, as the client is built or as the call is made, before any request, and
 * the case is answered with the refusal's class.
 */
object TransportTimeoutBudgetRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val server = input.numberOrNull("serverBudgetMs")?.milliseconds
        val client = input.numberOrNull("clientTimeoutMs")?.milliseconds
        val call = input.numberOrNull("callTimeoutMs")?.milliseconds
        val exchange = Scripted.Exchange(scope, emptyList(), exhausted = Scripted.Reply.Never)
        val started = Scripted.elapsed(scope)
        try {
            val mountaya =
                if (client == null) {
                    Scripted.client(scope, exchange, retry = Scripted.noRetries)
                } else {
                    Scripted.client(scope, exchange, retry = Scripted.noRetries, timeout = client)
                }
            mountaya.send(
                OperationDeclaration("writeThing", "POST", timeout = server),
                "/v1/things",
                JsonElement.serializer(),
                JsonElement.serializer().nullable,
                options = RequestOptions(timeout = call),
            )
        } catch (expired: ConnectionException) {
            return ConformanceOutcome.Value(Scripted.milliseconds(Scripted.elapsed(scope) - started))
        } catch (refused: ConfigurationException) {
            // A budget that fits no attempt is refused where it is given, before any request.
            if (exchange.sent.isNotEmpty()) throw AssertionError("the budget was refused after a request left")
            return ConformanceOutcome.Refused(refused.name)
        }
        throw AssertionError("the attempt settled as something other than a connection error")
    }
}
