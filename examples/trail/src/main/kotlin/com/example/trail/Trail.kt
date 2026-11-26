package com.example.trail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.MountayaException
import com.mountaya.elements.CameraTarget
import com.mountaya.elements.DeviceGeolocation
import com.mountaya.elements.GeometrySlot
import com.mountaya.elements.ItineraryProfile
import com.mountaya.elements.ItinerarySegmentTable
import com.mountaya.elements.ItineraryWaypointTable
import com.mountaya.elements.MapControls
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.MapView
import com.mountaya.elements.ProfilePart
import com.mountaya.elements.SearchControl
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.NavigationStatus
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.positionAtMeters
import com.mountaya.geo.watermarkClearance
import com.mountaya.navigation.SpeechNavigationVoice
import java.util.Locale
import kotlin.time.Clock

/** The colours of Trail's own chrome, around the elements, which draw in their own tokens. */
private class Palette(
    val sheet: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
)

/** The zoom the camera follows the traveller at, a walker's view of the paths around. */
private const val FOLLOWING_ZOOM = 16.0

/**
 * How far below the top of the map's safe area the search box starts: below the Mountaya watermark's frame, which the
 * map draws in that corner and whose extent the SDK publishes, with 8 density-independent pixels between them, so the
 * box never covers the mark.
 */
private val SEARCH_BOX_TOP = watermarkClearance.dp + 8.dp

/**
 * Places the search box over the map, which is drawn full bleed: inside the window's safe area, as the map measures the
 * watermark from it, and below the watermark's frame, so the mark stands clear of the status bar and of the box.
 */
internal fun Modifier.searchBoxPlacement(): Modifier =
    safeDrawingPadding().padding(start = 12.dp, top = SEARCH_BOX_TOP, end = 76.dp)

/**
 * Caps the sheet's top below the Mountaya watermark's frame, which the map draws at the top left of its safe area: the
 * sheet, laid at the bottom of the screen, is offered no more than the screen's height less the safe area's top and the
 * frame's extent below it, `watermarkClearance`, so however tall it grows it never rises into the frame.
 */
@Composable
internal fun Modifier.sheetPlacement(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)).padding(top = watermarkClearance.dp)

private val light = Palette(Color.White, Color(0xFF212529), Color(0xFFADB5BD), Color(0xFF2B8A3E))
private val dark = Palette(Color(0xFF1F2326), Color(0xFFF1F3F5), Color(0xFF495057), Color(0xFF40C057))

/**
 * Trail is the app's root: one portrait screen of a map, a search box over it and a bottom sheet holding the itinerary's
 * elevation profile, its tables and a navigation along it, everything read from [settings], the one value a test hands
 * in place of the app's own.
 */
@Composable
fun Trail(settings: TrailSettings) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val language = remember { Locale.getDefault().toLanguageTag() }
    val timeZone = remember { deviceTimeZone() }
    val palette = if (isSystemInDarkTheme()) dark else light

    // trail: trail.client
    val client = remember(settings) { trailClient(settings, scope, language) }
    DisposableEffect(client) { onDispose { client?.close() } }

    // trail: trail.itinerary
    val itinerary = remember(client) { TrailItinerary(client, TrailItinerary.demo(context.assets)) }
    val itineraries = remember(itinerary) { ItineraryResourceCache(scope, itinerary) }
    val itineraryId = settings.itineraryId?.takeIf { client != null } ?: itinerary.demo.id

    // trail: trail.map
    val map = remember(client) { client?.let { MapEngine(it, geolocation = DeviceGeolocation(context)) } }
    DisposableEffect(map) { onDispose { map?.close() } }
    val units = remember { mutableStateOf(UnitPreferences(language)) }

    // trail: trail.voice
    // One voice for the app, prepared as it starts, so a speech engine that cannot start is known before Start.
    val voice = remember { SpeechNavigationVoice(context, language, fallbackLanguage = "en") { sentence(it) } }
    DisposableEffect(voice) { onDispose { voice.close() } }
    LaunchedEffect(voice) { voice.prepare() }

    Box(Modifier.fillMaxSize().background(palette.muted)) {
        if (map == null) {
            // trail: trail.no-key
            Notice(
                Words.NOTICE,
                palette,
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp)
                    .testTag("trail.notice"),
            )
        } else {
            MapScreen(map, settings, units, itinerary, palette)
        }
        Sheet(
            Modifier.align(Alignment.BottomCenter).sheetPlacement(),
            settings,
            itinerary,
            itineraries,
            itineraryId,
            map,
            voice,
            units.value,
            timeZone,
            palette,
        )
    }
}

