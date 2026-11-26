package com.mountaya.geo

/**
 * isSearchListShown answers whether a search box shows its result list: the focus stands anywhere in the search
 * control, [hasFocus], the box, its filter chips or the list itself, over a [query] the reader has not dismissed,
 * [isDismissed].
 *
 * A query of white space alone shows no list, the white space being the set [trimWhiteSpace] trims, so a box the reader
 * cleared or only spaced shows none. A pick dismisses the list, and the next keystroke or the focus coming back to the
 * box lifts that, which the control itself tracks, as `sdk-dart`'s Geo holds the rule.
 */
public fun isSearchListShown(
    hasFocus: Boolean,
    query: String,
    isDismissed: Boolean,
): Boolean = hasFocus && trimWhiteSpace(query).isNotEmpty() && !isDismissed
