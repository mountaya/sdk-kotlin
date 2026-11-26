package com.mountaya.elements

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.geo.CurveDivider
import com.mountaya.geo.ProfilePlace
import com.mountaya.geo.ProfileRange
import com.mountaya.geo.ProfileWaypoint
import com.mountaya.geo.UnitPreferences
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfileDistanceAxisTest {
    @get:Rule
    val compose = createComposeRule()

    /** The place [id] named [name], dividing the itinerary [x] units across the chart. */
    private fun divider(
        id: String,
        name: String,
        x: Double,
    ): CurveDivider =
        CurveDivider(id, ProfilePlace(ProfileWaypoint.AtDistance(id, x * 100), name = name), x * 100, x, 50.0)

    /** Three names, the second crowding the first. */
    private val crowded =
        listOf(
            divider("hut", "Refuge du Lac Blanc", 10.0),
            divider("col", "Col des Montets", 14.0),
            divider("spring", "Source", 90.0),
        )

    /** What [read] answers of the text measurer and the density the composition measures with, on the main thread. */
    private fun <T> measuring(read: (measurer: TextMeasurer, density: Float) -> T): T {
        var measurer: TextMeasurer? = null
        var density = 0f
        compose.setContent {
            measurer = rememberTextMeasurer()
            density = LocalDensity.current.density
        }
        return compose.runOnIdle { read(assertNotNull(measurer, "the composition measures no text"), density) }
    }

    /** The keys of the names of [dividers] kept under a row [width] density-independent pixels wide. */
    private fun TextMeasurer.kept(
        dividers: List<CurveDivider>,
        width: Float,
        density: Float,
    ): List<String> = shownProfileNames(dividers, width * density, density, this).map { it.key }

    @Test
    fun everyNameIsDrawnWhereTheRowHasRoomForIt() {
        val dividers =
            listOf(divider("hut", "Refuge", 10.0), divider("col", "Col", 50.0), divider("spring", "Source", 90.0))

        val kept = measuring { measurer, density -> measurer.kept(dividers, 600f, density) }

        assertEquals(listOf("hut", "col", "spring"), kept)
    }

    @Test
    fun aNameCrowdingItsNeighbourIsLeftOutTheFirstAndTheLastAlwaysDrawn() {
        measuring { measurer, density ->
            assertEquals(listOf("hut", "spring"), measurer.kept(crowded, 300f, density))
            assertTrue(measurer.kept(emptyList(), 300f, density).isEmpty())
        }
    }

    @Test
    fun theRowIsAsHighAsItsStopsOverItsNames() {
        compose.setContent {
            Drawn("axis") {
                ProfileDistanceAxis(
                    12_000.0,
                    ProfileRange(0.0, 12_000.0),
                    emptyList(),
                    UnitPreferences("en"),
                    Modifier.width(300.dp),
                )
            }
        }

        compose.onNodeWithTag("axis").assertWidthIsEqualTo(300.dp).assertHeightIsEqualTo(32.dp)
    }

    @Test
    fun theRowGrowsWithTheReadersFontScaleSoItsNamesNeverClip() {
        var scale by mutableStateOf(2f)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = scale)) {
                Drawn("axis") {
                    ProfileDistanceAxis(
                        12_000.0,
                        ProfileRange(0.0, 12_000.0),
                        listOf(divider("hut", "Refuge", 10.0), divider("spring", "Source", 90.0)),
                        UnitPreferences("en"),
                        Modifier.width(300.dp),
                    )
                }
            }
        }

        for ((stated, height) in listOf(2f to 64.dp, 0.85f to 32.dp)) {
            scale = stated
            compose.waitForIdle()
            compose.onNodeWithTag("axis").assertHeightIsEqualTo(height)
            val row = compose.onNodeWithTag("axis").getBoundsInRoot()
            val name = compose.onNodeWithText("Source").getBoundsInRoot()
            assertTrue(name.bottom <= row.bottom && name.top >= row.top, "at $stated, $name within $row")
        }
    }

    @Test
    fun theAxisWritesTheNamesItKeepsAndLeavesTheCrowdedOneOut() {
        compose.setContent {
            Drawn("axis") {
                ProfileDistanceAxis(
                    12_000.0,
                    ProfileRange(0.0, 12_000.0),
                    crowded,
                    UnitPreferences("en"),
                    Modifier.width(300.dp),
                )
            }
        }

        compose.onAllNodesWithText("Refuge du Lac Blanc").assertCountEquals(1)
        compose.onAllNodesWithText("Source").assertCountEquals(1)
        compose.onAllNodesWithText("Col des Montets").assertCountEquals(0)
    }
}
