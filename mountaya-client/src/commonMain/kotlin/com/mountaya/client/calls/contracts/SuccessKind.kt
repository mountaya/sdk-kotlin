package com.mountaya.client

/**
 * SuccessKind is the shape an operation's success arrives in.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, prints its own from beside the core, and
 * no consumer reads it (SPEC.md section 7).
 */
@InternalMountayaApi
public enum class SuccessKind {
    /** The response envelope, with `data` and `extensions.metadata`. */
    Envelope,

    /** An event stream, one event per frame. */
    EventStream,

    /** A file, with the content type the server chose. */
    Binary,

    /** A GraphQL result, under `data` and the operation's own field. */
    Graphql,
}
