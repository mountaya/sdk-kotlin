package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProfileClassTest {
    @Test
    fun twoClassifiedStretchesAreEqualWhereTheirEndsColourAndWordAre() {
        val path = ProfileClass(0.0, 500.0, "#2f9e44", "Path")

        assertEquals(ProfileClass(0.0, 500.0, "#2f9e44", "Path"), path)
        assertEquals(ProfileClass(0.0, 500.0, "#2f9e44", "Path").hashCode(), path.hashCode())
        assertNotEquals(ProfileClass(0.0, 500.0, "#2f9e44", "Trail"), path)
        assertNotEquals(ProfileClass(0.0, 501.0, "#2f9e44", "Path"), path)
    }
}
