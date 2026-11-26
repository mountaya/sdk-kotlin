package com.mountaya.admin

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.MountayaException

/**
 * WebhookSignatureException is a webhook delivery `constructWebhookEvent` cannot trust: a
 * `Mountaya-Signature` header missing or malformed, a signature no `v1` of it matches, or a
 * timestamp outside the tolerance. [reason] says which.
 *
 * A receiver answers such a delivery with a `400` and does nothing it asks. Mountaya repeats a
 * delivery that failed, so a refusal a wrong secret or a wrong clock caused costs an attempt and not the event:
 * once the secret or the clock is right, the next attempt verifies. It is of the family
 * `isMountayaException` recognises, and never an error the API answered: a receiver's verification
 * raises it, and no answer of the API does.
 *
 * It is the admin unit's, as webhook receiving is, and a class of the error ladder all the same: the core's
 * `MountayaException` admits a subclass of Mountaya's own modules alone (SPEC.md section 7).
 */
@OptIn(InternalMountayaApi::class)
public class WebhookSignatureException(
    /** Why the delivery was refused. */
    public val reason: Reason,
    message: String,
) : MountayaException(message, null, null) {
    override val name: String get() = "WebhookSignatureError"

    /**
     * Reason is why a delivery was refused, spelled as the contract spells it in [value].
     *
     * It is open: a reason the contract adds is one more value, so a `when` over it ends in `else`.
     */
    @kotlin.jvm.JvmInline
    public value class Reason internal constructor(
        /** The contract's spelling: `missing-header`, `malformed-header`, `signature` or `timestamp`. */
        public val value: String,
    ) {
        override fun toString(): String = value

        /** The reasons the contract names. */
        public companion object {
            /** A request that carries no `Mountaya-Signature` header, or one that holds nothing but white space. */
            public val MissingHeader: Reason = Reason("missing-header")

            /**
             * A header that is not `t=<unix seconds>,v1=<signature>`: it holds no `t` or more than one, a
             * `t` that is not whole seconds in digits alone, no `v1`, or an element that is no
             * `key=value` pair.
             */
            public val MalformedHeader: Reason = Reason("malformed-header")

            /**
             * A header no `v1` of which is the signature of this body under this secret: the body was
             * changed on the way, the secret is another endpoint's or one rotated away, or the delivery
             * is not Mountaya's.
             */
            public val Signature: Reason = Reason("signature")

            /**
             * A signature that verifies, made further from the receiver's clock than the tolerance, either
             * way: a delivery replayed later, or a clock that is wrong.
             */
            public val Timestamp: Reason = Reason("timestamp")
        }
    }
}
