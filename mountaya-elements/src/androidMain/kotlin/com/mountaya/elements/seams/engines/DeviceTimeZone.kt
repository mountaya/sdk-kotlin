package com.mountaya.elements

import java.util.TimeZone

/**
 * The device's time zone, the one a time slider's readout writes its moment in, which the panel's caption says the times
 * are shown in (`layers.timeNote`): read where it is written, so a reader who travels reads the zone they are in.
 */
internal fun deviceTimeZone(): TimeZone = TimeZone.getDefault()
