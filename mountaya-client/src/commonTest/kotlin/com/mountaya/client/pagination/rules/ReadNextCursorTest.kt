package com.mountaya.client

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadNextCursorTest {
    private fun metadata(text: String): JsonObject = Json.parseToJsonElement(text).jsonObject

    @Test
    fun theCursorIsThePaginationsNextCursorAsText() {
        assertEquals("c2", readNextCursor(metadata("""{"pagination":{"limit":50,"next_cursor":"c2"}}""")))
    }

    @Test
    fun aLastPageAndAMapNamingNoCursorItCanReadEndTheWalk() {
        assertNull(readNextCursor(null))
        assertNull(readNextCursor(metadata("""{}""")))
        assertNull(readNextCursor(metadata("""{"pagination":{"next_cursor":null}}""")))
        assertNull(readNextCursor(metadata("""{"pagination":{"next_cursor":7}}""")))
        assertNull(readNextCursor(metadata("""{"pagination":{"next_cursor":{"at":"c2"}}}""")))
        assertNull(readNextCursor(metadata("""{"pagination":"c2"}""")))
    }
}
