package com.mountaya.geo

/**
 * AXIS_LABEL_GUTTER is the clear space a place name kept under a profile's distance axis keeps from its
 * neighbours, the `gutterPixels` `geo/profile-axis-label-culling.json` declares, in the boxes' own unit: two
 * names a hair apart read as glyphs and not as a row, since the eye cannot tell which distance belongs to
 * which name.
 */
public const val AXIS_LABEL_GUTTER: Double = 8.0

/**
 * cullCrowdedLabels is the keys of the place names under a profile's distance axis left undrawn, in itinerary
 * order (`geo.profile.axis-label-culling`).
 *
 * The first and the last of [boxes] are always drawn: they are the itinerary's own two ends, where a reader
 * looks first, and on a loop whose start and finish share a meter the order the places are listed in makes
 * the first the start and the last the finish. Each name between them, read left to right, is drawn only
 * where its left edge stands at least [gutter] past the right edge of the last name drawn and its right edge
 * at least that far before the last name's left edge, so the walk never paints itself into a collision at
 * the far end. Every other is left out: its place is still a pin on the curve, carrying its name. A key
 * listed twice and left out twice is stated once.
 */
public fun cullCrowdedLabels(
    boxes: List<AxisLabelBox>,
    gutter: Double = AXIS_LABEL_GUTTER,
): List<String> {
    if (boxes.size < 3) return emptyList()
    val last = boxes.last()
    val hidden = ArrayList<String>()
    var keptRight = boxes.first().right
    for (box in boxes.subList(1, boxes.size - 1)) {
        if (box.left >= keptRight + gutter && box.right + gutter <= last.left) {
            keptRight = box.right
            continue
        }
        if (box.key !in hidden) hidden += box.key
    }
    return hidden
}
