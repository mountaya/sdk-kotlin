package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class RoadAccessGlyphTest {
    @Test
    fun aKindsGlyphIsTheKindUnderTheNamespaceAKindOfTwoWordsReadWhole() {
        assertEquals("road-access-permit", roadAccessGlyph(RoadAccessKind.Permit))
        assertEquals("road-access-local-access", roadAccessGlyph(RoadAccessKind.LocalAccess))
        assertEquals(
            listOf(
                "road-access-forbidden",
                "road-access-private",
                "road-access-permit",
                "road-access-service",
                "road-access-local-access",
                "road-access-restricted",
            ),
            RoadAccessKind.entries.map(::roadAccessGlyph),
        )
    }
}
