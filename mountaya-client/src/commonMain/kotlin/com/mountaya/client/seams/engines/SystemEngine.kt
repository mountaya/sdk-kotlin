package com.mountaya.client

import io.ktor.client.engine.HttpClientEngine

/**
 * Returns a new engine of the platform's own HTTP stack, built with the rules of the platform's
 * exchange: OkHttp on the jvm and android, URLSession on iOS.
 *
 * Every engine keeps no cookie, no cached answer and no credential, follows no redirect, and has
 * its own timeouts set out of the way, so the client's budgets are the only ones an attempt has.
 * Each call builds one of its own, so a test's engine never shares a connection, or a close, with
 * the one the process shares.
 */
internal expect fun systemEngine(): HttpClientEngine

/**
 * Returns whether [failure] is the platform reporting a request it cancelled, which the exchange
 * throws as the coroutine's own cancellation rather than as a request that never arrived.
 */
internal expect fun isPlatformCancellation(failure: Throwable): Boolean
