package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpectedRefusalTest {
    @Test
    fun aRefusalIsWrittenAsItsClassUnderRefused() {
        assertEquals("SelectionError", expectedRefusal(parseFixture("""{"refused": "SelectionError"}""")))
    }

    @Test
    fun aValueThatCarriesARefusalBesideOtherKeysOrNoneIsAValue() {
        assertNull(expectedRefusal(parseFixture("""{"headers": {}, "refused": "ConfigurationError"}""")))
        assertNull(expectedRefusal(parseFixture("""{"refused": null}""")))
        assertNull(expectedRefusal(parseFixture("""{"error": "ConfigurationError", "throws": true}""")))
        assertNull(expectedRefusal(parseFixture("\"refused\"")))
    }
}
