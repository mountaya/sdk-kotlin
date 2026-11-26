package com.example.consumer.readme

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Instant

/** Stands for an app's own scope, the one the README's and the skill's snippets name without declaring. */
internal val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob())

/**
 * Stands for an app's own endpoint, the one the README's and the skill's snippets name without declaring:
 * it mints a session token with the secret key the app never holds, and answers what `postSession` answers.
 */
internal suspend fun fetchSessionFromYourBackend(): RestSchema.Session =
    RestSchema.Session(expires_at = Instant.fromEpochSeconds(0), user_id = null, token = "sess_2c3d")
