package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ConditionalTagTest {
    @Test
    fun aTagIsSentExactlyAsGivenAWildcardAndAWeakOneIncluded() {
        assertEquals("\"d42\"", ConditionalTag.tag(true, "\"d42\""))
        assertEquals("W/\"d42\"", ConditionalTag.tag(true, "W/\"d42\""))
        assertEquals("*", ConditionalTag.tag(true, "*"))
    }

    @Test
    fun noneIsSentWhereTheCallerNamesNoneOrWhereTheOperationDeclaresNoIfMatch() {
        assertNull(ConditionalTag.tag(true, null))
        assertNull(ConditionalTag.tag(false, "\"d42\""))
        assertNull(ConditionalTag.tag(false, ""))
    }

    @Test
    fun aTagNoHeaderCanCarryIsRefused() {
        for (tag in listOf("", "\"d4\n2\"", "\"№\"", "\u0085")) {
            assertFailsWith<ConfigurationException> { ConditionalTag.tag(true, tag) }
        }
    }
}
