package com.mountaya.elements

import kotlin.math.abs

/** The greatest latitude a centre may name, a pole's. */
private const val POLE_LATITUDE: Double = 90.0

/**
 * readCameraTarget reads a camera move's [target] before anything moves (`map.camera.pose`): it answers the
 * target as stated, each member it leaves null kept where the camera has it, and a centre past the
 * antimeridian handed on as written, since a renderer reads a longitude a turn away as the same meridian.
 *
 * It throws a [MapException] of kind `PositionRefused` where the centre is not two finite numbers with the
 * latitude from -90 to 90, an altitude beside them included, and of kind `NumberRefused` where the zoom, the
 * bearing or the pitch is not a finite number, the centre read first, then the zoom, the bearing and the
 * pitch; the refusal names the member refused.
 */
internal fun readCameraTarget(target: CameraTarget): CameraTarget {
    val center = target.center
    if (center != null &&
        !(center.size == 2 && center[0].isFinite() && center[1].isFinite() && abs(center[1]) <= POLE_LATITUDE)
    ) {
        throw MapException(MapException.Kind.PositionRefused, "center")
    }
    for ((member, value) in listOf("zoom" to target.zoom, "bearing" to target.bearing, "pitch" to target.pitch)) {
        if (value != null && !value.isFinite()) throw MapException(MapException.Kind.NumberRefused, member)
    }
    return target
}
