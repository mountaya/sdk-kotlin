package com.example.consumer

/**
 * Returns how many [values] it was handed: what the consumer reads off a value, so each read is a
 * use the compiler sees, and a member the modules stop publishing fails the build.
 */
internal fun seen(vararg values: Any?): Int = values.size
