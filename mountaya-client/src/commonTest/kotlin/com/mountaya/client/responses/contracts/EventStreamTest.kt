package com.mountaya.client

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventStreamTest {
    /** A stream whose every collection opens a reading as the user it is told, recorded in [opened]. */
    private fun stream(opened: MutableList<String?>): EventStream<String> =
        EventStream({ member ->
            opened += member
            flowOf(EventFrame("1-0", "frame"))
        })

    @Test
    fun everyCollectionOpensAReadingOfItsOwnAsNoUserByDefault() =
        runTest {
            val opened = mutableListOf<String?>()
            val events = stream(opened)

            assertEquals(listOf(EventFrame("1-0", "frame")), events.toList())
            assertEquals(1, events.count())
            assertEquals(listOf<String?>(null, null), opened)
        }

    @Test
    fun aStreamReadAsAUserOpensAsThatUserTheLatestNamedWinning() =
        runTest {
            val opened = mutableListOf<String?>()
            val events = stream(opened)

            events.readingAs("usr_7a4e").collect()
            events.readingAs("usr_7a4e").readingAs("usr_9b0c").collect()
            events.collect()

            assertEquals(listOf("usr_7a4e", "usr_9b0c", null), opened)
            assertEquals("EventStream(member=usr_9b0c)", events.readingAs("usr_9b0c").toString())
        }
}
