package com.mountaya.conformance

import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * `transport.conditional.if-match`: the case's write sent with its tag, after a read of the same
 * resource whose answer carries `seenEtag` where the case names one, answered with the `If-Match`
 * each attempt of the write sent and how it settled.
 */
object TransportConditionalIfMatchRunner : ConformanceRunner {
    private val codes = mapOf(404 to "NOT_FOUND", 412 to "PRECONDITION_FAILED", 503 to "SERVICE_UNAVAILABLE")

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val seen = input.textOrNull("seenEtag")
        val replies =
            buildList {
                if (seen != null) {
                    val body = """{"data":{"etag":${JsonPrimitive(seen)}}}""".encodeToByteArray()
                    add(Scripted.Reply.Answer(200, mapOf("ETag" to seen), body))
                }
                for (item in input.items("answers")) {
                    val status =
                        (item as? FixtureValue.Number)?.value?.toInt() ?: throw AssertionError("an answer is no status")
                    val body =
                        if (status in
                            200..299
                        ) {
                            Scripted.emptySuccess
                        } else {
                            Scripted.errorBody(codes[status] ?: "ERROR")
                        }
                    add(Scripted.Reply.Answer(status, body = body))
                }
            }
        val declaration =
            OperationDeclaration(
                operationId = "changeThing",
                method = input.text("method"),
                declaresIdempotencyKey = true,
                isConditional = Scripted.flag(input, "declaresIfMatch"),
            )
        val exchange = Scripted.Exchange(scope, replies)
        val client = Scripted.client(scope, exchange, retry = Scripted.twoQuickRetries)
        if (seen !=
            null
        ) {
            client.send(aRead, "/v1/things/th", JsonElement.serializer(), JsonElement.serializer().nullable)
        }
        val read = exchange.sent.size
        var outcome = "answered"
        try {
            client.send(
                declaration,
                "/v1/things/th",
                JsonElement.serializer(),
                JsonElement.serializer().nullable,
                ifMatch = input.textOrNull("ifMatch"),
            )
        } catch (error: MountayaException) {
            outcome = error.name
        }
        val tags = exchange.sent.drop(read).map { Scripted.text(it.request.header("If-Match")) }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "attempts" to Scripted.number(tags.size),
                "ifMatch" to FixtureValue.Array(tags),
                "outcome" to FixtureValue.Text(outcome),
            ),
        )
    }
}
