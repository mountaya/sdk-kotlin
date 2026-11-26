package com.mountaya.elements

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * locateMaxFixAge is how old a fix the platform holds may be and still be answered at once by the device's own
 * geolocation, the seam a map takes where it is handed none: `constants.maxFixAgeMs` of `map/camera-locate.json`.
 */
public val locateMaxFixAge: Duration = 60.seconds

/**
 * locateFixWait is how long the device's own geolocation waits for a fix it asked the platform for before it
 * answers `GeolocationRefusal.Unavailable`: `constants.fixWaitMs` of `map/camera-locate.json`.
 */
public val locateFixWait: Duration = 10.seconds

/**
 * Geolocation is where a map's fix of the device's position comes from: the platform's location services,
 * none at all, or the app's own, such as a fix its own location stack already holds.
 *
 * [locate] answers one fix or the reason there is none, and [permission] what asking would do, without
 * asking. Neither throws: a refusal is an answer a map reads, and `MapEngine.locate` moves nothing on one and
 * names its reason (`map.camera.locate`). A geolocation never asks the reader for location access: that
 * prompt and its wording are the app's, so one asked before the app holds a permission answers
 * `GeolocationRefusal.Denied`, and the permission reads `GeolocationPermission.Prompt`.
 *
 * It is asked from the thread the map is driven from, and suspends while it waits on a fix.
 */
public interface Geolocation {
    /** Answers one fix of the device's position, or the reason there is none. */
    public suspend fun locate(): GeolocationFix

    /** Answers what asking for a fix would do, without asking anyone. */
    public suspend fun permission(): GeolocationPermission
}
