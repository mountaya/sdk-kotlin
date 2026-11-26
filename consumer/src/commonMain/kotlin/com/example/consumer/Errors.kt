package com.example.consumer

import com.mountaya.client.ApiException
import com.mountaya.client.ConfigurationException
import com.mountaya.client.ConflictException
import com.mountaya.client.ConnectionException
import com.mountaya.client.DataException
import com.mountaya.client.ErrorEntry
import com.mountaya.client.IdempotencyKeyInUseException
import com.mountaya.client.IdempotentAnswerUnavailableException
import com.mountaya.client.MountayaException
import com.mountaya.client.NamedException
import com.mountaya.client.NotFoundException
import com.mountaya.client.PreconditionFailedException
import com.mountaya.client.RateLimitException
import com.mountaya.client.SelectionException
import com.mountaya.client.ServerException
import com.mountaya.client.generated.ApiErrorCode
import com.mountaya.client.isApiException
import com.mountaya.client.isMountayaException
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * Errors reads every failure the client raises, its classes open to the ones a later contract
 * adds, and builds them as a consumer's test throws them.
 */
object Errors {
    /** Returns what [failure] says, read by its class. */
    fun said(failure: Throwable): String {
        val read =
            when (failure) {
                is IdempotencyKeyInUseException -> {
                    "key in use: ${failure.idempotencyKey}"
                }

                is IdempotentAnswerUnavailableException -> {
                    val answer = failure.answer
                    "${answer.status} ${answer.location} ${answer.etag}"
                }

                is ConflictException -> {
                    "conflict: ${failure.code}"
                }

                is PreconditionFailedException -> {
                    "stale, now ${failure.etag}"
                }

                is RateLimitException -> {
                    "${failure.retryAfter} ${failure.rateLimit?.policies}"
                }

                is DataException -> {
                    failure.errors.joinToString { "${it.message} ${it.code} ${it.path}" }
                }

                is ApiException -> {
                    "${failure.name} ${failure.status} ${failure.code} ${failure.metadata} ${failure.requestId}"
                }

                is ConnectionException -> {
                    "never arrived: ${failure.cause}"
                }

                is ConfigurationException -> {
                    "refused: ${failure.message}"
                }

                is SelectionException -> {
                    "outside the tree: ${failure.path}"
                }

                is MountayaException -> {
                    failure.name
                }

                is CancellationException -> {
                    throw failure
                }

                else -> {
                    "$failure"
                }
            }
        return "$read ${isMountayaException(failure)} ${isApiException(failure)}"
    }

    /** Rebuilt is a failure the app rebuilt from another module's, declaring the contract's name of its class. */
    class Rebuilt(
        override val name: String,
    ) : Exception(name),
        NamedException

    /** Answers whether a failure of the app's own class that declares a name of the family reads as one. */
    fun rebuilt(): Boolean = isMountayaException(Rebuilt("ConflictError")) && isApiException(Rebuilt("ConflictError"))

    /**
     * Answers whether [failure] was refused for the key's scope, whichever of its entries says so, the first or another,
     * and whether its first code is one no surface of this build declares, kept as the API sent it.
     */
    fun refusedForScope(failure: Throwable): Pair<Boolean, Boolean> =
        if (failure is ApiException) {
            failure.hasCode(ApiErrorCode.Forbidden) to (failure.code == ApiErrorCode("CONFLICT_PENDING"))
        } else {
            false to false
        }

    /** Returns every failure as a consumer's own test throws one. */
    fun thrown(): List<MountayaException> =
        listOf(
            NotFoundException(errors = listOf(ErrorEntry("Gone.", code = ApiErrorCode.NotFound)), requestId = "req_a"),
            ServerException(503, retryAfter = 2.seconds),
            ConflictException(errors = listOf(ErrorEntry("Taken."))),
            ApiException.of(429, retryAfter = 1.seconds),
            ConnectionException(cause = IllegalStateException("offline")),
            ConfigurationException("A key no header carries."),
        )
}
