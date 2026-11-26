package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class RandomSourceTest {
    /** A source of the caller's own, answering the draws it was given in order and keys where it has a source of them. */
    private class Scripted(
        draws: List<Double>,
        private val keys: Boolean,
    ) : RandomSource {
        private val remaining = ArrayDeque(draws)

        override fun unitDraw(): Double = remaining.removeFirst()

        override fun cryptographicUuid(): Uuid? = if (keys) Uuid.parse("6f1e2d3c-4b5a-4987-8a6b-5c4d3e2f1a0b") else null
    }

    @Test
    fun aSourceOfTheCallersOwnAnswersItsDrawsInOrderAndAKeyOrNone() {
        val source = Scripted(listOf(0.25, 0.5, 0.75), keys = true)

        assertEquals(listOf(0.25, 0.5, 0.75), List(3) { source.unitDraw() })
        assertEquals("6f1e2d3c-4b5a-4987-8a6b-5c4d3e2f1a0b", source.cryptographicUuid().toString())
        assertNull(Scripted(emptyList(), keys = false).cryptographicUuid())
    }
}
