package com.mountaya.conformance

import com.mountaya.admin.WebhookSignatureException
import com.mountaya.admin.constructWebhookEvent
import com.mountaya.admin.generated.WebhookEvent
import com.mountaya.client.ConfigurationException
import kotlinx.coroutines.test.TestScope
import kotlin.math.floor
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * `transport.webhooks.verify-signature`: the case's body, header and secret handed to the public
 * `constructWebhookEvent` as the text that arrived, its clock stated as the case's `now`, and its
 * tolerance the case's where it states one and the function's own where it does not, answered with
 * the event's `id` and `type` read off the event a delivery that verifies holds, or with the class and
 * the reason of a refusal, and `known: false` beside an event of a type the build does not know, read as
 * `WebhookEvent.Unknown`. A delivery that verifies and holds no envelope is a `ConfigurationException`,
 * which names no reason and carries the decoder's failure as its cause, held so here; a tolerance below
 * zero is one too, refused before the header is read, with no cause.
 *
 * Any other failure is no answer the fixture writes, and fails the case. The function is declared on
 * the jvm alone, as the secret key is, so this runner sits in
 * the jvm tests and the entry runs on the jvm alone, whole: what it would meet on Android and iOS is a
 * missing symbol, which the ABI dumps of those targets hold (SPEC.md section 9).
 */
object TransportWebhooksVerifySignatureRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val body = input.text("body")
        val header = input.textOrNull("header")
        val secret = input.text("secret")
        val seconds = input.number("now")
        val whole = floor(seconds)
        val now = Instant.fromEpochSeconds(whole.toLong(), ((seconds - whole) * 1e9).toInt())
        val tolerance = input.numberOrNull("toleranceSeconds")
        val answer =
            try {
                val event =
                    if (tolerance == null) {
                        constructWebhookEvent(body, header, secret, now = now)
                    } else {
                        constructWebhookEvent(body, header, secret, tolerance.seconds, now)
                    }
                val read =
                    listOfNotNull(
                        "id" to FixtureValue.Text(event.id),
                        "type" to FixtureValue.Text(event.type.value),
                        // An event of a type the build does not know is read as one it does not know.
                        ("known" to FixtureValue.Bool(false)).takeIf { event is WebhookEvent.Unknown },
                    )
                Scripted.objectOf(
                    "valid" to FixtureValue.Bool(true),
                    "event" to FixtureValue.Object(read),
                )
            } catch (refusal: WebhookSignatureException) {
                Scripted.objectOf(
                    "valid" to FixtureValue.Bool(false),
                    "error" to FixtureValue.Text(refusal.name),
                    "reason" to FixtureValue.Text(refusal.reason.value),
                )
            } catch (refusal: ConfigurationException) {
                // A tolerance below zero is refused before the header is read; any other refusal is a body that
                // verified and holds no envelope, which carries the decoder's failure.
                if ((tolerance == null || tolerance >= 0) && refusal.cause == null) {
                    throw AssertionError("the refusal carries no decoder's failure as its cause")
                }
                Scripted.objectOf(
                    "valid" to FixtureValue.Bool(false),
                    "error" to FixtureValue.Text(refusal.name),
                    "reason" to FixtureValue.Null,
                )
            }
        return ConformanceOutcome.Value(answer)
    }
}
