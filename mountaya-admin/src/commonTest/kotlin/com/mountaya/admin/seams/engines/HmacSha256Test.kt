package com.mountaya.admin

import kotlin.test.Test
import kotlin.test.assertEquals

/** One test for every target's own implementation, against RFC 4231's test cases: each runs its own `actual`. */
class HmacSha256Test {
    private fun macOf(
        key: ByteArray,
        message: ByteArray,
    ): String = hmacSha256(key, message).toHexString()

    private fun bytes(
        count: Int,
        byte: Int,
    ): ByteArray = ByteArray(count) { byte.toByte() }

    @Test
    fun aMacIsThirtyTwoBytes() {
        assertEquals(32, hmacSha256(bytes(20, 0x0b), ByteArray(0)).size)
        assertEquals(32, hmacSha256(bytes(200, 0xaa), ByteArray(1_000)).size)
    }

    @Test
    fun theFirstFourTestCasesHaveTheirMacs() {
        assertEquals(
            "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7",
            macOf(bytes(20, 0x0b), "Hi There".encodeToByteArray()),
        )
        assertEquals(
            "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843",
            macOf("Jefe".encodeToByteArray(), "what do ya want for nothing?".encodeToByteArray()),
        )
        assertEquals(
            "773ea91e36800e46854db8ebd09181a72959098b3ef8c122d9635514ced565fe",
            macOf(bytes(20, 0xaa), bytes(50, 0xdd)),
        )
        assertEquals(
            "82558a389a443c0ea4cc819899f2083a85f0faa3e578f8077a2e3ff46729665b",
            macOf(ByteArray(25) { (it + 1).toByte() }, bytes(50, 0xcd)),
        )
    }

    /** RFC 4231 states this case's first 128 bits alone. */
    @Test
    fun theTruncationCaseOpensWithItsMac() {
        assertEquals(
            "a3b6167473100ee06e0c796c2955552b",
            hmacSha256(bytes(20, 0x0c), "Test With Truncation".encodeToByteArray()).copyOf(16).toHexString(),
        )
    }

    @Test
    fun aKeyLongerThanTheBlockIsHashedFirst() {
        assertEquals(
            "60e431591ee0b67f0d8a26aacbf5b77f8e0bc6213728c5140546040f0ee37f54",
            macOf(bytes(131, 0xaa), "Test Using Larger Than Block-Size Key - Hash Key First".encodeToByteArray()),
        )
        assertEquals(
            "9b09ffa71b942fcb27635fbcd5b0e944bfdc63644f0713938a7f51535c3a35e2",
            macOf(
                bytes(131, 0xaa),
                (
                    "This is a test using a larger than block-size key and a larger than block-size data. " +
                        "The key needs to be hashed before being used by the HMAC algorithm."
                ).encodeToByteArray(),
            ),
        )
    }

    /** RFC 2104 pads a key to the block with zeros, so the empty key is a key of one zero byte, or of 64. */
    @Test
    fun theEmptyKeyIsTheKeyOfZerosItIsPaddedTo() {
        assertEquals(
            "b613679a0814d9ec772f95d778c35fc5ff1697c493715653c6c712144292c5ad",
            macOf(ByteArray(0), ByteArray(0)),
        )
        val padded = "e48411262715c8370cd5e7bf8e82bef53bd53712d007f3429351843b77c7bb9b"
        assertEquals(padded, macOf(ByteArray(0), "Hi There".encodeToByteArray()))
        assertEquals(padded, macOf(ByteArray(1), "Hi There".encodeToByteArray()))
        assertEquals(padded, macOf(ByteArray(64), "Hi There".encodeToByteArray()))
    }
}
