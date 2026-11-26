package com.example.consumer

import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.MapEngine
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.GeolocateControlEngine
import com.mountaya.geo.GeolocateControlState
import com.mountaya.geo.GeolocateControlTarget
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayersPanelCategory
import com.mountaya.geo.LayersPanelChoice
import com.mountaya.geo.LayersPanelMark
import com.mountaya.geo.LayersPanelPress
import com.mountaya.geo.LayersPanelRow
import com.mountaya.geo.LayersPanelSection
import com.mountaya.geo.LayersSelection
import com.mountaya.geo.LayersSelectionEngine
import com.mountaya.geo.LayersSelectionTarget
import com.mountaya.geo.PoiPanelGroup
import com.mountaya.geo.PoiPanelRow
import com.mountaya.geo.PoiPanelRows
import com.mountaya.geo.PreferenceChoice
import com.mountaya.geo.PreferenceField
import com.mountaya.geo.PreferenceLabel
import com.mountaya.geo.PreferenceOption
import com.mountaya.geo.PreferencePress
import com.mountaya.geo.PreferenceValue
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.TimeSliderReadout
import com.mountaya.geo.TimeSliderRelease
import com.mountaya.geo.TimeSliderTick
import com.mountaya.geo.TimeSliderWindow
import com.mountaya.geo.TimestampedSource
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.WordBundle
import com.mountaya.geo.WordOverrides
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.ZoomStep
import com.mountaya.geo.ZoomWindowEnd
import com.mountaya.geo.countedText
import com.mountaya.geo.filledText
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.layersPanelPress
import com.mountaya.geo.layersPanelSections
import com.mountaya.geo.overlayInstant
import com.mountaya.geo.poiPanelNoteZoom
import com.mountaya.geo.poiPanelRows
import com.mountaya.geo.preferenceChoices
import com.mountaya.geo.preferencePress
import com.mountaya.geo.resolveBundle
import com.mountaya.geo.scopeOf
import com.mountaya.geo.themeTokenValue
import com.mountaya.geo.timeSliderReadout
import com.mountaya.geo.timeSliderRelease
import com.mountaya.geo.timeSliderTicks
import com.mountaya.geo.timeSliderWindow
import com.mountaya.geo.timestampedSources
import com.mountaya.geo.zoomControlsDisabled
import com.mountaya.geo.zoomLimitReached
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Controls reads what the controls and panels stand on as an app's shared code does, on every target: the scope an
 * element's words and glyphs come from, a counted word and a template, the tokens under each scheme, the zoom window's
 * ends, the pickers' models, and the two engines over the map, `MapEngine` among their targets, beside a target of the
 * app's own. None of it names a composable.
 */
object Controls {
    /** Returns the words, the tokens and the zoom window an app's own control reads. */
    fun worded(): List<String> {
        val french =
            scopeOf(
                ElementsScope.standard,
                ElementsScopeStatement(
                    language = RestSchema.Language.Fr,
                    locale = "fr-CH",
                    words =
                        WordOverrides.BySection(
                            JsonObject(
                                mapOf("controls" to JsonObject(mapOf("zoomIn" to JsonPrimitive("Plus")))),
                            ),
                        ),
                ),
            )
        val words = resolveBundle(WordBundle.elements, french.language)
        val count = (words["search"] as? JsonObject)?.get("resultCount")
        val counted = count?.let { countedText(it, 1234, french.language) }.orEmpty()
        val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
        val accent: HexColor = themeTokenValue(tokens.accent, ThemeScheme.Dark)
        val end: ZoomWindowEnd = zoomLimitReached(12.0, ZoomBounds(4.0, 12.0))
        val disabled: List<ZoomStep> = zoomControlsDisabled(end)
        return listOf(
            french.word("controls.zoomIn"),
            french.locale,
            french.controlGlyphs.name,
            french.markerGlyphs.name,
            counted,
            filledText("{elapsed} stop", "15 min"),
            accent.hex,
            ThemeScheme.entries.joinToString { it.id },
            disabled.joinToString { it.id },
        )
    }

