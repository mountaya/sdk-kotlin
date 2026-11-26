package com.mountaya.client

/**
 * EventFramer reads the bytes of a `text/event-stream` body into records, by the Server-Sent
 * Events rules and not by what the API happens to write.
 *
 * A line ends at a CRLF, an LF or a lone CR, a record ends at an empty line, and one byte-order
 * mark at the very start is dropped. A line is decoded as UTF-8 once it is whole, and a line
 * ending is never inside a character, so a character split between two reads arrives whole. A
 * comment, a line beginning with a colon, dispatches nothing, and a record still open when the
 * body ends is discarded. One framer reads one open of a stream, from its first byte.
 */
internal class EventFramer {
    /** Framed is what one record, once ended, is. */
    sealed interface Framed {
        /** A record that carried data. */
        class Record(
            val record: EventRecord,
        ) : Framed

        /**
         * A record that carried an `id:` and no data, which moves the position a reopen resumes
         * from without handing anything on.
         */
        class Position(
            val id: String,
        ) : Framed
    }

    private var line = ByteArray(64)
    private var length = 0
    private var startRead = false

    /** Whether the last byte read ended a line at a CR, so a line feed that follows, in this read or the next, belongs to it. */
    private var afterCarriageReturn = false
    private val data = mutableListOf<String>()
    private var event: String? = null
    private var id: String? = null

    /** Reads the next bytes of the body and returns the records they end, in order. */
    fun feed(bytes: ByteArray): List<Framed> {
        val framed = mutableListOf<Framed>()
        for (byte in bytes) {
            if (!startRead) {
                append(byte)
                if (isByteOrderMarkPrefix()) {
                    if (length == BYTE_ORDER_MARK.size) {
                        length = 0
                        startRead = true
                    }
                    continue
                }
                startRead = true
                val held = line.copyOf(length)
                length = 0
                for (heldByte in held) take(heldByte, framed)
                continue
            }
            take(byte, framed)
        }
        return framed
    }

    private fun isByteOrderMarkPrefix(): Boolean = (0 until length).all { line[it] == BYTE_ORDER_MARK[it] }

    private fun take(
        byte: Byte,
        framed: MutableList<Framed>,
    ) {
        if (afterCarriageReturn) {
            afterCarriageReturn = false
            if (byte == LINE_FEED) return
        }
        when (byte) {
            LINE_FEED -> {
                endLine(framed)
            }

            CARRIAGE_RETURN -> {
                endLine(framed)
                afterCarriageReturn = true
            }

            else -> {
                append(byte)
            }
        }
    }

    private fun append(byte: Byte) {
        if (length == line.size) line = line.copyOf(line.size * 2)
        line[length] = byte
        length += 1
    }

    private fun endLine(framed: MutableList<Framed>) {
        val text = line.decodeToString(0, length)
        length = 0
        if (text.isEmpty()) {
            dispatch(framed)
            return
        }
        if (text.startsWith(":")) return
        val colon = text.indexOf(':')
        val field: String
        val value: String
        if (colon >= 0) {
            field = text.substring(0, colon)
            value = text.substring(colon + 1).removePrefix(" ")
        } else {
            field = text
            value = ""
        }
        when (field) {
            "data" -> {
                data += value
            }

            "event" -> {
                event = value
            }

            "id" -> {
                if ('\u0000' !in value) id = value
            }

            else -> {
                // `retry:` and any field the rules do not name: the reconnect schedule is the
                // contract's, never the server's.
            }
        }
    }

    private fun dispatch(framed: MutableList<Framed>) {
        val id = id
        if (data.isNotEmpty()) {
            framed += Framed.Record(EventRecord(event, data.joinToString("\n"), id))
        } else if (id != null) {
            framed += Framed.Position(id)
        }
        data.clear()
        event = null
        this.id = null
    }
}

private val BYTE_ORDER_MARK = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
private const val LINE_FEED: Byte = 0x0A
private const val CARRIAGE_RETURN: Byte = 0x0D
