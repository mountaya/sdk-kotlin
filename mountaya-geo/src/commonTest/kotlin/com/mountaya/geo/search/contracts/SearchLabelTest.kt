package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchLabelTest {
    @Test
    fun twoLabelsAreEqualWhereTheyNameOneValueOfOneKind() {
        assertEquals(
            SearchLabel.PoiCategory(DataSchema.POICategory("HUT")),
            SearchLabel.PoiCategory(DataSchema.POICategory("HUT")),
        )
        assertNotEquals<SearchLabel>(SearchLabel.Highway("path"), SearchLabel.Surface("path"))
        assertEquals(SearchLabel.SacScale("alpine_hiking"), SearchLabel.SacScale("alpine_hiking"))
    }
}
