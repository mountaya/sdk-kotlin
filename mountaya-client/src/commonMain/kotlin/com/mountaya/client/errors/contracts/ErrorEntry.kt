package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode

/**
 * ErrorEntry is one entry of a failure envelope: one failed validation of a REST request, or one
 * GraphQL error of a Data API response.
 *
 * `message` is written for a person, in the language `Accept-Language` asked for, and free to
 * change. `code` is what a program branches on, in the one open vocabulary every surface's codes are
 * typed by, [ApiErrorCode], which keeps a code no surface declares exactly as sent.
 */
public class ErrorEntry(
    /** The failure, described for a person. */
    public val message: String,
    /** The machine-readable code the entry carries under `extensions.code`, or null where it carries none. */
    public val code: ApiErrorCode? = null,
    /**
     * Where the field the entry is about sits, from the root of the request or of the result, or
     * null where the entry is about no field in particular. An empty path is not an absent one.
     */
    public val path: List<PathComponent>? = null,
) {
    /** PathComponent is one step of the path to the field an entry is about. */
    public sealed interface PathComponent {
        /** Key is a member of an object. */
        public class Key(
            /** The member's name. */
            public val key: String,
        ) : PathComponent {
            override fun equals(other: Any?): Boolean = other is Key && other.key == key

            override fun hashCode(): Int = key.hashCode()

            override fun toString(): String = key
        }

        /** Index is a position in an array, which stays a number. */
        public class Index(
            /** The position, counted from 0. */
            public val index: Int,
        ) : PathComponent {
            override fun equals(other: Any?): Boolean = other is Index && other.index == index

            override fun hashCode(): Int = index

            override fun toString(): String = "[$index]"
        }
    }

    override fun equals(other: Any?): Boolean =
        other is ErrorEntry && other.message == message && other.code == code && other.path == path

    override fun hashCode(): Int = (message.hashCode() * 31 + (code?.hashCode() ?: 0)) * 31 + (path?.hashCode() ?: 0)

    override fun toString(): String = "ErrorEntry(message=$message, code=$code, path=$path)"
}
