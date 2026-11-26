package com.mountaya.geo

import platform.Foundation.NSCharacterSet

/** Foundation's non-base character set, the characters of Unicode's general category M. */
private val marks: NSCharacterSet = NSCharacterSet.nonBaseCharacterSet

/** Whether Foundation's non-base character set holds [codePoint]. */
internal actual fun isCombiningMark(codePoint: Int): Boolean =
    codePoint >= 0 && marks.longCharacterIsMember(codePoint.toUInt())
