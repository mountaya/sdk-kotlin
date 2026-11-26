package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/**
 * `transport.browser.cookie-mode`: whether a request under the case's credential to an operation
 * declaring the case's facts asks for the visitor's cookies, a share token that asks for them
 * carrying a cookie source. The projection a request makes is held to agree: cookies ride exactly
 * where the mode includes them, the request a read where the client's own rule says it is one, its
 * method or the operation's `read`.
 *
 * Its secret-key case builds `Credential.Secret` through the module's own constructor, so the
 * entry runs on every target (SPEC.md section 9).
 */
object TransportBrowserCookieModeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val parts = input.objectOrNull("credential") ?: FixtureValue.Object(emptyList())
        val share = parts.textOrNull("shareToken")
        val secret = parts.textOrNull("secretKey")
        val credential =
            when {
                share != null -> {
                    Credential.Share(share, if (Scripted.flag(parts, "cookie")) { _ -> "session=visitor" } else null)
                }

                secret != null -> {
                    Credential.Secret(secret)
                }

                else -> {
                    val minted = parts.objectOrNull("sessionToken")
                    val token =
                        if (minted != null) {
                            SessionToken.Minted(
                                RestSchema.Session(
                                    expires_at = Instant.fromEpochSeconds(0),
                                    token = minted.text("token"),
                                    user_id = minted.textOrNull("userId"),
                                ),
                            )
                        } else {
                            parts.textOrNull("sessionToken")?.let { SessionToken.Value(it) }
                        }
                    Credential.Publishable(parts.text("publishableKey"), token)
                }
            }
        val declaration =
            CredentialDeclaration(
                actingUser = actingUser(input.textOrNull("actingUser") ?: "none"),
                acceptsShareToken = Scripted.flag(input, "acceptsShareToken"),
                requiresSession = Scripted.flag(input, "requiresSession"),
                requiresSecretKey = Scripted.flag(input, "requiresSecretKey"),
                takesNoCredential = Scripted.flag(input, "takesNoCredential"),
                read = Scripted.flag(input, "read"),
            )
        val mode = CookieMode.mode(credential, declaration)
        val isRead = CredentialHeaders.isRead(input.text("method"), declaration)
        val session = (credential as? Credential.Publishable)?.sessionToken?.let { ResolvedSession.reading(it, null) }
        val projection = CredentialHeaders.project(credential, session, null, declaration, isRead)
        if ((projection.cookies != null) != (mode == CookieMode.Include)) {
            throw AssertionError("the projection and the mode disagree about cookies")
        }
        return ConformanceOutcome.Value(FixtureValue.Text(mode.word))
    }

    private fun actingUser(text: String): CredentialDeclaration.ActingUser =
        listOf(
            CredentialDeclaration.ActingUser.Required,
            CredentialDeclaration.ActingUser.Optional,
            CredentialDeclaration.ActingUser.None,
        ).firstOrNull { it.value == text } ?: throw AssertionError("no acting user is called `$text`")
}
