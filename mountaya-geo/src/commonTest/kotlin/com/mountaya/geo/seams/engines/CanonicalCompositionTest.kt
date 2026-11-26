package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CanonicalCompositionTest {
    @Test
    fun aBaseAndTheMarksThatComposeWithItAreOneLetter() {
        assertEquals("Évian", canonicalComposition("Évian"))
        assertEquals("가", canonicalComposition("가"))
        assertEquals("ガ", canonicalComposition("ガ"))
    }

    /** A mark that composes with nothing is kept beside its base. */
    @Test
    fun aMarkThatComposesWithNothingIsKept() {
        assertEquals("q́", canonicalComposition("q́"))
    }
}
