package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SearchWordingTest {
    @Test
    fun theDefaultWordsNothingAndTwoAreEqualByTheirResolverAndUnits() {
        val resolve: (SearchLabel) -> String? = { "word" }
        assertNull(SearchWording().resolveLabel(SearchLabel.Highway("path")))
        assertEquals(
            SearchWording(UnitPreferences(language = "fr"), resolve),
            SearchWording(UnitPreferences(language = "fr"), resolve),
        )
        assertNotEquals(SearchWording(resolveLabel = resolve), SearchWording { "word" })
    }
}
