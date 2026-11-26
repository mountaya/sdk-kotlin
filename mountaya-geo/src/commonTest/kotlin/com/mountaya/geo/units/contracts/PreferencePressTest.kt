package com.mountaya.geo.units.contracts

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.PreferencePress
import com.mountaya.geo.UnitPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PreferencePressTest {
    @Test
    fun twoPressesAreEqualWhereTheyAskForTheSameSetAndTheSameRelabelling() {
        val set = UnitPreferences(language = "fr")

        assertEquals(
            PreferencePress(set, RestSchema.Language.Fr),
            PreferencePress(set, RestSchema.Language("fr")),
        )
        assertNotEquals(PreferencePress(set, null), PreferencePress(set, RestSchema.Language("fr")))
        assertNotEquals(PreferencePress(set, null), PreferencePress(UnitPreferences(language = "de"), null))
    }
}
