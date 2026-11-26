package com.mountaya.admin

import com.mountaya.admin.Delivery.BODY
import com.mountaya.admin.Delivery.SECRET
import com.mountaya.admin.Delivery.SIGNED_AT
import com.mountaya.admin.Delivery.VECTOR_SIGNATURE
import com.mountaya.admin.Delivery.headerOf
import com.mountaya.admin.Delivery.signatureOf
import com.mountaya.admin.generated.WebhookEvent
import com.mountaya.client.ConfigurationException
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.SystemDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * The public `constructWebhookEvent`, declared on the jvm alone: the body as text and as bytes, the
 * tolerance of 300 seconds where none is given, and the platform's clock where no clock is given.
 * Every rule it applies is `verifiedWebhookEvent`'s, held on every target by its own test.
 */
@OptIn(InternalMountayaApi::class)
class ConstructWebhookEventTest {
    private val fiveAfter = Instant.fromEpochSeconds(SIGNED_AT + 5)

    @Test
    fun bytesVerifyAsTheTextTheySpellDoes() {
        val fromText = constructWebhookEvent(BODY, headerOf(VECTOR_SIGNATURE), SECRET, now = fiveAfter)
        val fromBytes =
            constructWebhookEvent(BODY.encodeToByteArray(), headerOf(VECTOR_SIGNATURE), SECRET, now = fiveAfter)

        assertIs<WebhookEvent.OrganizationMemberUpdated>(fromText)
        assertEquals(fromText, fromBytes)
    }

    @Test
    fun aBodyThatIsNotAsciiVerifiesByItsUtf8BytesAsTextAndAsBytes() {
        val body = BODY.replace(""""task":"organization.member.update"""", """"task":"organisation.café"""")
        val header = headerOf(signatureOf(body))
        val now = Instant.fromEpochSeconds(SIGNED_AT)

        assertEquals("organisation.café", constructWebhookEvent(body, header, SECRET, now = now).task)
        assertEquals(
            "organisation.café",
            constructWebhookEvent(body.encodeToByteArray(), header, SECRET, now = now).task,
        )
    }

    @Test
    fun theToleranceIsThreeHundredSecondsWhereNoneIsGiven() {
        val header = headerOf(VECTOR_SIGNATURE)

        constructWebhookEvent(BODY, header, SECRET, now = Instant.fromEpochSeconds(SIGNED_AT + 300))
        val refused =
            assertFailsWith<WebhookSignatureException> {
                constructWebhookEvent(BODY, header, SECRET, now = Instant.fromEpochSeconds(SIGNED_AT + 301))
            }
        assertEquals(WebhookSignatureException.Reason.Timestamp, refused.reason)
        constructWebhookEvent(BODY, header, SECRET, 10.minutes, Instant.fromEpochSeconds(SIGNED_AT + 301))
    }

    @Test
    fun theTimeIsJudgedByThePlatformsClockWhereNoClockIsGiven() {
        val stated = "${SystemDefaults.clock.now().epochSeconds}"
        val header = "t=$stated,v1=${signatureOf(BODY, stated)}"

        assertEquals("organization.member.updated", constructWebhookEvent(BODY, header, SECRET).type.value)
        // The vectors' `t` lies a year and more behind any clock this runs on.
        val refused =
            assertFailsWith<WebhookSignatureException> {
                constructWebhookEvent(
                    BODY,
                    headerOf(VECTOR_SIGNATURE),
                    SECRET,
                )
            }
        assertEquals(WebhookSignatureException.Reason.Timestamp, refused.reason)
        assertEquals(
            "organization.member.updated",
            constructWebhookEvent(BODY, headerOf(VECTOR_SIGNATURE), SECRET, tolerance = 1e12.seconds).type.value,
        )
    }
}
