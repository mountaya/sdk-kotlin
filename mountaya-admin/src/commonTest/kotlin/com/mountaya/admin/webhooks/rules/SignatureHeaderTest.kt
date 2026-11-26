package com.mountaya.admin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SignatureHeaderTest {
    @Test
    fun aHeaderStatesItsTAsWrittenTheInstantItNamesAndEveryV1InOrder() {
        val header = assertNotNull(SignatureHeader.parse("t=01763111564,v1=aa,v0=cc,v1=bb"), "the header is read")

        assertEquals("01763111564", header.stated)
        assertEquals(1_763_111_564L, header.timestamp)
        assertEquals(listOf("aa", "bb"), header.signatures)
    }

    @Test
    fun anEmptyV1IsASignatureWhichMatchesNothingAndNotAMalformedHeader() {
        assertEquals(listOf(""), assertNotNull(SignatureHeader.parse("t=1,v1="), "the header is read").signatures)
    }

    @Test
    fun aKeyAndAValueAreTrimmedOfWhiteSpaceAndAKeyIsMatchedWhole() {
        val header = assertNotNull(SignatureHeader.parse("  t\t= 5 ,v1 = a b ,V1=c,v1x=d"), "the header is read")

        assertEquals("5", header.stated)
        assertEquals(listOf("a b"), header.signatures)
    }

    @Test
    fun aValueKeepsEveryCharacterButTheWhiteSpaceAtItsEndsAnEqualsSignIncluded() {
        assertEquals(listOf("a=b"), assertNotNull(SignatureHeader.parse("t=5,v1=a=b"), "the header is read").signatures)
    }

    @Test
    fun aHeaderThatIsNotOneIsRefused() {
        for (header in listOf(
            "",
            ",",
            "t=5",
            "v1=a",
            "t=5,t=5,v1=a",
            "t=5,v1=a,",
            "t=5;v1=a",
            "t=+5,v1=a",
            "t=5 6,v1=a",
            "t=0x5,v1=a",
            "t=５,v1=a",
            "t=9007199254740992,v1=a",
        )) {
            assertNull(SignatureHeader.parse(header), header)
        }
    }

    @Test
    fun theLargestTReadIsTheLargestWholeNumberADoubleHoldsExactly() {
        assertEquals(9_007_199_254_740_991L, SignatureHeader.wholeSeconds("9007199254740991"))
        assertEquals(9_007_199_254_740_991L, SignatureHeader.wholeSeconds("0009007199254740991"))
        assertNull(SignatureHeader.wholeSeconds("9007199254740992"))
        assertEquals(0L, SignatureHeader.wholeSeconds("0"))
    }

    @Test
    fun aHeaderTheRequestDoesNotCarryOrOneOfWhiteSpaceAloneIsMissing() {
        assertTrue(SignatureHeader.isMissing(null))
        assertTrue(SignatureHeader.isMissing(""))
        assertTrue(SignatureHeader.isMissing(" \t\r\n ﻿"))
        assertFalse(SignatureHeader.isMissing(","))
        assertFalse(SignatureHeader.isMissing("\u0085"))
    }
}
