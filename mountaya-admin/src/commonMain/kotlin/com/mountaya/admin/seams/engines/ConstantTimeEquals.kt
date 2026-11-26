package com.mountaya.admin

/**
 * Returns whether [a] and [b] hold the same bytes, from the platform's own comparison, in a time that
 * never depends on where they first differ: a signature compared until its first wrong byte tells
 * whoever times the answer how much of a forgery is right.
 *
 * Arrays of different lengths are unequal, and are told apart by their lengths, which are never the
 * secret: a signature's length is the algorithm's.
 */
internal expect fun constantTimeEquals(
    a: ByteArray,
    b: ByteArray,
): Boolean