/** The map, drawn full screen under its controls and the search box, with the notice a map the API refuses shows. */
@Composable
private fun MapScreen(
    map: MapEngine,
    settings: TrailSettings,
    units: MutableState<UnitPreferences>,
    itinerary: TrailItinerary,
    palette: Palette,
) {
    // trail: trail.map
    LaunchedEffect(map) {
        try {
            map.setTerrain()
        } catch (unread: MountayaException) {
            // A catalogue the API refuses is held as the map's own refusal, `isAccessRequired`, which the notice reads;
            // one that could not be read, a connection lost among the causes, leaves the map as it was.
        } catch (undrawn: MapException) {
            // The catalogue names no terrain the map can draw: the map stays as it was.
        }
    }
    // trail: trail.no-key
    // The map holds a refusal of access until a source it refused loads again; Trail keeps no copy of its own.
    val drawn by map.state.collectAsState()
    // trail: trail.itinerary
    val held by itinerary.held.collectAsState()
    LaunchedEffect(map, held) { held?.let { TrailItinerary.draw(map, it) } }
    // A map no view draws frames nothing, so each itinerary is framed once, after the map is ready.
    var ready by remember(map) { mutableStateOf(false) }
    var framedId by remember(map) { mutableStateOf<String?>(null) }
    LaunchedEffect(map, ready, held) {
        val shown = held ?: return@LaunchedEffect
        if (!ready || framedId == shown.id) return@LaunchedEffect
        framedId = shown.id
        TrailItinerary.frame(map, shown)
    }

    Box(Modifier.fillMaxSize()) {
        // trail: trail.map
        MapView(map, Modifier.fillMaxSize(), onReady = { ready = true })
        // The stack keeps to the map's safe area and clear of the watermark's frame on its own.
        MapControls(
            map,
            zoom = true,
            compass = true,
            geolocate = true,
            layers = true,
            pois = true,
            preferences = true,
            unitPreferences = units,
            offersTime = true,
        )
        // trail: trail.search
        val sessions = settings.sessionUrl != null
        SearchControl(
            map,
            Modifier.align(Alignment.TopStart).searchBoxPlacement(),
            units = units.value,
            enabled = sessions,
            strings = if (sessions) null else mapOf("search.placeholder" to Words.SEARCH_NEEDS_SESSION),
        )
        if (drawn.isAccessRequired) Notice(Words.MAP_REFUSED, palette, Modifier.align(Alignment.Center).padding(24.dp))
    }
}

/**
 * The bottom sheet: Replay and Start, or the navigation's guidance and Stop, over the itinerary's elevation profile, and,
 * expanded, its segment and waypoint tables.
 */
@Composable
private fun Sheet(
    modifier: Modifier,
    settings: TrailSettings,
    itinerary: TrailItinerary,
    itineraries: ItineraryResourceCache,
    itineraryId: String,
    map: MapEngine?,
    voice: SpeechNavigationVoice,
    units: UnitPreferences,
    timeZone: String,
    palette: Palette,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var replay by remember { mutableStateOf(false) }
    var run by remember { mutableStateOf<TrailRun?>(null) }
    DisposableEffect(run) {
        val running = run
        onDispose { running?.stop() }
    }
    // trail: trail.location
    val askLocation =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            run?.device?.recheckPermission()
        }
    // Each cut-off is its date, medium, and its time of day, short, in the device's zone, and none is written where the
    // platform knows no zone of its name.
    val cutoff = remember(units.language, timeZone) { cutoffWriter(units.language, timeZone) }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(palette.sheet)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp),
    ) {
        Handle(expanded, palette) { expanded = !expanded }
        val running = run
        if (running == null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ReplaySwitch(replay, palette) { replay = it }
                Spacer(Modifier.weight(1f))
                // trail: trail.navigation
                Action(Words.START, palette, Modifier.testTag("trail.start")) {
                    run = TrailRun.start(context, scope, itinerary, itineraryId, replay, settings, voice)
                    // trail: trail.location
                    if (!replay && !holdsFineLocation(context)) askLocation.launch(locationPermissions)
                }
            }
        } else {
            Guidance(running, map, units, timeZone, palette) {
                // trail: trail.stop
                running.stop()
                run = null
                map?.clearGeometry(GeometrySlot.Preview)
            }
        }
        // trail: trail.profile
        ItineraryProfile(
            itineraryId = itineraryId,
            cache = itineraries,
            modifier = Modifier.fillMaxWidth().height(180.dp).testTag("trail.profile"),
            map = map,
            units = units,
            parts = setOf(ProfilePart.Amenities, ProfilePart.Tooltip, ProfilePart.DistanceAxis, ProfilePart.Navigator),
            formatCutoffAt = cutoff,
        )
        if (expanded) {
            // The tables take what the sheet's cap leaves them, at most 360, and scroll for the rest.
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                ItinerarySegmentTable(itineraryId = itineraryId, cache = itineraries, map = map, units = units)
                ItineraryWaypointTable(
                    itineraryId = itineraryId,
                    cache = itineraries,
                    map = map,
                    units = units,
                    formatCutoffAt = cutoff,
                )
            }
        }
    }
}

