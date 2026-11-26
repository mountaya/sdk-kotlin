package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.mountaya.elements.Controls.Drawn
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.ThemeScheme
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeSchemeInForceTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theSchemeInForceIsTheDarkOneUnderANightConfigurationAndTheLightOneOtherwise() {
        var light: ThemeScheme? = null
        var dark: ThemeScheme? = null
        compose.setContent {
            Column {
                Drawn("light") { light = themeSchemeInForce() }
                Drawn("dark", dark = true) { dark = themeSchemeInForce() }
            }
        }

        compose.runOnIdle {
            assertEquals(ThemeScheme.Light, light)
            assertEquals(ThemeScheme.Dark, dark)
        }
    }

    @Test
    fun aSchemeAScopeStatesStandsWhateverTheConfigurationAndANestedScopeStatingNoneKeepsIt() {
        var forcedDark: ThemeScheme? = null
        var forcedLight: ThemeScheme? = null
        var kept: ThemeScheme? = null
        var overridden: ThemeScheme? = null
        compose.setContent {
            Column {
                Drawn("light") {
                    MountayaScope(scheme = ThemeScheme.Dark) {
                        forcedDark = themeSchemeInForce()
                        MountayaScope(ElementsScopeStatement(locale = "fr-CH")) { kept = themeSchemeInForce() }
                        MountayaScope(scheme = ThemeScheme.Light) { overridden = themeSchemeInForce() }
                    }
                }
                Drawn("dark", dark = true) {
                    MountayaScope(scheme = ThemeScheme.Light) {
                        forcedLight =
                            themeSchemeInForce()
                    }
                }
            }
        }

        compose.runOnIdle {
            assertEquals(ThemeScheme.Dark, forcedDark, "a dark scope in a light system")
            assertEquals(ThemeScheme.Light, forcedLight, "a light scope in a dark system")
            assertEquals(ThemeScheme.Dark, kept, "a nested scope stating no scheme keeps the one around it")
            assertEquals(ThemeScheme.Light, overridden, "a nested scope stating one replaces it")
        }
    }
}
