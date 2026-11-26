package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlotCommandTest {
    @Test
    fun aCommandIsToldByItsKindAndItsPoint() {
        val point = PlotPoint(0.0, 100.0)

        assertEquals(PlotCommand.Move(PlotPoint(0.0, 100.0)), PlotCommand.Move(point))
        assertNotEquals<PlotCommand>(PlotCommand.Line(point), PlotCommand.Move(point))
        assertEquals(PlotCommand.Close, PlotCommand.Close)
    }
}
