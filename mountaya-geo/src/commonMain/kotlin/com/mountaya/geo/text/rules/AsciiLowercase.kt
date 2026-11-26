package com.mountaya.geo

/**
 * Returns [text] with each ASCII capital, `A` to `Z`, written as its small letter, and every other
 * character as it is.
 *
 * Where a rule compares words the contract writes in ASCII (a language subtag, a hex colour, an
 * attribute name), only those letters are folded, since a platform's full case mapping differs by
 * platform and by Unicode version, and folds letters such as the dotted capital I that compare as no
 * ASCII word.
 */
internal fun asciiLowercase(text: String): String {
    if (text.none { it in 'A'..'Z' }) return text
    return buildString(text.length) {
        for (char in text) append(if (char in 'A'..'Z') char + ('a' - 'A') else char)
    }
}
