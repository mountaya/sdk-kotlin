package com.mountaya.elements

import com.mountaya.geo.CurveColor
import com.mountaya.geo.slopeBuckets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProfileColoringTest {
    @Test
    fun aColoringIsToldApartByItsCaseAndByWhatItCarries() {
        val stretch = CurveColor(0.0, 100.0, "#e03131")

        assertEquals<ProfileColoring>(ProfileColoring.Plain, ProfileColoring.Plain)
        assertEquals(ProfileColoring.Stretches(listOf(stretch)), ProfileColoring.Stretches(listOf(stretch)))
        assertNotEquals(ProfileColoring.Stretches(listOf(stretch)), ProfileColoring.Stretches(emptyList()))
        assertNotEquals(ProfileColoring.Slope(slopeBuckets), ProfileColoring.Slope(emptyList()))
        assertNotEquals<ProfileColoring>(ProfileColoring.Classes(emptyList()), ProfileColoring.Stretches(emptyList()))
        assertEquals(
            2,
            setOf(ProfileColoring.Plain, ProfileColoring.Plain, ProfileColoring.Slope(slopeBuckets)).size,
        )
    }

    /** A slope coloring the app names no buckets for paints Geo's own ramp, as the other SDKs' default theirs. */
    @Test
    fun aSlopeColoringNamingNoBucketsPaintsGeosOwnRamp() {
        assertEquals(slopeBuckets, ProfileColoring.Slope().buckets)
        assertEquals(ProfileColoring.Slope(slopeBuckets), ProfileColoring.Slope())
    }
}
