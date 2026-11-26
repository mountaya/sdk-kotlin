package com.mountaya.elements

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SymbolLabelTest {
    @Test
    fun aLabelIsItsLayersIdAndItsTextField() {
        assertEquals(
            SymbolLabel("place-label", JsonPrimitive("{name}")),
            SymbolLabel("place-label", JsonPrimitive("{name}")),
        )
        assertNotEquals(
            SymbolLabel("place-label", JsonPrimitive("{name}")),
            SymbolLabel("place-label", JsonPrimitive("{ref}")),
        )
    }
}
