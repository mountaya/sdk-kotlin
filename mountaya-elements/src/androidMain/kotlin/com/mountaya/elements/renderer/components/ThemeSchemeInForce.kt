package com.mountaya.elements

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.mountaya.geo.ThemeScheme

/**
 * themeSchemeInForce is the colour scheme the elements are drawn under where it is read: the one the nearest
 * `MountayaScope` around it states, and where none states one, the dark one where the platform's configuration is in
 * night mode, `isSystemInDarkTheme()`, and the light one otherwise. It is how an app's own composable under a scope
 * reads the elements' tokens as the elements beside it do, as `sdk-dart`'s `MountayaElements.schemeOf` reads it, and
 * a composition reading it composes again as the scheme in force changes. A host draws the elements dark in a light
 * system by stating the scheme on a scope, or as Compose draws anything so, by the configuration it hands its content.
 *
 * ```kotlin
 * val tokens = LocalMountayaTokens.current
 * val ink = themeTokenValue(tokens.controlFg, themeSchemeInForce())
 * ```
 *
 * `ink` is then the colour the controls beside it draw their glyphs in under the scheme in force.
 */
@Composable
@ReadOnlyComposable
public fun themeSchemeInForce(): ThemeScheme = LocalMountayaScheme.current ?: themeScheme(isSystemInDarkTheme())
