package com.mountaya.client

/** The pieces a part is written in at the most: its opening, two header lines, a blank line, its content, its end. */
private const val PIECES_A_PART = 6

/** FormBody writes a `multipart/form-data` body. */
internal object FormBody {
    /** The boundary a body takes where no part holds it. */
    val base: String = "MountayaFormBoundary7Hq2Xw"

    /** Written is a body and the `Content-Type` that names its boundary. */
    class Written(
        val contentType: String,
        val bytes: ByteArray,
    )

    /** The media type a file's part is sent as where its caller gives none, which RFC 7578 has a sender write. */
    val unknownFileType: String = "application/octet-stream"

    /** [base] as the bytes a part is searched for it in. */
    private val baseBytes: ByteArray = base.encodeToByteArray()

    /** The byte a boundary is lengthened by, an `x`. */
    private const val LENGTHENING: Byte = 0x78

    /**
     * Returns [parts] as one body, in their order, each named by its form field: a text part with no media type,
     * and a file's part with the file name its caller gives it and the media type its caller gives it, or
     * [unknownFileType] where it gives none.
     *
     * The boundary is the same for every body but where a part's bytes hold it, which lengthens it
     * until none does, so two writes of the same parts send the same bytes.
     *
     * Each part's content is read once, and the body is written into one array sized to it, so a file is copied once,
     * into the body, however many parts come after it.
     *
     * @throws ConfigurationException where a file's media type holds a character no header carries, before any
     * byte of the body is written.
     */
    fun write(parts: List<MultipartPart>): Written {
        for (part in parts) {
            val type = (part as? MultipartPart.File)?.contentType ?: continue
            if (HeaderValue.isCarried(type)) continue
            throw ConfigurationException(
                "The media type of the file part `${part.name}` holds a control character or a character outside " +
                    "printable ASCII, which no header carries: pass a media type such as `application/gpx+xml`.",
            )
        }
        val contents = parts.map(::content)
        val boundary = boundaryOf(contents)
        val opening = "--$boundary\r\n".encodeToByteArray()
        val lineBreak = "\r\n".encodeToByteArray()
        val pieces = ArrayList<ByteArray>(parts.size * PIECES_A_PART + 1)
        for ((index, part) in parts.withIndex()) {
            pieces += opening
            pieces += "Content-Disposition: ${disposition(part)}\r\n".encodeToByteArray()
            if (part is MultipartPart.File) {
                pieces += "Content-Type: ${part.contentType ?: unknownFileType}\r\n".encodeToByteArray()
            }
            pieces += lineBreak
            pieces += contents[index]
            pieces += lineBreak
        }
        pieces += "--$boundary--\r\n".encodeToByteArray()
        val bytes = ByteArray(pieces.sumOf { it.size })
        var written = 0
        for (piece in pieces) {
            piece.copyInto(bytes, written)
            written += piece.size
        }
        return Written("multipart/form-data; boundary=$boundary", bytes)
    }

    /**
     * The boundary of a body whose parts' contents are [contents]: [base] where no part holds it, and otherwise [base]
     * lengthened by one `x` more than the longest run of `x` that follows it where a part holds it. A part holds [base]
     * and `n` `x` after it exactly where it holds [base] followed by a run of `n` or more, so this is the boundary
     * lengthening [base] an `x` at a time until no part holds it reaches, found in one pass over each part.
     */
    private fun boundaryOf(contents: List<ByteArray>): String {
        var longest = -1
        for (content in contents) longest = maxOf(longest, longestRunAfterBase(content))
        return if (longest < 0) base else base + "x".repeat(longest + 1)
    }

    /** The longest run of `x` after [base] where [bytes] hold it, none counting 0, or -1 where they never hold it. */
    private fun longestRunAfterBase(bytes: ByteArray): Int {
        var longest = -1
        val first = baseBytes[0]
        for (start in 0..bytes.size - baseBytes.size) {
            if (bytes[start] != first || !holdsBaseAt(bytes, start)) continue
            var end = start + baseBytes.size
            while (end < bytes.size && bytes[end] == LENGTHENING) end += 1
            longest = maxOf(longest, end - start - baseBytes.size)
        }
        return longest
    }

    /** Whether [bytes] hold [base] from [start]. */
    private fun holdsBaseAt(
        bytes: ByteArray,
        start: Int,
    ): Boolean {
        for (index in 1 until baseBytes.size) {
            if (bytes[start + index] != baseBytes[index]) return false
        }
        return true
    }

    /** The `Content-Disposition` a part is written under: its form field, and a file's name where its caller gives one. */
    private fun disposition(part: MultipartPart): String {
        val field = "form-data; name=\"${escaped(part.name)}\""
        val filename = (part as? MultipartPart.File)?.filename ?: return field
        return "$field; filename=\"${escaped(filename)}\""
    }

    private fun content(part: MultipartPart): ByteArray =
        when (part) {
            is MultipartPart.Text -> part.value.encodeToByteArray()
            is MultipartPart.File -> part.bytes
        }

    /**
     * A field name or a file name inside its quotes, a quote and a line break percent-encoded as HTML's form encoding
     * writes them, so a name cannot close the header or write another.
     */
    private fun escaped(name: String): String = name.replace("\"", "%22").replace("\r", "%0D").replace("\n", "%0A")
}
