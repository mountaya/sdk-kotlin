package com.mountaya.conformance

import com.mountaya.client.ApiException
import com.mountaya.client.Credential
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.CredentialDeclaration.ActingUser
import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.generated.ApiErrorCode
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlin.time.DurationUnit
import kotlin.time.Instant

/**
 * `transport.errors.envelope`: what the error a failed answer raises carries, read off a scripted
 * answer of the case's status, headers and body by a call that repeats nothing.
 *
 * A case about the Data API calls the printed `directions`; every other case calls a read by its
 * declaration, declaring what the case's `declares` says of its credentials, sent under a secret
 * key where a secret key authenticates it and under a publishable key and a session token
 * otherwise, and answered at the case's `answeredAtMs`, which the client's wall clock reads.
 * `nextStep` is what the error's message carries after the API's own words, the status, the first
 * entry's code and its message: a message that does not begin with them fails the case.
 *
 * The cases of `postSession`'s refusals go through the factory that takes a secret key, which the
 * jvm alone declares, so this runner sits in the jvm tests and the entry runs on the jvm alone,
 * whole, as `transport.credentials` does (SPEC.md section 9).
 */
object TransportErrorsEnvelopeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val status = input.number("status").toInt()
        val body = input.entries.filter { it.first == "errors" || it.first == "extensions" }
        val bytes = if (body.isEmpty()) ByteArray(0) else Scripted.json(FixtureValue.Object(body))
        val surface = input.textOrNull("surface")
        if (surface != null && surface != "data") throw AssertionError("no surface is called `$surface`")
        val declares = input.objectOrNull("declares") ?: FixtureValue.Object(emptyList())
        for (key in declares.keys) {
            if (key != "actingUser" &&
                key != "requiresSecretKey"
            ) {
                throw AssertionError("no declaration is called `$key`")
            }
        }
        val requiresSecretKey = Scripted.flag(declares, "requiresSecretKey")
        val start = input.numberOrNull("answeredAtMs")?.let { Instant.fromEpochMilliseconds(it.toLong()) }
        val clock = if (start == null) Scripted.clock(scope) else Scripted.clock(scope, start)
        val exchange = Scripted.Exchange(scope, listOf(Scripted.Reply.Answer(status, Scripted.headers(input), bytes)))
        val error =
            try {
                if (surface == "data") {
                    Scripted
                        .client(scope, exchange, Scripted.session, Scripted.noRetries, clock = clock)
                        .data
                        .directions(directionsInput)
                } else {
                    val credential = if (requiresSecretKey) Credential(secretKey = "sk_test_abc") else Scripted.session
                    Scripted.client(scope, exchange, credential, Scripted.noRetries, clock = clock).send(
                        OperationDeclaration(
                            "readThing",
                            "GET",
                            idempotent = true,
                            credentials =
                                CredentialDeclaration(
                                    actingUser = actingUser(declares.textOrNull("actingUser") ?: "none"),
                                    requiresSecretKey = requiresSecretKey,
                                ),
                        ),
                        "/v1/things",
                        JsonElement.serializer(),
                        JsonElement.serializer().nullable,
                    )
                }
                throw AssertionError("the answer raised nothing")
            } catch (raised: MountayaException) {
                raised
            }
        val api =
            error as? ApiException
                ?: throw AssertionError("the answer raised ${error.name}, which the API did not answer")
        // Each code the case asks of the error, answered by whether any of its entries carries it.
        val asked =
            input["hasCode"]?.let {
                FixtureValue.Object(
                    input.items("hasCode").map { code ->
                        val written =
                            (code as? FixtureValue.Text)?.value ?: throw AssertionError("a code that is not text")
                        written to FixtureValue.Bool(api.hasCode(ApiErrorCode(written)))
                    },
                )
            }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "class" to FixtureValue.Text(api.name),
                "code" to Scripted.text(api.code?.value),
                "errors" to FixtureValue.Array(api.errors.map(::entryValue)),
                *listOfNotNull(asked?.let { "hasCode" to it }).toTypedArray(),
                "metadata" to (api.metadata?.let(::fixtureValueOf) ?: FixtureValue.Null),
                "nextStep" to Scripted.text(nextStep(api)),
                "rateLimit" to rateLimitValue(api.rateLimit),
                "requestId" to Scripted.text(api.requestId),
                "retryAfter" to
                    (
                        api.retryAfter?.let {
                            FixtureValue.Number(
                                it.toDouble(DurationUnit.SECONDS),
                            )
                        } ?: FixtureValue.Null
                    ),
                "status" to Scripted.number(api.status),
            ),
        )
    }

    private fun actingUser(spelled: String): ActingUser =
        when (spelled) {
            "required" -> ActingUser.Required
            "optional" -> ActingUser.Optional
            "none" -> ActingUser.None
            else -> throw AssertionError("no acting user is called `$spelled`")
        }

    /** The sentence [error]'s message carries after the API's own words, or null where it carries none. */
    private fun nextStep(error: ApiException): String? {
        val said =
            buildString {
                append(error.status)
                val first = error.errors.firstOrNull()
                first?.code?.let { append(" ").append(it.value) }
                if (first != null && first.message.isNotEmpty()) append(": ").append(first.message)
            }
        val message = error.message
        if (!message.startsWith(said)) {
            throw AssertionError("the message `$message` does not begin with the API's words `$said`")
        }
        val rest = message.substring(said.length)
        if (rest.isEmpty()) return null
        for (separator in listOf(". ", " ")) {
            if (rest.startsWith(separator)) return rest.substring(separator.length)
        }
        throw AssertionError("the message `$message` runs on past the API's words with `$rest`")
    }
}
