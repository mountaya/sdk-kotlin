package com.mountaya.geo

/** `Character.getType`, which reads a supplementary code point as the one character it is. */
internal actual fun isCombiningMark(codePoint: Int): Boolean =
    when (Character.getType(codePoint).toByte()) {
        Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.ENCLOSING_MARK -> true
        else -> false
    }
