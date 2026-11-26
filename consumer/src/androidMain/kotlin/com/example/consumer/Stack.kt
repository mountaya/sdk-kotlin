package com.example.consumer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.CompassControl
import com.mountaya.elements.DirectionsControl
import com.mountaya.elements.DownloadControl
import com.mountaya.elements.GeolocateControl
import com.mountaya.elements.LayersPanel
import com.mountaya.elements.LocalMountayaClock
import com.mountaya.elements.LocalMountayaScope
import com.mountaya.elements.LocalMountayaTokens
import com.mountaya.elements.MapControls
import com.mountaya.elements.MapCorner
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapPanel
import com.mountaya.elements.MapView
import com.mountaya.elements.MountayaScope
import com.mountaya.elements.PanelControl
import com.mountaya.elements.PoiPanel
import com.mountaya.elements.PreferencesPanel
import com.mountaya.elements.ZoomControl
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayersSelectionEngine
import com.mountaya.geo.PoiPanelGroup
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.generated.ElementTokens
import kotlin.time.Clock

/**
 * Stack reads the controls and panels as an Android app does: the README's trail map with its control stack, a stack
 * of the app's own composed of the controls with handlers standing in for their defaults, the three panels on their
 * own, and the scope and tokens an app states around them.
 */
object Stack {
    /**
     * Shows the README's map and its control stack, the app's own download control and panel among them, the panel open
     * and the units hoisted.
     */
    @Composable
    fun TrailControls(mountaya: Mountaya) {
        val map = remember { MapEngine(mountaya) }
        val open = remember { mutableStateOf<MapPanel?>(null) }
        val units = remember { mutableStateOf(UnitPreferences(language = "fr")) }
        var exporting by remember { mutableStateOf(false) }
        val pictures = remember { mapOf("topo" to ColorPainter(Color(0xFF2F9E44))) }
        LaunchedEffect(map) { map.setTerrain() }
        Box(Modifier.fillMaxSize()) {
            MapView(map, Modifier.fillMaxSize())
            MapControls(
                map,
                zoom = true,
                compass = true,
                geolocate = true,
                layers = true,
                pois = true,
                preferences = true,
                openPanel = open,
                unitPreferences = units,
                offersTime = true,
                thumbnail = { _, slug -> pictures[slug] },
            ) {
                DownloadControl(onClick = { exporting = true })
                PanelControl(MapPanel("filters"), label = "Filters", glyph = "preferences", help = "Huts only") {
                    BasicText("Huts only")
                }
            }
            if (exporting) BasicText("Exporting")
        }
    }

    /**
     * Shows a stack of the app's own: each control with a handler standing in for its default, in another corner, its
     * panel hoisted.
     */
    @Composable
    fun OwnStack(
        map: MapEngine,
        open: MutableState<MapPanel?>,
        preferences: MutableState<UnitPreferences>,
        steps: (String) -> Unit,
    ) {
        val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
        CompositionLocalProvider(LocalMountayaTokens provides tokens) {
            MountayaScope(
                ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH"),
                scheme = ThemeScheme.Dark,
            ) {
                MapControls(
                    map,
                    pois = true,
                    preferences = true,
                    corner = MapCorner.BottomLeft,
                    openPanel = open,
                    unitPreferences = preferences,
                    enabled = LocalMountayaScope.current.locale.isNotEmpty(),
                    strings = mapOf("controls.zoomIn" to "Plus"),
                ) {
                    ZoomControl(
                        map,
                        strings = mapOf("controls.zoomOut" to "Moins"),
                        onZoomInClick = { steps("in") },
                        onZoomOutClick = { steps("out") },
                    )
                    CompassControl(map, strings = mapOf("controls.compass" to "Nord"), onClick = { steps("north") })
                    GeolocateControl(
                        map,
                        isLocating = false,
                        strings = mapOf("controls.geolocate" to "Ici"),
                        onClick = { steps("locate") },
                        onError = { steps("refused") },
                    )
                    DirectionsControl(onClick = { steps("directions") }, enabled = true, strings = emptyMap())
                    DownloadControl(onClick = { steps("download") }, strings = null)
                    PanelControl(
                        MapPanel("filters"),
                        label = "Filters",
                        glyph = "preferences",
                        help = "Only open huts",
                    ) {
                        BasicText("Only open huts")
                    }
                }
            }
        }
    }

    /**
     * Shows the README's three panels on their own, as an app lays them out on a screen of its own, the locked press
     * its own notice.
     */
    @Composable
    fun Panels(
        map: MapEngine,
        upsell: (String) -> Unit,
        replay: Clock = Clock.System,
    ) {
        val scope = rememberCoroutineScope()
        val selection = remember(map) { LayersSelectionEngine(scope, map) }
        var units by remember { mutableStateOf(UnitPreferences(language = "de")) }
        LayersPanel(selection, isLocked = { it == "avalanche-risk" }, onLockedClick = { slug -> upsell(slug) })
        val pictures =
            remember { mapOf("topo" to ColorPainter(Color(0xFF2F9E44)), "slopes" to ColorPainter(Color(0xFFE8590C))) }
        CompositionLocalProvider(LocalMountayaClock provides replay) {
            LayersPanel(selection, offersTime = true, thumbnail = { _, slug -> pictures[slug] })
        }
        PreferencesPanel(units, onPreferencesChange = { units = it }, map = map)
        PoiPanel(map, groups = listOf(PoiPanelGroup("water", "Water", listOf("spring", "fountain"))))
        LayersPanel(selection, strings = mapOf("layers.overlays" to "Calques"))
        PreferencesPanel(units, { units = it }, strings = mapOf("preferences.length" to "Longueurs"))
    }

    /** Shows the points-of-interest panel with every option an app may hand it. */
    @Composable
    fun Concepts(map: MapEngine) {
        PoiPanel(
            map,
            keys = listOf("parking"),
            groups = listOf(PoiPanelGroup("water", "Water", listOf("spring", "drinking_water"), glyph = "pin")),
            activeGroups = listOf("water"),
            labels = mapOf("parking" to "Parking"),
            minZoom = 12.0,
            showsAllToggle = true,
            strings = mapOf("pois.categories" to "Sortes"),
            onToggle = { _, _ -> },
            onToggleGroup = { _, _ -> },
            onToggleAll = {},
        )
    }
}
