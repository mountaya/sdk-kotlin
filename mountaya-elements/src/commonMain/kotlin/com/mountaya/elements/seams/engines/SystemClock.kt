package com.mountaya.elements

import kotlin.time.Clock

/**
 * The system's wall clock, the one read of the clock the elements make for themselves: a layers panel reads the time
 * through `LocalMountayaClock`, which holds it until a host provides a clock of its own, so a host that pins or replays
 * time moves the panel's window with it (`geo.layers.time-window`). No rule and no engine reads it.
 */
internal val systemClock: Clock = Clock.System
