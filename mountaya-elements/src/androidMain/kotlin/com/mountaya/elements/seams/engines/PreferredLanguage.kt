package com.mountaya.elements

import java.util.Locale

/**
 * The runtime's default locale, as a tag: on Android the language the reader set first, the app's own
 * where they chose one for it, and on the jvm the one the process was started in.
 */
internal actual fun preferredLanguage(): String? = Locale.getDefault().toLanguageTag().takeUnless { it == "und" }
