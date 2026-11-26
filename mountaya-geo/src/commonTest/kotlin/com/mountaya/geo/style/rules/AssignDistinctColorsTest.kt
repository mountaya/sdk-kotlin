package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class AssignDistinctColorsTest {
    @Test
    fun aBatchIsSpreadEvenlyOverTheColoursLeft() {
        assertEquals(listOf("#fa5252", "#4c6ef5", "#40c057"), assignDistinctColors(3, emptyList()))
        assertEquals(
            listOf("#be4bdb", "#228be6", "#40c057"),
            assignDistinctColors(3, listOf("#fa5252", "#e64980")),
        )
    }

    /** A reserved colour is compared without regard to the case of its letters. */
    @Test
    fun aReservedColourIsAvoidedWhateverItsCase() {
        assertEquals(listOf("#e64980"), assignDistinctColors(1, listOf("#FA5252")))
    }

    @Test
    fun moreColoursThanThePaletteHoldsRepeatAndACountUnderOneIsNone() {
        val fourteen = assignDistinctColors(14, emptyList())
        assertEquals(livelyPalette + livelyPalette.take(2), fourteen)
        assertEquals(emptyList(), assignDistinctColors(0, emptyList()))
        assertEquals(emptyList(), assignDistinctColors(-3, emptyList()))
    }

    @Test
    fun aPaletteEveryColourOfWhichIsReservedIsReadWhole() {
        val two = listOf("#000000", "#ffffff")
        assertEquals(listOf("#000000", "#ffffff"), assignDistinctColors(2, two, two))
        assertEquals(listOf("#fa5252"), assignDistinctColors(1, emptyList(), emptyList()))
    }
}
