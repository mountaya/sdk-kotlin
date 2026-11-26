package com.mountaya.client

/**
 * MountayaException is every failure the client reports, one subclass per class of the
 * contract's error ladder.
 *
 * Catching [ApiException] catches every status, which separates a failure of the API from a bug
 * in the calling program. A cancelled call is never one of these: it throws the coroutine's own
 * `CancellationException`, which is how structured concurrency reads a cancellation, and a
 * cancellation wrapped in something else reads as a failure that cancels the caller's parent.
 *
 * The ladder grows when the contract adds a class, so this is an abstract class rather than a
 * sealed one: a `when` over it ends in `else`, and a class added later breaks no caller's build.
 * Its constructor is `InternalMountayaApi`, so every subclass is one of Mountaya's: this module's, or
 * `WebhookSignatureException`, which the admin unit, `mountaya-admin`, declares beside the core (SPEC.md
 * section 7).
 */
public abstract class MountayaException
    @InternalMountayaApi
    protected constructor(
        message: String,
        cause: Throwable?,
        idempotencyKey: String?,
    ) : Exception(message, cause),
        NamedException {
        /**
         * The contract's name for the failure's class, such as `PreconditionFailedError`.
         *
         * It is what a conformance fixture names, and what a log or a report can carry. A Kotlin
         * caller matches the class with `is` instead: a release build renames classes, so their
         * simple names are not this.
         */
        abstract override val name: String

        /**
         * The `Idempotency-Key` the call that raised the failure sent, the caller's as given or the
         * one the client drew, whatever the failure, or null where it sent none.
         *
         * An answer that never came, a 5xx and a refusal do not say the write was not made, so a
         * caller sends the same call again with this key, never a new one: a new key is a second
         * write, made beside the first. The client sets it on every failure a keyed call raises, from
         * the moment the key is fixed, before the first attempt.
         */
        public var idempotencyKey: String? = idempotencyKey
            internal set

        /**
         * Returns the failure as a log shows it: the contract's name for its class, then its message, such as
         * `RateLimitError: 429 LIMIT_REACHED: Seats used up.`, the line `sdk-swift` prints for the same failure. A
         * release build renames classes, so the class's own name is not printed.
         */
        override fun toString(): String = "$name: $message"
    }
