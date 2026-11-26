package com.mountaya.client

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.engine.darwin.KtorNSURLSessionDelegate
import platform.Foundation.NSHTTPCookieAcceptPolicy
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURLErrorCancelled
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.setHTTPShouldHandleCookies
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

/**
 * URLSession, through Ktor's Darwin engine, over a session of its own that
 * [systemSessionConfiguration] configures. Ktor's delegate follows no redirect, and every request
 * is written by [configured].
 */
internal actual fun systemEngine(): HttpClientEngine {
    val delegate = KtorNSURLSessionDelegate()
    val session = NSURLSession.sessionWithConfiguration(systemSessionConfiguration(), delegate, delegateQueue = null)
    return Darwin.create {
        usePreconfiguredSession(session, delegate)
        configureRequest { configured(this) }
    }
}

/** URLSession reports a task it cancelled as `NSURLErrorCancelled`, which Ktor carries in its own failure. */
internal actual fun isPlatformCancellation(failure: Throwable): Boolean =
    generateSequence(failure) { it.cause }.take(CAUSES_READ).any {
        it is DarwinHttpRequestException && it.origin.domain == NSURLErrorDomain &&
            it.origin.code == NSURLErrorCancelled
    }

/**
 * How long URLSession lets an attempt sit idle, or last, before it gives up on its own: a week,
 * so that every budget the client holds runs out first.
 */
internal val idleAllowance: Double = 7.days.toDouble(DurationUnit.SECONDS)

/**
 * Returns an ephemeral session configuration with the exchange's rules written over it: no cookie
 * storage and cookies off, no cache, no stored HTTP credential, no wait for connectivity the client's
 * own retries do not decide, and the platform's timeouts set out of the way.
 */
internal fun systemSessionConfiguration(): NSURLSessionConfiguration =
    NSURLSessionConfiguration.ephemeralSessionConfiguration().apply {
        HTTPCookieStorage = null
        HTTPCookieAcceptPolicy = NSHTTPCookieAcceptPolicy.NSHTTPCookieAcceptPolicyNever
        HTTPShouldSetCookies = false
        URLCache = null
        requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData
        URLCredentialStorage = null
        waitsForConnectivity = false
        timeoutIntervalForRequest = idleAllowance
        timeoutIntervalForResource = idleAllowance
    }

/** Writes the exchange's rules over one request: cookie handling off, no cache, and the platform's timeout out of the way. */
internal fun configured(request: NSMutableURLRequest) {
    request.setHTTPShouldHandleCookies(false)
    request.setCachePolicy(NSURLRequestReloadIgnoringLocalCacheData)
    request.setTimeoutInterval(idleAllowance)
}

/** How deep a chain of causes is read, so a chain that loops is never read for ever. */
private const val CAUSES_READ = 16
