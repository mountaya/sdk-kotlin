package com.mountaya.conformance

import com.mountaya.client.Credential
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.CredentialDeclaration.ActingUser
import com.mountaya.client.MountayaException
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/**
 * `transport.credentials`: the credential is built from its parts through the public factory,
 * which is where a share token replaces a key, and the case is answered with the headers it and the
 * case's language send to an operation of the case's method declaring the case's facts, or with the
 * class it is refused with. A share token whose case asks for the visitor's cookies is built with a
 * cookie source, which `Credential.Share` alone takes: the factory builds a share token that asks
 * for none.
 *
 * A session token written as a string is a [SessionToken.Value], whose user the client is not told.
 * One written as an object is a source reporting the user its token was minted for, which is a
 * [SessionToken.Minted] holding what `postSession` answered; the fixture names no expiry, and the
 * projection reads none, so the runner gives it the epoch.
 *
 * A case naming a `stream` makes the stream's open in place of a request. The open is a request to
 * the stream's operation and goes through the same projection, so the runner projects the facts the
 * case declares, holding them to what every stream declares; the `Accept` that asks for the stream
 * is the stream's own and is not among what the fixture compares.
 *
 * Every case goes through the factory that takes a secret key, which the jvm alone declares and
 * which is the factory that takes none where the case names no secret key. That is why this runner
 * sits in the jvm tests and the entry runs on the jvm alone, whole: what a secret-key case would
 * meet on Android and iOS is a missing symbol, which the ABI dumps of those targets hold
 * (SPEC.md section 9).
 */
object TransportCredentialsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val parts = input.objectOrNull("credential") ?: FixtureValue.Object(emptyList())
        val declaration =
            CredentialDeclaration(
                actingUser = actingUser(input.textOrNull("actingUser") ?: "none"),
                acceptsShareToken = input.flag("acceptsShareToken"),
                requiresSession = input.flag("requiresSession"),
                requiresSecretKey = input.flag("requiresSecretKey"),
                takesNoCredential = input.flag("takesNoCredential"),
                read = input.flag("read"),
            )
        val stream = input.textOrNull("stream")
        if (stream != null) {
            if (stream !in setOf("agent", "collaboration")) throw AssertionError("no stream is called `$stream`")
            if (!declaration.requiresSession || declaration.actingUser != ActingUser.Required) {
                throw AssertionError("a stream's open declares less than every stream does")
            }
        }

        val answer =
            try {
                val credential =
                    if (parts.flag("cookie")) {
                        Credential.Share(parts.text("shareToken")) { "visitor=7" }
                    } else {
                        Credential(
                            publishableKey = parts.textOrNull("publishableKey"),
                            sessionToken = sessionToken(parts["sessionToken"]),
                            shareToken = parts.textOrNull("shareToken"),
                            secretKey = parts.textOrNull("secretKey"),
                        )
                    }
                val sent = credential.headers(declaration, input.textOrNull("language"), input.text("method"))
                val headers = sent.entries.sortedBy { it.key }.map { (name, value) -> name to FixtureValue.Text(value) }
                listOf("headers" to FixtureValue.Object(headers), "refused" to FixtureValue.Null)
            } catch (refusal: MountayaException) {
                listOf("headers" to FixtureValue.Null, "refused" to FixtureValue.Text(refusal.name))
            }
        return ConformanceOutcome.Value(FixtureValue.Object(answer))
    }

    private fun actingUser(spelled: String): ActingUser =
        when (spelled) {
            "required" -> ActingUser.Required
            "optional" -> ActingUser.Optional
            "none" -> ActingUser.None
            else -> throw AssertionError("no acting user is called `$spelled`")
        }

    /** A case's session token: a string, or an object holding the `token` a source holds and the `userId` it reports. */
    private fun sessionToken(written: FixtureValue?): SessionToken? =
        when (written) {
            null, FixtureValue.Null -> {
                null
            }

            is FixtureValue.Text -> {
                SessionToken.Value(written.value)
            }

            is FixtureValue.Object -> {
                if (written["userId"] == null) throw AssertionError("a source's session token names its `userId`")
                SessionToken.Minted(
                    RestSchema.Session(
                        expires_at = Instant.fromEpochMilliseconds(0),
                        token = written.text("token"),
                        user_id = written.textOrNull("userId"),
                    ),
                )
            }

            else -> {
                throw AssertionError("a session token is a string or a source's object")
            }
        }

    /** The fact at [key], false where the case leaves it out, as the fixture's description says. */
    private fun FixtureValue.Object.flag(key: String): Boolean = if (this[key] == null) false else bool(key)
}
