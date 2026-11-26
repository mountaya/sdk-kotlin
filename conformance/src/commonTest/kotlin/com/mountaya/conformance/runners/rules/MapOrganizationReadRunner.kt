package com.mountaya.conformance

import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * `map.organization.read`: the case's script played on maps a consumer makes, through the published engine. A `map`
 * makes a map over a client holding the case's publishable `key`, one client for each key, or a share token where the
 * key is null; a `load` asks the map for its organization in a coroutine of its own; an `answer` settles the read it
 * numbers with an organization named after it, of its `whiteLabel`; a `fail` drops its connection; a `timeout` lets the
 * test's clock run past the client's own time budget, so the transport fails the read as it fails any call that reaches
 * it; and a `close` closes the map. Every client sends to a REST origin of the case's own, as two apps' clients of one
 * key never share a process, and no read may carry a credential.
 *
 * The answer reads `reads`, the body each read sent, `loads`, what each load got, the read whose organization it was,
 * `failed`, `no-key`, `closed` or null for one still waiting, and `held`, the read whose organization each map holds.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapOrganizationReadRunner : ConformanceRunner {
    /** The headers a credential rides in, none of which a read of an organization carries. */
    private val credentialHeaders = setOf("authorization", "x-api-key", "x-session-token", "x-share-token")

    /** The exchange of one case: every request held until the script settles it. */
    private class HeldExchange : HttpExchange {
        val sent = mutableListOf<ExchangeRequest>()
        val answers = mutableListOf<CompletableDeferred<ExchangeResponse>>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val answer = CompletableDeferred<ExchangeResponse>()
            sent += request
            answers += answer
            return answer.await()
        }
    }

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val exchange = HeldExchange()
        val dispatcher = StandardTestDispatcher(scope.testScheduler)
        val origin = "https://case-${case.name.hashCode().toUInt()}.organization.test"
        val clients = mutableMapOf<String?, Mountaya>()
        val maps = mutableListOf<MapEngine>()
        val loads = mutableListOf<FixtureValue?>()

        fun client(key: String?): Mountaya =
            clients.getOrPut(key) {
                Mountaya(
                    credential = key?.let { Credential.Publishable(it) } ?: Credential.Share("share_case"),
                    baseUrls = BaseUrls(rest = Url(origin)),
                    retry = Scripted.noRetries,
                    timeout = 5.seconds,
                    exchange = exchange,
                    dispatcher = dispatcher,
                )
            }

        for (step in case.fields.items("steps")) {
            val written = step as? FixtureValue.Object ?: throw AssertionError("a step is no object")
            when (val kind = written.text("step")) {
                "map" -> {
                    maps += MapEngine(client(written.textOrNull("key")), geolocation = InertGeolocation())
                }

                "load" -> {
                    val map = maps[written.number("map").toInt()]
                    val load = loads.size
                    loads += null
                    scope.backgroundScope.launch {
                        loads[load] =
                            try {
                                map.loadOrganization()?.let { FixtureValue.Number(readOf(it.name).toDouble()) }
                                    ?: FixtureValue.Text("no-key")
                            } catch (closed: MapException) {
                                if (closed.kind != MapException.Kind.Closed) throw closed
                                FixtureValue.Text("closed")
                            } catch (failed: Exception) {
                                FixtureValue.Text("failed")
                            }
                    }
                }

                "answer" -> {
                    val read = written.number("read").toInt()
                    exchange.answers[read].complete(
                        ExchangeResponse(
                            200,
                            mapOf("Content-Type" to "application/json", "X-Request-Id" to "0".repeat(32)),
                            ExchangeBody(organization(read, written.bool("whiteLabel")).encodeToByteArray()),
                        ),
                    )
                }

                "fail" -> {
                    exchange.answers[written.number("read").toInt()].completeExceptionally(Scripted.Dropped())
                }

                "timeout" -> {
                    // The read stays unanswered past the client's own time budget, which fails it.
                    scope.testScheduler.advanceTimeBy(1.minutes)
                }

                "close" -> {
                    maps[written.number("map").toInt()].close()
                }

                else -> {
                    throw AssertionError("no step is called `$kind`")
                }
            }
            scope.testScheduler.runCurrent()
        }

        val reads =
            exchange.sent.map { request ->
                val carried =
                    request.headers.keys
                        .map { it.lowercase() }
                        .filter { it in credentialHeaders }
                if (carried.isNotEmpty()) throw AssertionError("a read of the organization carried $carried")
                val body = request.body?.decodeToString() ?: throw AssertionError("a read sent no body")
                fixtureValueOf(Json.parseToJsonElement(body))
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "held" to
                        FixtureValue.Array(
                            maps.map { map ->
                                map.organization?.let { FixtureValue.Number(readOf(it.name).toDouble()) }
                                    ?: FixtureValue.Null
                            },
                        ),
                    "loads" to FixtureValue.Array(loads.map { it ?: FixtureValue.Null }),
                    "reads" to FixtureValue.Array(reads),
                ),
            ),
        )
    }

    /** The read an organization this runner answered was answered by, which its name carries. */
    private fun readOf(name: String): Int =
        name.removePrefix("read-").toIntOrNull()
            ?: throw AssertionError("`$name` is no organization this case answered")

    /** The envelope a read numbered [read] is answered with: an organization named after it, of [whiteLabel]. */
    private fun organization(
        read: Int,
        whiteLabel: Boolean,
    ): String =
        """{"data": {
            "created_at": "2026-01-01T00:00:00Z", "etag": "\"e\"", "id": "00000000-0000-4000-8000-000000000000",
            "name": "read-$read", "awaiting_agreement": false, "updated_at": "2026-01-01T00:00:00Z",
            "white_label": $whiteLabel,
            "settings": {
                "etag": "\"s\"",
                "default_props": {
                    "preferred_language": "en", "preferred_theme": "auto", "preferred_unit_length": "metric",
                    "preferred_unit_pace": "per-time", "preferred_unit_slope": "degrees",
                    "preferred_unit_temperature": "celsius", "segments_color": "#bc4a51", "terrain": "",
                    "terrain_variant": "light", "with_access_restrictions": true, "with_collection_itineraries": true,
                    "with_collection_zones": true, "with_directions_control": true, "with_download_control": true,
                    "with_navigation_control": true, "with_overlays": [], "with_overlays_control": false,
                    "with_preferences_control": true, "with_profile_analysis": true,
                    "with_profile_characteristics": true, "with_profile_elevation": true,
                    "with_profile_pace_control": true, "with_streetview_control": true, "with_terrains": []
                },
                "safety_rules": {
                    "boundaries": [], "domains": [], "require_session_token": false,
                    "with_collection_itineraries": true, "with_collection_zones": true, "with_overlays": [],
                    "with_overlays_control": false, "with_profile_analysis": true, "with_profile_elevation": true,
                    "zoom_max": 22, "zoom_min": 0
                },
                "theme": {"colors": {"ui": ["#ecefff", "#d5dafb", "#a9b1f1", "#7a87e9", "#5362e1", "#3a4bdd",
                    "#2c40dc", "#1f32c4", "#182cb0", "#0a259c"]}}
            }
        }}"""
}
