package com.mountaya.admin

import com.mountaya.admin.Delivery.BODY
import com.mountaya.admin.Delivery.ROTATED_SECRET
import com.mountaya.admin.Delivery.SECRET
import com.mountaya.admin.Delivery.SIGNED_AT
import com.mountaya.admin.Delivery.VECTOR_SIGNATURE
import com.mountaya.admin.Delivery.headerOf
import com.mountaya.admin.Delivery.signatureOf
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.admin.generated.WebhookEvent
import com.mountaya.client.ConfigurationException
import com.mountaya.client.MountayaException
import com.mountaya.client.Stated
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.isApiException
import com.mountaya.client.isMountayaException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * `verifiedWebhookEvent` is what stands between a receiver and a forged delivery, so each way a
 * delivery can be wrong is driven here and named by the reason it is refused for: a header that is
 * missing or malformed, a signature no `v1` matches, a timestamp outside the tolerance. The order
 * matters as much as the rules, since `t` is signed: a forged delivery is refused for its signature
 * whatever time it claims, and never for its time.
 *
 * It runs on every target, each against its own HMAC and comparison, with the clock stated; the
 * public function, the jvm's alone, reads the platform's clock where none is given.
 */
class VerifiedWebhookEventTest {
    /** Returns the receiver's clock [seconds] after the vectors' `t`. */
    private fun after(seconds: Double): Instant =
        Instant.fromEpochMilliseconds(SIGNED_AT * 1000 + (seconds * 1000).toLong())

    /** Verifies a delivery whose clock is [seconds] after the vectors' `t`, held to 300 seconds where no tolerance is given. */
    private fun verify(
        body: String = BODY,
        header: String?,
        secret: String = SECRET,
        tolerance: Duration = 300.seconds,
        seconds: Double = 5.0,
    ): WebhookEvent = verifiedWebhookEvent(body.encodeToByteArray(), header, secret, tolerance, after(seconds))

    /** Returns the reason [call] was refused for, or null where it was not refused as a signature. */
    private fun refusal(call: () -> WebhookEvent): WebhookSignatureException.Reason? =
        try {
            call()
            null
        } catch (refused: WebhookSignatureException) {
            refused.reason
        }

    /** Returns the message [call] was refused with as set up wrong. */
    private fun configuration(call: () -> WebhookEvent): ConfigurationException =
        assertFailsWith<ConfigurationException> { call() }

    @Test
    fun theContractsVectorIsTheEventItCarriesItsInstantsDecoded() {
        val event = verify(header = headerOf(VECTOR_SIGNATURE))

        assertEquals(AdminSchema.WebhookEventType.OrganizationMemberUpdated, event.type)
        val updated = assertIs<WebhookEvent.OrganizationMemberUpdated>(event)
        assertEquals("019a8eed-e1ad-7753-b049-b0e20d79577a", updated.payload.id)
        assertEquals(Instant.fromEpochSeconds(1_794_647_564), updated.payload.created_at)
        assertEquals(RestSchema.OrganizationRole.Contributor, updated.data.`object`.role)
        assertEquals(Instant.fromEpochSeconds(1_773_237_729), updated.data.`object`.joined_at)
        assertEquals(RestSchema.OrganizationRole.Viewer, updated.data.previous_attributes?.role)
        assertEquals("2c5ed6d8b8f7c706e9a0d5c5144ed90b", updated.payload.request?.id)
    }

