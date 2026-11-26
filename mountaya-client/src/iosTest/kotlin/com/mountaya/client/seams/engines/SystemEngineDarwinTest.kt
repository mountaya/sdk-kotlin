package com.mountaya.client

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import kotlinx.coroutines.CancellationException
import platform.Foundation.HTTPShouldHandleCookies
import platform.Foundation.NSError
import platform.Foundation.NSHTTPCookieAcceptPolicy
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorCancelled
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorTimedOut
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** iOS's engine: URLSession, configured to keep nothing, follow nothing and time nothing out. */
class SystemEngineDarwinTest {
    @Test
    fun theSessionKeepsNothingForARequestAndItsOwnTimeoutsStandOutOfTheWay() {
        val configuration = systemSessionConfiguration()

        assertNull(configuration.HTTPCookieStorage)
        assertEquals(NSHTTPCookieAcceptPolicy.NSHTTPCookieAcceptPolicyNever, configuration.HTTPCookieAcceptPolicy)
        assertFalse(configuration.HTTPShouldSetCookies)
        assertNull(configuration.URLCache)
        assertEquals(NSURLRequestReloadIgnoringLocalCacheData, configuration.requestCachePolicy)
        assertNull(configuration.URLCredentialStorage)
        assertFalse(configuration.waitsForConnectivity)
        assertEquals(idleAllowance, configuration.timeoutIntervalForRequest)
        assertEquals(idleAllowance, configuration.timeoutIntervalForResource)
        assertTrue(idleAllowance >= 7 * 24 * 60 * 60.0)
    }

    @Test
    fun anAttemptIsWrittenWithCookiesOffNoCacheAndThePlatformsTimeoutOutOfTheWay() {
        val url = NSURL(string = "http://127.0.0.1/v1/zones/zo")
        val request = NSMutableURLRequest(uRL = url)
        configured(request)

        assertFalse(request.HTTPShouldHandleCookies)
        assertEquals(NSURLRequestReloadIgnoringLocalCacheData, request.cachePolicy)
        assertEquals(idleAllowance, request.timeoutInterval)
    }

    @Test
    fun onlyATaskURLSessionCancelledIsReadAsACancellation() {
        val cancelled = DarwinHttpRequestException(NSError.errorWithDomain(NSURLErrorDomain, NSURLErrorCancelled, null))
        val timedOut = DarwinHttpRequestException(NSError.errorWithDomain(NSURLErrorDomain, NSURLErrorTimedOut, null))

        assertTrue(isPlatformCancellation(cancelled))
        assertTrue(SystemExchange.reading(cancelled) is CancellationException)
        assertFalse(isPlatformCancellation(timedOut))
        assertFalse(SystemExchange.reading(timedOut) is CancellationException)
    }
}
