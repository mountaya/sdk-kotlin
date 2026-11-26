package com.mountaya.client

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExchangeBodyTest {
    private class BrokeOff : Exception("the body broke off")

    @Test
    fun aBodyOfBytesIsReadInOneChunkAndAnEmptyOneInNone() =
        runTest {
            val body = ExchangeBody("abc".encodeToByteArray())

            assertContentEquals("abc".encodeToByteArray(), body.next())
            assertNull(body.next())
            assertNull(body.next())
            assertTrue(ExchangeBody().readAll().isEmpty())
        }

    @Test
    fun aChannelsChunksAreReadInOrderAndCollectedWhole() =
        runTest {
            val chunks = Channel<ByteArray>(Channel.UNLIMITED)
            chunks.send("data: ".encodeToByteArray())
            chunks.send("a\n\n".encodeToByteArray())
            chunks.close()

            assertEquals("data: a\n\n", ExchangeBody(chunks).readAll().decodeToString())
        }

    @Test
    fun aBodyThePlatformsExchangeReadsIsReadWholeStraightIntoItsBytesAndClosedOnce() =
        runTest {
            val text = "data: a\n\n".encodeToByteArray()
            val handed = mutableListOf<ByteArray>()
            var at = 0
            var closes = 0
            val reader =
                BodyReader(text.size.toLong()) { bytes, offset, length ->
                    handed += bytes
                    if (at == text.size) {
                        -1
                    } else {
                        val count = minOf(length, 3, text.size - at)
                        text.copyInto(bytes, offset, at, at + count)
                        at += count
                        count
                    }
                }
            val body = ExchangeBody(reader) { closes += 1 }

            val read = body.readAll()

            assertContentEquals(text, read)
            assertTrue(read === handed.first(), "the bytes answered are the ones every read filled")
            assertEquals(1, closes)
            assertNull(body.next(), "a body read to its end reads nothing more")
            assertTrue(ExchangeBody(BodyReader(null) { _, _, _ -> -1 }) {}.also { it.close() }.readAll().isEmpty())
        }

    @Test
    fun aReadItsCallerGivesUpThrowsItsCancellationNeverTheEndOfTheBody() =
        runTest {
            val chunks = Channel<ByteArray>()
            val body = ExchangeBody(chunks)
            val reading = async { body.next() }
            reading.cancel()

            assertFailsWith<CancellationException> { reading.await() }
        }

    @Test
    fun aBodyThatBreaksOffThrowsWhereItBrokeAndClosingItTellsTheExchangeOnce() =
        runTest {
            val chunks = Channel<ByteArray>(Channel.UNLIMITED)
            chunks.send("a".encodeToByteArray())
            chunks.close(BrokeOff())
            val body = ExchangeBody(chunks)

            assertContentEquals("a".encodeToByteArray(), body.next())
            assertFailsWith<BrokeOff> { body.next() }

            var closes = 0
            val told = ExchangeBody(onClose = { closes += 1 }) { "x".encodeToByteArray() }
            told.close()
            told.close()
            assertEquals(1, closes)
            assertNull(told.next())
        }
}
