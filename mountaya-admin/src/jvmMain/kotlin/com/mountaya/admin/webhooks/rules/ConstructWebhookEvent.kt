package com.mountaya.admin

import com.mountaya.admin.generated.WebhookEvent
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.SystemDefaults
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * constructWebhookEvent verifies a webhook delivery's signature and returns the event its body holds, typed by its
 * `type`.
 *
 * [rawBody] is the body exactly as it arrived, before any JSON parser read it: the signature covers
 * those bytes, and a body parsed and written out again is other bytes, which do not verify.
 * [signatureHeader] is the request's `Mountaya-Signature`, null where it carries none, and [secret] the
 * signing secret of the endpoint the delivery was sent to, `whsk_` included.
 *
 * The header is read first, then the signature, then the time, so a forged delivery is refused for
 * its signature whatever time it claims. A `v1` that matches is enough: while a rotated secret
 * overlaps its successor the header carries one for each, and either secret verifies it. The
 * signature is then held to [now], the receiver's clock, the platform's wall clock where none is
 * given: a delivery signed further from it than [tolerance], 300 seconds unless the caller names
 * another, either way, is refused, and one signed exactly that far is not.
 *
 * The event's instants are `kotlin.time.Instant`s, as a method of the client answers them. An event
 * this build cannot type, of a type a newer contract added or whose payload its types do not read, is
 * returned too, as `WebhookEvent.Unknown` holding the contract's envelope, so it is still acknowledged
 * and deduplicated by its `id`.
 *
 * It throws `WebhookSignatureException` for a delivery that cannot be trusted, its `reason` saying
 * why, which a receiver answers with a `400`. It throws `ConfigurationException` for a secret that does not start
 * with `whsk_`, which it never quotes, a tolerance below 0, and a body that verifies and holds no
 * webhook event envelope at all.
 *
 * It is declared on the jvm alone, as the secret key is: a delivery is received by a server, and the
 * endpoint's secret never ships in an app, so an Android or iOS build has nothing to call. It does not
 * suspend: nothing it does waits.
 */
@OptIn(InternalMountayaApi::class)
public fun constructWebhookEvent(
    rawBody: ByteArray,
    signatureHeader: String?,
    secret: String,
    tolerance: Duration = 300.seconds,
    now: Instant = SystemDefaults.clock.now(),
): WebhookEvent = verifiedWebhookEvent(rawBody, signatureHeader, secret, tolerance, now)

/**
 * constructWebhookEvent verifies a webhook delivery whose body arrived as text, by the text's UTF-8 bytes, which
 * are the bytes Mountaya signed, and returns the event it holds, as the overload that takes the bytes does, and is
 * declared on the jvm alone as it is.
 */
@OptIn(InternalMountayaApi::class)
public fun constructWebhookEvent(
    rawBody: String,
    signatureHeader: String?,
    secret: String,
    tolerance: Duration = 300.seconds,
    now: Instant = SystemDefaults.clock.now(),
): WebhookEvent = verifiedWebhookEvent(rawBody.encodeToByteArray(), signatureHeader, secret, tolerance, now)
