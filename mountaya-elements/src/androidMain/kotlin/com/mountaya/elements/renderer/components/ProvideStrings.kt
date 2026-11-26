package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.mountaya.client.InternalMountayaApi

/**
 * ProvideStrings provides [content] the scope around it with [strings] layered over it, a composable's own words by
 * path, `controls.zoomIn`, each standing over the scope's word there (`ElementsScope.withStrings`): the composable, every
 * control and panel it draws and every rule of Geo's it hands the scope read them, and every word they do not name is
 * the scope's. Handed none, it provides the very scope around it, so a composable that takes no strings recomposes
 * nothing for them.
 */
@OptIn(InternalMountayaApi::class)
@Composable
internal fun ProvideStrings(
    strings: Map<String, String>?,
    content: @Composable () -> Unit,
) {
    val outer = LocalMountayaScope.current
    val scope = remember(outer, strings) { outer.withStrings(strings) }
    CompositionLocalProvider(LocalMountayaScope provides scope, content = content)
}
