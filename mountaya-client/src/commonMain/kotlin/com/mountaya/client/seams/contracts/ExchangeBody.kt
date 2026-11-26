package com.mountaya.client

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * ExchangeBody is the body of an answer, read a chunk at a time as it arrives.
 *
 * An event stream's body has no end for a read to wait on, so the client reads every body this
 * way, a buffered answer included, and decides itself how long a read may take. It reads a chunk
 * only when it wants one, so the time a stream's consumer holds a frame is never spent reading.
 *
 * The client closes every body it stops reading: one read to its end, an event stream it gives up
 * or reopens, one its consumer lets go. [onClose] is what an exchange lets its connection go with.
 */
public class ExchangeBody(
    private val onClose: () -> Unit = {},
    private val read: suspend () -> ByteArray?,
) : AutoCloseable {
    /** Creates a body that holds [bytes] whole, or nothing where they are empty. */
    public constructor(bytes: ByteArray = ByteArray(0)) : this(read = whole(bytes))

    /**
     * Creates a body of the chunks [chunks] receives, ended when it is closed. A channel closed
     * with a cause is a body that broke off, and closing the body cancels the channel.
     */
    public constructor(chunks: ReceiveChannel<ByteArray>) : this(
        onClose = { chunks.cancel() },
        read = {
            val received = chunks.receiveCatching()
            received.exceptionOrNull()?.let { throw it }
            received.getOrNull()
        },
    )

    /**
     * Creates the body [reader] reads, the platform's exchange's: its chunks read through the one buffer the reader
     * reuses, and the body read whole straight into the bytes it answers.
     */
    internal constructor(reader: BodyReader, onClose: () -> Unit) : this(onClose, reader::next) {
        this.reader = reader
    }

    /** The reader of a body the platform's exchange answered, or null for a body of any other exchange's. */
    private var reader: BodyReader? = null

    private var closed = false

    /**
     * Returns the next chunk, or null once the body has ended, and null again on every call after
     * that. It throws where the body broke off, which the client reads as a dropped connection.
     */
    internal suspend fun next(): ByteArray? = if (closed) null else read()

    /**
     * Reads the body to its end and closes it. A body the platform's exchange answered is read
     * straight into the bytes it answers; any other's chunks are joined once at the end, so a body
     * of many chunks, an export's, is copied once rather than once for every chunk.
     */
    internal suspend fun readAll(): ByteArray =
        try {
            val reader = reader
            when {
                reader == null -> joined()
                closed -> ByteArray(0)
                else -> reader.readAll()
            }
        } finally {
            close()
        }

    /** Reads every chunk to the body's end and joins them once. */
    private suspend fun joined(): ByteArray {
        val chunks = mutableListOf<ByteArray>()
        var size = 0
        while (true) {
            val chunk = next() ?: break
            chunks += chunk
            size += chunk.size
        }
        val bytes = ByteArray(size)
        var at = 0
        for (chunk in chunks) {
            chunk.copyInto(bytes, at)
            at += chunk.size
        }
        return bytes
    }

    /** Lets the body go: nothing more is read, and the exchange is told once. */
    override fun close() {
        if (closed) return
        closed = true
        onClose()
    }
}

/** A reader that answers [bytes] once, where there are any, and then the end. */
private fun whole(bytes: ByteArray): suspend () -> ByteArray? {
    var remaining: ByteArray? = bytes.takeIf { it.isNotEmpty() }
    return {
        val chunk = remaining
        remaining = null
        chunk
    }
}
