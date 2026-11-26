package com.mountaya.client

import com.mountaya.client.ErrorEntry.PathComponent.Index
import com.mountaya.client.ErrorEntry.PathComponent.Key
import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ErrorEntryTest {
    @Test
    fun aPathKeepsAnArrayIndexAsANumberBesideTheKeys() {
        val entry =
            ErrorEntry(
                message = "Required.",
                code = ApiErrorCode("VALIDATION_FAILED"),
                path = listOf(Key("request"), Key("body"), Key("waypoints"), Index(2), Key("name")),
            )

        assertEquals(Index(2), entry.path?.get(3))
        assertNotEquals<ErrorEntry.PathComponent?>(Key("2"), entry.path?.get(3))
    }

    @Test
    fun anEntryAboutNoFieldInParticularHasNoPathWhichIsNotAnEmptyOne() {
        assertNull(ErrorEntry("Not JSON.", ApiErrorCode("BAD_REQUEST")).path)
        assertEquals(emptyList(), ErrorEntry("x", path = emptyList()).path)
        assertNotEquals(ErrorEntry("x"), ErrorEntry("x", path = emptyList()))
    }

    @Test
    fun twoEntriesAreEqualWhenEveryPartIs() {
        val path = listOf(Key("routes"), Index(0))

        assertEquals(
            ErrorEntry("x", ApiErrorCode("CODE"), path),
            ErrorEntry("x", ApiErrorCode("CODE"), listOf(Key("routes"), Index(0))),
        )
        assertEquals(
            ErrorEntry("x", ApiErrorCode("CODE"), path).hashCode(),
            ErrorEntry("x", ApiErrorCode("CODE"), path).hashCode(),
        )
        assertNotEquals(ErrorEntry("x", ApiErrorCode("CODE")), ErrorEntry("x", ApiErrorCode("OTHER")))
    }
}
