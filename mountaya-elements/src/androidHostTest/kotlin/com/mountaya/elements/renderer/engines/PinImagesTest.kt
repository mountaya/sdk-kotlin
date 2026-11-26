package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.geo.CameraPose
import com.mountaya.geo.HexColor
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The drawings each look of pin may cost however many markers carry it and however often they move: one. A drawing
 * rasterizes the disc, the ring and the glyph anew, so one per marker or per change would draw the same pin forty
 * times over (`pin-images` in the contract's `sdk/hot-paths.yaml`).
 */
private const val PIN_IMAGES_BUDGET = 1

/** The images of the markers' pins, drawn here as the renderer draws them, with a drawing of the test's own. */
class PinImagesTest {
    /** A drawing of the test's own: the look it was drawn for, told apart from another drawing of it by identity. */
    private class Drawing(
        val look: PinImages.Look,
    )

    private val accent = HexColor("#1c7ed6")

    /** A map with no renderer and no network, whose state the drawing reads. */
    private fun engine(): MapEngine =
        MapEngine(
            Mountaya(
                Credential.Publishable("pk_test_pins"),
                exchange =
                    object : HttpExchange {
                        override suspend fun send(request: ExchangeRequest): ExchangeResponse =
                            error("a map drawing pins asks the network nothing")
                    },
                dispatcher = Dispatchers.Unconfined,
            ),
            camera = CameraPose(listOf(6.86, 45.83), 12.0, 0.0, 0.0),
        )

    @Test
    fun aLookIsDrawnOnceForEveryMarkerShownCarryingItAndLetGoOnceNoneDoes() {
        val map = engine()
        val pins = PinImages { look -> Drawing(look) }
        val huts =
            listOf(
                MapMarker("a", listOf(6.86, 45.83), glyph = "hut"),
                MapMarker("b", listOf(6.87, 45.84), glyph = "hut"),
            )
        val red = MapMarker("c", listOf(6.88, 45.85), color = "#e03131")

        val first = pins.show(huts + red, map.glyphs, accent, DENSITY)
        assertSame(first[0], first[1], "one drawing for one look")
        assertNotSame(first[0], first[2])
        assertEquals(2, pins.drawn)

        val moved =
            pins.show(
                listOf(huts[0], MapMarker("b", listOf(6.9, 45.9), glyph = "hut")),
                map.glyphs,
                accent,
                DENSITY,
            )
        assertSame(first[0], moved[1], "a marker moved keeps its look's drawing")
        assertEquals(2, pins.drawn)

        val again = pins.show(huts + red, map.glyphs, accent, DENSITY)
        assertNotSame(first[2], again[2], "a look no marker carried is drawn again")
        assertEquals(3, pins.drawn)
    }

    @Test
    fun aNewAccentDrawsTheLooksNamingNoColourAgainAndALookOfItsOwnColourKeepsItsDrawing() {
        val map = engine()
        val pins = PinImages { look -> Drawing(look) }
        val markers =
            listOf(MapMarker("plain", listOf(6.86, 45.83)), MapMarker("red", listOf(6.87, 45.84), color = "#e03131"))
        val before = pins.show(markers, map.glyphs, accent, DENSITY)

        val after = pins.show(markers, map.glyphs, HexColor("#e9501e"), DENSITY)

        assertNotSame(before[0], after[0], "the accent's pin is drawn again")
        assertEquals(HexColor("#e9501e"), after[0].look.accent)
        assertSame(before[1], after[1], "a marker naming its colour keeps its pin")
        assertNull(after[1].look.accent)
        assertEquals(3, pins.drawn)
    }

    @Test
    fun theImageOfAMarkerIsTheOneItsLookShownCarriesOrOneDrawnAndNotKept() {
        val map = engine()
        val pins = PinImages { look -> Drawing(look) }
        val hut = MapMarker("a", listOf(6.86, 45.83), glyph = "hut")
        val shown = pins.show(listOf(hut), map.glyphs, accent, DENSITY)

        assertSame(
            shown[0],
            pins.imageOf(MapMarker("b", listOf(7.0, 46.0), glyph = "hut"), map.glyphs, accent, DENSITY),
        )
        assertEquals(1, pins.drawn)
        val camping = MapMarker("c", listOf(6.86, 45.83), glyph = "camping")
        assertNotSame(
            pins.imageOf(camping, map.glyphs, accent, DENSITY),
            pins.imageOf(camping, map.glyphs, accent, DENSITY),
        )
        assertEquals(3, pins.drawn, "a look no marker shown carries is drawn each time it is asked for")
    }

    @Test
    fun hotPathBudgetPinImagesFortyMarkersOfFourLooksMovedThirtyTimesDrawEachLookOnce() {
        val map = engine()
        val pins = PinImages { look -> Drawing(look) }
        var accent = this.accent
        var shownMarkers: List<MapMarker> = emptyList()
        var images: List<Drawing> = emptyList()
        var annotated = 0

        // What the renderer does with every state the engine reports: it shows the markers again where they changed.
        fun draw() {
            val markers = map.state.value.markers
            if (markers == shownMarkers) return
            shownMarkers = markers
            images = pins.show(markers, map.glyphs, accent, DENSITY)
            annotated += 1
        }

        // Forty markers of four looks: two glyphs on the accent, a red dot, and a glyph on a green of its own.
        val looks = listOf(null to "hut", null to "camping", "#e03131" to null, "#2f9e44" to "bed")
        for (index in 0 until 40) {
            val (color, glyph) = looks[index % looks.size]
            map.addMarker(MapMarker("m$index", listOf(6.8 + index / 1_000.0, 45.8), color, glyph))
            draw()
        }
        assertEquals(4, pins.drawn, "four looks, four pins drawn")
        // Thirty rounds of every marker moved, each move drawn where it lands.
        for (round in 1..30) {
            for (index in 0 until 40) {
                map.editMarker("m$index", position = listOf(6.8 + index / 1_000.0, 45.8 + round / 1_000.0))
                draw()
            }
        }

        // The work happened: every move was shown, every marker stands where it last moved, and each look is one image.
        assertEquals(40 + 40 * 30, annotated)
        assertEquals(
            listOf(6.8 + 39 / 1_000.0, 45.8 + 30 / 1_000.0),
            map.state.value.markers
                .last()
                .position,
        )
        for (look in looks.indices) {
            val carried = images.filterIndexed { index, _ -> index % looks.size == look }
            assertTrue(carried.all { it === carried.first() }, "the markers of look $look share one image")
        }
        assertTrue(pins.drawn <= PIN_IMAGES_BUDGET * looks.size, "${pins.drawn} pins drawn for ${looks.size} looks")

        // A look a marker moves to is drawn once, and a new accent draws again the looks it paints alone.
        map.editMarker("m0", glyph = "bed")
        draw()
        assertTrue(pins.drawn <= PIN_IMAGES_BUDGET * 5, "${pins.drawn} pins drawn for five looks")
        accent = HexColor("#e9501e")
        shownMarkers = emptyList()
        draw()
        assertEquals(8, pins.drawn, "the hut, the camping and the bed again, on the new accent")
        assertTrue(pins.drawn <= PIN_IMAGES_BUDGET * 8, "${pins.drawn} pins drawn for eight looks")
    }

    private companion object {
        /** The screen's density the pins are drawn for. */
        const val DENSITY = 420
    }
}
