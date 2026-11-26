package com.mountaya.conformance

import com.mountaya.client.ConfigurationException
import com.mountaya.client.Credential
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.RequestOptions
import com.mountaya.client.SessionSnapshot
import com.mountaya.client.SessionToken
import com.mountaya.client.SessionTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * `transport.session.first-token`: a request whose session token is read per request, from a
 * source of the runner's own that it moves through the case's states on the test's virtual time,
 * or from a function, answered with whether and when the request went out, under which token, or
 * how it was refused.
 *
 * The call repeats nothing, so a budget the wait spends is read as the one attempt it ends.
 */
object TransportSessionFirstTokenRunner : ConformanceRunner {
    /** MintFailure is the failure of a mint the case says failed. */
    private class MintFailure : Exception("the scripted mint failed")

    /** ScriptedSource is a session source of the application's own, which the runner moves from state to state. */
    private class ScriptedSource(
        initial: SessionSnapshot,
    ) : SessionTokenSource {
        val state = MutableStateFlow(initial)
        override val snapshot: StateFlow<SessionSnapshot> = state
    }

    private fun snapshot(state: FixtureValue.Object): SessionSnapshot =
        SessionSnapshot(
            token = state.textOrNull("token"),
            isMinting = Scripted.flag(state, "loading"),
            error = if (Scripted.flag(state, "failed")) MintFailure() else null,
        )

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val timeout = input.number("timeoutMs").milliseconds
        val declaration =
            OperationDeclaration(
                "readThing",
                "GET",
                credentials = CredentialDeclaration(requiresSession = Scripted.flag(input, "requiresSession")),
            )
        val source = ScriptedSource(snapshot(input.objectOrNull("initial") ?: FixtureValue.Object(emptyList())))
        val steps =
            (input["steps"] as? FixtureValue.Array)?.items.orEmpty().map { item ->
                val step = item as? FixtureValue.Object ?: throw AssertionError("a step is no object")
                step.number("atMs").milliseconds to snapshot(step)
            }
        val answers = input.textOrNull("answers")
        val token =
            when (val session = input.text("session")) {
                "source" -> SessionToken.Source(source)
                "function" -> SessionToken.Function { answers }
                else -> throw AssertionError("no session is called `$session`")
            }
        val exchange = Scripted.Exchange(scope, listOf(Scripted.Reply.Answer(200, body = Scripted.emptySuccess)))
        val client = Scripted.client(scope, exchange, Credential.Publishable("pk_test_abc", token), Scripted.noRetries)
        val started = Scripted.elapsed(scope)
        val moving =
            scope.backgroundScope.launch {
                for ((at, state) in steps) {
                    delay(started + at - Scripted.elapsed(scope))
                    source.state.value = state
                }
            }
        var refusal: MountayaException? = null
        var refusedAt: Duration? = null
        try {
            client.send(
                declaration,
                "/v1/things",
                JsonElement.serializer(),
                JsonElement.serializer().nullable,
                options = RequestOptions(timeout = timeout),
            )
        } catch (error: MountayaException) {
            refusal = error
            refusedAt = Scripted.elapsed(scope) - started
        }
        moving.cancel()
        val sent = exchange.sent.firstOrNull()
        val elapsed = sent?.let { it.at - started } ?: refusedAt ?: Duration.ZERO
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "causedByMint" to FixtureValue.Bool((refusal as? ConfigurationException)?.cause is MintFailure),
                "elapsedMs" to Scripted.milliseconds(elapsed),
                "refused" to Scripted.text(refusal?.name),
                "sent" to FixtureValue.Bool(sent != null),
                "sessionToken" to Scripted.text(sent?.request?.header("X-Session-Token")),
            ),
        )
    }
}