/**
 * The navigation as it runs: the instruction of the maneuver ahead and what is left of the route, or the arrival, beside
 * Stop, while the route is drawn in the map's preview slot, the camera follows the traveller and the screen stays awake.
 */
@Composable
private fun Guidance(
    run: TrailRun,
    map: MapEngine?,
    units: UnitPreferences,
    timeZone: String,
    palette: Palette,
    onStop: () -> Unit,
) {
    // trail: trail.progress
    val state by run.engine.state.collectAsState()
    val route = state.route
    val along = state.progress?.along
    LaunchedEffect(map, route) {
        if (map != null && route != null) map.setGeometry(GeoJsonLineString(route.line), GeometrySlot.Preview)
    }
    LaunchedEffect(map, route, along) {
        if (map != null && route != null && along != null) {
            map.ease(CameraTarget(center = positionAtMeters(route, along), zoom = FOLLOWING_ZOOM))
        }
    }
    KeepScreenOn()
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (state.status == NavigationStatus.Arrived) {
                // trail: trail.stop
                Label(Words.ARRIVED, palette, Modifier.testTag("trail.arrived"), strong = true)
            } else if (route != null) {
                val instruction = sentence(instructionAhead(route, state.progress))
                Label(instruction, palette, Modifier.testTag("trail.instruction"), strong = true)
                state.progress?.let { progress ->
                    Label(progressLine(progress, units, timeZone, Clock.System.now()), palette)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        // trail: trail.stop
        Action(Words.STOP, palette, Modifier.testTag("trail.stop"), onStop)
    }
}

/** Keeps the screen awake for as long as it stands in the composition. */
@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

/** The switch that walks the route with the replay in place of the device's positions. */
@Composable
private fun ReplaySwitch(
    on: Boolean,
    palette: Palette,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .testTag("trail.replay")
            .toggleable(value = on, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(Words.REPLAY, palette)
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(width = 40.dp, height = 22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (on) palette.accent else palette.muted)
                .padding(3.dp),
            contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(Modifier.size(16.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
        }
    }
}

/** The grip that opens the sheet onto the itinerary's tables, and closes it. */
@Composable
private fun Handle(
    expanded: Boolean,
    palette: Palette,
    onToggle: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable(onClick = onToggle)
            .semantics {
                contentDescription = Words.DETAILS
                if (expanded) {
                    collapse {
                        onToggle()
                        true
                    }
                } else {
                    expand {
                        onToggle()
                        true
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 36.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(palette.muted))
    }
}

/** A button of Trail's own: its words on the accent. */
@Composable
private fun Action(
    text: String,
    palette: Palette,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    BasicText(
        text,
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(palette.accent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    )
}

/** A notice of Trail's own, on a card. */
@Composable
private fun Notice(
    text: String,
    palette: Palette,
    modifier: Modifier,
) {
    BasicText(
        text,
        modifier.clip(RoundedCornerShape(12.dp)).background(palette.sheet).padding(16.dp),
        style = TextStyle(color = palette.ink, fontSize = 16.sp),
    )
}

/** A line of Trail's own words. */
@Composable
private fun Label(
    text: String,
    palette: Palette,
    modifier: Modifier = Modifier,
    strong: Boolean = false,
) {
    BasicText(
        text,
        modifier,
        style =
            TextStyle(
                color = palette.ink,
                fontSize = if (strong) 18.sp else 15.sp,
                fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Normal,
            ),
    )
}
