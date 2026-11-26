package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class SuccessKindTest {
    @Test
    fun theFourShapesASuccessArrivesIn() {
        assertEquals(listOf("Envelope", "EventStream", "Binary", "Graphql"), SuccessKind.entries.map { it.name })
    }
}
