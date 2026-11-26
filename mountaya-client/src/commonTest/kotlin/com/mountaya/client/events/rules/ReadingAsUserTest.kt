package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingAsUserTest {
    @Test
    fun aCollaborationStreamReadAsAUserOpensEveryReadingAsThatUserASecondCallReplacingTheFirst() =
        runTest {
            val opened = mutableListOf<String?>()
            val editors =
                EventStream<RestSchema.CollabStreamEvent>({ member ->
                    opened += member
                    emptyFlow()
                })

            val told = editors.readingAsUser("usr_7a4e")
            told.collect()
            told.collect()
            told.readingAsUser("usr_9b0c").collect()
            editors.collect()

            assertEquals(listOf("usr_7a4e", "usr_7a4e", "usr_9b0c", null), opened)
        }
}