    /**
     * Returns the pickers' models: the layers picker's sections and a press, the points-of-interest rows, the
     * preferences.
     */
    fun picked(catalogue: TileSchema.Layers): List<Any> {
        val sections: List<LayersPanelSection> =
            layersPanelSections(
                LayersSelection(catalogue, terrain = "outdoor"),
                "fr-CA",
                isLocked = { it == "snow-depth" },
                picked = "hazards",
                offersTime = true,
            )
        val categories: List<LayersPanelCategory> =
            sections.firstOrNull { it.choice == LayersPanelChoice.Overlay }?.categories.orEmpty()
        val row: LayersPanelRow =
            sections.first().rows.firstOrNull() ?: LayersPanelRow("outdoor", "Outdoor", true, LayersPanelMark.Tick)
        val press: LayersPanelPress =
            when (val asked = layersPanelPress(LayersPanelChoice.Terrain, row)) {
                is LayersPanelPress.Command -> asked
                is LayersPanelPress.Locked -> asked
            }
        val heading: String = sections.first().heading.word
        val rows: PoiPanelRows =
            poiPanelRows(
                drawn = listOf("spring", "parking"),
                isKeyVisible = { it != "parking" },
                groups = listOf(PoiPanelGroup("water", "Water", listOf("spring", "fountain"), glyph = "pin")),
            )
        val first: PoiPanelRow? = rows.rows.firstOrNull()
        val preferences = UnitPreferences(language = "fr-CA", length = RestSchema.UnitLength.Imperial)
        val choices: List<PreferenceChoice> = preferenceChoices(preferences)
        val option: PreferenceOption =
            choices
                .first {
                    it.field == PreferenceField.Language
                }.options
                .first { it.isChecked }
        val label =
            when (val written = option.label) {
                is PreferenceLabel.Word -> written.word
                is PreferenceLabel.Name -> written.name
            }
        val pressed: PreferencePress =
            preferencePress(preferences, PreferenceValue.Language(RestSchema.Language.De))
        return listOf(
            press,
            heading,
            categories.map { it.slug to it.name },
            first?.keys.orEmpty(),
            poiPanelNoteZoom(12.0),
            label,
            pressed.next,
            pressed.relabel ?: "",
        )
    }

    /**
     * Returns the time a layers panel's slider shows, laid out around the app's own clock: the moment a surface shows,
     * the addresses a stamped overlay is asked at, the track, its ticks, a release and a readout.
     */
    fun timed(
        catalogue: TileSchema.Layers,
        pinned: Instant?,
        clock: Clock,
    ): List<Any?> {
        val now = clock.now()
        val moment: Instant = overlayInstant(pinned, now)
        val sources: List<TimestampedSource> = timestampedSources(catalogue, "snow-depth", moment)
        val window: TimeSliderWindow = timeSliderWindow(now)
        val ticks: List<TimeSliderTick> = timeSliderTicks(window)
        val release: TimeSliderRelease? = timeSliderRelease(window, window.valueAt(pinned) + 1)
        val readout: TimeSliderReadout = timeSliderReadout(window, window.maxValue)
        return listOf(
            sources.map { it.sourceId to it.tiles },
            window.start,
            window.nowValue,
            window.dateAt(0),
            ticks.mapNotNull { it.word },
            release?.pin,
            readout.instant,
            readout.word,
            readout.count,
        )
    }

    /**
     * OwnMap is a map of the app's own a layers selection drives, which reads its catalogue through the app's own
     * client.
     */
    class OwnMap(
        private val catalogue: TileSchema.Layers,
    ) : LayersSelectionTarget {
        override val terrain: String? = null

        override suspend fun loadLayers(): TileSchema.Layers = catalogue

        override suspend fun setTerrain(slug: String?) = Unit

        override suspend fun setOverlay(slug: String) = Unit

        override suspend fun clearOverlay() = Unit

        override suspend fun addEnrichment(slug: String) = Unit

        override suspend fun removeEnrichment(slug: String) = Unit

        override suspend fun setInstant(instant: Instant?) = Unit
    }

    /**
     * Returns the two engines over a map: a selection and a locate control over the map engine, and a selection over
     * the app's own map.
     */
    fun engines(
        scope: CoroutineScope,
        map: MapEngine,
        own: TileSchema.Layers,
    ): List<Any> {
        val layers = LayersSelectionEngine(scope, map)
        layers.setOverlay("slopes")
        layers.toggleEnrichment("hillshade")
        layers.setTerrain("outdoor")
        layers.setInstant(Instant.parse("2026-03-15T12:00:00Z"))
        layers.configure(OwnMap(own))
        val selection: LayersSelection = layers.state.value
        val resource: AutoCloseable = layers
        resource.close()
        val target: GeolocateControlTarget = map
        val state: GeolocateControlState =
            GeolocateControlEngine(scope, target).use { control ->
                control.locate(onError = { error -> error.message })
                control.configure(null)
                control.state.value
            }
        return listOf(
            selection.enrichments,
            selection.instant ?: "",
            map.instant ?: "",
            selection.refusal ?: "",
            state.isBusy,
            state.isBlocked,
            state.isLive,
            map.terrain ?: "",
        )
    }

    /** Answers whether the map's standing permission refuses a fix, read without asking anyone. */
    suspend fun refused(map: MapEngine): Boolean = map.isLocationRefused()
}
