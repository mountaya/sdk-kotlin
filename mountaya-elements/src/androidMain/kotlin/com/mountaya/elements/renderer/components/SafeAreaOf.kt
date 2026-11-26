package com.mountaya.elements

import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * safeAreaOf answers the safe area of a map laid at [map], where it stands in its window in pixels, `[left, top, right,
 * bottom]` in density-independent pixels from the map's own top-left corner, or null before it is laid out: the part
 * of the map the window's insets leave visible, the system bars, the status bar and the navigation bar among them, and a
 * display cutout, and never the on-screen keyboard, which comes and goes over the map as the reader types and moves
 * nothing laid out from the safe area, as `sdk-swift` reads a window's `safeAreaInsets` and `sdk-dart` a view's
 * `padding`.
 *
 * The insets are the window's own, its root window insets as the operating system reports them to the window
 * (`ViewCompat.getRootWindowInsets`), never a value the app's layout consumes or pads away, so a map drawn edge to edge
 * keeps its watermark and the SDK's own interface where the device leaves them visible (`geo.organization.watermark`),
 * and one laid out clear of the insets measures its safe area from its own corner. Compose's value stands in only until
 * the window holds root insets, before the view is attached, and the window's insets are read again at each layout of
 * its views, so a bar coming or going is followed.
 */
@Composable
internal fun safeAreaOf(map: Rect?): List<Double>? {
    // Compose's own reading, which composes this again as it moves, stands in until the window holds root insets.
    val composed = composedSafeInsets()
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val view = LocalView.current
    val root = rootInsetsOf(view)
    val laid = map ?: return null
    val insets =
        root ?: listOf(
            composed.getLeft(density, direction).toDouble(),
            composed.getTop(density).toDouble(),
            composed.getRight(density, direction).toDouble(),
            composed.getBottom(density).toDouble(),
        )
    // A window not yet measured is read as the map's own extent, which no inset reaches past.
    val window =
        if (view.rootView.width > 0 && view.rootView.height > 0) {
            listOf(view.rootView.width.toDouble(), view.rootView.height.toDouble())
        } else {
            listOf(laid.right.toDouble(), laid.bottom.toDouble())
        }
    return mapSafeArea(
        insets = insets,
        window = window,
        map = listOf(laid.left.toDouble(), laid.top.toDouble(), laid.width.toDouble(), laid.height.toDouble()),
    ).map { it / density.density }
}

/**
 * The root window insets [view]'s window holds, `[left, top, right, bottom]` in pixels ([rootSafeInsets]), read again
 * at each layout of the window's views, which the window's insets moving brings about, or null while it holds none.
 */
@Composable
private fun rootInsetsOf(view: View): List<Double>? {
    var insets by remember(view) { mutableStateOf(rootSafeInsets(ViewCompat.getRootWindowInsets(view))) }
    DisposableEffect(view) {
        val read =
            ViewTreeObserver.OnGlobalLayoutListener {
                val now = rootSafeInsets(ViewCompat.getRootWindowInsets(view))
                if (now != insets) insets = now
            }
        val observer = view.viewTreeObserver
        observer.addOnGlobalLayoutListener(read)
        read.onGlobalLayout()
        onDispose {
            if (observer.isAlive) observer.removeOnGlobalLayoutListener(read)
            view.viewTreeObserver.removeOnGlobalLayoutListener(read)
        }
    }
    return insets
}

/**
 * composedSafeInsets answers Compose's own reading of the insets a map's safe area keeps clear of, the system bars and a
 * display cutout, the keyboard left out, which stands in for the window's root insets until the window holds them.
 */
@Composable
internal fun composedSafeInsets(): WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)

/**
 * rootSafeInsets answers the part of a window [insets] reports covered, `[left, top, right, bottom]` in pixels from the
 * window's edges, physical whichever way the text runs: the system bars and a display cutout, or null where the window
 * holds no insets yet. The on-screen keyboard is no part of it, so a map's safe area, and all laid out from it, stays
 * where it stands as the keyboard comes and goes.
 */
internal fun rootSafeInsets(insets: WindowInsetsCompat?): List<Double>? {
    val types = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
    val covered = insets?.getInsets(types) ?: return null
    return listOf(covered.left.toDouble(), covered.top.toDouble(), covered.right.toDouble(), covered.bottom.toDouble())
}
