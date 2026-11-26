package com.mountaya.elements

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.mountaya.geo.ElementsScope

/**
 * LocalMountayaScope is the elements scope every element under it reads (`geo.i18n.nested-scope`): the language its
 * words are in, the locale its figures are written in, the words themselves, and the glyph sets its controls and its
 * markers draw with. It is `ElementsScope.standard` where no composable states one, and `MountayaScope` nests a
 * scope in the one around its content.
 */
public val LocalMountayaScope: ProvidableCompositionLocal<ElementsScope> =
    staticCompositionLocalOf { ElementsScope.standard }
