package com.example.consumer

import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.Mountaya
import com.mountaya.client.RetryPolicy
import com.mountaya.client.SessionToken
import com.mountaya.client.SessionTokenEngine
import com.mountaya.client.SystemExchange
import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Clients builds a client the ways an app does: every credential shape, every option and every
 * seam, each from the public surface alone.
 */
object Clients {
    /** Returns a client as an app builds one: a publishable key and a session its own backend mints. */
    fun app(engine: SessionTokenEngine): Mountaya =
        Mountaya(
            credential = Credential.Publishable("pk_live_a", SessionToken.Source(engine)),
            language = "fr-CH, fr;q=0.9",
            baseUrls = BaseUrls(rest = Url("https://api.example.com")),
            retry = RetryPolicy(maxRetries = 1, baseDelay = 250.milliseconds, waitBudget = 10.seconds),
            timeout = 20.seconds,
            onResponse = { report ->
                seen(
                    report.operationId,
                    report.surface,
                    report.status,
                    report.attempt,
                    report.isReplayed,
                    report.requestId,
                )
                for (policy in report.rateLimit?.policies.orEmpty()) {
                    seen(policy.name, policy.quota, policy.window, policy.remaining, policy.reset)
                }
            },
        )

    /**
     * Returns a session engine in the app's own scope, over its own endpoint, which answers what `postSession` answers.
     */
    fun engine(scope: CoroutineScope): SessionTokenEngine {
        val engine =
            SessionTokenEngine(scope, mint = {
                RestSchema.Session(expires_at = Instant.fromEpochSeconds(0), user_id = "us_a", token = "sess_a")
            })
        engine.mint = { RestSchema.Session(expires_at = Instant.DISTANT_FUTURE, user_id = null, token = "sess_b") }
        engine.refresh()
        engine.disable()
        engine.enable()
        val snapshot = engine.snapshot.value
        seen(
            snapshot.token,
            snapshot.expiresAt,
            snapshot.userId,
            snapshot.isMinting,
            snapshot.error,
            engine.snapshot,
        )
        return engine
    }

    /** Returns every way a session token is given. */
    fun tokens(engine: SessionTokenEngine): List<SessionToken> =
        listOf(
            SessionToken.Value("sess_a"),
            SessionToken.Minted(
                RestSchema.Session(expires_at = Instant.DISTANT_FUTURE, user_id = "us_a", token = "sess_a"),
            ),
            SessionToken.Function { "sess_a" },
            SessionToken.Source(engine),
        )

    /** Returns a client under a share token, the visitor's own cookies riding its reads. */
    fun visitor(): Mountaya = Mountaya(Credential.Share("share_0a", cookies = { url -> "visitor=v1; at=$url" }))

    /**
     * Returns a credential an app assembles from its configuration and a link, the share token winning over the key.
     */
    fun fromParts(shareToken: String?): Credential =
        Credential(publishableKey = "pk_live_a", sessionToken = SessionToken.Value("sess_a"), shareToken = shareToken)

    /** Returns a client whose every seam is the consumer's own, as its tests build one. */
    fun scripted(): Mountaya =
        Mountaya(
            Credential.Publishable("pk_test_a"),
            exchange = Recorded(SystemExchange()),
            clock = Clock.System,
            random = Draws,
        )

    /** Returns what a credential sends for an operation, as a map's own request hook reads it, called by a read. */
    fun headers(credential: Credential): Map<String, String> =
        credential.headers(
            CredentialDeclaration(actingUser = CredentialDeclaration.ActingUser.Optional, acceptsShareToken = true),
            language = "fr",
            method = "GET",
        )
}
