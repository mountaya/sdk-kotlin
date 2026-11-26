package com.mountaya.elements

import android.graphics.Region
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import com.mountaya.geo.PlotCommand
import com.mountaya.geo.PlotPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlotPathTest {
    /** Whether [path], filled, covers the pixel at ([x], [y]) of a box [width] by [height] pixels. */
    private fun Path.covers(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): Boolean {
        val filled = Region()
        filled.setPath(asAndroidPath(), Region(0, 0, width, height))
        return filled.contains(x, y)
    }

    @Test
    fun aPointOfTheProfilesSquareStandsWhereTheBoxStretchesIt() {
        val size = Size(300f, 120f)

        assertEquals(Offset(150f, 30f), plotPoint(PlotPoint(50.0, 25.0), size))
        assertEquals(Offset.Zero, plotPoint(PlotPoint(0.0, 0.0), size))
        assertEquals(Offset(300f, 120f), plotPoint(PlotPoint(100.0, 100.0), size))
    }

    @Test
    fun commandsDrawTheShapeTheyStateClosedWhereTheyCloseIt() {
        val area =
            plotPath(
                listOf(
                    PlotCommand.Move(PlotPoint(0.0, 96.0)),
                    PlotCommand.Line(PlotPoint(0.0, 6.0)),
                    PlotCommand.Line(PlotPoint(100.0, 50.0)),
                    PlotCommand.Line(PlotPoint(100.0, 96.0)),
                    PlotCommand.Close,
                ),
                Size(200f, 100f),
            )

        assertEquals(Rect(0f, 6f, 200f, 96f), area.getBounds())
        assertTrue(area.covers(20, 80, 200, 100))
        assertFalse(area.covers(180, 20, 200, 100), "above the line from (0, 6) to (200, 50)")
    }

    @Test
    fun pointsDrawALineThroughThemAndNoPointNoLine() {
        val size = Size(200f, 100f)
        val line = plotLine(listOf(PlotPoint(0.0, 50.0), PlotPoint(50.0, 0.0)), size)

        assertEquals(Rect(0f, 0f, 100f, 50f), line.getBounds())
        assertTrue(plotLine(emptyList(), size).isEmpty)
    }
}
