package com.mountaya.elements

import com.mountaya.client.BaseUrls
import com.mountaya.client.ConnectionException
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.RetryPolicy
import com.mountaya.client.Url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OrganizationReadsTest {
    /** Answers each read with an organization named after the read, or fails it where [failing]. */
    private class Lookups(
        var failing: Boolean = false,
    ) : HttpExchange {
        val sent = mutableListOf<ExchangeRequest>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            sent += request
            if (failing) {
                return ExchangeResponse(
                    500,
                    mapOf("Content-Type" to "application/json"),
                    ExchangeBody("""{"errors":[]}""".encodeToByteArray()),
                )
            }
            return ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(organization("read-${sent.size - 1}").encodeToByteArray()),
            )
        }
    }

    private fun client(
        origin: String,
        exchange: HttpExchange,
    ): Mountaya =
        Mountaya(
            Credential.Publishable("pk_test_reads"),
            baseUrls = BaseUrls(rest = Url(origin)),
            retry = RetryPolicy(maxRetries = 0, baseDelay = Duration.ZERO, maxDelay = Duration.ZERO),
            exchange = exchange,
            dispatcher = Dispatchers.Unconfined,
        )

    @Test
    fun oneReadOfAKeyIsKeptAndServesEveryClientOfItsOrigin() =
        runTest {
            val lookups = Lookups()
            val first =
                OrganizationReads
                    .request(
                        client("https://kept.organization.test", lookups),
                        "pk_test_reads",
                    ).outcome
                    .await()
            val second =
                OrganizationReads
                    .request(
                        client("https://kept.organization.test", lookups),
                        "pk_test_reads",
                    ).outcome
                    .await()

            assertEquals("read-0", first.name)
            assertEquals("read-0", second.name)
            assertEquals(1, lookups.sent.size)
            assertEquals(
                """{"publishable_key":"pk_test_reads"}""",
                lookups.sent
                    .single()
                    .body
                    ?.decodeToString(),
            )
        }

    @Test
    fun aReadOfAnotherOriginIsAReadOfItsOwn() =
        runTest {
            val lookups = Lookups()
            OrganizationReads.request(client("https://one.organization.test", lookups), "pk_test_reads").outcome.await()
            OrganizationReads.request(client("https://two.organization.test", lookups), "pk_test_reads").outcome.await()

            assertEquals(2, lookups.sent.size)
        }

    @Test
    fun aReadThatFailsIsKeptByNobody() =
        runTest {
            val lookups = Lookups(failing = true)
            val reads = client("https://failing.organization.test", lookups)

            assertFailsWith<Exception> { OrganizationReads.request(reads, "pk_test_reads").outcome.await() }
            lookups.failing = false
            assertEquals(
                "read-1",
                OrganizationReads
                    .request(reads, "pk_test_reads")
                    .outcome
                    .await()
                    .name,
            )
        }

    /** Holds every read until the test answers it, or fails it with what the test hands. */
    private class Held : HttpExchange {
        val answers = mutableListOf<CompletableDeferred<ExchangeResponse>>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val answer = CompletableDeferred<ExchangeResponse>()
            answers += answer
            return answer.await()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun aReadIsOverdueOnceOneAttemptsBudgetPassesWithNoAnswerAndItsAnswerStillLands() =
        runTest {
            val held = Held()
            val reads =
                Mountaya(
                    Credential.Publishable("pk_test_reads"),
                    baseUrls = BaseUrls(rest = Url("https://overdue.organization.test")),
                    retry = RetryPolicy(maxRetries = 2, baseDelay = Duration.ZERO, maxDelay = Duration.ZERO),
                    exchange = held,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )
            val read = OrganizationReads.request(reads, "pk_test_reads")
            runCurrent()

            advanceTimeBy(35.seconds - 1.milliseconds)
            assertFalse(read.overdue.isCompleted, "the read is not overdue within one attempt's budget")
            advanceTimeBy(1.milliseconds)
            runCurrent()
            assertTrue(read.overdue.isCompleted, "the read is overdue once one attempt's budget has passed")
            assertFalse(read.outcome.isCompleted, "the read goes on")
            assertEquals(2, held.answers.size, "a further attempt is out")

            held.answers.last().complete(
                ExchangeResponse(
                    200,
                    mapOf("Content-Type" to "application/json"),
                    ExchangeBody(organization("late").encodeToByteArray()),
                ),
            )
            assertEquals("late", read.outcome.await().name)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun aCancellationTheReadMeetsFailsItAsARequestThatNeverArrived() =
        runTest {
            val held = Held()
            val reads =
                Mountaya(
                    Credential.Publishable("pk_test_reads"),
                    baseUrls = BaseUrls(rest = Url("https://cancelled.organization.test")),
                    retry = RetryPolicy(maxRetries = 0, baseDelay = Duration.ZERO, maxDelay = Duration.ZERO),
                    exchange = held,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )
            val read = OrganizationReads.request(reads, "pk_test_reads")
            runCurrent()
            held.answers.single().completeExceptionally(CancellationException("the client's engine closed"))
            runCurrent()

            val failure = assertFailsWith<ConnectionException> { read.outcome.await() }
            assertIs<CancellationException>(failure.cause)
            assertFalse(read.overdue.isCompleted)
        }

    private companion object {
        fun organization(name: String): String =
            """{"data": {"created_at": "2026-01-01T00:00:00Z", "etag": "e", "id": "00000000-0000-4000-8000-000000000000",
            "name": "$name", "awaiting_agreement": false, "updated_at": "2026-01-01T00:00:00Z", "white_label": false,
            "settings": {"etag": "s",
              "default_props": {"preferred_language": "en", "preferred_theme": "auto", "preferred_unit_length": "metric",
                "preferred_unit_pace": "per-time", "preferred_unit_slope": "degrees", "preferred_unit_temperature": "celsius",
                "segments_color": "#bc4a51", "terrain": "", "terrain_variant": "light", "with_access_restrictions": true,
                "with_collection_itineraries": true, "with_collection_zones": true, "with_directions_control": true,
                "with_download_control": true, "with_navigation_control": true, "with_overlays": [],
                "with_overlays_control": false, "with_preferences_control": true, "with_profile_analysis": true,
                "with_profile_characteristics": true, "with_profile_elevation": true, "with_profile_pace_control": true,
                "with_streetview_control": true, "with_terrains": []},
              "safety_rules": {"boundaries": [], "domains": [], "require_session_token": false,
                "with_collection_itineraries": true, "with_collection_zones": true, "with_overlays": [],
                "with_overlays_control": false, "with_profile_analysis": true, "with_profile_elevation": true,
                "zoom_max": 22, "zoom_min": 0},
              "theme": {"colors": {"ui": []}}}}}"""
    }
}
