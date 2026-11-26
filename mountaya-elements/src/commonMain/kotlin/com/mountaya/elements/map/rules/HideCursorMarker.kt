package com.mountaya.elements

/**
 * hideCursorMarker takes an element's cursor marker, pinned under [id], down from [map] where the cursor no longer
 * stands or the element goes (`map.profile.cursor-marker`), only where the element added it, [pinned]: a marker
 * another holder of the id pinned is never its to take. The element holds no marker after it.
 */
public fun hideCursorMarker(
    map: CursorMarkerTarget,
    pinned: CursorMarkerPin?,
    id: String,
) {
    if (pinned == null) return
    map.removeMarker(id)
}
