package com.mountaya.geo

import kotlin.jvm.JvmInline

/**
 * SearchNeedle is a query folded for matching (`geo.search.highlight`): [text] is the query folded as a
 * title is, the empty string where it folds to nothing. A round folds its query once into one, and every
 * row of every section it fills matches its title against that fold rather than folding the query again.
 */
@JvmInline
internal value class SearchNeedle(
    /** The query folded for matching, the empty string for one that folds to nothing. */
    val text: String,
)
