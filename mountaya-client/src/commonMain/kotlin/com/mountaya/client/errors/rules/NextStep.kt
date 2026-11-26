package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * NextStep names what a caller can do about a failure the API answered, for the statuses that name
 * one: an error's message ends on it, after the API's own words.
 *
 * A 401, 402, 403, 412 and 429 each have a remedy on the caller's side, and so do the 409 coded
 * `IDEMPOTENCY_ANSWER_UNAVAILABLE`, whose write was made, and the one coded `IDEMPOTENCY_KEY_IN_USE`,
 * whose first request is still running; a message that stops at the status leaves the reader of a log
 * to work it out. Every other status says nothing a caller can act on beyond the API's own message: a
 * 400 or another 409 is about this request, which the entries describe, and a 5xx is the API's. A 429
 * coded `LIMIT_REACHED` is a ceiling, which no wait lifts, so its step names what does rather than a
 * wait, and a 429 refused for a spent monthly allowance names when the allowance renews.
 *
 * What the operation declares sharpens the step: a call that acts as a member is told which user its
 * session token has to name, and `postSession`, the one operation a secret key authenticates, is told
 * about the secret key, the publishable key and the user it mints for. Every text is the one
 * `sdk-typescript` and `sdk-swift` write, byte for byte.
 */
internal object NextStep {
    /**
     * Declaration is what a failed call's operation declares about the credentials it takes, as far as
     * the step its refusal names turns on it.
     */
    class Declaration(
        /** Whether the operation acts as the user its session token names (`x-acting-user: required`). */
        val actsAsMember: Boolean,
        /** Whether a secret key authenticates the operation, as it does `postSession` alone. */
        val requiresSecretKey: Boolean,
    ) {
        /** Creates the declaration an operation's credential facts make. */
        constructor(credentials: CredentialDeclaration) : this(
            actsAsMember = credentials.actingUser == CredentialDeclaration.ActingUser.Required,
            requiresSecretKey = credentials.requiresSecretKey,
        )

        override fun equals(other: Any?): Boolean =
            other is Declaration && other.actsAsMember == actsAsMember && other.requiresSecretKey == requiresSecretKey

        override fun hashCode(): Int = actsAsMember.hashCode() * 31 + requiresSecretKey.hashCode()

        override fun toString(): String =
            "Declaration(actsAsMember=$actsAsMember, requiresSecretKey=$requiresSecretKey)"

        companion object {
            /** The declaration of an operation that sharpens no step, which an error a caller builds carries. */
            val none: Declaration = Declaration(actsAsMember = false, requiresSecretKey = false)
        }
    }

    /**
     * What a 402 tells a caller: where each of its remedies is taken, with the API's message naming which
     * one applies, since the status alone does not say which fact it refused for.
     */
    val billing: String =
        "The API's message says which applies: a product of the organization's agreement is changed with " +
            "Mountaya (https://mountaya.com/en/contact/), and a missing payment method is added, or an invoice " +
            "still owed paid, in the organization's billing settings."

    /** What a 429 coded `LIMIT_REACHED` asks a caller to do: no wait lifts a ceiling, so the step names the two things that do. */
    val ceiling: String =
        "No wait lifts a ceiling, so this call is not repeated: free a place in what the error's metadata names, " +
            "or change the organization's agreement with Mountaya (https://mountaya.com/en/contact/) where it " +
            "sets that ceiling."

    /** What a 401 asks a caller to check, whatever the call. */
    val credential: String =
        "Check the credential this client sends: a key that is unknown, rotated or disabled, and a session token " +
            "that has expired, are refused."

    /**
     * What a 401 adds for a call that acts as a member: its session token has to name the user it acts as,
     * which a token minted without `user_id` does not.
     */
    val memberSession: String =
        "This call acts as the user its session token was minted for, so mint the token with postSession's " +
            "user_id set to that user."

    /** What a 401 on `postSession` asks a caller to check: the secret key, which it alone takes. */
    val secretKey: String =
        "Check the secret key this client sends: one that is unknown, revoked or malformed, or one of a canceled " +
            "organization, is refused."

