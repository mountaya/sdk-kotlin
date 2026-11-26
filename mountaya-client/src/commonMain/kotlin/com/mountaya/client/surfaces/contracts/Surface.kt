package com.mountaya.client

/**
 * Surface is one of the contract's three public surfaces, each with its own origin and its own
 * limits.
 *
 * An operation this build prints no method for names its surface in its `OperationDeclaration`,
 * and the `onResponse` hook is told the surface of every answer.
 */
public enum class Surface(
    /** The contract's word for the surface: `rest`, `tiles` or `data`, which it prints as. */
    public val value: String,
) {
    /** The REST API, the Map Studio API among it. */
    Rest("rest"),

    /** The Tile API. */
    Tiles("tiles"),

    /** The Data API. */
    Data("data"),
    ;

    override fun toString(): String = value
}
