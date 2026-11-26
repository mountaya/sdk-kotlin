package com.mountaya.elements

/**
 * preferredLanguage answers the language the device's reader prefers first, a BCP 47 tag, or null
 * where the platform names none: the environment a map's labels fall back to where neither the map
 * nor its client names a language (`geo.style.label-language`). It is the one read of the device's
 * language this module makes, and it reads nothing else.
 */
internal expect fun preferredLanguage(): String?
