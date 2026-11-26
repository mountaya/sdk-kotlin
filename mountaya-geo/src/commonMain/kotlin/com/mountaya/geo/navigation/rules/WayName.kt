package com.mountaya.geo

/** The name the API writes for a way that has none. */
private const val UNNAMED_WAY: String = "-"

/**
 * Returns the name of the way a step follows, or null where the way has none: an empty name and the
 * `-` the API writes for an unnamed way are no name, and any other is kept as written, white space
 * included.
 */
internal fun wayName(name: String?): String? = name?.takeIf { it.isNotEmpty() && it != UNNAMED_WAY }
