package com.mountaya.elements

import com.mountaya.geo.CameraPose
import com.mountaya.geo.ZoomBounds

/**
 * controlMoveTarget is where a map control moves [camera] (`map.camera.control-moves`): a zoom step one zoom
 * level in or out from the zoom it stands at, unrounded and held within [window], so a step past an end stops
 * at it; a reset of north the bearing and the pitch turned to 0. Each keeps every other member of the camera,
 * which a renderer's own verb may not, one rounding the zoom or keeping the pitch.
 */
internal fun controlMoveTarget(
    camera: CameraPose,
    window: ZoomBounds,
    move: ControlMove,
): CameraPose =
    when (move) {
        ControlMove.ZoomIn -> CameraPose(camera.center, held(camera.zoom + 1, window), camera.pitch, camera.bearing)
        ControlMove.ZoomOut -> CameraPose(camera.center, held(camera.zoom - 1, window), camera.pitch, camera.bearing)
        ControlMove.ResetNorth -> CameraPose(camera.center, camera.zoom, 0.0, 0.0)
    }

/** [zoom] brought within [window], a zoom that is no number left as it is. */
private fun held(
    zoom: Double,
    window: ZoomBounds,
): Double =
    when {
        zoom > window.maxZoom -> window.maxZoom
        zoom < window.minZoom -> window.minZoom
        else -> zoom
    }
