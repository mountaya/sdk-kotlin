package com.mountaya.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.mountaya.geo.ElementsScope
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalMountayaScopeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aComposableUnderNoScopeStatedReadsTheStandardOne() {
        var seen: ElementsScope? = null
        compose.setContent { seen = LocalMountayaScope.current }

        compose.runOnIdle { assertEquals(ElementsScope.standard, seen) }
    }
}