    /** What a 403 asks a caller to check, whatever the call. */
    val forbidden: String =
        "Check that the key carries the scope this call needs, that the resource belongs to the key's " +
            "organization, and that the page calling is one the organization allows."

    /**
     * What a 403 adds for a call that acts as a member: the user its session token names has to be a member
     * of the key's organization holding the role the call asks.
     */
    val actingUser: String =
        "This call acts as the user its session token names (postSession's user_id), who must be a member of the " +
            "key's organization holding the role the call asks."

    /** What a 403 on `postSession` asks a caller to check: the publishable key it names, and the user it mints for. */
    val mintRefusal: String =
        "Check that the publishable key in the body is one this secret key's organization issued, and that " +
            "user_id names a member of that organization or a user holding an invitation to it."

    /** What a 412 asks a caller to do: the tag the write named is stale, so the write is not repeated as it is. */
    val precondition: String =
        "The resource changed since the etag this write names was read: read it again, and decide what the write " +
            "should be now."

    /** What the 409 coded `IDEMPOTENCY_ANSWER_UNAVAILABLE` asks a caller to do: the write was made, so it is read again. */
    val idempotentAnswer: String =
        "The write was made, and its answer cannot be given again: read the resource again rather than sending the " +
            "write once more."

    /**
     * What the 409 coded `IDEMPOTENCY_KEY_IN_USE` asks a caller to do once the client stopped asking: the
     * first request with the key is still running, so the call is sent again with the same key.
     */
    val keyInUse: String =
        "The first request with this Idempotency-Key is still running: send the same call again with the error's " +
            "idempotencyKey, never a new key, which would make the write twice."

    /** What a 429 on `postSession` adds: the limits it keeps, the last of which a `user_id` the API refuses runs into. */
    val mintLimit: String =
        "Minting is limited per secret key, per client IP address and secret key whose requests keep failing, and " +
            "per user_id it refuses, so check the publishable key and the user_id before asking again."

    /**
     * Returns what a caller can do about a failure of [status], its first entry coded [code], or null where
     * the status names nothing to do. [retryAfter] is the wait the answer named, counted from
     * [answeredAt], and [rateLimit] the state it described.
     */
    fun step(
        status: Int,
        code: ApiErrorCode?,
        retryAfter: Duration?,
        rateLimit: RateLimitState?,
        declaration: Declaration,
        answeredAt: Instant?,
    ): String? {
        val mints = declaration.requiresSecretKey
        val actsAsMember = declaration.actsAsMember
        return when (status) {
            401 -> {
                when {
                    mints -> secretKey
                    actsAsMember -> "$credential $memberSession"
                    else -> credential
                }
            }

            402 -> {
                billing
            }

            403 -> {
                when {
                    mints -> mintRefusal
                    actsAsMember -> "$forbidden $actingUser"
                    else -> forbidden
                }
            }

            409 -> {
                when (code) {
                    ApiErrorCode.IdempotencyAnswerUnavailable -> idempotentAnswer
                    ApiErrorCode.IdempotencyKeyInUse -> keyInUse
                    else -> null
                }
            }

            412 -> {
                precondition
            }

            429 -> {
                when {
                    code == ApiErrorCode.LimitReached -> {
                        ceiling
                    }

                    MonthlyAllowance.isSpent(rateLimit) -> {
                        MonthlyAllowance.step(retryAfter, answeredAt)
                    }

                    else -> {
                        val wait = waitStep(retryAfter)
                        if (mints) "$wait $mintLimit" else wait
                    }
                }
            }

            else -> {
                null
            }
        }
    }

    /**
     * Returns what a 429 asks a caller to do where a wait lifts it: wait the whole seconds the answer
     * named, or, where it named none or none at all, wait without a number, since a wait of none is still
     * a wait for the limit's window.
     */
    fun waitStep(retryAfter: Duration?): String {
        val seconds = retryAfter?.inWholeSeconds ?: 0
        return if (seconds <= 0) "Wait before calling again." else "Wait ${seconds(seconds)} before calling again."
    }

    /** Returns a whole count of seconds, in the singular for one: `1 second`, `23 seconds`. */
    fun seconds(count: Long): String = "$count ${if (count == 1L) "second" else "seconds"}"
}
