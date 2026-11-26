package com.mountaya.geo

/**
 * PROFILE_SCRUB_COLUMNS is how many equal columns a scrub reads a profile's window through, the `columns`
 * `geo/profile-scrub-grid.json` declares: on a chart three hundred pixels wide a little over one pixel a
 * column, finer than a pointer is read and than a curve of five hundred samples can show. It is a count
 * rather than a width, so a chart reads its pointer without measuring itself, and a column is exactly what a
 * key press steps.
 */
public const val PROFILE_SCRUB_COLUMNS: Int = 240

/** The fraction of a column its centre stands at. */
private const val COLUMN_CENTRE = 0.5

/**
 * scrubGrid is the sample of [points] each column of a scrub over [window] resolves to, column by column from
 * the window's start (`geo.profile.scrub-grid`): the one [nearestSampleIndex] finds nearest the distance at
 * the column's centre, [scrubDistance], and null for a series with no sample. A chart reads a pointer and a
 * key press through the same grid, and a navigator drags through it.
 */
public fun scrubGrid(
    points: List<ElevationPoint>,
    window: ProfileRange,
): List<Int?> = List(PROFILE_SCRUB_COLUMNS) { nearestSampleIndex(points, scrubDistance(it, window)) }

/**
 * scrubDistance is the distance [column] of a scrub over [window] stands for, in meters from the itinerary's
 * start: `window.from + (column + 0.5) / PROFILE_SCRUB_COLUMNS` of the window's width, the window read as
 * given (`geo.profile.scrub-grid`). A window's edge a navigator drags is a place on the axis rather than a
 * sample, so it moves by this distance and never snaps to a sample.
 */
public fun scrubDistance(
    column: Int,
    window: ProfileRange,
): Double = window.from + (column + COLUMN_CENTRE) / PROFILE_SCRUB_COLUMNS * (window.to - window.from)
