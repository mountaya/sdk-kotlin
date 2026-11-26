package com.mountaya.elements

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.watermarkHeight
import com.mountaya.geo.watermarkOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Where the watermark stands on a map drawn full bleed, and how the stack, its panels and the readout keep clear of it. */
class WatermarkFrameTest {
    private val mark =
        assertNotNull(
            watermarkOf(OrganizationRead.NoKey, RestSchema.Language.En),
            "a map with no key draws the watermark",
        )

    /** The frame's bottom edge below the safe area's top, 71.65 density-independent pixels: -4 plus its height. */
    private val frameBottom = mark.y + mark.height

    /** Whether a box of [size] at [origin] shares any area with [frame], `[x, y, width, height]`. */
    private fun overlaps(
        origin: List<Double>,
        size: List<Double>,
        frame: List<Double>,
    ): Boolean =
        origin[0] < frame[0] + frame[2] &&
            origin[0] + size[0] > frame[0] &&
            origin[1] < frame[1] + frame[3] &&
            origin[1] + size[1] > frame[1]

    /** The frame kept clear on a map whose safe area starts [top] below its own top, the read still waiting. */
    private fun kept(top: Double = 0.0): List<Double> =
        assertNotNull(
            keptClearFrame(OrganizationRead.Waiting, listOf(0.0, top, 400.0, 800.0)),
            "a waiting read keeps it",
        )

    @Test
    fun aMapDrawnFullBleedMeasuresItsSafeAreaFromEveryInsetItLiesUnder() {
        // A 400 by 800 window, a 24 pixel cutout on its left, a 48 pixel status bar and a 32 pixel navigation bar.
        val insets = listOf(24.0, 48.0, 0.0, 32.0)
        val window = listOf(400.0, 800.0)

        assertEquals(
            listOf(24.0, 48.0, 400.0, 768.0),
            mapSafeArea(insets, window, map = listOf(0.0, 0.0, 400.0, 800.0)),
        )
        // A map laid out below a toolbar starts clear of the status bar, part way under the cutout, above the bar below.
        assertEquals(
            listOf(8.0, 0.0, 384.0, 600.0),
            mapSafeArea(insets, window, map = listOf(16.0, 120.0, 384.0, 600.0)),
        )
        // A map the insets cover whole holds an empty safe area, which never leaves the map.
        assertEquals(listOf(10.0, 10.0, 10.0, 10.0), mapSafeArea(insets, window, map = listOf(14.0, 38.0, 10.0, 10.0)))
    }

    @Test
    fun theMarksFrameIsMeasuredFromTheSafeAreasCornerSoTheStatusBarNeverCoversIt() {
        val frame = watermarkFrame(mark, safeArea = listOf(0.0, 24.0, 400.0, 800.0))

        assertEquals(listOf(-4.0, 20.0, 180.0, mark.height), frame)
        assertEquals(listOf(-4.0, -4.0, 180.0, mark.height), watermarkFrame(mark, listOf(0.0, 0.0)))
    }

    @Test
    fun theFrameIsKeptClearFromFirstDrawWhileTheReadWaitsAndReleasedOnlyByWhiteLabel() {
        val area = listOf(0.0, 24.0, 400.0, 800.0)
        val frame = listOf(-4.0, 20.0, 180.0, watermarkHeight)
        val holding =
            listOf(
                OrganizationRead.Waiting,
                OrganizationRead.TimedOut,
                OrganizationRead.Failed,
                OrganizationRead.NoKey,
                OrganizationRead.Answered(whiteLabel = false),
            )

        for (read in holding) assertEquals(frame, keptClearFrame(read, area), "$read keeps the frame clear")
        assertEquals(watermarkFrame(mark, area), keptClearFrame(OrganizationRead.Waiting, area), "the mark lands in it")
        assertNull(keptClearFrame(OrganizationRead.Answered(whiteLabel = true), area), "white label releases it")
    }

    /** A stack laid over more than the map, the map offset below a header, keeps to the map's own safe area. */
    @Test
    fun aStackLaidOverMoreThanTheMapKeepsToTheMapsSafeArea() {
        val mapSafeArea = listOf(0.0, 56.0, 390.0, 810.0)

        assertEquals(mapSafeArea, safeAreaIn(mapSafeArea, space = listOf(0.0, 0.0, 390.0, 844.0)))
        assertEquals(listOf(0.0, 36.0, 390.0, 790.0), safeAreaIn(mapSafeArea, space = listOf(0.0, 20.0, 390.0, 844.0)))
        // A space the map's safe area does not reach keeps to the whole of itself.
        assertEquals(listOf(0.0, 0.0, 100.0, 50.0), safeAreaIn(mapSafeArea, space = listOf(0.0, 900.0, 100.0, 950.0)))
    }

