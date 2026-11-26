package com.mountaya.geo

/** How many stops of a time slider's track make a day: one an hour. */
private const val DAY_STOPS: Int = 24

/**
 * The days, counted from the stop of the clock's reading, whose ticks carry a word, with the path of that word: two days
 * back, the reading itself, two days ahead and five days ahead, fixed anchors a reader scrubs by, the days between them
 * bare, since the thumb's readout states the exact moment.
 */
private val LABELLED_DAYS: List<Pair<Int, String>> =
    listOf(
        -2 to "layers.timeMinus2d",
        0 to "layers.timeNow",
        2 to "layers.timePlus2d",
        5 to "layers.timePlus5d",
    )

/**
 * timeSliderTicks answers the ticks a time slider marks on [window]'s track (`geo.layers.time-slider`): the stop of the
 * clock's reading and every whole day either side of it as far as the track runs, in the track's order, the ticks two
 * days back, at the reading, two days ahead and five days ahead carrying `layers.timeMinus2d`, `layers.timeNow`,
 * `layers.timePlus2d` and `layers.timePlus5d` and the others bare. The track's length is no clock's, so a track of no
 * instant is ticked the same.
 */
public fun timeSliderTicks(window: TimeSliderWindow): List<TimeSliderTick> {
    val labelled = LABELLED_DAYS.associate { (days, word) -> window.nowValue + days * DAY_STOPS to word }
    return (window.nowValue % DAY_STOPS..window.maxValue step DAY_STOPS).map { TimeSliderTick(it, labelled[it]) }
}
