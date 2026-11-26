package com.mountaya.elements

/**
 * EditSnap is where an edit's magnet places a point the pointer holds: the position the point takes,
 * and whether the magnet moved it onto a line.
 *
 * A point the magnet leaves is the pointer's position as it was given, an altitude included; one it
 * moves is the position its projection reads back, `[longitude, latitude]`.
 */
internal class EditSnap(
    /** The position the point takes. */
    val position: List<Double>,
    /** Whether the magnet moved the point onto a line. */
    val isSnapped: Boolean,
)
