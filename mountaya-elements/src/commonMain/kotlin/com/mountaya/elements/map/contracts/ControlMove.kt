package com.mountaya.elements

/** ControlMove is how a map control moves the camera (`map.camera.control-moves`), carrying the contract's spelling. */
internal enum class ControlMove(
    /** The move as `map/camera-control-moves.json` spells it. */
    val id: String,
) {
    /** Steps the camera one zoom in. */
    ZoomIn("zoom-in"),

    /** Steps the camera one zoom out. */
    ZoomOut("zoom-out"),

    /** Turns the bearing and the pitch to 0. */
    ResetNorth("reset-north"),
}
