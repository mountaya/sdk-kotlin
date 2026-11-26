package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.PROFILE_SCRUB_COLUMNS
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.clampRange
import com.mountaya.geo.formatDistance
import com.mountaya.geo.profileDomain
import com.mountaya.geo.scrubDistance
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileNavigatorTest {
    @get:Rule
    val compose = createComposeRule()

    private val total = 10_000.0
    private val whole = ProfileRange(0.0, total)
    private val window = ProfileRange(2_000.0, 6_000.0)
    private val series = ItineraryDrawing(Itineraries.resolved, null).series

    /** The test itinerary's navigator over [shown], 300 density-independent pixels wide on a black ground. */
    @Composable
    private fun Strip(
        shown: ProfileRange,
        enabled: Boolean = true,
        onWindowChange: (ProfileRange) -> Unit = {},
    ) {
        Box(Modifier.background(Color.Black)) {
            ProfileNavigator(series, profileDomain(series), shown, enabled, Modifier.width(300.dp), onWindowChange)
        }
    }

    /** The node read as [word] of the navigator drawn under [tag]. */
    private fun read(
        word: String,
        tag: String,
    ): SemanticsNodeInteraction = compose.onNode(hasContentDescription(word) and hasAnyAncestor(hasTestTag(tag)))

    @Test
    fun aDragGrabsTheHandleWithinReachOfItElseTheWholeWindow() {
        assertEquals(NavigatorGrab.Part.Start, navigatorPart(70f, 60f, 180f, NAVIGATOR_HANDLE_REACH_DP))
        assertEquals(NavigatorGrab.Part.End, navigatorPart(200f, 60f, 180f, NAVIGATOR_HANDLE_REACH_DP))
        assertEquals(NavigatorGrab.Part.Whole, navigatorPart(120f, 60f, 180f, NAVIGATOR_HANDLE_REACH_DP))
    }

    @Test
    fun aHandleMovesItsEndToTheDistanceTheColumnUnderItStandsForNeverToASample() {
        val start = NavigatorGrab(NavigatorGrab.Part.Start, window)

        // Column 24 of 240 on a strip 240 pixels wide: 24.5 / 240 of the itinerary.
        assertEquals(ProfileRange(scrubDistance(24, whole), 6_000.0), movedWindow(start, 60f, 24f, 240f, total))
    }

    @Test
    fun aHandleDraggedPastTheStripStopsAtItsLastColumnAndPastItsPartnerSwaps() {
        val end = NavigatorGrab(NavigatorGrab.Part.End, window)

        assertEquals(ProfileRange(2_000.0, scrubDistance(239, whole)), movedWindow(end, 144f, 900f, 240f, total))
        assertEquals(ProfileRange(scrubDistance(24, whole), 2_000.0), movedWindow(end, 144f, 24f, 240f, total))
    }

    @Test
    fun theWindowDraggedWholeKeepsItsSpanAndStaysInsideTheItinerary() {
        val grab = NavigatorGrab(NavigatorGrab.Part.Whole, window)

        assertEquals(ProfileRange(3_000.0, 7_000.0), movedWindow(grab, 100f, 124f, 240f, total))
        assertEquals(ProfileRange(6_000.0, 10_000.0), movedWindow(grab, 100f, 400f, 240f, total))
        assertEquals(ProfileRange(0.0, 4_000.0), movedWindow(grab, 100f, -300f, 240f, total))
    }

    @Test
    fun aWindowNarrowerThanTheNarrowestIsWidenedAndAStripOfNoWidthMovesNothing() {
        val end = NavigatorGrab(NavigatorGrab.Part.End, window)

        assertEquals(ProfileRange(2_000.0, 2_100.0), movedWindow(end, 144f, 48f, 240f, total))
        assertEquals(window, movedWindow(end, 0f, 100f, 0f, total))
    }

    @Test
    fun theItineraryOutsideTheWindowIsVeiledAndTheWindowIsNot() {
        val length = series.totalDistance
        compose.setContent {
            Column {
                Drawn("open") { Strip(ProfileRange(0.0, length)) }
                Drawn("narrowed") { Strip(ProfileRange(length / 3, length * 2 / 3)) }
            }
        }
        val open = compose.image("open")
        val narrowed = compose.image("narrowed")

        compose.onNodeWithTag("open").assertHeightIsEqualTo(36.dp)
        assertFalse(compose.pixel(narrowed, 30.0, 33.0).isNear(compose.pixel(open, 30.0, 33.0)), "veiled")
        assertTrue(compose.pixel(narrowed, 150.0, 33.0).isNear(compose.pixel(open, 150.0, 33.0), tolerance = 2))
    }

    @Test
    fun eachHandleIsAdjustableAStepATwentiethOfTheItineraryAndNeitherWhileTheNavigatorIsDisabled() {
        val length = series.totalDistance
        val shown = ProfileRange(length / 3, length * 2 / 3)
        val changes = mutableListOf<ProfileRange>()
        compose.setContent {
            Column {
                Drawn("on") { Strip(shown, onWindowChange = { changes += it }) }
                Drawn("off") { Strip(shown, enabled = false, onWindowChange = { changes += it }) }
            }
        }

        read("Itinerary overview", "on").assertExists()
        read(START, "on")
            .assertRangeInfoEquals(ProgressBarRangeInfo(shown.from.toFloat(), 0f..length.toFloat(), steps = 19))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    formatDistance(shown.from, UnitPreferences("en")),
                ),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(length.toFloat()) }
        read(END, "on").performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        compose.runOnIdle {
            assertEquals(
                listOf(
                    clampRange(ProfileRange(shown.from + length / 20, shown.to), length),
                    clampRange(ProfileRange(shown.from, shown.to - length / 20), length),
                ),
                changes,
            )
        }
        read(START, "off").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetProgress))
        read(END, "off").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetProgress))
    }

    @Test
    fun aDragOverTheWindowMovesItWholeKeepingItsSpan() {
        val length = series.totalDistance
        val shown = ProfileRange(length / 3, length * 2 / 3)
        val changes = mutableListOf<ProfileRange>()
        compose.setContent { Drawn("strip") { Strip(shown, onWindowChange = { changes += it }) } }

        // From the middle of column 120 to the middle of column 146, clear of either handle.
        compose.onNodeWithContentDescription("Itinerary overview").performTouchInput {
            down(Offset(width * 120.5f / PROFILE_SCRUB_COLUMNS, height / 2f))
            moveTo(Offset(width * 146.5f / PROFILE_SCRUB_COLUMNS, height / 2f))
            up()
        }
        compose.runOnIdle {
            val moved = changes.last()
            val column = length / PROFILE_SCRUB_COLUMNS
            assertEquals(shown.to - shown.from, moved.to - moved.from, 1e-6, "$moved")
            assertEquals(shown.from + 26 * column, moved.from, column, "$moved")
        }
    }

    private companion object {
        /** What the window's start handle is read as. */
        const val START = "Start of the range shown"

        /** What the window's end handle is read as. */
        const val END = "End of the range shown"
    }
}
