package com.mountaya.conformance

import com.mountaya.client.Credential
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.SessionSnapshot
import com.mountaya.client.SessionToken
import com.mountaya.client.SessionTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

/**
 * `transport.errors.idempotency-key`: a call whose every attempt fails as the case says, answered
 * with the class of the error it raised and the key that error carries, written `drawn` where it is
 * the key the client drew: once held to be the key every attempt sent, or, where the call is refused
 * before its first attempt leaves, the key the client drew from the runner's random source for it.
 *
 * A refusal before the first attempt reads a session token for that attempt, under an operation that
 * requires one: `credential` from a function that answers a token of the wrong prefix, and
 * `first-token` from a source of the runner's own that reports a token on its way and never hands one.
 */
object TransportErrorsIdempotencyKeyRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val failure = input.objectOrNull("failure") ?: FixtureValue.Object(emptyList())
        val before = failure.textOrNull("before")
        val status = failure.numberOrNull("status")?.toInt()
        val reply =
            when {
                before != null -> Scripted.Reply.Answer(200, body = Scripted.emptySuccess)
                status == null -> Scripted.Reply.Drop()
                else -> Scripted.Reply.Answer(status, body = Scripted.errorBody(failure.textOrNull("code")))
            }
        val credential =
            when (before) {
                null -> Scripted.publishable
                "credential" -> Credential.Publishable("pk_test_abc", SessionToken.Function { "pk_test_abc" })
                "first-token" -> Credential.Publishable("pk_test_abc", SessionToken.Source(TokenOnItsWay))
                else -> throw AssertionError("no failure comes before an attempt as `$before`")
            }
        val declaration =
            OperationDeclaration(
                operationId = "writeThing",
                method = input.text("method"),
                idempotent = Scripted.flag(input, "idempotent"),
                credentials =
                    if (before ==
                        null
                    ) {
                        CredentialDeclaration()
                    } else {
                        CredentialDeclaration(requiresSession = true)
                    },
                declaresIdempotencyKey = Scripted.flag(input, "declaresKey"),
            )
        val given = input.textOrNull("idempotencyKey")
        val keys = (input["randomSource"] as? FixtureValue.Bool)?.value ?: true
        val random = Scripted.Random(keys = keys)
        val exchange = Scripted.Exchange(scope, emptyList(), exhausted = reply)
        val client = Scripted.client(scope, exchange, credential, retry = Scripted.twoQuickRetries, random = random)
        val error =
            try {
                client.send(
                    declaration,
                    "/v1/things",
                    JsonElement.serializer(),
                    JsonElement.serializer().nullable,
                    idempotencyKey = given,
                )
                throw AssertionError("the call raised nothing")
            } catch (raised: MountayaException) {
                raised
            }
        val fixed =
            if (before != null) {
                if (exchange.sent.isNotEmpty()) {
                    throw AssertionError(
                        "the call was refused before its first attempt, and sent one",
                    )
                }
                if (random.drawn.size >
                    (if (given == null) 1 else 0)
                ) {
                    throw AssertionError("the call drew ${random.drawn}")
                }
                given ?: random.drawn
                    .firstOrNull()
                    ?.toString()
                    ?.lowercase()
            } else {
                val sent = exchange.sent.map { it.request.header("Idempotency-Key") }.toSet()
                if (sent.size != 1) throw AssertionError("the attempts sent the keys $sent")
                sent.single()
            }
        val key = error.idempotencyKey
        if (key != fixed) throw AssertionError("the error carries $key, and the call fixed $fixed")
        val carried = if (key == null) FixtureValue.Null else FixtureValue.Text(if (key == given) key else "drawn")
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "class" to FixtureValue.Text(error.name),
                "idempotencyKey" to carried,
            ),
        )
    }

    /** A session source of the application's own that reports a token on its way and never hands one. */
    private object TokenOnItsWay : SessionTokenSource {
        override val snapshot: StateFlow<SessionSnapshot> = MutableStateFlow(SessionSnapshot(isMinting = true))
    }
}
