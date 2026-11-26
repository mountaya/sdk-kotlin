package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class FirstGraphemeClusterTest {
    @Test
    fun aLetterKeepsEveryMarkWrittenOverIt() {
        assertEquals("D", firstGraphemeCluster("Drop bag"))
        assertEquals("é", firstGraphemeCluster("étape"))
        assertEquals("ä́", firstGraphemeCluster("ä́b"))
        assertEquals("é", firstGraphemeCluster("étape"))
    }

    @Test
    fun anEmojiIsReadWholeWithItsModifiersAndJoinedParts() {
        assertEquals("🚰", firstGraphemeCluster("🚰 Water"))
        assertEquals("👋🏽", firstGraphemeCluster("👋🏽!"))
        val family = "👨‍👩‍👧"
        assertEquals(family, firstGraphemeCluster("$family Famille"))
        assertEquals("🇫🇷", firstGraphemeCluster("🇫🇷🇩🇪"))
    }

    @Test
    fun aLineEndAndAHangulSyllableAreOneClusterEach() {
        assertEquals("\r\n", firstGraphemeCluster("\r\nhut"))
        assertEquals("각", firstGraphemeCluster("각ᄀ"))
    }

    @Test
    fun anEmptyTextHoldsNone() {
        assertEquals("", firstGraphemeCluster(""))
    }
}
