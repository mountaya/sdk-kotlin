package com.mountaya.geo

/**
 * themeTokenValue is the value [token] reads under [scheme] (`geo.style.theme-tokens`): its dark value
 * under the dark scheme where it states one, and its light value otherwise.
 *
 * A consumer states a value of its own by setting the token whole on a copy of `ElementTokens.standard`,
 * `ElementTokens.standard.copy { controlBg = ThemeToken(light = HexColor("#000000")) }`. A token of a light
 * value alone reads it under both schemes, so a consumer's value stands in both, where a token as the table
 * states it reads the table's dark value under the dark scheme. A consumer who wants a value of its own
 * under each scheme states both, `ThemeToken(light = ..., dark = ...)`.
 */
public fun <Value> themeTokenValue(
    token: ThemeToken<Value>,
    scheme: ThemeScheme,
): Value =
    when (scheme) {
        ThemeScheme.Light -> token.light
        ThemeScheme.Dark -> token.dark ?: token.light
    }
