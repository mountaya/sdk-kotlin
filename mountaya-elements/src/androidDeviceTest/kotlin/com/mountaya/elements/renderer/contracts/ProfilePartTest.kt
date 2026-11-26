package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ProfilePartTest {
    @Test
    fun theStandardPartsAreTheServicesAboveThePinsAloneAndEachPartIsItsOwn() {
        val every =
            listOf(
                ProfilePart.Amenities,
                ProfilePart.Sections,
                ProfilePart.Tooltip,
                ProfilePart.Navigator,
                ProfilePart.ElevationAxis,
                ProfilePart.DistanceAxis,
            )

        assertEquals(setOf(ProfilePart.Amenities), ProfilePart.standard)
        assertEquals(every, ProfilePart.entries, "six parts, each its own")
        assertEquals(every.size, every.toSet().size)
        assertFalse(ProfilePart.Tooltip in ProfilePart.standard, "the strip states the cursor by default")
    }
}
