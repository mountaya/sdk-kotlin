package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.scopeOf

/**
 * MountayaScope nests an elements scope stating [statement] in the one around it (`scopeOf`) for its [content]: every
 * element under it reads the settings the statement states, and every other one from the scope around it, as
 * `LocalMountayaScope` carries it. A scope nested in another nests the same way, so a surface keeps its host's
 * language, words and glyph sets where it states none of its own.
 *
 * [scheme] is the colour scheme the elements under it read their tokens in, light or dark whatever the system's, or
 * null for the one around it: the scheme an outer scope states, else the platform's configuration, whose night mode
 * reads the dark values.
 *
 * ```kotlin
 * MountayaScope(ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH")) {
 *     ZoomControl(map)
 * }
 * MountayaScope(scheme = ThemeScheme.Dark) { MapControls(map) }
 * ```
 *
 * @param statement What the nested scope states, every member it leaves null the scope around it's.
 * @param scheme The colour scheme the elements under it are drawn in, or null for the one around it.
 * @param content The elements under the nested scope.
 */
@Composable
public fun MountayaScope(
    statement: ElementsScopeStatement = ElementsScopeStatement(),
    scheme: ThemeScheme? = null,
    content: @Composable () -> Unit,
) {
    val outer = LocalMountayaScope.current
    val scope = remember(outer, statement) { scopeOf(outer, statement) }
    val stated = scheme ?: LocalMountayaScheme.current
    CompositionLocalProvider(LocalMountayaScope provides scope, LocalMountayaScheme provides stated, content = content)
}
