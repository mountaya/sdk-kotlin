package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InChainOrderTest {
    private class Entry(
        val name: String,
        val position: Int,
    )

    @Test
    fun aChainRunsByPositionWhateverOrderItArrivedIn() {
        val chain = listOf(Entry("c", 2), Entry("a", 0), Entry("b", 1))

        assertEquals(listOf("a", "b", "c"), inChainOrder(chain) { it.position }.map { it.name })
    }

    /**
     * A sort that kept no order between equals would swap two entries at one position from one run to the
     * next.
     */
    @Test
    fun twoEntriesAtOnePositionKeepTheOrderTheyAreListedIn() {
        val chain = (0 until 64).map { Entry("$it", it % 2) }

        val ordered = inChainOrder(chain) { it.position }

        assertEquals((0 until 32).map { "${it * 2}" } + (0 until 32).map { "${it * 2 + 1}" }, ordered.map { it.name })
    }

    @Test
    fun aPositionUnderZeroRunsFirstAndAnEmptyChainIsEmpty() {
        val chain = listOf(Entry("a", 0), Entry("before", -1))

        assertEquals(listOf("before", "a"), inChainOrder(chain) { it.position }.map { it.name })
        assertTrue(inChainOrder(emptyList<Entry>()) { it.position }.isEmpty())
    }
}
