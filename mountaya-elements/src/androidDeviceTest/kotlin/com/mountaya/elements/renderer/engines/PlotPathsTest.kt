package com.mountaya.elements

import androidx.compose.ui.geometry.Size
import com.mountaya.geo.profileDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class PlotPathsTest {
    private val drawing = ItineraryDrawing(Itineraries.resolved, null)

    @Test
    fun thePathsAreBuiltOnceForEachSizeTheChartIsDrawnAtAndKeptWhileItStands() {
        val paths = PlotPaths(drawing.series, profileDomain(drawing.series), drawing.whole)

        val first = paths.at(Size(300f, 150f))
        val again = paths.at(Size(300f, 150f))
        val wider = paths.at(Size(600f, 150f))

        assertSame(first, again, "the paths kept for the size they were built at")
        assertNotSame(first, wider)
        assertEquals(2, paths.builds, "one build for each size")
        assertEquals(3, paths.draws)
        assertEquals(600f, wider.area.getBounds().right, 1f)
        assertEquals(300f, first.line.getBounds().right, 1f)
    }

    @Test
    fun noWindowReadsTheWholeItineraryAsANavigatorDrawsIt() {
        val domain = profileDomain(drawing.series)
        val whole = PlotPaths(drawing.series, domain, drawing.whole).at(Size(300f, 36f))
        val unwindowed = PlotPaths(drawing.series, domain, null).at(Size(300f, 36f))

        assertEquals(whole.area.getBounds(), unwindowed.area.getBounds())
        assertEquals(whole.line.getBounds(), unwindowed.line.getBounds())
    }
}
