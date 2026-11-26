package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchPickTest {
    @Test
    fun aPickHoldsItsCameraMoveAndItsMarkAndIsEqualWhereBothAre() {
        val pick = SearchPick(SearchPickCamera.Fly(listOf(6.0, 45.0), 13.0), SearchHighlight.Place(listOf(6.0, 45.0)))

        assertEquals(SearchPickCamera.Fly(listOf(6.0, 45.0), 13.0), pick.camera)
        assertEquals(SearchHighlight.Place(listOf(6.0, 45.0)), pick.mark)
        assertEquals(
            SearchPick(SearchPickCamera.Fly(listOf(6.0, 45.0), 13.0), SearchHighlight.Place(listOf(6.0, 45.0))),
            pick,
        )
        assertNotEquals(SearchPick(SearchPickCamera.Fly(listOf(6.0, 45.0), 13.0), null), pick)
        assertEquals(SearchPick(null, null), SearchPick(null, null))
    }
}
