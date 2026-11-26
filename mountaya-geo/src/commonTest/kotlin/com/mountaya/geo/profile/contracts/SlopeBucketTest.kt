package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SlopeBucketTest {
    @Test
    fun twoBucketsAreEqualWhereTheyStartAtOneNumberWithTextsAlike() {
        val flat = SlopeBucket(-3.0, "flat", "#2f9e44")

        assertEquals(SlopeBucket(-3.0, "flat", "#2f9e44"), flat)
        assertEquals(SlopeBucket(-3.0, "flat", "#2f9e44").hashCode(), flat.hashCode())
        assertNotEquals(SlopeBucket(3.0, "flat", "#2f9e44"), flat)
        assertEquals(
            SlopeBucket(Double.NEGATIVE_INFINITY, "a", "#000"),
            SlopeBucket(Double.NEGATIVE_INFINITY, "a", "#000"),
        )
    }
}
