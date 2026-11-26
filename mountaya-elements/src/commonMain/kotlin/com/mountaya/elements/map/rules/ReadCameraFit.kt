package com.mountaya.elements

/**
 * readCameraFit reads a [fit]'s options before anything is read or drawn (`map.camera.fit-options`): it
 * answers the fit as stated, a member left null taking its default.
 *
 * It throws a [MapException] of kind `NumberRefused` where the ceiling is not a finite number, where the
 * duration is under 0 or endless, or where any edge of the padding is not a finite number from 0 up, the
 * ceiling read first, then the duration and the padding; the refusal names the member refused. A Kotlin
 * `Duration` holds an endless one, `Duration.INFINITE`, which is refused here where the contract's
 * endless duration is.
 */
internal fun readCameraFit(fit: CameraFit): CameraFit {
    val maxZoom = fit.maxZoom
    if (maxZoom != null && !maxZoom.isFinite()) throw MapException(MapException.Kind.NumberRefused, "maxZoom")
    val duration = fit.duration
    if (duration != null && (duration.isNegative() || duration.isInfinite())) {
        throw MapException(MapException.Kind.NumberRefused, "duration")
    }
    val padding = fit.padding
    if (padding != null &&
        !listOf(padding.top, padding.right, padding.bottom, padding.left).all { it.isFinite() && it >= 0.0 }
    ) {
        throw MapException(MapException.Kind.NumberRefused, "padding")
    }
    return fit
}
