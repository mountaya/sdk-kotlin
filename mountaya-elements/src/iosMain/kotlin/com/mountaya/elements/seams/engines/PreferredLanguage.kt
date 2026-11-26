package com.mountaya.elements

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/** The first of Foundation's preferred languages, the order the reader set in the device's settings. */
internal actual fun preferredLanguage(): String? = NSLocale.preferredLanguages.firstOrNull() as? String
