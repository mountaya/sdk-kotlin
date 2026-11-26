package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import com.mountaya.client.generated.OperationTable
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Every step an error's message ends on, word for word, each for the status, the code and the
 * declaration that names it, and nothing for a status that names none.
 */
class NextStepTest {
    private val member = NextStep.Declaration(OperationTable.Rest.patchItinerary.credentials)

    /** What `postSession` declares, the one operation a secret key authenticates, whose facts the admin unit prints. */
    private val mints = NextStep.Declaration(CredentialDeclaration(requiresSecretKey = true))

    /** The step a failure of [status] names, with no rate-limit state and no arrival. */
    private fun step(
        status: Int,
        code: ApiErrorCode? = null,
        retryAfter: Duration? = null,
        declaration: NextStep.Declaration = NextStep.Declaration.none,
    ): String? = NextStep.step(status, code, retryAfter, null, declaration, null)

    @Test
    fun anOperationsCredentialFactsSayWhetherItActsAsAMemberAndWhetherItMints() {
        assertEquals(NextStep.Declaration(actsAsMember = true, requiresSecretKey = false), member)
        assertEquals(NextStep.Declaration(actsAsMember = false, requiresSecretKey = true), mints)
        assertEquals(NextStep.Declaration.none, NextStep.Declaration(OperationTable.Rest.getItinerary.credentials))
        assertEquals(
            NextStep.Declaration.none,
            NextStep.Declaration(
                CredentialDeclaration(CredentialDeclaration.ActingUser.Optional, requiresSession = true),
            ),
        )
    }

    @Test
    fun a401NamesTheCredentialToCheckTheMembersTokenOrTheSecretKeyThatMints() {
        val credential =
            "Check the credential this client sends: a key that is unknown, rotated or disabled, and a session token " +
                "that has expired, are refused."
        assertEquals(credential, step(401, ApiErrorCode("UNAUTHORIZED")))
        assertEquals(
            "$credential This call acts as the user its session token was minted for, so mint the token with " +
                "postSession's user_id set to that user.",
            step(401, ApiErrorCode("UNAUTHORIZED"), declaration = member),
        )
        assertEquals(
            "Check the secret key this client sends: one that is unknown, revoked or malformed, or one of a canceled " +
                "organization, is refused.",
            step(401, ApiErrorCode("UNAUTHORIZED"), declaration = mints),
        )
    }

    @Test
    fun a402NamesWhereEachOfItsRemediesIsTakenWhateverItsCodeAndOffersNothing() {
        for (code in listOf(ApiErrorCode.PaymentRequired, null)) {
            val named = step(402, code)
            assertEquals(
                "The API's message says which applies: a product of the organization's agreement is changed with " +
                    "Mountaya (https://mountaya.com/en/contact/), and a missing payment method is added, or an " +
                    "invoice still owed paid, in the organization's billing settings.",
                named,
            )
            assertFalse(named.orEmpty().contains("ceiling"))
        }
    }

    @Test
    fun a403NamesTheScopeTheMembersRoleOrThePublishableKeyAndUserItMintsFor() {
        val forbidden =
            "Check that the key carries the scope this call needs, that the resource belongs to the key's " +
                "organization, and that the page calling is one the organization allows."

        assertEquals(forbidden, step(403, ApiErrorCode("FORBIDDEN")))
        assertEquals(
            "$forbidden This call acts as the user its session token names (postSession's user_id), who must be a " +
                "member of the key's organization holding the role the call asks.",
            step(403, ApiErrorCode("FORBIDDEN"), declaration = member),
        )
        assertEquals(
            "Check that the publishable key in the body is one this secret key's organization issued, and that " +
                "user_id names a member of that organization or a user holding an invitation to it.",
            step(403, ApiErrorCode("FORBIDDEN"), declaration = mints),
        )
        // A product not held is a 402 and a ceiling a 429: a 403 sends its reader to neither.
        for (word in listOf(
            "agreement",
            "payment method",
            "user_id",
        )) {
            assertFalse(step(403).orEmpty().contains(word), word)
        }
    }

    @Test
    fun ofThe409sTheTwoThatAnswerAKeyNameWhatToDoAndEveryOtherNone() {
        assertEquals(
            "The write was made, and its answer cannot be given again: read the resource again rather than sending " +
                "the write once more.",
            step(409, ApiErrorCode("IDEMPOTENCY_ANSWER_UNAVAILABLE")),
        )
        assertEquals(
            "The first request with this Idempotency-Key is still running: send the same call again with the " +
                "error's idempotencyKey, never a new key, which would make the write twice.",
            step(409, ApiErrorCode("IDEMPOTENCY_KEY_IN_USE")),
        )
        assertNull(step(409, ApiErrorCode("CONFLICT")))
        assertNull(step(409))
    }

    @Test
    fun a412SaysTheResourceChangedSinceTheTagTheWriteNames() {
        assertEquals(
            "The resource changed since the etag this write names was read: read it again, and decide what the write " +
                "should be now.",
            step(412, ApiErrorCode("PRECONDITION_FAILED")),
        )
    }

