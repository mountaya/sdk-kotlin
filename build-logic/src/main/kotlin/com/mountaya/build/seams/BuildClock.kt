package com.mountaya.build.seams

/**
 * BuildClock is the one place the build's own tooling reads the wall clock (SPEC section 7).
 *
 * A gate that reads the environment where it decides cannot be reproduced, so nothing else in build-logic
 * reads the clock: a convention script reads the instant here and hands it down as an argument, as the
 * start of a device run is handed to `DeviceTestResults`, which counts only the reports written since.
 */
object BuildClock {
    /** Returns the wall clock's reading, in milliseconds since the epoch. */
    fun nowMillis(): Long = System.currentTimeMillis()
}
