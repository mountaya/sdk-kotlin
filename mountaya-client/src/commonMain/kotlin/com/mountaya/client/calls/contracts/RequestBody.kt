package com.mountaya.client

import kotlinx.serialization.json.JsonElement

/**
 * RequestBody is what a request carries after its headers: nothing, a JSON value, or a form.
 *
 * A printed method writes its operation's body itself. A call by declaration, `Mountaya.send`, is handed one, sent in
 * the media type it names with the `Content-Type` that says so: [Json] as `application/json` and [Multipart] as
 * `multipart/form-data`.
 */
public sealed interface RequestBody {
    /** Empty is no body at all. */
    public object Empty : RequestBody {
        override fun toString(): String = "Empty"
    }

    /**
     * Json is a JSON body, written by the client's `Json` straight to the text sent, never built as a tree of JSON
     * first where a printed method writes it, so what is sent is what a test reads. Two are equal where their texts
     * are.
     */
    public class Json
        @InternalMountayaApi
        public constructor(
            /** The body as it goes on the wire. */
            internal val text: String,
        ) : RequestBody {
            /** Creates the body [value] is, as the client's `Json` writes it. */
            public constructor(value: JsonElement) : this(wireJson.encodeToString(JsonElement.serializer(), value))

            override fun equals(other: Any?): Boolean = other is Json && other.text == text

            override fun hashCode(): Int = text.hashCode()

            override fun toString(): String = "Json($text)"
        }

    /**
     * Multipart is a `multipart/form-data` body, its parts in the order they are given, which for a printed method is
     * the order the contract declares the fields. Two are equal where their parts are.
     */
    public class Multipart(
        /** The parts, in order. */
        public val parts: List<MultipartPart>,
    ) : RequestBody {
        override fun equals(other: Any?): Boolean = other is Multipart && other.parts == parts

        override fun hashCode(): Int = parts.hashCode()

        override fun toString(): String = "Multipart($parts)"
    }
}
