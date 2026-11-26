package com.mountaya.admin

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** One test for every target's own comparison: each runs it against its own `actual`. */
class ConstantTimeEqualsTest {
    /** RFC 4231's first test case, the bytes a signature is compared as. */
    private val mac = hmacSha256(ByteArray(20) { 0x0b }, "Hi There".encodeToByteArray())

    @Test
    fun theSameBytesAreEqualWhicheverArraysHoldThem() {
        assertTrue(constantTimeEquals(mac, mac))
        assertTrue(constantTimeEquals(mac, mac.copyOf()))
        assertTrue(
            constantTimeEquals(
                mac,
                "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7".hexToByteArray(),
            ),
        )
        assertTrue(constantTimeEquals(ByteArray(0), ByteArray(0)))
    }

    @Test
    fun oneByteThatDiffersWhereverItStandsMakesTwoArraysUnequal() {
        for (index in listOf(0, 15, 31)) {
            val forged = mac.copyOf()
            forged[index] = (forged[index].toInt() xor 0x01).toByte()
            assertFalse(constantTimeEquals(mac, forged), "byte $index")
            assertFalse(constantTimeEquals(forged, mac), "byte $index")
        }
    }

    @Test
    fun arraysOfDifferentLengthsAreUnequalEvenWhereOneOpensTheOther() {
        assertFalse(constantTimeEquals(mac, mac.copyOf(16)))
        assertFalse(constantTimeEquals(mac.copyOf(16), mac))
        assertFalse(constantTimeEquals(mac, ByteArray(0)))
        assertFalse(constantTimeEquals(ByteArray(0), mac))
        assertFalse(constantTimeEquals(mac, mac.copyOf(33)))
    }
}
