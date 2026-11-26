package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class SelectionExceptionTest {
    @Test
    fun theMessageNamesTheFieldByItsPath() {
        val failure = SelectionException(listOf("routes", "nope"), "no such field")

        assertEquals("`routes.nope`: no such field", failure.message)
        assertEquals(listOf("routes", "nope"), failure.path)
    }

    @Test
    fun aSelectionWrongAsAWholeHasNoPathToName() {
        assertEquals("selects nothing", SelectionException(emptyList(), "selects nothing").message)
    }
}