    @Test
    fun aBodyThatIsNotAsciiVerifiesByItsUtf8Bytes() {
        val body = BODY.replace(""""task":"organization.member.update"""", """"task":"organisation.café"""")

        val event = verify(body, headerOf(signatureOf(body)), seconds = 0.0)

        assertEquals("organisation.café", event.task)
    }

    @Test
    fun theHeadersTIsSignedAsWrittenALeadingZeroIncluded() {
        val written = "0$SIGNED_AT"

        val event = verify(header = "t=$written,v1=${signatureOf(BODY, written)}", seconds = 30.0)
        assertEquals("organization.member.updated", event.type.value)
        assertEquals(
            WebhookSignatureException.Reason.Signature,
            refusal { verify(header = "t=$written,v1=${signatureOf(BODY)}", seconds = 30.0) },
        )
    }

    @Test
    fun anyV1ThatMatchesVerifiesSoEitherSecretOfARotationDoes() {
        val header = headerOf(VECTOR_SIGNATURE, signatureOf(BODY, secret = ROTATED_SECRET))

        assertEquals("organization.member.updated", verify(header = header, seconds = 30.0).type.value)
        assertEquals(
            "organization.member.updated",
            verify(header = header, secret = ROTATED_SECRET, seconds = 30.0).type.value,
        )
        // The control: a secret neither `v1` was signed with matches neither.
        val other = SECRET.dropLast(1) + "0"
        assertEquals(
            WebhookSignatureException.Reason.Signature,
            refusal {
                verify(header = header, secret = other, seconds = 30.0)
            },
        )
    }

    @Test
    fun aBodyChangedOnTheWayAndOneSignedWithAnotherSecretAreRefusedForIt() {
        val tampered = BODY.replace(""""role":"contributor"""", """"role":"admin"""")

        for (refused in listOf(
            { verify(tampered, headerOf(VECTOR_SIGNATURE)) },
            { verify(header = headerOf(VECTOR_SIGNATURE), secret = ROTATED_SECRET) },
            // One character off at the end, and a signature of another length, are each no match.
            { verify(header = headerOf(VECTOR_SIGNATURE.dropLast(1) + "e")) },
            { verify(header = headerOf(VECTOR_SIGNATURE.dropLast(2))) },
            // The comparison is of the bytes written: an uppercase spelling of the signature is another.
            { verify(header = headerOf(VECTOR_SIGNATURE.uppercase())) },
        )) {
            assertEquals(WebhookSignatureException.Reason.Signature, refusal(refused))
        }
    }

    @Test
    fun aTimestampPastTheToleranceEitherWayIsRefusedAndOneAtItAccepted() {
        val header = headerOf(VECTOR_SIGNATURE)

        assertEquals("organization.member.updated", verify(header = header, seconds = 300.0).type.value)
        assertEquals("organization.member.updated", verify(header = header, seconds = -300.0).type.value)
        for (seconds in listOf(301.0, -301.0, 300.5)) {
            assertEquals(
                WebhookSignatureException.Reason.Timestamp,
                refusal {
                    verify(header = header, seconds = seconds)
                },
                "$seconds",
            )
        }
    }

    @Test
    fun theSignatureIsJudgedBeforeTheTimeSoAForgedDeliveryIsRefusedForIt() {
        val tampered = BODY.replace(""""role":"contributor"""", """"role":"admin"""")

        assertEquals(
            WebhookSignatureException.Reason.Signature,
            refusal { verify(tampered, headerOf(VECTOR_SIGNATURE), seconds = 9000.0) },
        )
    }

    @Test
    fun theTimestampIsHeldToTheToleranceAReceiverStates() {
        val header = headerOf(VECTOR_SIGNATURE)

        assertEquals(
            "organization.member.updated",
            verify(header = header, tolerance = 600.seconds, seconds = 599.0).type.value,
        )
        assertEquals(
            "organization.member.updated",
            verify(header = header, tolerance = Duration.ZERO, seconds = 0.0).type.value,
        )
        assertEquals(
            WebhookSignatureException.Reason.Timestamp,
            refusal {
                verify(header = header, tolerance = Duration.ZERO, seconds = 1.0)
            },
        )
        assertEquals(
            WebhookSignatureException.Reason.Timestamp,
            refusal { verify(header = header, tolerance = 600.seconds, seconds = 601.0) },
        )
    }

    @Test
    fun aHeaderTheRequestDoesNotCarryAnEmptyOneAndABlankOneAreMissing() {
        for (header in listOf(null, "", "  ", "\t　")) {
            assertEquals(WebhookSignatureException.Reason.MissingHeader, refusal { verify(header = header) }, "$header")
        }
    }

    @Test
    fun aHeaderThatIsNotTAndV1IsMalformed() {
        for (header in listOf(
            "v1=$VECTOR_SIGNATURE",
            "t=$SIGNED_AT",
            "t=$SIGNED_AT,t=$SIGNED_AT,v1=$VECTOR_SIGNATURE",
            "t=yesterday,v1=$VECTOR_SIGNATURE",
            "t=$SIGNED_AT.5,v1=$VECTOR_SIGNATURE",
            "t=-$SIGNED_AT,v1=$VECTOR_SIGNATURE",
            "t=,v1=$VECTOR_SIGNATURE",
            "t=99999999999999999999,v1=$VECTOR_SIGNATURE",
            "t=9007199254740992,v1=$VECTOR_SIGNATURE",
            "t=$SIGNED_AT,v1",
            "t=$SIGNED_AT,v1=$VECTOR_SIGNATURE,",
            "t=$SIGNED_AT,v0=$VECTOR_SIGNATURE",
            "t=١٧٦٣١١١٥٦٤,v1=$VECTOR_SIGNATURE",
        )) {
            assertEquals(WebhookSignatureException.Reason.MalformedHeader, refusal { verify(header = header) }, header)
        }
    }

    @Test
    fun aSchemeItDoesNotComputeIsPassedOverAndWhiteSpaceAroundAnElementTrimmed() {
        for (header in listOf(
            "t=$SIGNED_AT,v0=00ff,v1=$VECTOR_SIGNATURE",
            "t=$SIGNED_AT, v1=$VECTOR_SIGNATURE",
            " t = $SIGNED_AT ,\tv1 = $VECTOR_SIGNATURE",
        )) {
            assertEquals("organization.member.updated", verify(header = header).type.value, header)
        }
    }

    @Test
    fun theInstantsOfPreviousAttributesAreDecodedAsTheObjectsOwn() {
        val body =
            """{"actor":{"type":"user","user_id":"1b9d4e72-0c35-4a8f-b6e1-5f2c7d904a18"},"api_version":"0.8.0",""" +
                """"created_at":"2026-11-14T09:12:44Z","data":{"object":{"object":"zone",""" +
                """"id":"5f1c8a94-2d63-4e07-b9a1-0c4e7d2f8b36",""" +
                """"organization_id":"d5a83e17-92c6-4b0f-a7d3-1e6482f9c05b","name":"Rockfall",""" +
                """"description":null,"level":"danger",""" +
                """"mode":"polygon","status":"published","is_private":false,"start_at":"2026-11-20T06:00:00Z",""" +
                """"end_at":null,"version":7,""" +
                """"created_by":{"id":"1b9d4e72-0c35-4a8f-b6e1-5f2c7d904a18"},"created_at":"2026-05-02T10:14:51Z",""" +
                """"updated_at":"2026-11-14T09:12:44Z"},"previous_attributes":{"name":"2026-01-01T00:00:00Z",""" +
                """"start_at":"2026-11-18T06:00:00Z","end_at":null}},"id":"019a8d5b-1059-73c5-849c-ffaf86349866",""" +
                """"object":"webhook_event","organization_id":"d5a83e17-92c6-4b0f-a7d3-1e6482f9c05b",""" +
                """"related_object":{"id":"5f1c8a94-2d63-4e07-b9a1-0c4e7d2f8b36","type":"zone"},"request":null,""" +
                """"task":"zone.update","type":"zone.updated"}"""

        val event = assertIs<WebhookEvent.ZoneUpdated>(verify(body, headerOf(signatureOf(body)), seconds = 1.0))

        val previous = assertNotNull(event.data.previous_attributes, "the update states the attributes it changed")
        assertEquals(Instant.fromEpochSeconds(1_795_154_400), event.data.`object`.start_at)
        assertEquals(Stated(Instant.fromEpochSeconds(1_794_981_600)), previous.start_at)
        // A field the contract types as a string stays one, whatever it holds.
        assertEquals("2026-01-01T00:00:00Z", previous.name)
        // A field that moved from null holds null, and one that did not move holds nothing.
        assertEquals(Stated(null), previous.end_at)
        assertNull(previous.description)
        assertNull(event.request)
    }

    @Test
    fun anEventOfATypeThisBuildDoesNotKnowIsDeliveredAsTheEnvelopeNotRefused() {
        val body =
            BODY
                .replace(""""type":"organization.member.updated"""", """"type":"organization.member.teleported"""")
                .replace(
                    """"object":"organization.member","user_id"""",
                    """"object":"organization.teleport","user_id"""",
                )

        val event = verify(body, headerOf(signatureOf(body)))

        assertEquals("organization.member.teleported", event.type.value)
        val unknown = assertIs<WebhookEvent.Unknown>(event)
        assertEquals("019a8eed-e1ad-7753-b049-b0e20d79577a", unknown.event.id)
        val held = assertIs<AdminSchema.WebhookEventObject.Unknown>(unknown.event.data.`object`)
        assertEquals(
            buildJsonObject {
                put("object", "organization.teleport")
                put("user_id", "7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1")
                put("role", "contributor")
                put("joined_at", "2026-03-11T14:02:09Z")
            },
            held.json,
        )
    }

    @Test
    fun aBodyThatVerifiesAndHoldsNoEnvelopeAtAllIsRefusedAsSetUpWrong() {
        for (body in listOf(
            """{"hello":"world"}""",
            "not json",
            // An envelope member missing: no event is delivered without its `id`.
            """{"type":"zone.updated","object":"webhook_event","api_version":"0.8.0",""" +
                """"created_at":"2026-11-14T09:12:44Z",""" +
                """"organization_id":"o","task":"zone.update","related_object":{"id":"z","type":"zone"},""" +
                """"data":{"object":{"object":"zone"}},"request":null,"actor":{"type":"mountaya"}}""",
        )) {
            val refused = configuration { verify(body, headerOf(signatureOf(body))) }
            assertIs<IllegalArgumentException>(refused.cause, body)
            assertTrue(refused.message.orEmpty().startsWith("The body verified, and holds no webhook event"), body)
        }
    }

    /**
     * The decoder copies a body under 200 characters whole into its failure's message, and a delivery may carry a
     * share link's token or a setup intent's client secret: the refusal's cause says where the body did not read,
     * and holds no byte of it.
     */
    @Test
    fun aBodyThatHoldsNoEnvelopeIsRefusedWithACauseThatCarriesNoByteOfIt() {
        val token = "share_0123456789abcdef0123456789abcdef"
        // A type this build cannot type, read as the envelope, whose `created_at` is no timestamp.
        val body =
            """{"type":"share_link.teleported","created_at":12,""" +
                """"data":{"object":{"object":"share_link","share_token":"$token"}}}"""

        val refused = configuration { verify(body, headerOf(signatureOf(body))) }

        assertIs<IllegalArgumentException>(refused.cause)
        assertTrue("created_at" in refused.cause?.message.orEmpty(), "where it did not read stays said")
        var link: Throwable? = refused
        while (link != null) {
            for (piece in listOf(token, token.take(16), token.takeLast(16))) {
                assertFalse(piece in link.message.orEmpty(), "${link::class.simpleName}'s message: ${link.message}")
                assertFalse(piece in link.stackTraceToString(), "${link::class.simpleName}'s stack trace")
            }
            link = link.cause
        }
    }

    @Test
    fun aKnownTypeWhoseObjectItsTypesDoNotReadIsDeliveredAsTheEnvelopeHeldWhole() {
        // A zone carrying its name alone: the zone's own type reads none of it, and the envelope's union
        // reads it as the zone it says it is, so it is held as it arrived.
        val body =
            """{"actor":{"type":"user","user_id":"1b9d4e72-0c35-4a8f-b6e1-5f2c7d904a18"},"api_version":"0.8.0",""" +
                """"created_at":"2026-11-14T09:12:44Z","data":{"object":{"object":"zone","name":"Rockfall"},""" +
                """"previous_attributes":{"name":"Old"}},"id":"019a8d5b-1059-73c5-849c-ffaf86349866",""" +
                """"object":"webhook_event",""" +
                """"organization_id":"d5a83e17-92c6-4b0f-a7d3-1e6482f9c05b",""" +
                """"related_object":{"id":"5f1c8a94-2d63-4e07-b9a1-0c4e7d2f8b36","type":"zone"},"request":null,""" +
                """"task":"zone.update","type":"zone.updated"}"""

        val event = verify(body, headerOf(signatureOf(body)))

        val unknown = assertIs<WebhookEvent.Unknown>(event)
        assertEquals("zone.updated", event.type.value)
        assertEquals("019a8d5b-1059-73c5-849c-ffaf86349866", event.id)
        val held = assertIs<AdminSchema.WebhookEventObject.Unknown>(unknown.event.data.`object`)
        assertEquals(
            buildJsonObject {
                put("object", "zone")
                put("name", "Rockfall")
            },
            held.json,
        )
        assertEquals(mapOf("name" to JsonPrimitive("Old")), unknown.event.data.previous_attributes)
    }

    @Test
    fun previousAttributesItsTypesDoNotReadDeliverTheEnvelopeTheObjectTyped() {
        val body = BODY.replace(""""previous_attributes":{"role":"viewer"}""", """"previous_attributes":{"role":5}""")

        val event = verify(body, headerOf(signatureOf(body)))

        val unknown = assertIs<WebhookEvent.Unknown>(event)
        val member = assertIs<AdminSchema.OrganizationMemberEventObject>(unknown.event.data.`object`)
        assertEquals(RestSchema.OrganizationRole.Contributor, member.role)
        assertEquals(mapOf("role" to JsonPrimitive(5)), unknown.event.data.previous_attributes)
        assertEquals("organization.member.update", event.task)
    }

    @Test
    fun everyMemberOfTheEnvelopeIsReadOffEveryEventAlikeAnUntypedOneIncluded() {
        val typed = verify(header = headerOf(VECTOR_SIGNATURE))
        val body = BODY.replace(""""type":"organization.member.updated"""", """"type":"organization.member.moved"""")
        val untyped = verify(body, headerOf(signatureOf(body)))

        for (event in listOf(typed, untyped)) {
            assertEquals("019a8eed-e1ad-7753-b049-b0e20d79577a", event.id)
            assertEquals("webhook_event", event.`object`)
            assertEquals("0.8.0", event.api_version)
            assertEquals(Instant.fromEpochSeconds(1_794_647_564), event.created_at)
            assertEquals("d5a83e17-92c6-4b0f-a7d3-1e6482f9c05b", event.organization_id)
            assertEquals("organization.member.update", event.task)
            assertEquals("7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1", event.related_object.id)
            assertEquals("2c5ed6d8b8f7c706e9a0d5c5144ed90b", event.request?.id)
            assertEquals(AdminSchema.WebhookEventActorType.User, event.actor.type)
        }
        assertEquals("organization.member.updated", typed.type.value)
        assertEquals("organization.member.moved", untyped.type.value)
    }

    @Test
    fun aSecretWithoutItsPrefixIsRefusedAndNeverQuoted() {
        val bare = SECRET.removePrefix("whsk_")

        val message = configuration { verify(header = headerOf(VECTOR_SIGNATURE), secret = bare) }.message.orEmpty()

        assertTrue("whsk_" in message)
        assertFalse(bare in message)
    }

    @Test
    fun aSecretKeyInPlaceOfTheSigningSecretIsRefusedThoughItVerifiesItsOwnMac() {
        val secretKey = "sk_" + SECRET.removePrefix("whsk_")
        val header = headerOf(signatureOf(BODY, secret = secretKey))

        val message = configuration { verify(header = header, secret = secretKey) }.message.orEmpty()

        assertTrue("does not start with `whsk_`" in message)
        assertFalse(secretKey in message)
    }

    @Test
    fun aToleranceBelowZeroIsRefusedNamingItsSeconds() {
        for ((tolerance, printed) in listOf(
            (-1).seconds to "-1",
            (-0.5).seconds to "-0.5",
            (-300).seconds to "-300",
            -Duration.INFINITE to "-Infinity",
        )) {
            assertEquals(
                "A tolerance of $printed seconds is no number of seconds from 0: pass one, or leave it out for 300.",
                configuration { verify(header = headerOf(VECTOR_SIGNATURE), tolerance = tolerance) }.message,
            )
        }
    }

    @Test
    fun anInfiniteToleranceHoldsADeliveryToNoTimeAtAll() {
        assertEquals(
            "organization.member.updated",
            verify(header = headerOf(VECTOR_SIGNATURE), tolerance = Duration.INFINITE, seconds = 9e9).type.value,
        )
    }

    @Test
    fun aCallSetUpWrongIsRefusedBeforeItsHeaderIsRead() {
        configuration { verify(header = null, secret = "sk_bare") }
    }

    @Test
    fun aRefusalIsAnErrorOfTheFamilyNamedByTheContractAndNoAnswerOfTheApi() {
        val refused = assertFailsWith<WebhookSignatureException> { verify(header = null) }

        assertEquals("WebhookSignatureError", refused.name)
        assertTrue(isMountayaException(refused))
        assertFalse(isApiException(refused))
        assertNull(refused.idempotencyKey)
        assertIs<MountayaException>(refused)
        assertEquals(WebhookSignatureException.Reason.MissingHeader, refused.reason)
        assertEquals("missing-header", refused.reason.value)
        assertTrue(refused.message.orEmpty().startsWith("The request carries no `Mountaya-Signature` header"))
        assertEquals("WebhookSignatureError: ${refused.message}", refused.toString())
    }

    @Test
    fun aTimestampRefusalNamesTheInstantSignedAtAndTheDistanceFromTheClock() {
        val refused =
            assertFailsWith<WebhookSignatureException> { verify(header = headerOf(VECTOR_SIGNATURE), seconds = 301.0) }

        assertEquals(
            "WebhookSignatureError: The delivery was signed at 2025-11-14T09:12:44.000Z, 301 seconds from this " +
                "clock, " +
                "past the tolerance of 300 seconds: a delivery replayed later is refused this way, and where " +
                "the clock " +
                "is wrong, set it right.",
            refused.toString(),
        )
    }
}
