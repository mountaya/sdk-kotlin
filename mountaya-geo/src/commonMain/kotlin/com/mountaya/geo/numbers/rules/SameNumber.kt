package com.mountaya.geo

/**
 * Returns whether [a] and [b] are the same number, the relation every value of the geo layer is
 * compared by: `NaN` is the same as `NaN`, and 0 is another number than -0, as
 * `geo.camera.route-content` states for a route.
 *
 * It is the bit pattern compared, every `NaN` read as one. IEEE equality, which `==` on two
 * `Double` values is, says `NaN` is no number it equals and that the two zeros are one, so a value
 * compared by it would differ from itself and hash apart from a value it equals.
 */
internal fun sameNumber(
    a: Double,
    b: Double,
): Boolean = a.toBits() == b.toBits()

/** Returns whether [a] and [b] hold the same numbers, [sameNumber] each, in the same order. */
internal fun sameNumbers(
    a: List<Double>,
    b: List<Double>,
): Boolean = a.size == b.size && a.indices.all { sameNumber(a[it], b[it]) }

/**
 * Returns whether [a] and [b] are both absent or are the same number, [sameNumber], for a value
 * whose figure may be one nobody stated.
 */
internal fun sameNumberOrNull(
    a: Double?,
    b: Double?,
): Boolean = if (a == null || b == null) a == null && b == null else sameNumber(a, b)
