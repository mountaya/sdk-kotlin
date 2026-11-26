package com.mountaya.build.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderedJsonTest {
    @Test
    fun anObjectKeepsItsMembersInTheOrderWrittenAKeyWrittenTwiceIncluded() {
        val read = readOrderedJson("""{ "b": 1, "a": [true, null, "x"], "b": { "c": -2.5e1 } }""") as JsonMembers

        assertEquals(listOf("b", "a", "b"), read.members.map { it.first })
        assertEquals(1.0, read.members[0].second)
        assertEquals(listOf(true, null, "x"), read.members[1].second)
        assertEquals(listOf("c" to -25.0), (read.members[2].second as JsonMembers).members)
    }

    @Test
    fun aStringReadsEveryEscapeJsonWrites() {
        assertEquals("Say \"hi\"\\now/\n\t\u00e9\u65e5", readOrderedJson("\"Say \\\"hi\\\"\\\\now\\/\\n\\t\\u00e9日\""))
        assertNull(readOrderedJson(" null "))
    }

    @Test
    fun whatJsonDoesNotWriteIsRefusedNamingItsLine() {
        for (text in listOf("{", "[1,]", "{\"a\" 1}", "01", "'a'", "\"\\x\"", "{} {}", "tru", "\"a\nb\"")) {
            val refused = assertFailsWith<ModelException>(text) { readOrderedJson(text) }
            assertTrue(refused.message.orEmpty().startsWith("not JSON: "), refused.message)
        }
        val refused = assertFailsWith<ModelException> { readOrderedJson("{\n\"a\": [1,\n2,,]}") }
        assertTrue(refused.message.orEmpty().endsWith("at line 3"), refused.message)
    }
}
