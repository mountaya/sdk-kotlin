package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AccessAnswerTest {
    @Test
    fun aRefusalNamesItsSourceOrNoneATileItsSourceAStyleNothing() {
        assertNotEquals<AccessAnswer>(AccessAnswer.Refused(null), AccessAnswer.Refused("slope"))
        assertNotEquals<AccessAnswer>(AccessAnswer.Tile("slope"), AccessAnswer.Refused("slope"))
        assertEquals<AccessAnswer>(AccessAnswer.Style, AccessAnswer.Style)
        assertEquals<AccessAnswer>(AccessAnswer.Tile("slope"), AccessAnswer.Tile("slope"))
    }
}