    @Test
    fun a429NamesTheWaitOneSecondInTheSingularAndAWaitOfNoneWithoutANumber() {
        assertEquals("Wait before calling again.", step(429, ApiErrorCode("TOO_MANY_REQUESTS")))
        assertEquals("Wait before calling again.", step(429, retryAfter = Duration.ZERO))
        assertEquals("Wait 1 second before calling again.", step(429, retryAfter = 1.seconds))
        assertEquals("Wait 120 seconds before calling again.", step(429, retryAfter = 120.seconds))
        assertEquals(
            "Wait 30 seconds before calling again. Minting is limited per secret key, per client IP address and " +
                "secret key whose requests keep failing, and per user_id it refuses, so check the publishable key " +
                "and the user_id before asking again.",
            step(429, retryAfter = 30.seconds, declaration = mints),
        )
    }

    @Test
    fun aCeilingNamesItsRemediesNeverAWaitBeforeASpentAllowanceOrAMintsLimit() {
        val spent = RateLimitState(listOf(RateLimitPolicy("month", 1, remaining = 0)))

        assertEquals(NextStep.ceiling, step(429, ApiErrorCode("LIMIT_REACHED")))
        assertEquals(NextStep.ceiling, step(429, ApiErrorCode("LIMIT_REACHED"), declaration = mints))
        assertEquals(
            NextStep.ceiling,
            NextStep.step(429, ApiErrorCode("LIMIT_REACHED"), 5.seconds, spent, NextStep.Declaration.none, null),
        )
    }

    @Test
    fun aSpentAllowanceNamesWhenItRenewsBeforeTheWaitAndAMintsLimit() {
        val spent = RateLimitState(listOf(RateLimitPolicy("month", 1, remaining = 0)))

        assertEquals(
            MonthlyAllowance.step(60.seconds, null),
            NextStep.step(429, ApiErrorCode("TOO_MANY_REQUESTS"), 60.seconds, spent, mints, null),
        )
    }

    @Test
    fun everyOtherStatusNamesNothingBeyondTheApisOwnWords() {
        for (status in listOf(200, 400, 404, 405, 413, 418, 500, 502, 503, 504)) {
            assertNull(step(status, ApiErrorCode("ANY"), 3.seconds, member), "$status")
        }
    }

    @Test
    fun theMessageEndsOnTheStepAPeriodAddedWhereTheApisWordsEndInNone() {
        assertEquals(
            "403 FORBIDDEN: Access is forbidden. Check that the key carries the scope this call needs, that the " +
                "resource belongs to the key's organization, and that the page calling is one the organization allows.",
            ForbiddenException(listOf(ErrorEntry("Access is forbidden", ApiErrorCode("FORBIDDEN")))).message,
        )
        assertTrue(
            ApiException
                .of(412, listOf(ErrorEntry("Stale!", ApiErrorCode("PRECONDITION_FAILED"))))
                .message
                .startsWith("412 PRECONDITION_FAILED: Stale! The resource changed"),
        )
        assertTrue(ApiException.of(412).message.startsWith("412. The resource changed"))
        assertEquals(
            "404 NOT_FOUND: Itinerary not found.",
            ApiException.of(404, listOf(ErrorEntry("Itinerary not found.", ApiErrorCode("NOT_FOUND")))).message,
        )
        assertEquals(
            "RateLimitError: 429 LIMIT_REACHED: No seat left. ${NextStep.ceiling}",
            RateLimitException(listOf(ErrorEntry("No seat left.", ApiErrorCode("LIMIT_REACHED")))).toString(),
        )
    }

    @Test
    fun anAnswersErrorNamesTheStepItsOperationsDeclarationSharpens() {
        val refused =
            ErrorEnvelope.read(
                """{"errors":[{"message":"refused","extensions":{"code":"UNAUTHORIZED"}}]}""".encodeToByteArray(),
            )
        val error =
            AnswerError.exception(
                401,
                FoldedHeaders(emptyMap()),
                refused,
                Instant.fromEpochSeconds(0),
                null,
                declaration = member,
            )

        assertTrue(error.message.startsWith("401 UNAUTHORIZED: refused. Check the credential"))
        assertTrue(error.message.endsWith("mint the token with postSession's user_id set to that user."))
        val before = error.message
        error.idempotencyKey = "k"
        assertEquals(before, error.message)
    }

    @Test
    fun aCallsErrorNamesItsOperationsStepThroughTheClient() =
        runTest {
            val client =
                Mountaya(
                    Credential.Publishable("pk_test_a", SessionToken.Value("sess_a")),
                    exchange = Refusing(403, "FORBIDDEN"),
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            val refused =
                assertFailsWith<ForbiddenException> {
                    client.studio.itineraries.patchItinerary("it", RestSchema.ItineraryUpdateParams())
                }
            assertTrue(
                refused.message.endsWith(
                    "This call acts as the user its session token names (postSession's user_id), who must be a " +
                        "member " +
                        "of the key's organization holding the role the call asks.",
                ),
                refused.message,
            )
        }

    /** Refusing answers every attempt with one refusal. */
    private class Refusing(
        private val status: Int,
        private val code: String,
    ) : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse =
            ExchangeResponse(
                status,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(
                    """{"errors":[{"message":"refused","extensions":{"code":"$code"}}]}""".encodeToByteArray(),
                ),
            )
    }
}
