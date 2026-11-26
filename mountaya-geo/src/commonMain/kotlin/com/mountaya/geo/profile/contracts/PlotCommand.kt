package com.mountaya.geo

/**
 * PlotCommand is one command of a path drawn in a profile's plot, in the order a renderer draws them: the
 * area under a profile's curve is a move, the lines along the curve and down to the bottom of the plot, and a
 * close (`geo.profile.chart-path`).
 *
 * Two are equal where they are one command to the same point.
 */
public sealed interface PlotCommand {
    /** Move starts the path at [point]. */
    public class Move(
        /** Where the path starts. */
        public val point: PlotPoint,
    ) : PlotCommand {
        override fun equals(other: Any?): Boolean = other is Move && other.point == point

        override fun hashCode(): Int = point.hashCode()

        override fun toString(): String = "PlotCommand.Move(point=$point)"
    }

    /** Line draws a straight line to [point]. */
    public class Line(
        /** Where the line ends. */
        public val point: PlotPoint,
    ) : PlotCommand {
        override fun equals(other: Any?): Boolean = other is Line && other.point == point

        override fun hashCode(): Int = point.hashCode() * 31 + 1

        override fun toString(): String = "PlotCommand.Line(point=$point)"
    }

    /** Close draws the path back to where it started. */
    public object Close : PlotCommand {
        override fun toString(): String = "PlotCommand.Close"
    }
}
