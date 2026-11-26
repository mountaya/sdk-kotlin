package com.mountaya.conformance

import com.mountaya.client.BaseUrls
import com.mountaya.client.Url
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.ScriptedMapSurface
import kotlinx.coroutines.test.TestScope
import kotlin.math.floor

/** The scheme a URL starts with, which `BaseUrls` asks of every origin. */
private val SCHEME = Regex("[A-Za-z][A-Za-z0-9+.-]*:.+")

/** The origin a Tile API address written as a path of a page's own origin is read under, a page no native app has. */
private const val PAGE_ORIGIN: String = "https://page.test"

/**
 * `map.events.access-refused`: how a failure of one of the map's own requests is told, through the engine's
 * `events`. The layers manifest's read is the engine's own `setTerrain` through a client reaching the Tile API
 * at the case's `tilesBaseUrl`, whose exchange answers its status, or fails with none, and repeats nothing; a
 * renderer's request is played on the scripted surface, a status written as text being one that is no number,
 * which a renderer reports as none, sent under the credential its `sentUnder` names, which the surface hands the
 * engine as `MapView`'s renderer hands it what its request hook read off the request, so a refusal of a credential
 * the client no longer sends is dropped by the engine's own rule and by nothing here. The refusing statuses are
 * held to those the engine tells as refusals, every status from 100 to 599 played, and the manifest's path to the
 * one a refusal of it is told at.
 *
 * A Tile API address written as a path of the page's own origin, `/tiles`, names no origin `BaseUrls`
 * takes, since a native app has no page: the case is played on the page's origin the case states, or a stand-in,
 * `https://page.test`, where it states none, and the path, and the address told is answered relative to that
 * origin, so the join the engine makes is the one the case reads.
 *
 * A request whose credential is `unreadable` never leaves, so the renderer reports it with no status heard and with
 * what the client's read threw, as `MapView`'s request hook hands it, and the error told is read as one only where it
 * carries that very failure as its cause; a style asked for `again` is refused once more, under the credential the case
 * names for that second request.
 */
object MapEventsAccessRefusedRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "manifestPath" to FixtureValue.Text(manifestPath(scope)),
                    "refusalStatuses" to
                        FixtureValue.Array(refusingStatuses(scope).map { FixtureValue.Number(it.toDouble()) }),
                ),
        )
        val input = case.fields
        val failure = input.objectOrNull("failure") ?: throw AssertionError("the input states no `failure`")
        val status = (failure["status"] as? FixtureValue.Number)?.value?.takeIf { it == floor(it) }?.toInt()
        val written = input.text("tilesBaseUrl")
        val relative = !SCHEME.matches(written)
        val page = input.textOrNull("pageOrigin") ?: PAGE_ORIGIN
        val tiles = if (relative) page + written else written
        val request = failure.text("request")
        val url = failure.textOrNull("url")
        // A request whose credential could not be read never leaves, so no status is heard of it.
        val unreadable =
            when (val credential = failure.textOrNull("credential")) {
                null -> false
                "unreadable" -> true
                else -> throw AssertionError("no credential is `$credential`")
            }
        val again = failure.textOrNull("again")
        // What the client's read of the credential threw, which the error of a request that never left carries.
        val read = if (unreadable) IllegalStateException("The client's credential could not be read.") else null

        val exchange =
            MapFixtures.Recording(
                status?.let { MapFixtures.Recording.Answer.Status(it) } ?: MapFixtures.Recording.Answer.Failing,
            )
        val map =
            MapEngine(MapFixtures.client(BaseUrls(tiles = Url(tiles)), exchange), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        val told =
            MapFixtures.told(scope, map) {
                when (request) {
                    "manifest" -> {
                        try {
                            map.setTerrain()
                        } catch (failed: Exception) {
                            // The client's failure is what the map tells, which is the answer read here.
                        }
                    }

                    "renderer" -> {
                        val heard = if (unreadable) null else status
                        surface.failRequest(url, heard, null, MapFixtures.isSuperseded(failure), read)
                        // A style refused under a credential the client no longer sends is asked for once more, and
                        // its second answer is told as its own credential has it told.
                        if (again != null) {
                            if (again != "current" && again != "superseded") {
                                throw AssertionError("no credential is `$again`")
                            }
                            surface.failRequest(url, heard, null, again == "superseded", read)
                        }
                    }

                    else -> {
                        throw AssertionError("no request is called `$request`")
                    }
                }
            }

        var refused: FixtureValue = FixtureValue.Null
        var error = false
        for (event in told) {
            when (event) {
                is MapEvent.AccessRefused -> {
                    if (refused !is FixtureValue.Null) throw AssertionError("two refusals were told")
                    val address = if (relative) event.url.removePrefix(page) else event.url
                    refused =
                        MapFixtures.objectOf(
                            "status" to FixtureValue.Number(event.status.toDouble()),
                            "url" to FixtureValue.Text(address),
                        )
                }

                is MapEvent.Failure -> {
                    // The error of a request whose credential could not be read is one only where it carries what
                    // the read threw.
                    if (read == null || event.cause === read) error = true
                }

                else -> {
                    throw AssertionError("a failure told $event")
                }
            }
        }
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "accessRefused" to refused,
                "error" to FixtureValue.Bool(error),
            ),
        )
    }

    /** Every status from 100 to 599 the engine tells, failing a renderer's request, as a refusal of access. */
    private suspend fun refusingStatuses(scope: TestScope): List<Int> =
        (100..599).filter { status ->
            val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
            val surface = ScriptedMapSurface(map)
            val told = MapFixtures.told(scope, map) { surface.failRequest("https://tiles.test/x", status, null) }
            told.firstOrNull() is MapEvent.AccessRefused
        }

    /** The path a refusal of the layers manifest is told at, past the Tile API's address. */
    private suspend fun manifestPath(scope: TestScope): String {
        val base = "https://tiles.test"
        val map =
            MapEngine(
                MapFixtures.client(
                    BaseUrls(tiles = Url(base)),
                    MapFixtures.Recording(MapFixtures.Recording.Answer.Status(403)),
                ),
                geolocation = InertGeolocation(),
            )
        val told =
            MapFixtures.told(scope, map) {
                try {
                    map.setTerrain()
                } catch (refused: Exception) {
                    // The refusal is what the map tells, which is the answer read here.
                }
            }
        val refusal = told.firstOrNull() as? MapEvent.AccessRefused
        if (refusal == null || !refusal.url.startsWith(base)) throw AssertionError("a refused manifest told $told")
        return refusal.url.removePrefix(base)
    }
}
