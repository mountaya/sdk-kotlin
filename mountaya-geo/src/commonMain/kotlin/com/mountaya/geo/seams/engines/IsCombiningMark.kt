package com.mountaya.geo

/**
 * Returns whether [codePoint] is a combining mark, a character of Unicode's general category M: a
 * nonspacing mark (an accent, an Arabic vowel), a spacing one (an Indic vowel sign) or an enclosing
 * one. Each is drawn on the character before it. The platform's character database answers it:
 * `Character.getType` on the jvm and android, Foundation's non-base character set on iOS, which is
 * that category.
 */
internal expect fun isCombiningMark(codePoint: Int): Boolean
