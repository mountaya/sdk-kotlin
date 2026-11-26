package com.mountaya.client

/**
 * Stated is a value a payload states, `null` included, where a property it leaves out reads as no
 * `Stated` at all.
 *
 * A webhook event's previous attributes are its one use: a field that may be null reads as a
 * `Stated` holding the value it held before the change, `null` included where it held null, and as
 * null where the change did not move it, so the two absences a plain nullable would read alike stay
 * apart.
 */
public class Stated<out T>(
    /** The value stated, `null` included. */
    public val value: T,
) {
    override fun equals(other: Any?): Boolean = other is Stated<*> && other.value == value

    override fun hashCode(): Int = value?.hashCode() ?: 0

    override fun toString(): String = "Stated($value)"
}
