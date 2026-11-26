package com.mountaya.admin.generated

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.wireJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The admin unit's printed types against the wire, read and written through the core's `Json`: every one of the
 * contract's `int64` components is the admin unit's.
 */
@OptIn(InternalMountayaApi::class)
class GeneratedTypesTest {
    @Test
    fun anInt64KeepsEveryDigit() {
        val point =
            wireJson.decodeFromString(
                AdminSchema.UsageTilePoint.serializer(),
                """{"date":"2026-03-14","resource_type":"zones","surface":"api","status_class":"2xx","requests":9007199254740993}""",
            )

        assertEquals(9_007_199_254_740_993L, point.requests)
    }

    @Test
    fun anInt64IsWrittenBackWithEveryDigit() {
        val text = """{"date":"2026-03-14","resource_type":"zones","surface":"api","status_class":"2xx","requests":9007199254740993}"""
        val point = wireJson.decodeFromString(AdminSchema.UsageTilePoint.serializer(), text)

        assertTrue("9007199254740993" in wireJson.encodeToString(AdminSchema.UsageTilePoint.serializer(), point))
    }
}
