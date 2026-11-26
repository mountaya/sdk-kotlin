package com.example.consumer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.ItineraryProfile
import com.mountaya.elements.ItinerarySegmentTable
import com.mountaya.elements.ItinerarySparkline
import com.mountaya.elements.ItineraryWaypointTable
import com.mountaya.elements.LocalMountayaScope
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapView
import com.mountaya.elements.MountayaScope
import com.mountaya.elements.ProfileColoring
import com.mountaya.elements.ProfilePart
import com.mountaya.elements.WaypointCard
import com.mountaya.geo.DateLength
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.ElevationSeries
import com.mountaya.geo.ItineraryPlace
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import com.mountaya.geo.ProfileAnchor
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.SegmentTableColumn
import com.mountaya.geo.TimeLength
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.slopeBuckets
import java.util.TimeZone
import kotlin.time.Instant

/**
 * Roadbook reads the data elements as an Android app does: the README's profile under a map and its two tables over
 * one cache of the app's client, every option a profile and a table take, and a place's card beside its sparkline and
 * with its links taken over.
 */
object Roadbook {
    /**
     * Shows the README's itinerary screen: the map, the profile over it driving its cursor marker, and the two tables.
     */
    @Composable
    fun ItineraryScreen(mountaya: Mountaya) {
        val applicationScope = rememberCoroutineScope()
        val itineraries =
            remember {
                ItineraryResourceCache(applicationScope) { id, surface ->
                    mountaya.studio.itineraries
                        .getItinerary(id) { if (surface != null) this.surface = surface }
                        .data
                }
            }
        val map = remember { MapEngine(mountaya) }
        LaunchedEffect(map) { map.setTerrain() }
        val locale = LocalMountayaScope.current.locale
        val zone = TimeZone.getDefault().id
        val writeCutoff = remember(locale, zone) { cutoffWriter(locale, zone) }
        Column {
            MapView(map, Modifier.fillMaxWidth().height(320.dp))
            ItineraryProfile(
                itineraryId = "itin_abc",
                cache = itineraries,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                map = map,
                parts =
                    setOf(
                        ProfilePart.Amenities,
                        ProfilePart.Tooltip,
                        ProfilePart.DistanceAxis,
                        ProfilePart.Navigator,
                    ),
            )
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ItinerarySegmentTable(itineraryId = "itin_abc", cache = itineraries, map = map)
                ItineraryWaypointTable(
                    itineraryId = "itin_abc",
                    cache = itineraries,
                    map = map,
                    formatCutoffAt = writeCutoff,
                )
            }
        }
    }

    /**
     * Shows a profile and two tables every option of which is stated, in French, the window and the choice held by the
     * app.
     */
    @Composable
    fun EveryOption(
        itineraries: ItineraryResourceCache,
        map: MapEngine,
    ) {
        val window = remember { mutableStateOf<ProfileRange?>(null) }
        val picked = remember { mutableStateOf<List<RestSchema.WaypointAmenity>>(emptyList()) }
        MountayaScope(ElementsScopeStatement(language = RestSchema.Language.Fr)) {
            // The scope's locale is French here, so each cut-off is written in French, in the device's zone.
            val locale = LocalMountayaScope.current.locale
            val zone = TimeZone.getDefault().id
            val writeCutoff = remember(locale, zone) { cutoffWriter(locale, zone) }
            Column {
                ItineraryProfile(
                    itineraryId = "itin_abc",
                    cache = itineraries,
                    modifier = Modifier.height(260.dp),
                    surface = RestSchema.ReadSurface.Published,
                    legs = null,
                    map = map,
                    units = UnitPreferences("fr"),
                    coloring = ProfileColoring.Slope(slopeBuckets),
                    bands = emptyList(),
                    parts = ProfilePart.entries.toSet(),
                    anchor = ProfileAnchor.Window,
                    window = window,
                    highlighted = "seg_1",
                    emphasisColor = "#e9501e",
                    markerColor = "#e03131",
                    formatCutoffAt = writeCutoff,
                    enabled = true,
                    onHover = { point -> seen(point?.index) },
                    onWaypointClick = { id -> seen(id) },
                    onSegmentClick = { id -> seen(id) },
                    onError = { failure -> seen(failure.message) },
                )
                ItinerarySegmentTable(
                    itineraryId = "itin_abc",
                    cache = itineraries,
                    map = map,
                    columns = SegmentTableColumn.entries,
                    highlighted = "seg_1",
                    onHover = { row -> seen(row?.key) },
                    onSegmentClick = { id -> seen(id) },
                )
                ItineraryWaypointTable(
                    itineraryId = "itin_abc",
                    cache = itineraries,
                    map = map,
                    picked = picked,
                    onHover = { row -> seen(row?.place?.id) },
                    onWaypointClick = { id -> seen(id) },
                )
            }
        }
    }

    /**
     * Shows the profile and the two tables over an engine of the app's own, which the app configures and closes and the
     * elements read and never close, each element's words its own where it names them.
     */
    @Composable
    fun OverTheAppsEngine(
        itineraries: ItineraryResourceCache,
        map: MapEngine,
    ) {
        val engine = remember { ItineraryResourceEngine(itineraries, itineraryId = "itin_abc") }
        DisposableEffect(engine) { onDispose { engine.close() } }
        Column {
            ItineraryProfile(
                engine,
                Modifier.height(220.dp),
                map = map,
                strings = mapOf("profile.title" to "Elevation"),
            )
            ItinerarySegmentTable(engine, map = map, strings = mapOf("table.title" to "Stages"))
            ItineraryWaypointTable(engine = engine, map = map, strings = mapOf("table.title" to "Huts"))
        }
        LaunchedEffect(engine) { engine.configure(itineraries, "itin_def") }
    }

    /** Shows a place's card beside its sparkline, each photo drawn by the app as its caption. */
    @Composable
    fun PlaceCard(
        place: ItineraryPlace,
        series: ElevationSeries,
    ) {
        val locale = LocalMountayaScope.current.locale
        val zone = TimeZone.getDefault().id
        val writeCutoff = remember(locale, zone) { cutoffWriter(locale, zone) }
        Column {
            ItinerarySparkline(
                series,
                Modifier.size(96.dp, 24.dp),
                lit = ProfileRange(0.0, series.totalDistance / 2),
                color = "#e8590c",
            )
            WaypointCard(
                place,
                units = UnitPreferences("en"),
                formatCutoffAt = writeCutoff,
                strings = mapOf("waypointCard.amenities" to "Services"),
                photo = { photo -> BasicText(photo.caption ?: photo.url) },
            )
        }
    }

    /**
     * Shows the README's place card whose every link the app opens itself, through the `UriHandler` it provides around
     * it.
     */
    @Composable
    fun LinkedPlaceCard(
        place: ItineraryPlace,
        opened: (String) -> Unit,
    ) {
        val links =
            remember(opened) {
                object : UriHandler {
                    override fun openUri(uri: String) {
                        opened(uri)
                    }
                }
            }
        CompositionLocalProvider(LocalUriHandler provides links) {
            WaypointCard(place)
        }
    }

    /**
     * Answers how a cut-off is written for a reader of [language], its date and its time of day in [zone], or null
     * where the platform knows no zone of that name, so no cut-off is written, as the README writes it.
     */
    private fun cutoffWriter(
        language: String,
        zone: String,
    ): ((Instant) -> String)? {
        // Whether the platform knows a zone does not hang on the instant, so one reading answers for every cut-off.
        formatDateTime(Instant.fromEpochSeconds(0), language, zone) ?: return null
        return { cutoff ->
            checkNotNull(formatDateTime(cutoff, language, zone, DateLength.Medium, TimeLength.Short)) {
                "The platform knew the zone $zone when the writer was made."
            }
        }
    }
}
