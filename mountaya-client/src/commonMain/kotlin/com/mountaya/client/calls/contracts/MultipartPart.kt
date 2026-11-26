package com.mountaya.client

/**
 * MultipartPart is one part of a `multipart/form-data` body: a field's text, or a file's bytes, under the form
 * field it fills, the contract's property name verbatim. A printed method writes one per property of its form, and
 * a call by declaration hands its own in a [RequestBody.Multipart]. "Multipart" is the word every SDK names a part
 * of such a body by.
 */
public sealed interface MultipartPart {
    /** The form field this part fills. */
    public val name: String

    /** Text is a field whose value is its text. Two are equal where their names and values are. */
    public class Text(
        override val name: String,
        /** The field's value, as text. */
        public val value: String,
    ) : MultipartPart {
        override fun equals(other: Any?): Boolean = other is Text && other.name == name && other.value == value

        override fun hashCode(): Int = name.hashCode() * 31 + value.hashCode()

        override fun toString(): String = "Text($name=$value)"
    }

    /**
     * File is a field whose value is a file's bytes, sent with the file name [filename] its part states and the
     * media type [contentType] it is sent as, `application/octet-stream` where the caller gives none, which RFC 7578
     * has a sender write for a file of no known type. Two are equal where their names, bytes, file names and media
     * types are.
     */
    public class File(
        override val name: String,
        /** The file, as the caller gave it. */
        public val bytes: ByteArray,
        /**
         * The file name the part states, a quote and a line break in it percent-encoded as HTML's form encoding writes
         * them, or null where the part states none.
         */
        public val filename: String? = null,
        /**
         * The media type the part is sent as, or null where the caller gives none, which sends it as
         * `application/octet-stream`. One holding a character no header carries is refused before the request leaves.
         */
        public val contentType: String? = null,
    ) : MultipartPart {
        override fun equals(other: Any?): Boolean =
            other is File && other.name == name && other.bytes.contentEquals(bytes) && other.filename == filename &&
                other.contentType == contentType

        override fun hashCode(): Int =
            ((name.hashCode() * 31 + bytes.contentHashCode()) * 31 + filename.hashCode()) * 31 + contentType.hashCode()

        override fun toString(): String =
            buildString {
                append("File($name, ${bytes.size} bytes")
                filename?.let { append(", filename=").append(it) }
                contentType?.let { append(", contentType=").append(it) }
                append(")")
            }
    }
}
