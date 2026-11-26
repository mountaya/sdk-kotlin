package com.mountaya.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.mountaya.geo.ThemeScheme
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalMountayaSchemeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aComposableUnderNoScopeStatingASchemeReadsNoneAndAScopeHandsItsOwnDown() {
        var unstated: ThemeScheme? = ThemeScheme.Light
        var stated: ThemeScheme? = null
        compose.setContent {
            unstated = LocalMountayaScheme.current
            MountayaScope(scheme = ThemeScheme.Dark) { stated = LocalMountayaScheme.current }
        }

        compose.runOnIdle {
            assertNull(unstated)
            assertEquals(ThemeScheme.Dark, stated)
        }
    }
}
