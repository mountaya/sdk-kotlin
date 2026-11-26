package com.mountaya.elements

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalMountayaTokensTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aComposableUnderNoTokensReadsTheStandardOnesAndTheTokensProvidedReachEveryComposableUnderThem() {
        val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
        var standard: ElementTokens? = null
        var provided: ElementTokens? = null
        compose.setContent {
            standard = LocalMountayaTokens.current
            CompositionLocalProvider(LocalMountayaTokens provides tokens) { provided = LocalMountayaTokens.current }
        }

        compose.runOnIdle {
            assertEquals(ElementTokens.standard, standard)
            assertEquals(tokens, provided)
        }
    }
}
