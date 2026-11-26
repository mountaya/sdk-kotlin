package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.PreferenceLabel
import com.mountaya.geo.PreferenceValue
import com.mountaya.geo.UnitPreferences
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreferencesPanelTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aPressHandsTheWholeSetBackWithOnePreferenceChangedTheMapsLabelsKept() {
        val map = Controls.engine()
        var held = UnitPreferences(language = "de")
        compose.runOnUiThread {
            val labels = map.state.value.labelLanguage
            preferenceSelect(PreferenceValue.Length(RestSchema.UnitLength.Imperial), held, { held = it }, map)
            assertEquals(UnitPreferences(language = "de", length = RestSchema.UnitLength.Imperial), held)
            assertEquals(labels, map.state.value.labelLanguage)
        }
    }

    @Test
    fun aPressOnALanguageHandsItBackAndRelabelsTheMap() {
        val map = Controls.engine()
        var held = UnitPreferences(language = "de")
        compose.runOnUiThread {
            preferenceSelect(PreferenceValue.Language(RestSchema.Language.Fr), held, { held = it }, map)
            assertEquals("fr", held.language)
            assertEquals(RestSchema.Language.Fr, map.state.value.labelLanguage)
        }
    }

    @Test
    fun aUnitIsLabelledByTheScopesWordAndALanguageByItsOwnName() {
        assertEquals("Metric", preferenceLabel(PreferenceLabel.Word("metric"), ElementsScope.standard))
        assertEquals("Pace", preferenceLabel(PreferenceLabel.Word("perDistance"), ElementsScope.standard))
        assertEquals("Deutsch", preferenceLabel(PreferenceLabel.Name("Deutsch"), ElementsScope.standard))
    }

    @Test
    fun thePanelDrawsOnThePanelsGroundUnderTheSchemeInForceAndAPressWritesTheSet() {
        var held by mutableStateOf(UnitPreferences(language = "de"))
        compose.setContent {
            Column {
                Drawn("light") { PreferencesPanel(held, { held = it }) }
                Drawn("dark", dark = true) { PreferencesPanel(held, { held = it }) }
            }
        }

        assertTrue(compose.pixel(compose.image("light"), 4.0, 4.0).isNear(color("#ffffff")))
        assertTrue(compose.pixel(compose.image("dark"), 4.0, 4.0).isNear(color("#25262b")))
        compose.onAllNodesWithText("Imperial")[0].performClick()
        compose.runOnIdle { assertEquals(RestSchema.UnitLength.Imperial, held.length) }
    }
}
