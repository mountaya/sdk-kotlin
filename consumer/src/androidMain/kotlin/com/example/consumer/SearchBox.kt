package com.example.consumer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.DataSchema
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapView
import com.mountaya.elements.SearchControl
import com.mountaya.geo.SearchConfiguration
import com.mountaya.geo.SearchEngine
import com.mountaya.geo.SearchFetch
import com.mountaya.geo.SearchLimit
import com.mountaya.geo.SearchRow
import com.mountaya.geo.SearchWording
import com.mountaya.geo.UnitPreferences

/**
 * SearchBox reads the search control as an Android app does: the README's search over a trail map, its text and its
 * scope hoisted, a control of the app's own every parameter states, its pick routed the app's own way, and a box over
 * an engine the app holds, its words its own.
 */
object SearchBox {
    /** Shows the README's search box over the map, its text and its scope held by the app. */
    @Composable
    fun TrailSearch(mountaya: Mountaya) {
        val map = remember { MapEngine(mountaya) }
        val query = remember { mutableStateOf("") }
        val confined = remember { mutableStateOf(true) }
        LaunchedEffect(map) { map.setTerrain() }
        Box(Modifier.fillMaxSize()) {
            MapView(map, Modifier.fillMaxSize())
            SearchControl(map, Modifier.padding(16.dp), query = query, isConfined = confined)
        }
    }

    /**
     * Shows a control every parameter states: its branches, language, cap, units, words and transport, a pick and a
     * failure handed to the app.
     */
    @Composable
    fun OwnSearch(mountaya: Mountaya) {
        var picked by remember { mutableStateOf<SearchRow?>(null) }
        var failed by remember { mutableStateOf(0) }
        SearchControl(
            map = null,
            targets = listOf(DataSchema.SearchTarget.Itineraries, DataSchema.SearchTarget.Zones),
            language = DataSchema.Language.Fr,
            limit = SearchLimit.of(10),
            units = UnitPreferences("fr"),
            wording = SearchWording(resolveLabel = { null }),
            fetch = SearchFetch { input -> mountaya.data.search(input) },
            enabled = true,
            onSelect = { picked = it },
            onError = { failed += 1 },
        )
        picked?.let { BasicText(it.title) }
        if (failed > 0) BasicText("$failed failed")
    }

    /**
     * Shows the README's box over the app's own engine, which the box renders and drives and never configures or
     * closes, its text the engine's own, a count and a placeholder in the app's words.
     */
    @Composable
    fun AppEngineSearch(
        mountaya: Mountaya,
        map: MapEngine,
    ) {
        val applicationScope = rememberCoroutineScope()
        val search =
            remember {
                SearchEngine(
                    applicationScope,
                    SearchConfiguration(fetch = SearchFetch { input -> mountaya.data.search(input) }),
                    isConfined = true,
                )
            }
        DisposableEffect(search) { onDispose { search.close() } }
        SearchControl(
            map,
            Modifier.padding(16.dp),
            engine = search,
            strings = mapOf("search.placeholder" to "Find a hut", "search.resultCount" to "{count} huts"),
        )
    }
}
