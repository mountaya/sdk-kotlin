package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeaderNameTest {
    @Test
    fun aNameIsAToken() {
        for (name in listOf("X-Trace", "x_trace.2", "!#$%&'*+-.^_`|~", "A")) assertTrue(HeaderName.isToken(name), name)
        for (name in listOf(
            "",
            "X Trace",
            "X:Trace",
            "X(Trace)",
            "X\"T\"",
            "X/T",
            "X@T",
            "X\tT",
            "X\u007fT",
            "Xé",
            "X😀",
        )) {
            assertFalse(HeaderName.isToken(name), name)
        }
    }

    @Test
    fun theStackWritesTransferEncodingAndUpgradeItselfWhateverTheirCase() {
        for (name in listOf(
            "Transfer-Encoding",
            "UPGRADE",
            "upgrade",
        )) {
            assertTrue(HeaderName.isStackWritten(name), name)
        }
        for (name in listOf("Content-Type", "X-Upgrade", "Accept")) assertFalse(HeaderName.isStackWritten(name), name)
    }
}
