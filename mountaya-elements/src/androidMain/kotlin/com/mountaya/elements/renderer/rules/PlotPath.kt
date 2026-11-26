package com.mountaya.elements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.mountaya.geo.PROFILE_VIEW_BOX
import com.mountaya.geo.PlotCommand
import com.mountaya.geo.PlotPoint

/**
 * plotPath is a path drawn in a profile's 100-unit square, scaled into a box of [size]: each of [commands]' points
 * stretched across its width and down its height, as a chart stretches its square (`geo.profile.chart-path`).
 */
internal fun plotPath(
    commands: List<PlotCommand>,
    size: Size,
): Path {
    val path = Path()
    for (command in commands) {
        when (command) {
            is PlotCommand.Move -> plotPoint(command.point, size).let { path.moveTo(it.x, it.y) }
            is PlotCommand.Line -> plotPoint(command.point, size).let { path.lineTo(it.x, it.y) }
            PlotCommand.Close -> path.close()
        }
    }
    return path
}

/** plotLine is a line through [points] of a profile's square, scaled into a box of [size], empty for no point. */
internal fun plotLine(
    points: List<PlotPoint>,
    size: Size,
): Path {
    val path = Path()
    for ((index, point) in points.withIndex()) {
        val at = plotPoint(point, size)
        if (index == 0) path.moveTo(at.x, at.y) else path.lineTo(at.x, at.y)
    }
    return path
}

/** plotPoint is where [point] of a profile's square stands in a box of [size]. */
internal fun plotPoint(
    point: PlotPoint,
    size: Size,
): Offset =
    Offset((point.x / PROFILE_VIEW_BOX * size.width).toFloat(), (point.y / PROFILE_VIEW_BOX * size.height).toFloat())
