package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class EventFramerTest {
    /** What [reads] frame into, each record as its record and each position as its id. */
    private fun frame(vararg reads: ByteArray): List<Any> {
        val framer = EventFramer()
        return reads.flatMap { read ->
            framer.feed(read).map {
                when (it) {
                    is EventFramer.Framed.Record -> it.record
                    is EventFramer.Framed.Position -> it.id
                }
            }
        }
    }

    private fun bytes(vararg values: Int): ByteArray = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun aLineEndsAtACrlfAnLfOrALoneCrSplitAcrossReadsIncluded() {
        val expected = listOf(EventRecord(null, "a\nb", null))

        assertEquals(expected, frame("data: a\r".encodeToByteArray(), "\ndata: b\r\n\r\n".encodeToByteArray()))
        assertEquals(expected, frame("data: a\rdata: b\r\r".encodeToByteArray()))
        assertEquals(expected, frame("data:a\ndata:b\n\n".encodeToByteArray()))
    }

    @Test
    fun oneByteOrderMarkAtTheStartIsDroppedAndACharacterSplitAcrossReadsIsWhole() {
        val framed =
            frame(bytes(0xEF, 0xBB), bytes(0xBF) + "data: ".encodeToByteArray() + bytes(0xC3), bytes(0xA9, 0x0A, 0x0A))

        assertEquals(listOf(EventRecord(null, "é", null)), framed)
    }

    @Test
    fun anIdWithNoDataMovesThePositionACommentDispatchesNothingAndATailIsDropped() {
        val framed = frame("id: 1-0\nevent: message\ndata: a\n\n: beat\n\nid: 2-0\n\ndata: tail\n".encodeToByteArray())

        assertEquals(listOf(EventRecord("message", "a", "1-0"), "2-0"), framed)
    }

    @Test
    fun aRetryAFieldNoRuleNamesAndAnIdHoldingNulAreIgnored() {
        val framed = frame("retry: 10\nkind: x\nid: a\u0000b\ndata: a\n\n".encodeToByteArray())

        assertEquals(listOf(EventRecord(null, "a", null)), framed)
    }
}
