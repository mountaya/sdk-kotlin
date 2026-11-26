package com.mountaya.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SafeAreaOfTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aSpaceNotYetLaidOutHasNoSafeAreaYet() {
        var area: List<Double>? = listOf(0.0)
        compose.setContent { area = safeAreaOf(null) }

        compose.runOnIdle { assertNull(area) }
    }

    @Test
    fun aSpaceLaidInItsWindowMeasuresItsSafeAreaFromEveryInsetItLiesUnder() {
        var area: List<Double>? = null
        var expected: List<Double>? = null
        compose.setContent {
            var laid by remember { mutableStateOf<Rect?>(null) }
            val density = LocalDensity.current
            val direction = LocalLayoutDirection.current
            val insets = composedSafeInsets()
            val view = LocalView.current
            val root = view.rootView
            area = safeAreaOf(laid)
            expected =
                laid?.let { space ->
                    mapSafeArea(
                        // The window's own root insets, Compose's standing in only before the window holds any.
                        insets =
                            rootSafeInsets(ViewCompat.getRootWindowInsets(view)) ?: listOf(
                                insets.getLeft(density, direction).toDouble(),
                                insets.getTop(density).toDouble(),
                                insets.getRight(density, direction).toDouble(),
                                insets.getBottom(density).toDouble(),
                            ),
                        window = listOf(root.width.toDouble(), root.height.toDouble()),
                        map =
                            listOf(
                                space.left.toDouble(),
                                space.top.toDouble(),
                                space.width.toDouble(),
                                space.height.toDouble(),
                            ),
                    ).map { it / density.density }
                }
            Box(Modifier.fillMaxSize().onGloballyPositioned { laid = Rect(it.positionInWindow(), it.size.toSize()) })
        }

        compose.runOnIdle {
            val measured = assertNotNull(area, "the space is laid out")
            assertEquals(assertNotNull(expected, "the space is laid out"), measured)
            assertTrue(measured[2] > measured[0] && measured[3] > measured[1], "the window leaves part of it visible")
        }
    }

    /**
     * The window's own insets are read off its root insets: the system bars and a display cutout, and never the
     * keyboard, which leaves the safe area where it stands as it comes and goes.
     */
    @Test
    fun theWindowsRootInsetsAreReadTheBarsAndTheCutoutAndNeverTheKeyboard() {
        val insets =
            WindowInsetsCompat
                .Builder()
                .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, 48, 0, 0))
                .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, 32))
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(24, 0, 0, 0))
                .build()

        assertEquals(listOf(24.0, 48.0, 0.0, 32.0), rootSafeInsets(insets))
        val typing =
            WindowInsetsCompat
                .Builder(insets)
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 300))
                .build()
        assertEquals(listOf(24.0, 48.0, 0.0, 32.0), rootSafeInsets(typing), "the keyboard moves no edge of it")
        assertNull(rootSafeInsets(null), "a window holding no insets yet reads none")
    }

    /** A view attached to its window reads the root insets the window holds, which the safe area is measured from. */
    @Test
    fun anAttachedViewReadsItsWindowsRootInsets() {
        var read: List<Double>? = null
        var held: List<Double>? = null
        compose.setContent {
            val view = LocalView.current
            Box(
                Modifier.fillMaxSize().onGloballyPositioned {
                    read = rootSafeInsets(ViewCompat.getRootWindowInsets(view))
                    held = rootSafeInsets(ViewCompat.getRootWindowInsets(view.rootView))
                },
            )
        }

        compose.runOnIdle {
            assertNotNull(read, "an attached view's window holds root insets")
            assertEquals(held, read, "every view of a window reads the window's own insets")
        }
    }
}
