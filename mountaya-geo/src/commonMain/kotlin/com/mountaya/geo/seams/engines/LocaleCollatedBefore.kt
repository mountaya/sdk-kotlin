package com.mountaya.geo

/**
 * Returns whether [first] sorts before [second] for a reader of [languageTag], collated by the platform's locale
 * services in that locale, never the platform's default: `sauna` between `Food` and `Water` in English, whatever
 * their case. On the jvm and android this is `java.text`'s `Collator`, and on iOS Foundation's locale-aware
 * comparison of strings.
 */
internal expect fun localeCollatedBefore(
    first: String,
    second: String,
    languageTag: String,
): Boolean
