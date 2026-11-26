package com.mountaya.admin

import com.mountaya.admin.generated.WebhookEvent
import com.mountaya.client.ConfigurationException
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.decoderFailure
import com.mountaya.client.formatTimestamp
import com.mountaya.client.wireJson
import kotlin.math.abs
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.Instant

/** What opens every endpoint's signing secret, which is keyed with the rest of it. */
internal const val WEBHOOK_SECRET_PREFIX: String = "whsk_"

/** The latest `t` the timestamp refusal writes as an instant, the last second of the year 9999. */
private const val LATEST_WRITTEN_SECONDS: Long = 253_402_300_799

/** The nanoseconds in a second. */
private const val NANOSECONDS_PER_SECOND = 1e9

/**
 * Returns the event a webhook delivery's [rawBody] holds, once its [signatureHeader] verifies under
 * [secret] and its timestamp lies within [tolerance] of [now], the receiver's clock: what
 * `constructWebhookEvent` does with the clock it judges a timestamp by stated.
 *
 * The secret and the tolerance are checked first, each a `ConfigurationException`; then the header,
 * then the signature, then the time, each a `WebhookSignatureException`, so a forged delivery is
 * refused for its signature whatever time it claims. A body that verifies is read as the
 * `WebhookEvent` its `type` names, or as `WebhookEvent.Unknown` where this build cannot type it, and
 * only a body that holds no webhook event envelope at all is a `ConfigurationException`, carrying the
 * decoder's failure as its cause.
 */
@OptIn(InternalMountayaApi::class)
internal fun verifiedWebhookEvent(
    rawBody: ByteArray,
    signatureHeader: String?,
    secret: String,
    tolerance: Duration,
    now: Instant,
): WebhookEvent {
    if (!secret.startsWith(WEBHOOK_SECRET_PREFIX)) {
        throw ConfigurationException(
            "The webhook signing secret does not start with `$WEBHOOK_SECRET_PREFIX`: pass the signing secret of " +
                "the endpoint the delivery was sent to, `$WEBHOOK_SECRET_PREFIX` included.",
        )
    }
    // A duration is a count of nanoseconds or one of the two infinities, held here as the seconds the timestamp is
    // compared in and the refusals write. A negative one is the only tolerance refused, and an infinite one holds
    // a delivery to no time at all.
    val toleranceSeconds = tolerance.toDouble(DurationUnit.SECONDS)
    if (tolerance.isNegative()) {
        throw ConfigurationException(
            "A tolerance of ${spelled(toleranceSeconds)} seconds is no number of seconds from 0: pass one, or " +
                "leave it out for 300.",
        )
    }

    if (SignatureHeader.isMissing(signatureHeader) || signatureHeader == null) {
        throw WebhookSignatureException(
            WebhookSignatureException.Reason.MissingHeader,
            "The request carries no `Mountaya-Signature` header: pass the header the request carries, and " +
                "answer a request without one with a 400.",
        )
    }
    val header =
        SignatureHeader.parse(signatureHeader)
            ?: throw WebhookSignatureException(
                WebhookSignatureException.Reason.MalformedHeader,
                "The `Mountaya-Signature` header is not `t=<unix seconds>,v1=<signature>`: pass the header " +
                    "exactly as the request carries it.",
            )
    val expected = WebhookSignature.expected(header.stated, rawBody, secret)
    if (!WebhookSignature.verifies(header.signatures, expected)) {
        throw WebhookSignatureException(
            WebhookSignatureException.Reason.Signature,
            "No `v1` of the `Mountaya-Signature` header is the signature of this body under this secret: pass " +
                "the body exactly as it arrived, before any JSON parser reads it, and the secret of the " +
                "endpoint the delivery was sent to.",
        )
    }
    val clock = now.epochSeconds + now.nanosecondsOfSecond / NANOSECONDS_PER_SECOND
    val skew = abs(clock - header.timestamp.toDouble())
    if (skew > toleranceSeconds) {
        throw WebhookSignatureException(
            WebhookSignatureException.Reason.Timestamp,
            "The delivery was signed at ${signedAt(header.timestamp)}, ${spelled(floor(skew + 0.5))} seconds " +
                "from this clock, past the tolerance of ${spelled(toleranceSeconds)} seconds: a delivery " +
                "replayed later is refused this way, and where the clock is wrong, set it right.",
        )
    }

    // The body verified, so it is the event Mountaya signed, read as the contract declares it, or as
    // its envelope where this build cannot type it.
    try {
        return wireJson.decodeFromString(
            WebhookEvent.serializer(),
            rawBody.decodeToString(throwOnInvalidSequence = true),
        )
    } catch (undecodable: IllegalArgumentException) {
        // The decoder's own failure copies the body into its message, and a delivery may hold a secret.
        throw noEnvelope(decoderFailure(undecodable))
    } catch (undecodable: CharacterCodingException) {
        throw noEnvelope(undecodable)
    }
}

/** The refusal of a body that verified and holds no webhook event envelope, carrying why it does not read and none of it. */
private fun noEnvelope(cause: Exception): ConfigurationException =
    ConfigurationException(
        "The body verified, and holds no webhook event: not even the envelope every event is delivered in reads.",
        cause = cause,
    )

/** Returns a count of seconds as a whole number where it is one, and as Kotlin writes a `Double` otherwise. */
private fun spelled(seconds: Double): String =
    if (seconds.isFinite() && floor(seconds) == seconds &&
        abs(seconds) < 1e15
    ) {
        seconds.toLong().toString()
    } else {
        seconds.toString()
    }

/** Returns the instant a `t` names, as the contract writes an instant, or the `t` itself where it falls past the year 9999. */
@OptIn(InternalMountayaApi::class)
private fun signedAt(timestamp: Long): String =
    if (timestamp <= LATEST_WRITTEN_SECONDS) formatTimestamp(Instant.fromEpochSeconds(timestamp)) else "t=$timestamp"