    /** A label stands 18 past its point toward the end its text runs to, as `sdk-swift` and `sdk-dart` place it. */
    @Test
    fun aMarkersLabelStandsPastItsPinTowardTheEndItsTextRunsToCentredOnItsPoint() {
        val label = listOf(80.0, 24.0)
        val bounds = listOf(0.0, 0.0, 800.0, 600.0)

        assertEquals(
            listOf(318.0, 188.0),
            markerLabelOrigin(listOf(300.0, 200.0), label, 18.0, isRightToLeft = false, bounds, keepClear = null),
            "to the pin's right in a layout that runs left to right",
        )
        assertEquals(
            listOf(202.0, 188.0),
            markerLabelOrigin(listOf(300.0, 200.0), label, 18.0, isRightToLeft = true, bounds, keepClear = null),
            "to the pin's left in a layout that runs right to left",
        )
    }

    @Test
    fun aMarkersLabelIsHeldInsideTheSafeAreaInEitherDirection() {
        val label = listOf(80.0, 24.0)

        // Past the right edge and under the status bar of a map drawn full bleed.
        assertEquals(
            listOf(720.0, 47.0),
            markerLabelOrigin(listOf(760.0, 50.0), label, 18.0, false, listOf(0.0, 47.0, 800.0, 566.0), null),
        )
        // Past the left edge in a layout that runs right to left.
        assertEquals(
            listOf(0.0, 288.0),
            markerLabelOrigin(listOf(50.0, 300.0), label, 18.0, true, listOf(0.0, 0.0, 800.0, 600.0), null),
        )
    }

    @Test
    fun aMarkersLabelThatWouldReachOverTheFrameMovesClearOfItByTheReadoutsSmallestMove() {
        val label = listOf(80.0, 24.0)
        val bounds = listOf(0.0, 0.0, 800.0, 600.0)
        val frame = kept()
        val bottom = frame[1] + frame[3]

        val below = markerLabelOrigin(listOf(60.0, 40.0), label, 18.0, false, bounds, frame)
        assertEquals(listOf(78.0, bottom), below, "below the frame, the smaller move")
        assertFalse(overlaps(below, label, frame), "the label never covers the mark")
        val beside = markerLabelOrigin(listOf(100.0, 20.0), label, 18.0, false, bounds, frame)
        assertEquals(listOf(frame[0] + frame[2], 8.0), beside, "beside the frame where that moves it less")
        assertFalse(overlaps(beside, label, frame))
        // Right to left, the label stands left of its pin, and moves below the frame where that moves it less.
        val leftward = markerLabelOrigin(listOf(120.0, 40.0), label, 18.0, true, bounds, frame)
        assertEquals(listOf(22.0, bottom), leftward)
        assertFalse(overlaps(leftward, label, frame))
        // No frame held, the label stays beside its point.
        assertEquals(listOf(78.0, 28.0), markerLabelOrigin(listOf(60.0, 40.0), label, 18.0, false, bounds, null))
    }

    /**
     * A marker whose point stands off the map shows no label, never one held at the map's edge without its pin, as
     * `sdk-dart` reads its canvas: the left and top edges on the map, the right and bottom ones off it.
     */
    @Test
    fun aMarkerWhosePointIsOffTheMapShowsNoLabel() {
        val map = listOf(390.0, 844.0)

        assertTrue(isMarkerLabelShown(listOf(200.0, 400.0), map), "a point inside the map")
        assertTrue(isMarkerLabelShown(listOf(0.0, 0.0), map), "the map's top-left corner")
        assertTrue(isMarkerLabelShown(listOf(389.5, 843.5), map), "just inside its bottom-right corner")
        assertFalse(isMarkerLabelShown(listOf(390.0, 400.0), map), "on its right edge")
        assertFalse(isMarkerLabelShown(listOf(200.0, 844.0), map), "on its bottom edge")
        assertFalse(isMarkerLabelShown(listOf(-0.5, 400.0), map), "past its left edge")
        assertFalse(isMarkerLabelShown(listOf(200.0, -12.0), map), "above its top edge")
        assertFalse(isMarkerLabelShown(listOf(1200.0, 2400.0), map), "far off the map")
        assertFalse(isMarkerLabelShown(listOf(Double.NaN, 400.0), map), "a point that is no number")
        assertFalse(isMarkerLabelShown(listOf(200.0, Double.POSITIVE_INFINITY), map), "a point at no finite place")
        assertFalse(isMarkerLabelShown(listOf(200.0), map), "a point missing a figure")
        assertFalse(isMarkerLabelShown(listOf(0.0, 0.0), listOf(0.0, 0.0)), "a map not laid out yet")
    }

