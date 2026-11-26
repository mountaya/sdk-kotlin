package com.mountaya.client

/** The most a chunk read from a body holds, and the size of the one buffer a reader reuses. */
internal const val BODY_CHUNK: Int = 16 * 1024

/** The most bytes a body read whole is given at once from the length its answer states, so no stated length is trusted with a larger allocation. */
private const val PRESIZE_CEILING: Int = 8 * 1024 * 1024

/** The largest array the platforms allocate. */
private const val LARGEST_ARRAY: Int = Int.MAX_VALUE - 8

/**
 * BodyReader reads one answer's body from its [source] through one buffer, which every read reuses: a chunk handed
 * on is a copy of what one read filled, sized to it, and a body read whole is read straight into the bytes it answers,
 * never gathered as chunks and joined.
 *
 * [source] reads into the bytes it is handed, from an offset and for a length, and answers how many it read, at least
 * one, or -1 once the body has ended; it throws where the body broke off. [statedLength] is the body's length where
 * its answer states one, `Content-Length`, which sizes a whole read up to a ceiling: a body longer than stated is read
 * to its end all the same, and one shorter is answered at its own length.
 */
internal class BodyReader(
    private val statedLength: Long?,
    private val source: suspend (bytes: ByteArray, offset: Int, length: Int) -> Int,
) {
    /** The one buffer every chunk is read into, made at the first read that needs it. */
    private var buffer: ByteArray? = null

    private fun buffer(): ByteArray = buffer ?: ByteArray(BODY_CHUNK).also { buffer = it }

    /** Returns the next chunk, a copy of what one read filled, or null once the body has ended. */
    suspend fun next(): ByteArray? {
        val buffer = buffer()
        val count = source(buffer, 0, buffer.size)
        return if (count < 0) null else buffer.copyOf(count)
    }

    /**
     * Reads the rest of the body straight into the bytes it answers: an array its stated length sizes, or one chunk's
     * size where it states none, doubled as it fills. A body that fills the array exactly is answered as it is, once a
     * read into the buffer finds its end.
     */
    suspend fun readAll(): ByteArray {
        val stated = statedLength?.takeIf { it >= 0 }?.coerceAtMost(PRESIZE_CEILING.toLong())?.toInt()
        var bytes = ByteArray(stated ?: BODY_CHUNK)
        var size = 0
        while (true) {
            if (size == bytes.size) {
                // A full array may hold the whole body: the buffer looks for its end before the array grows.
                val probe = buffer()
                val more = source(probe, 0, probe.size)
                if (more < 0) return bytes
                bytes = grown(bytes, size + more)
                probe.copyInto(bytes, size, 0, more)
                size += more
                continue
            }
            val count = source(bytes, size, bytes.size - size)
            if (count < 0) break
            size += count
        }
        return if (size == bytes.size) bytes else bytes.copyOf(size)
    }

    /** [bytes] copied into an array holding at least [needed], twice its size where that is larger. */
    private fun grown(
        bytes: ByteArray,
        needed: Int,
    ): ByteArray {
        if (needed < 0 || needed > LARGEST_ARRAY) throw IllegalStateException("The body is larger than an array holds.")
        val doubled = if (bytes.size > LARGEST_ARRAY / 2) LARGEST_ARRAY else maxOf(bytes.size * 2, BODY_CHUNK)
        return bytes.copyOf(maxOf(doubled, needed))
    }
}
