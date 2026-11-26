package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.ReadoutCell
import com.mountaya.geo.ReadoutStat
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.amenityStacks
import com.mountaya.geo.placeOnCurve
import com.mountaya.geo.profileDomain
import com.mountaya.geo.profilePolyline
import com.mountaya.geo.profileReadout
import com.mountaya.geo.profileX
import com.mountaya.geo.scrubGrid
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileChartTest {
    @get:Rule
    val compose = createComposeRule()

    private val drawing = ItineraryDrawing(Itineraries.resolved, null)
    private val english = UnitPreferences("en")
    private val accent = color("#1c7ed6")
    private val black = color("#000000")

    /**
     * The test itinerary's chart over its whole window, [WIDTH] by [HEIGHT] density-independent pixels on a black
     * ground: its places pinned where [places], the stretch [lit] tinted, the cursor on sample [cursor], and its card
     * where [tooltip].
     */
    @Composable
    private fun Chart(
        places: Boolean = true,
        lit: ProfileRange? = null,
        cursor: Int? = null,
        tooltip: Boolean = false,
        enabled: Boolean = true,
        onScrub: (Int?) -> Unit = {},
        onWaypointClick: ((String) -> Unit)? = null,
    ) {
        val series = drawing.series
        val domain = profileDomain(series)
        val placement =
            placeOnCurve(series, drawing.geometry, domain, drawing.whole, if (places) drawing.places else emptyList())
        val point = cursor?.let { series.points[it] }
        Box(Modifier.background(Color.Black)) {
            ProfileChart(
                series = series,
                domain = domain,
                window = drawing.whole,
                painted = emptyList(),
                placement = placement,
                stacks = amenityStacks(placement.pins),
                lit = lit,
                cursor = { point },
                readout = if (tooltip) ({ at -> profileReadout(series, at) }) else null,
                showsElevationAxis = false,
                units = english,
                formatCutoffAt = null,
                enabled = enabled,
                onScrub = onScrub,
                onWaypointClick = onWaypointClick,
                modifier = Modifier.size(WIDTH.dp, HEIGHT.dp),
            )
        }
    }

    /** The plot of the chart drawn under [tag]. */
    private fun plot(tag: String): SemanticsNodeInteraction =
        compose.onNode(hasContentDescription(PLOT) and hasAnyAncestor(hasTestTag(tag)))

    /** [values] with each run of one value told once. */
    private fun changes(values: List<Int?>): List<Int?> =
        values.filterIndexed { index, value -> index == 0 || values[index - 1] != value }

    @Test
    fun aScrubDrawsTheCursorAloneTheCurveKeptAsItWasDrawnFromPathsBuiltOnce() {
        val series = drawing.series
        val domain = profileDomain(series)
        val placement = placeOnCurve(series, drawing.geometry, domain, drawing.whole, emptyList())
        val paths = PlotPaths(series, domain, drawing.whole)
        var cursor by mutableStateOf<Int?>(null)
        compose.setContent {
            Drawn("chart") {
                Box(Modifier.background(Color.Black)) {
                    ProfileChart(
                        series = series,
                        domain = domain,
                        window = drawing.whole,
                        painted = emptyList(),
                        placement = placement,
                        stacks = emptyList(),
                        lit = null,
                        cursor = { cursor?.let { series.points[it] } },
                        readout = null,
                        showsElevationAxis = false,
                        units = english,
                        formatCutoffAt = null,
                        enabled = true,
                        onScrub = {},
                        onWaypointClick = null,
                        modifier = Modifier.size(WIDTH.dp, HEIGHT.dp),
                        paths = paths,
                    )
                }
            }
        }
        compose.image("chart")
        val drawn = compose.runOnIdle { paths.draws }

        for (index in listOf(1, 3, 5, series.points.lastIndex)) {
            cursor = index
            compose.waitForIdle()
            compose.image("chart")
        }

        compose.runOnIdle {
            assertTrue(drawn >= 1, "the curve was drawn")
            assertEquals(drawn, paths.draws, "a scrub draws the cursor alone")
            assertEquals(1, paths.builds, "the paths are built once for the chart's one size")
        }
        plot("chart").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun theCurveAndTheAreaUnderItAreDrawnWhereTheScalePlacesThemInTheAccent() {
        compose.setContent { Drawn("chart") { Chart(places = false) } }
        val image = compose.image("chart")
        val line = profilePolyline(drawing.series, profileDomain(drawing.series))
        val between = (plotPoint(line[0], Size(WIDTH, HEIGHT)) + plotPoint(line[1], Size(WIDTH, HEIGHT))) / 2f
        val stroke = compose.pixel(image, between.x.toDouble(), between.y.toDouble())
        val shaded = compose.pixel(image, between.x.toDouble(), 138.0)

        assertTrue(stroke.isNear(accent), "$stroke")
        // The area at 0.18 of the accent, over the black ground.
        assertTrue(shaded.isNear(Pixels.Pixel(5, 23, 39, 255)), "$shaded")
        assertTrue(compose.pixel(image, between.x.toDouble(), 2.0).isNear(black), "nothing above it")
    }

    @Test
    fun theLegAReaderPointsAtIsTintedUnderTheCurveAndOnlyThere() {
        val climb = drawing.rows[0]
        compose.setContent {
            Column {
                Drawn("plain") { Chart(places = false) }
                Drawn("tinted") { Chart(places = false, lit = ProfileRange(climb.from, climb.to)) }
            }
        }
        val plain = compose.image("plain")
        val tinted = compose.image("tinted")

        assertFalse(compose.pixel(tinted, 40.0, 138.0).isNear(compose.pixel(plain, 40.0, 138.0), tolerance = 2))
        assertTrue(compose.pixel(tinted, 260.0, 138.0).isNear(compose.pixel(plain, 260.0, 138.0), tolerance = 2))
    }

    @Test
    fun aPinStandsOnTheCurveAtEachPlaceInTheProfilesColourWhereThePlaceNamesNone() {
        compose.setContent { Drawn("chart") { Chart() } }
        val image = compose.image("chart")
        val series = drawing.series
        val placement = placeOnCurve(series, drawing.geometry, profileDomain(series), drawing.whole, drawing.places)

        assertEquals(listOf("hut", "col", "spring"), placement.pins.map { it.place.id })
        // The col stands at the summit, its pin above the chart's top edge, drawn over what is above.
        for (pin in placement.pins.filter { it.place.id != "col" }) {
            val centreX = pin.x / PROFILE_VIEW_BOX * WIDTH
            val centreY = pin.y / PROFILE_VIEW_BOX * HEIGHT - PROFILE_PIN_SIDE_DP / 2
            val face = compose.pixel(image, centreX + 7, centreY)
            assertTrue(face.isNear(color("#868e96")), "${pin.place.id}: $face")
        }
    }

    @Test
    fun theCursorsRuleCrossesTheChartAtItsSampleAndItsCardStandsBesideIt() {
        compose.setContent {
            Column {
                Drawn("none") { Chart(places = false) }
                Drawn("shown") { Chart(places = false, cursor = 1, tooltip = true) }
            }
        }
        val none = compose.image("none")
        val shown = compose.image("shown")
        val x = profileX(drawing.series.points[1].distance, drawing.series.totalDistance) / PROFILE_VIEW_BOX * WIDTH
        val column = generateSequence(1.0) { it + 0.5 }.takeWhile { it < 20.0 }.toList()
        val rule = color("#212529")

        assertTrue(column.any { compose.pixel(shown, x, it).isNear(rule) }, "the dashed rule above the curve")
        assertTrue(column.all { compose.pixel(none, x, it).isNear(black) })
        // The card stands 70 density-independent pixels after the cursor, on the side with room, on its own ground,
        // over the area under the curve, which the chart with no cursor shows there: the accent at 0.18 on black.
        val area = Pixels.Pixel(5, 23, 39, 255)
        assertFalse(compose.pixel(shown, x + 70, 36.0).isNear(area))
        assertTrue(compose.pixel(none, x + 70, 36.0).isNear(area))
    }

    @Test
    fun aPointerReadsTheSampleItsColumnStandsForHeldToTheChartsTwoEnds() {
        val grid = scrubGrid(drawing.series.points, drawing.whole)

        assertEquals(0, profileSampleAt(0f, 300f, grid))
        assertEquals(1, profileSampleAt(75f, 300f, grid))
        assertEquals(5, profileSampleAt(300f, 300f, grid))
        assertEquals(0, profileSampleAt(-20f, 300f, grid))
        assertEquals(5, profileSampleAt(900f, 300f, grid))
        assertNull(profileSampleAt(10f, 0f, grid))
        assertNull(profileSampleAt(10f, 300f, emptyList()))
    }

    @Test
    fun theCardLabelsEachFigureWithItsWordAndWritesItInTheReadersUnits() {
        val imperial = UnitPreferences("en", RestSchema.UnitLength.Imperial)

        assertEquals("profile.distance", readoutWord(ReadoutStat.Distance))
        assertEquals("profile.elevation", readoutWord(ReadoutStat.Elevation))
        assertEquals("1,200 m", readoutValue(ReadoutCell(ReadoutStat.Elevation, 1_200.0), english))
        assertEquals("2.2 km", readoutValue(ReadoutCell(ReadoutStat.Distance, 2_224.0), english))
        assertEquals("1,312 ft", readoutValue(ReadoutCell(ReadoutStat.Ascent, 400.0), imperial))
    }

    @Test
    fun thePlotIsReadAsAnAdjustableValueItsCursorItsStateAnAdjustmentScrubbingASample() {
        val scrubbed = mutableListOf<Int?>()
        compose.setContent {
            Column {
                Drawn("idle") { Chart(places = false, onScrub = { scrubbed += it }) }
                Drawn("cursor") { Chart(places = false, cursor = 2) }
                Drawn("off") { Chart(places = false, enabled = false) }
            }
        }

        plot("idle").assertRangeInfoEquals(ProgressBarRangeInfo(0f, 0f..5f, steps = 4))
        plot("cursor")
            .assertRangeInfoEquals(ProgressBarRangeInfo(2f, 0f..5f, steps = 4))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "1.1 km, 1,400 m"))
        plot("idle").performSemanticsAction(SemanticsActions.SetProgress) { it(3.4f) }
        compose.runOnIdle { assertEquals(listOf<Int?>(3), scrubbed, "the nearest sample") }
        plot("off").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetProgress))
    }

    @Test
    fun eachPinIsReadByItsPlacesNameAndPressedWhereTheAppHandsAHandlerItsServicesOneLabel() {
        val pressed = mutableListOf<String>()
        compose.setContent {
            Column {
                Drawn("pressed") { Chart(onWaypointClick = { pressed += it }) }
                Drawn("read") { Chart() }
            }
        }

        compose
            .onNode(hasContentDescription("Refuge") and hasAnyAncestor(hasTestTag("pressed")))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()
        compose.runOnIdle { assertEquals(listOf("hut"), pressed) }
        compose
            .onNode(hasContentDescription("Refuge") and hasAnyAncestor(hasTestTag("read")))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        compose.onAllNodesWithContentDescription("Col").assertCountEquals(2)
        compose.onAllNodesWithContentDescription("Unnamed waypoint").assertCountEquals(2)
        compose.onAllNodesWithContentDescription("Water, Hot meal, Toilets, First aid, Showers").assertCountEquals(2)
    }

    @Test
    fun aTouchScrubsWhileItIsDownAndAHoveringPointerWhileItHoversEachLettingGoAsItLeaves() {
        val scrubbed = mutableListOf<Int?>()
        compose.setContent { Drawn("chart") { Chart(places = false, onScrub = { scrubbed += it }) } }

        compose.onNodeWithContentDescription(PLOT).performTouchInput {
            down(Offset(width * 0.3f, height * 0.9f))
            moveTo(Offset(width * 0.8f, height * 0.9f))
            up()
        }
        compose.runOnIdle {
            assertEquals(listOf(1, 4, null), changes(scrubbed), "$scrubbed")
            scrubbed.clear()
        }
        compose.onNodeWithContentDescription(PLOT).performMouseInput {
            enter(Offset(width * 0.3f, height * 0.9f))
            moveTo(Offset(width * 0.99f, height * 0.9f))
            exit()
        }
        compose.runOnIdle { assertEquals(listOf(1, 5, null), changes(scrubbed), "$scrubbed") }
    }

    private companion object {
        /** How wide a chart is drawn, in density-independent pixels. */
        const val WIDTH = 300f

        /** How high a chart is drawn, in density-independent pixels. */
        const val HEIGHT = 150f

        /** What the plot is read as. */
        const val PLOT = "Elevation along the itinerary"
    }
}
