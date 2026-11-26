package com.mountaya.elements

import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.RetryPolicy
import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.watermarkOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MapEngineOrganizationReadTest {
    /** Holds every read until the test answers it, or fails it with what the test hands. */
    private class Held : HttpExchange {
        val answers = mutableListOf<CompletableDeferred<ExchangeResponse>>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val answer = CompletableDeferred<ExchangeResponse>()
            answers += answer
            return answer.await()
        }
    }

    /** A map over a client of [origin], its own for each test, whose reads [held] holds, repeated at once. */
    private fun TestScope.map(
        origin: String,
        held: Held,
        credential: Credential = Credential.Publishable("pk_test_watermark"),
    ): MapEngine =
        MapEngine(
            Mountaya(
                credential,
                baseUrls = BaseUrls(rest = Url(origin)),
                retry = RetryPolicy(maxRetries = 2, baseDelay = Duration.ZERO, maxDelay = Duration.ZERO),
                exchange = held,
                dispatcher = StandardTestDispatcher(testScheduler),
            ),
            geolocation = InertGeolocation(),
        )

    /** Asks [map] for its organization beside the test, as a view drawing it does, whatever the read ends in. */
    private fun TestScope.load(map: MapEngine) {
        backgroundScope.launch {
            try {
                map.loadOrganization()
            } catch (failed: Exception) {
                // The read's standing is what the test reads.
            }
        }
        runCurrent()
    }

    @Test
    fun aReadWaitingPastOneAttemptsBudgetDrawsTheMarkWhileItGoesOnAndALaterWhiteLabelTakesItAway() =
        runTest {
            val held = Held()
            val map = map("https://window.organization.test", held)
            load(map)

            advanceTimeBy(35.seconds - 1.milliseconds)
            assertEquals<OrganizationRead>(OrganizationRead.Waiting, map.organizationRead.value)
            assertNull(watermarkOf(map.organizationRead.value, RestSchema.Language.En))

            advanceTimeBy(1.milliseconds)
            runCurrent()
            assertEquals<OrganizationRead>(OrganizationRead.TimedOut, map.organizationRead.value)
            assertNotNull(
                watermarkOf(map.organizationRead.value, RestSchema.Language.En),
                "a read waiting past one attempt's budget draws the watermark",
            )
            assertEquals(2, held.answers.size, "the read's further attempt goes on")

            held.answers.last().complete(answer(whiteLabel = true))
            runCurrent()
            assertEquals<OrganizationRead>(OrganizationRead.Answered(whiteLabel = true), map.organizationRead.value)
            assertNull(watermarkOf(map.organizationRead.value, RestSchema.Language.En))
        }

    @Test
    fun aHostCollectingTheReadIsToldEachStandingFromTheMapsMakingAndAClosedMapLeavesItWhereItStood() =
        runTest {
            val held = Held()
            val map = map("https://host.organization.test", held)
            val told = mutableListOf<OrganizationRead>()
            backgroundScope.launch { map.organizationRead.collect { told += it } }
            runCurrent()
            assertEquals(
                listOf<OrganizationRead>(OrganizationRead.Waiting),
                told,
                "a map stands waiting from its making, before any load",
            )

            load(map)
            advanceTimeBy(35.seconds)
            runCurrent()
            held.answers.last().complete(answer(whiteLabel = false))
            runCurrent()
            map.close()
            runCurrent()

            assertEquals(
                listOf(
                    OrganizationRead.Waiting,
                    OrganizationRead.TimedOut,
                    OrganizationRead.Answered(whiteLabel = false),
                ),
                told,
                "a host is told each standing in turn",
            )
            assertEquals<OrganizationRead>(
                OrganizationRead.Answered(whiteLabel = false),
                map.organizationRead.value,
                "a closed map leaves the read where it stood",
            )
            assertNotNull(
                watermarkOf(map.organizationRead.value, RestSchema.Language.En),
                "an answer of no white label draws the mark",
            )
        }

    @Test
    fun aReadAnsweredWithinOneAttemptsBudgetNeverDrawsTheMarkOfAWhiteLabelOrganization() =
        runTest {
            val held = Held()
            val map = map("https://prompt.organization.test", held)
            load(map)

            advanceTimeBy(20.seconds)
            held.answers.single().complete(answer(whiteLabel = true))
            runCurrent()
            advanceTimeBy(1.seconds)

            assertEquals<OrganizationRead>(OrganizationRead.Answered(whiteLabel = true), map.organizationRead.value)
            assertNull(watermarkOf(map.organizationRead.value, RestSchema.Language.En))
        }

    @Test
    fun aReadThatFailsDrawsTheMarkAndSoDoesOneMeetingACancellationThatIsNotTheCallers() =
        runTest {
            val refused = Held()
            val failing = map("https://refused.organization.test", refused)
            load(failing)
            refused.answers.single().complete(
                ExchangeResponse(
                    404,
                    mapOf("Content-Type" to "application/json"),
                    ExchangeBody("""{"errors":[]}""".encodeToByteArray()),
                ),
            )
            runCurrent()

            val cancelled = Held()
            val closing = map("https://cancelled.organization.test", cancelled)
            load(closing)
            cancelled.answers.single().completeExceptionally(CancellationException("the client's engine closed"))
            runCurrent()

            assertEquals<OrganizationRead>(OrganizationRead.Failed, failing.organizationRead.value)
            assertEquals<OrganizationRead>(OrganizationRead.Failed, closing.organizationRead.value)
            assertNotNull(
                watermarkOf(closing.organizationRead.value, RestSchema.Language.En),
                "a read meeting a cancellation that is not the caller's draws the watermark",
            )
        }

    @Test
    fun aClientHoldingNoPublishableKeyDrawsTheMarkAtOnceAndReadsNothing() =
        runTest {
            val held = Held()
            val map = map("https://no-key-drawn.organization.test", held, Credential.Share("share_test_watermark"))
            load(map)

            assertEquals<OrganizationRead>(OrganizationRead.NoKey, map.organizationRead.value)
            assertNotNull(
                watermarkOf(map.organizationRead.value, RestSchema.Language.En),
                "a client holding no publishable key draws the watermark",
            )
            assertEquals(0, held.answers.size)
        }

    /** A load of a map whose client holds no publishable key answers null and never throws, a closed map's among them. */
    @Test
    fun aLoadWithNoPublishableKeyAnswersNullAndNeverThrowsAClosedMapsAmongThem() =
        runTest {
            val held = Held()
            val map = map("https://no-key.organization.test", held, Credential.Share("share_test_watermark"))

            assertNull(map.loadOrganization())
            map.close()
            assertNull(map.loadOrganization(), "a closed map with no key answers null too")
            assertEquals<OrganizationRead>(OrganizationRead.NoKey, map.organizationRead.value)
            assertEquals(0, held.answers.size)
        }

    private companion object {
        /** An answer of an organization of [whiteLabel]. */
        fun answer(whiteLabel: Boolean): ExchangeResponse =
            ExchangeResponse(
                200,
                mapOf("Content-Type" to "application/json"),
                ExchangeBody(
                    """{"data": {"created_at": "2026-01-01T00:00:00Z", "etag": "e",
                    "id": "00000000-0000-4000-8000-000000000000", "name": "window", "awaiting_agreement": false,
                    "updated_at": "2026-01-01T00:00:00Z", "white_label": $whiteLabel,
                    "settings": {"etag": "s",
                      "default_props": {"preferred_language": "en", "preferred_theme": "auto",
                        "preferred_unit_length": "metric", "preferred_unit_pace": "per-time",
                        "preferred_unit_slope": "degrees", "preferred_unit_temperature": "celsius",
                        "segments_color": "#bc4a51", "terrain": "", "terrain_variant": "light",
                        "with_access_restrictions": true, "with_collection_itineraries": true,
                        "with_collection_zones": true, "with_directions_control": true, "with_download_control": true,
                        "with_navigation_control": true, "with_overlays": [], "with_overlays_control": false,
                        "with_preferences_control": true, "with_profile_analysis": true,
                        "with_profile_characteristics": true, "with_profile_elevation": true,
                        "with_profile_pace_control": true, "with_streetview_control": true, "with_terrains": []},
                      "safety_rules": {"boundaries": [], "domains": [], "require_session_token": false,
                        "with_collection_itineraries": true, "with_collection_zones": true, "with_overlays": [],
                        "with_overlays_control": false, "with_profile_analysis": true, "with_profile_elevation": true,
                        "zoom_max": 22, "zoom_min": 0},
                      "theme": {"colors": {"ui": []}}}}}""".encodeToByteArray(),
                ),
            )
    }
}
