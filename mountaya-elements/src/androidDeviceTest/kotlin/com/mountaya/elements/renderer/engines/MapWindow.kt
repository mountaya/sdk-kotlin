package com.mountaya.elements

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertNotNull

/** MapWindow is a map drawn in a test's own activity, as an app shows one, and the waits a test reads it after. */
internal object MapWindow {
    /** How long a test waits for MapLibre, which loads and draws on threads of its own. */
    const val TIMEOUT = 20_000L

    /** A phone's screen, 390 by 844 density-independent pixels, whatever room the test's activity gives. */
    val screen: Modifier =
        Modifier.layout { measurable, _ ->
            val width = 390.dp.roundToPx()
            val height = 844.dp.roundToPx()
            val placed = measurable.measure(Constraints.fixed(width, height))
            layout(width, height) { placed.place(0, 0) }
        }

    /**
     * Shows [map] through a renderer of its own over [okHttpClient], the stub's client unless a test reaches a
     * server of its own, and answers it once its style is loaded.
     */
    fun ComposeContentTestRule.showing(
        map: MapEngine,
        okHttpClient: OkHttpClient? = StubTileApi.client,
    ): MapRenderer {
        val loaded = AtomicBoolean(false)
        var shown: MapRenderer? = null
        setContent {
            val context = LocalContext.current
            val renderer =
                remember {
                    MapLibre.getInstance(context.applicationContext)
                    MapRenderer(map, MapView(context), okHttpClient) { loaded.set(true) }
                }
            shown = renderer
            RendererView(renderer, screen)
        }
        waitUntil(TIMEOUT) { loaded.get() }
        return assertNotNull(shown, "the renderer is shown once the style is loaded")
    }

    /** Waits until [condition], read on the main thread where MapLibre's map lives, holds. */
    fun ComposeContentTestRule.until(condition: () -> Boolean) {
        waitUntil(TIMEOUT) { runOnUiThread(condition) }
    }

    /** Runs [command] on the main thread, the one the engine and MapLibre's map are driven from, and answers what it answers. */
    fun <T> ComposeContentTestRule.onMain(command: () -> T): T = runOnUiThread(command)
}
