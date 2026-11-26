package com.example.consumer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mountaya.client.Mountaya
import com.mountaya.elements.DeviceGeolocation
import com.mountaya.elements.Glyph
import com.mountaya.elements.LocalMountayaTokens
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.MapTap
import com.mountaya.elements.MapView
import com.mountaya.elements.MountayaScope
import com.mountaya.elements.themeSchemeInForce
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.themeTokenValue
import okhttp3.OkHttpClient
import org.maplibre.android.maps.MapLibreMap

/**
 * Map reads the map's composables as an Android app does: the README's trail map, the app's own client
 * for MapLibre's requests, MapLibre's own map reached through `onReady`, whose types an app names
 * through `mountaya-elements` with nothing declared of its own, a glyph drawn on its own, and a map that
 * locates its reader through the device's location services.
 */
object Map {
    /** Shows the README's map, as the app writes it. */
    @Composable
    fun TrailMap(mountaya: Mountaya) {
        val map = remember { MapEngine(mountaya) }
        LaunchedEffect(map) { map.setTerrain() }
        MapView(map)
    }

    /** Shows a map over the app's own client for MapLibre's requests, reaching MapLibre's map once it is ready. */
    @Composable
    fun OwnClient(
        map: MapEngine,
        modifier: Modifier,
        okHttpClient: OkHttpClient,
        ready: (MapLibreMap) -> Unit,
    ) {
        MapView(map, modifier, okHttpClient, onReady = { mapLibre ->
            mapLibre.uiSettings.isRotateGesturesEnabled = false
            ready(mapLibre)
        })
    }

    /**
     * Shows the README's glyphs: a control's role from the scope's control set, the bundled one here, in the controls'
     * ink, and a waypoint's from the marker set, named; then a control's role under a scope drawn dark whatever the
     * system's scheme, beside the app's own ink read under the scheme in force there.
     */
    @Composable
    fun Glyphs() {
        Glyph("zoom-in")
        Glyph("hut", set = GlyphSet.marker, size = 24.dp, color = Color(0xFF1C7ED6), label = "Refuge")
        MountayaScope(scheme = ThemeScheme.Dark) {
            Glyph("zoom-out")
            val ink: HexColor = themeTokenValue(LocalMountayaTokens.current.controlFg, themeSchemeInForce())
            seen(ink)
        }
    }

    /** Shows a map whose clicks select a marker and whose camera at rest the app keeps, as an app's screen does. */
    @Composable
    fun Selecting(
        map: MapEngine,
        selected: (String?) -> Unit,
        rested: (CameraPose) -> Unit,
    ) {
        MapView(
            map,
            onClick = { tap: MapTap -> selected(tap.marker) },
            onCameraChange = { camera -> rested(camera) },
        )
    }

    /**
     * Shows a map that frames where its reader stands, once the app holds the location permission it asked for in its
     * own flow.
     */
    @Composable
    fun Locating(
        mountaya: Mountaya,
        located: (List<Double>?) -> Unit,
    ) {
        val context = LocalContext.current
        val map = remember { MapEngine(mountaya, geolocation = DeviceGeolocation(context)) }
        LaunchedEffect(map) {
            located(
                try {
                    map.locate()
                } catch (refused: MapException) {
                    null
                },
            )
        }
        MapView(map)
    }
}
