package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CanonicalDecompositionTest {
    @Test
    fun aComposedLetterIsWrittenAsItsBaseAndItsMarks() {
        assertEquals("Évian", canonicalDecomposition("Évian"))
        assertEquals("ẵ", canonicalDecomposition("ẵ"))
        assertEquals("가", canonicalDecomposition("가"))
        assertEquals("plain", canonicalDecomposition("plain"))
    }

    /** A stroke is no mark: nothing decomposes it. */
    @Test
    fun aLetterWithAStrokeHasNoDecomposition() {
        assertEquals("ł", canonicalDecomposition("ł"))
        assertEquals("ß", canonicalDecomposition("ß"))
    }
}
