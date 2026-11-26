package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

/** One test for every target's own implementation: each runs it against its own `actual`. */
class Sha256Test {
    private fun digestOf(text: String): String = sha256(text.encodeToByteArray()).toHexString()

    @Test
    fun aDigestIsThirtyTwoBytes() {
        assertEquals(32, sha256(ByteArray(0)).size)
        assertEquals(32, sha256(ByteArray(1_000)).size)
    }

    @Test
    fun theEmptyMessageHasItsStandardDigest() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", digestOf(""))
    }

    @Test
    fun theStandardsOwnExamplesHaveTheirDigests() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", digestOf("abc"))
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            digestOf("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"),
        )
    }

    @Test
    fun aMessageOfAMillionBytesHasItsStandardDigest() {
        assertEquals(
            "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0",
            sha256(ByteArray(1_000_000) { 'a'.code.toByte() }).toHexString(),
        )
    }
}