    @Test
    fun aMarkersLabelNothingInsideTheSafeAreaClearsStandsWithItsTopAtTheFramesBottom() {
        val label = listOf(280.0, 24.0)
        val placed = markerLabelOrigin(listOf(40.0, 30.0), label, 18.0, false, listOf(0.0, 0.0, 300.0, 80.0), kept())

        assertEquals(frameBottom, placed[1])
        assertFalse(overlaps(placed, label, kept()), "the mark is never covered")
    }

    @Test
    fun aStackInTheTopLeftCornerStartsBelowTheFrame() {
        val area = listOf(0.0, 24.0, 400.0, 800.0)

        val origin = controlStackOrigin(MapCorner.TopLeft, listOf(48.0, 200.0), area, inset = 10.0, kept(top = 24.0))

        assertEquals(10.0, origin[0])
        assertEquals(24.0 + frameBottom + 10.0, origin[1], 1e-9)
        assertEquals(listOf(10.0, 34.0), controlStackOrigin(MapCorner.TopLeft, listOf(48.0, 200.0), area, 10.0, null))
    }

    @Test
    fun aStackInAnyCornerKeepsToTheSafeArea() {
        val area = listOf(24.0, 48.0, 400.0, 768.0)
        val stack = listOf(48.0, 200.0)
        val frame = assertNotNull(keptClearFrame(OrganizationRead.Waiting, area), "a waiting read keeps it")

        assertEquals(listOf(342.0, 58.0), controlStackOrigin(MapCorner.TopRight, stack, area, 10.0, frame))
        assertEquals(listOf(342.0, 558.0), controlStackOrigin(MapCorner.BottomRight, stack, area, 10.0, frame))
        assertEquals(listOf(34.0, 558.0), controlStackOrigin(MapCorner.BottomLeft, stack, area, 10.0, frame))
    }

    @Test
    fun aStackInTheTopRightCornerOfANarrowMapStandsBelowTheFrame() {
        val area = listOf(0.0, 0.0, 200.0, 600.0)
        val stack = listOf(48.0, 200.0)

        val origin = controlStackOrigin(MapCorner.TopRight, stack, area, 10.0, kept())

        assertEquals(142.0, origin[0])
        assertEquals(frameBottom + 10.0, origin[1], 1e-9)
        assertFalse(overlaps(origin, stack, kept()))
    }

    @Test
    fun aStackInABottomCornerOfAShortMapMovesBesideTheFrame() {
        val area = listOf(0.0, 0.0, 400.0, 120.0)
        val stack = listOf(48.0, 100.0)

        val origin = controlStackOrigin(MapCorner.BottomLeft, stack, area, 10.0, kept())

        assertEquals(listOf(186.0, 10.0), origin, "beside the frame, as far from it as from the safe area's edge")
        assertFalse(overlaps(origin, stack, kept()))
    }

    @Test
    fun aReadoutStandsAtThePointFlippedLeftOrUpToStayInTheSafeArea() {
        val card = listOf(288.0, 200.0)
        val bounds = listOf(0.0, 0.0, 400.0, 800.0)

        assertEquals(listOf(50.0, 300.0), readoutOrigin(listOf(50.0, 300.0), card, bounds, kept()))
        assertEquals(listOf(102.0, 590.0), readoutOrigin(listOf(390.0, 790.0), card, bounds, kept()))
        assertEquals(listOf(10.0, 20.0), readoutOrigin(listOf(10.0, 20.0), card, bounds, keepClear = null))
        // A safe area too small for either is held inside it.
        assertEquals(listOf(0.0, 0.0), readoutOrigin(listOf(250.0, 150.0), card, listOf(0.0, 0.0, 280.0, 180.0), null))
    }

