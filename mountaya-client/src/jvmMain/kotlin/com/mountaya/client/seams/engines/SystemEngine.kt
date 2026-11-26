package com.mountaya.client

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CookieJar
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** OkHttp, through Ktor's engine for it, over a client of its own that [systemOkHttpClient] builds. */
internal actual fun systemEngine(): HttpClientEngine = OkHttp.create { preconfigured = systemOkHttpClient() }

/** OkHttp reports a call it cancelled as an `IOException` it names `Canceled`, wherever it sits in the chain of causes. */
internal actual fun isPlatformCancellation(failure: Throwable): Boolean =
    generateSequence(failure) { it.cause }.take(CAUSES_READ).any { it is IOException && it.message == CANCELED }

/**
 * Returns a new OkHttp client with the exchange's rules written over OkHttp's own defaults.
 *
 * It keeps no cookie, caches no answer and follows no redirect, a `3xx` answered as it arrived.
 * OkHttp's three ten-second timeouts are turned off, since a read timeout would end a stream between
 * two of its heartbeats and an attempt the API is still running: the client's budgets are the only
 * ones an attempt has. Its dispatcher runs as many calls to one host as to all of them, where
 * OkHttp's runs five, so an open event stream never queues a request behind it and a queued request
 * never spends its budget waiting for a slot. Its threads are daemon threads, so a process whose
 * work is done is never kept alive by the minute an idle one waits for its next call.
 *
 * Its recovery from a dropped connection is OkHttp's own, a request sent again on another route or
 * a fresh connection, which repeats no effect: every operation the contract declares is idempotent
 * or carries an `Idempotency-Key`, which a request sent again carries too.
 */
internal fun systemOkHttpClient(): OkHttpClient =
    OkHttpClient
        .Builder()
        .dispatcher(Dispatcher(daemonThreads()).apply { maxRequestsPerHost = maxRequests })
        .cookieJar(CookieJar.NO_COOKIES)
        .cache(null)
        .followRedirects(false)
        .followSslRedirects(false)
        .connectTimeout(0, TimeUnit.MILLISECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .build()

/** An executor of daemon threads, as many as calls run at once, each let go after a minute idle. */
private fun daemonThreads(): ThreadPoolExecutor =
    ThreadPoolExecutor(0, Int.MAX_VALUE, IDLE_SECONDS, TimeUnit.SECONDS, SynchronousQueue()) { task ->
        Thread(task, THREAD_NAME).apply { isDaemon = true }
    }

/** The message OkHttp gives the failure of a call it cancelled. */
private const val CANCELED = "Canceled"

/** How deep a chain of causes is read, so a chain that loops is never read for ever. */
private const val CAUSES_READ = 16

/** How long an idle dispatcher thread waits for its next call before it ends, OkHttp's own minute. */
private const val IDLE_SECONDS = 60L

/** The name every dispatcher thread carries, so a thread dump says whose it is. */
private const val THREAD_NAME = "Mountaya OkHttp Dispatcher"
