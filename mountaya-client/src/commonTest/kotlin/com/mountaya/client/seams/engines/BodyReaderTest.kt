package com.mountaya.client

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The one buffer a body is read through: what each read is handed, by identity, so the arrays a read allocates are
 * counted rather than timed.
 */
class BodyReaderTest {
    private class BrokeOff : Exception("the body broke off")

    /**
     * A body of [bytes] handed out at most [step] bytes a read, recording every array it is handed to read into,
     * throwing [failure] once its bytes are out where one is given.
     */
    private class Source(
        private val bytes: ByteArray,
        private val step: Int = Int.MAX_VALUE,
        private val failure: Exception? = null,
    ) {
        val handed = mutableListOf<ByteArray>()
        private var at = 0

        fun read(
            into: ByteArray,
            offset: Int,
            length: Int,
        ): Int {
            handed += into
            if (at == bytes.size) {
                failure?.let { throw it }
                return -1
            }
            val count = minOf(length, step, bytes.size - at)
            bytes.copyInto(into, offset, at, at + count)
            at += count
            return count
        }

        /** How many distinct arrays it was handed. */
        val arrays: Int
            get() = handed.withIndex().count { (index, array) -> handed.subList(0, index).none { it === array } }
    }

    private fun bytes(size: Int): ByteArray = ByteArray(size) { (it % 251).toByte() }

    @Test
    fun everyChunkIsReadThroughTheOneBufferAndHandedOnAsACopySizedToTheRead() =
        runTest {
            val body = bytes(40_000)
            val source = Source(body, step = 10_000)
            val reader = BodyReader(null, source::read)

            val chunks = mutableListOf<ByteArray>()
            while (true) chunks += reader.next() ?: break

            assertEquals(listOf(10_000, 10_000, 10_000, 10_000), chunks.map { it.size })
            assertContentEquals(body, chunks.fold(ByteArray(0)) { all, chunk -> all + chunk })
            assertEquals(1, source.arrays, "one buffer for every read, the end's included")
            assertEquals(BODY_CHUNK, source.handed.first().size)
            assertTrue(
                chunks.none { chunk ->
                    source.handed.any { it === chunk }
                },
                "a chunk is a copy the next read cannot overwrite",
            )
            assertNull(reader.next())
        }

    @Test
    fun aBodyOfItsStatedLengthIsReadStraightIntoTheBytesItAnswersNeverJoined() =
        runTest {
            val body = bytes(100_000)
            val source = Source(body, step = 7_000)

            val read = BodyReader(body.size.toLong(), source::read).readAll()

            assertContentEquals(body, read)
            assertSame(source.handed.first(), read, "the bytes answered are the ones every read filled")
            assertEquals(2, source.arrays, "the body's own bytes, and the buffer that found its end")
        }

    @Test
    fun aBodyStatingNoLengthGrowsAsItFillsAndIsAnsweredAtItsOwnLength() =
        runTest {
            val body = bytes(3 * BODY_CHUNK + 5)
            val source = Source(body, step = 4_096)

            val read = BodyReader(null, source::read).readAll()

            assertContentEquals(body, read)
            assertTrue(source.arrays <= 4, "the array doubles, ${source.arrays} arrays for ${body.size} bytes")
        }

    @Test
    fun aStatedLengthThatIsWrongEitherWayStillAnswersTheBodyAsItArrived() =
        runTest {
            val body = bytes(50_000)

            assertContentEquals(body, BodyReader(10_000, Source(body, step = 3_000)::read).readAll())
            assertContentEquals(body, BodyReader(90_000, Source(body, step = 3_000)::read).readAll())
            assertContentEquals(ByteArray(0), BodyReader(0, Source(ByteArray(0))::read).readAll())
            assertContentEquals(body, BodyReader(-1, Source(body)::read).readAll())
        }

    @Test
    fun aBodyThatBreaksOffThrowsWhereItBrokeWhetherReadByChunksOrWhole() =
        runTest {
            val chunked = BodyReader(null, Source(bytes(10), failure = BrokeOff())::read)
            chunked.next()
            assertFailsWith<BrokeOff> { chunked.next() }

            assertFailsWith<BrokeOff> { BodyReader(10, Source(bytes(10), failure = BrokeOff())::read).readAll() }
        }
}
