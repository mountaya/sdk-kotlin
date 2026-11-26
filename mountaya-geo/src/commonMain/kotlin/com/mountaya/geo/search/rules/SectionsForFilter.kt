package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * sectionsForFilter answers the sections of [sections], each named by its search target and listed
 * in the order they render, that [filter] shows, in that same order: a list that renders its sections
 * in one order keeps it under every filter. A target the filter does not show, one this build does
 * not know among them, is left out.
 */
public fun sectionsForFilter(
    sections: List<DataSchema.SearchTarget>,
    filter: SearchFilter,
): List<DataSchema.SearchTarget> = sections.filter { it in filter.targets }
