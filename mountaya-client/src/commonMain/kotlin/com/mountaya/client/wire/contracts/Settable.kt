package com.mountaya.client

import kotlinx.serialization.Serializable

/**
 * Settable is a request property with three states: left out, set to a value, and set to `null`.
 *
 * The contract's rule is that `null` clears a stored value on a merge, so a property a request
 * sends that is optional and nullable says one of three things, where a plain nullable says only
 * two. A printed type keeps one privately, its builder writes it, and the client's `Json` leaves
 * [Absent] out of the request, since it is the property's default.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, keeps its own from beside the core, and no
 * consumer reads it (SPEC.md section 7).
 */
@InternalMountayaApi
@Serializable(with = SettableSerializer::class)
public sealed interface Settable<out T> {
    /** The value set, `null` included, or null where nothing was set. */
    public val valueOrNull: T?

    /** The value set as a [Stated], which holds a `null` set as one, or null where nothing was set. */
    public val stated: Stated<T>?

    /** Absent is a property nobody set. It is left out of the request, and the stored value is untouched. */
    @InternalMountayaApi
    public object Absent : Settable<Nothing> {
        override val valueOrNull: Nothing? get() = null

        override val stated: Stated<Nothing>? get() = null

        override fun toString(): String = "Absent"
    }

    /** Present is a property set to [value], which replaces the stored value, or clears it where it is `null`. */
    @InternalMountayaApi
    public class Present<out T>(
        /** The value set, `null` included. */
        public val value: T,
    ) : Settable<T> {
        override val valueOrNull: T get() = value

        override val stated: Stated<T> get() = Stated(value)

        override fun equals(other: Any?): Boolean = other is Present<*> && other.value == value

        override fun hashCode(): Int = value?.hashCode() ?: 0

        override fun toString(): String = "Present($value)"
    }

    /**
     * The receiver the serialization plugin reads a `Settable` by, `SettableSerializer`, declared here so it is marked
     * too.
     */
    @InternalMountayaApi
    public companion object
}
