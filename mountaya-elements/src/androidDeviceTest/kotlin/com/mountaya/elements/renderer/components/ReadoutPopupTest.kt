package com.mountaya.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.geo.DateLength
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.TimeLength
import com.mountaya.geo.filledText
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.generated.ElementTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import java.util.SimpleTimeZone
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ReadoutPopupTest {
    @get:Rule
    val compose = createComposeRule()

    /** A source with no tile at hand, so the readout states what the map holds alone. */
    private val nothing =
        object : ElevationReader {
            override suspend fun source(): ElevationSource = ElevationSource(ElevationEncoding.Mapbox, 4, 16)

            override suspend fun tile(address: ElevationTile.Address): ElevationTile = error("no tile is at hand")
        }

    @Test
    fun anOpenReadoutStatesItsCoordinatesAndBelowTheDetailZoomAsksToZoomIn() {
        val map = Controls.engine()
        val readout = ReadoutEngine(CoroutineScope(Dispatchers.Main), nothing)
        readout.press(listOf(-0.1276, 51.5072), listOf(20.0, 20.0), 10.0)
        compose.setContent {
            Box(Modifier.size(400.dp)) { ReadoutPopup(map, readout, safeArea = null, keepClear = null) }
        }

        compose.onNodeWithText("51.5072°N 0.1276°W").assertExists()
        compose.onNodeWithText("Copy coordinates").assertExists()
        compose.onNodeWithText("Zoom in to see more details.").assertExists()
        compose.runOnIdle { readout.close() }
        compose.onNodeWithText("Copy coordinates").assertDoesNotExist()
    }

    /** The colour the text [text] is written in, as Compose lays it out. */
    private fun colorOf(text: String): Color {
        val layouts = mutableListOf<TextLayoutResult>()
        val node = compose.onNodeWithText(text).fetchSemanticsNode()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        return assertNotNull(layouts.firstOrNull(), "\"$text\" is laid out").layoutInput.style.color
    }

    /**
     * The copy action is written in the accent, the one action the card holds, as `sdk-swift` tints its copy button and
     * `sdk-dart` writes its own, and the coordinates beside it are not.
     */
    @Test
    fun theCopyActionIsWrittenInTheAccent() {
        val map = Controls.engine()
        val readout = ReadoutEngine(CoroutineScope(Dispatchers.Main), nothing)
        readout.press(listOf(-0.1276, 51.5072), listOf(20.0, 20.0), 10.0)
        compose.setContent {
            Controls.Drawn("readout") {
                Box(Modifier.size(400.dp)) { ReadoutPopup(map, readout, safeArea = null, keepClear = null) }
            }
        }

        val accent = tokenColor(ElementTokens.standard.accent, ThemeScheme.Light)
        assertEquals(accent, colorOf("Copy coordinates"), "the copy action is written in the accent")
        assertNotEquals(accent, colorOf("51.5072°N 0.1276°W"), "the coordinates are written in the card's ink")
        compose.runOnIdle { readout.close() }
    }

    /** A map whose catalogue holds an overlay the server stamps at an instant, every request answered by that catalogue. */
    private fun stampedMap(): MapEngine {
        val catalogue =
            """{"data": {"elevation": {"id": "e", "tile_size": 512, "url": "https://tiles.test/elevation.json"},
              "itineraries": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "i", "locales": {}, "slug": "itineraries", "sources": []},
              "zones": {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": false}},
                "content_type": "application/x-protobuf", "id": "z", "locales": {}, "slug": "zones", "sources": []},
              "overlays": [{"id": "c", "slug": "conditions", "locales": {}, "children": [
                {"capabilities": {"client": {"timestamp": false}, "server": {"timestamp": true}},
                  "content_type": "application/x-protobuf", "id": "s", "slug": "slopes",
                  "locales": {"en": {"name": "Slope"}}, "sources": []}]}],
              "terrains": []}}"""
        val serving =
            object : HttpExchange {
                override suspend fun send(request: ExchangeRequest): ExchangeResponse =
                    ExchangeResponse(
                        200,
                        mapOf("Content-Type" to "application/json"),
                        ExchangeBody(catalogue.encodeToByteArray()),
                    )
            }
        return MapEngine(
            Mountaya(
                Credential.Publishable("pk_test_readout"),
                exchange = serving,
                dispatcher = Dispatchers.Unconfined,
            ),
            geolocation = InertGeolocation(),
        )
    }

    /** While a card is open its moment follows the map's pin live, and its band stays as it was read at the press. */
    @Test
    fun anOpenCardsMomentFollowsTheMapsPinLive() {
        val map = stampedMap()
        val first = Instant.parse("2026-03-14T09:00:00Z")
        val second = Instant.parse("2026-03-15T18:30:00Z")
        runBlocking {
            map.setOverlay("slopes")
            map.setInstant(first)
        }
        val readout = ReadoutEngine(CoroutineScope(Dispatchers.Main), nothing)
        readout.press(listOf(-0.1276, 51.5072), listOf(30.0, 200.0), 10.0)
        compose.setContent {
            Box(Modifier.size(400.dp)) { ReadoutPopup(map, readout, safeArea = null, keepClear = null) }
        }

        fun moment(instant: Instant): String {
            val scope = ElementsScope.standard
            val written =
                assertNotNull(
                    formatDateTime(instant, scope.locale, deviceTimeZone().id, DateLength.Medium, TimeLength.Short),
                    "the device's zone is one the platform knows",
                )
            return filledText(scope.word("readout.overlayTime"), mapOf("moment" to written))
        }
        compose.onNodeWithText(moment(first)).assertExists()
        runBlocking { map.setInstant(second) }
        compose.onNodeWithText(moment(second)).assertExists()
        compose.onNodeWithText(moment(first)).assertDoesNotExist()
        compose.runOnIdle { readout.close() }
    }

    /**
     * Where the platform knows no zone of the device's name, the card writes no moment, and never the instant in UTC in
     * its place: the row that would state the moment the map shows is left out, and the rest of the card stands.
     */
    @Test
    fun aCardWritesNoMomentWhereTheDevicesZoneIsUnknown() {
        val map = stampedMap()
        val pinned = Instant.parse("2026-03-14T09:00:00Z")
        runBlocking {
            map.setOverlay("slopes")
            map.setInstant(pinned)
        }
        val readout = ReadoutEngine(CoroutineScope(Dispatchers.Main), nothing)
        readout.press(listOf(-0.1276, 51.5072), listOf(30.0, 200.0), 10.0)
        val before = TimeZone.getDefault()
        try {
            TimeZone.setDefault(SimpleTimeZone(0, "Mountaya/Nowhere"))
            compose.setContent {
                Box(Modifier.size(400.dp)) { ReadoutPopup(map, readout, safeArea = null, keepClear = null) }
            }

            compose.onNodeWithText("51.5072°N 0.1276°W").assertExists()
            compose.onNodeWithText(pinned.toString(), substring = true).assertDoesNotExist()
            val lead = ElementsScope.standard.word("readout.overlayTime").substringBefore("{")
            if (lead.isNotBlank()) compose.onNodeWithText(lead.trim(), substring = true).assertDoesNotExist()
            compose.runOnIdle { readout.close() }
        } finally {
            TimeZone.setDefault(before)
        }
    }

    @Test
    fun aReadoutPressedUnderTheWatermarkIsLaidClearOfItsFrame() {
        val map = Controls.engine()
        val readout = ReadoutEngine(CoroutineScope(Dispatchers.Main), nothing)
        val area = listOf(0.0, 24.0, 400.0, 400.0)
        val frame =
            assertNotNull(keptClearFrame(OrganizationRead.Waiting, area), "a waiting read keeps the frame clear")
        readout.press(listOf(-0.1276, 51.5072), listOf(30.0, 30.0), 10.0)
        compose.setContent {
            Box(Modifier.size(400.dp)) { ReadoutPopup(map, readout, area, frame) }
        }

        val card = compose.onNodeWithText("51.5072°N 0.1276°W").fetchSemanticsNode().boundsInRoot
        val bottom = with(compose.density) { (frame[1] + frame[3]).dp.toPx() }
        assertTrue(card.top >= bottom, "the readout starts below the watermark's frame, at ${card.top} of $bottom")
    }

    @Test
    fun aReadoutOnAShortNarrowMapOpensBelowTheFrameAndTheMapsEdgeCutsIt() {
        val map = Controls.engine()
        val readout = ReadoutEngine(CoroutineScope(Dispatchers.Main), nothing)
        val area = listOf(0.0, 0.0, 390.0, 160.0)
        val frame =
            assertNotNull(keptClearFrame(OrganizationRead.Waiting, area), "a waiting read keeps the frame clear")
        readout.press(listOf(-0.1276, 51.5072), listOf(50.0, 50.0), 10.0)
        compose.setContent {
            Box(Modifier.width(390.dp).height(160.dp)) { ReadoutPopup(map, readout, area, frame) }
        }

        val card = compose.onNodeWithText("51.5072°N 0.1276°W").fetchSemanticsNode().boundsInRoot
        val bottom = with(compose.density) { (frame[1] + frame[3]).dp.toPx() }
        assertTrue(card.top >= bottom, "the readout starts below the watermark's frame, at ${card.top} of $bottom")
    }

    @Test
    fun aReadoutNearTheSafeAreasFarCornerFlipsLeftAndUp() {
        val map = Controls.engine()
        val readout = ReadoutEngine(CoroutineScope(Dispatchers.Main), nothing)
        val area = listOf(0.0, 0.0, 400.0, 400.0)
        readout.press(listOf(-0.1276, 51.5072), listOf(390.0, 390.0), 10.0)
        compose.setContent {
            Box(Modifier.size(400.dp)) { ReadoutPopup(map, readout, area, keepClear = null) }
        }

        val card = compose.onNodeWithText("51.5072°N 0.1276°W").fetchSemanticsNode().boundsInRoot
        val edge = with(compose.density) { 390.dp.toPx() }
        assertTrue(card.right <= edge, "the card stands left of the point, its right edge at ${card.right} of $edge")
        assertTrue(card.bottom <= edge, "the card stands above the point, its bottom edge at ${card.bottom} of $edge")
    }
}