    @Test
    fun aReadoutThatWouldReachOverTheFrameMovesBelowItWhereThatMovesItLeast() {
        val card = listOf(288.0, 200.0)
        val frame = kept(top = 24.0)

        val placed = readoutOrigin(listOf(30.0, 40.0), card, listOf(0.0, 24.0, 400.0, 800.0), frame)

        assertEquals(listOf(30.0, frame[1] + frame[3]), placed)
        assertFalse(overlaps(placed, card, frame), "the readout keeps clear of the frame")
    }

    @Test
    fun aReadoutBesideTheFrameMovesRightWhereThatMovesItLess() {
        val card = listOf(100.0, 40.0)

        val placed = readoutOrigin(listOf(170.0, 10.0), card, listOf(0.0, 0.0, 800.0, 400.0), kept())

        assertEquals(listOf(176.0, 10.0), placed)
        assertFalse(overlaps(placed, card, kept()))
    }

    @Test
    fun aReadoutOnAShortNarrowMapOpensBelowTheFrameAndTheMapsEdgeCutsIt() {
        val card = listOf(288.0, 240.0)

        val placed = readoutOrigin(listOf(50.0, 50.0), card, listOf(0.0, 0.0, 390.0, 300.0), kept())

        assertEquals(listOf(50.0, frameBottom), placed)
        assertFalse(overlaps(placed, card, kept()), "the mark is never covered")
        assertTrue(placed[1] + card[1] > 300.0, "the map's edge cuts the card")
    }

    @Test
    fun aPanelOpensTowardTheInsideOfTheMapHeldInsideTheSafeArea() {
        val map = listOf(0.0, 0.0, 800.0, 600.0)
        val area = listOf(0.0, 24.0, 800.0, 600.0)
        val right = listOf(742.0, 34.0, 790.0, 82.0)
        val left = listOf(10.0, 200.0, 58.0, 248.0)

        assertEquals(742.0 - 8.0 - 340.0, panelLeft(right, map, area, 340.0, 8.0, fills = false, fillInset = 10.0))
        assertEquals(66.0, panelLeft(left, map, area, 340.0, 8.0, fills = false, fillInset = 10.0))
        assertEquals(10.0, panelLeft(right, map, area, 370.0, 8.0, fills = true, fillInset = 10.0))
        // A map in the window's left half opens a top-right toggle's panel to its left, inside the map.
        val half = listOf(0.0, 0.0, 600.0, 600.0)
        assertEquals(
            542.0 - 8.0 - 340.0,
            panelLeft(listOf(542.0, 10.0, 590.0, 58.0), half, half, 340.0, 8.0, false, 10.0),
        )
    }

    @Test
    fun aPanelThatWouldReachOverTheFrameOpensBelowIt() {
        // A stack with no zoom pair on a phone: its toggles stand at the top, and its panel fills the narrow map.
        val map = listOf(0.0, 0.0, 390.0, 800.0)
        val toggleTop = 10.0
        val left =
            panelLeft(listOf(332.0, toggleTop, 380.0, 58.0), map, map, 370.0, 8.0, fills = true, fillInset = 10.0)

        val room = panelRoom(map, map, left, 370.0, kept())
        val top = panelTop(toggleTop, room, height = 300.0)

        assertEquals(listOf(0.0, frameBottom, 390.0, 800.0), room)
        assertEquals(frameBottom, top)
        assertFalse(overlaps(listOf(left, top), listOf(370.0, 300.0), kept()))
        // A panel that stands clear of the frame across keeps the whole safe area.
        assertEquals(map, panelRoom(map, map, 200.0, 180.0, kept()))
        assertEquals(map, panelRoom(map, map, left, 370.0, keepClear = null))
    }

    @Test
    fun aPanelWithNoRoomUnderTheFrameOpensAtItsBottomAndTheMapsEdgeCutsIt() {
        val map = listOf(0.0, 0.0, 390.0, 90.0)
        val area = listOf(0.0, 0.0, 390.0, 60.0)

        val room = panelRoom(area, map, 10.0, 370.0, kept())

        assertEquals(listOf(0.0, frameBottom, 390.0, 90.0), room)
        assertEquals(frameBottom, panelTop(10.0, room, height = 200.0))
    }
}
